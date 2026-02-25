# Gemini API 영수증 파싱: 순차 → 병렬 처리 전환기

## 문제 배경

### 서비스 소개

Unipocket은 **교환학생을 위한 여행 가계부** 서비스다. 해외에서 발생하는 결제 영수증을 촬영하여 업로드하면, 가맹점명, 금액, 통화, 카테고리 등을 자동 추출하여 지출내역으로 등록해 준다. 이때 영수증 이미지를 분석하는 데 **Google Gemini AI API**를 사용한다.

### 사용자 흐름

```
사용자                     프론트엔드                     백엔드
 ├─ 영수증 촬영 (1~3장)       │                              │
 ├──────────────────────→ Presigned URL 발급 요청           │
 │                           │──────────────────────→ S3 Presigned URL 생성
 │                           │←──────────────────────
 │                           │── S3 직접 업로드 (PUT)──→  AWS S3
 │                           │                              │
 │                           │── POST /parse ─────────→ 202 Accepted + taskId
 │                           │                              │── parsingExecutor 비동기 실행
 │                           │── GET /sse/{taskId} ──→ SSE 스트림 구독
 │                           │                              │   ├─ Gemini API 호출 (파일1)
 │   진행률 33% ←── SSE ─────│←─────────────────────        │   ├─ DB 저장
 │   진행률 66% ←── SSE ─────│←─────────────────────        │   ├─ Gemini API 호출 (파일2)
 │   완료 100% ←── SSE ──────│←─────────────────────        │   └─ ...
 │                           │                              │
 │   확인 후 "등록" 탭 ─→ POST /confirm ─────────→ 임시지출 → 정식 지출로 전환
 └──────────────────────────────────────────────────────────┘
```

사용자에게는 영수증을 올린 뒤 **SSE(Server-Sent Events)**를 통해 실시간 진행률이 표시된다. 파일별로 "파싱 중 → 성공/실패" 상태가 갱신되고, 전체 완료 시 결과 확인 화면으로 전환된다.

### 왜 WebSocket이 아닌 SSE인가

| 기준 | SSE | WebSocket |
|------|-----|-----------|
| 통신 방향 | **단방향** (서버 → 클라이언트) | 양방향 |
| 프로토콜 | HTTP/1.1 기반 | 독립 프로토콜 (ws://) |
| 로드밸런서 호환 | 일반 HTTP로 취급 | Upgrade 핸들링 필요 |
| 자동 재연결 | 브라우저 `EventSource` 내장 | 직접 구현 필요 |
| Spring 지원 | `SseEmitter` 기본 제공 | 별도 의존성 (spring-websocket + STOMP) |
| 서버 리소스 | 단순 HTTP 커넥션 | 풀 듀플렉스 소켓 유지 |

이 프로젝트에서 SSE를 선택한 이유:

1. **단방향이면 충분하다** — 클라이언트가 서버에 보낼 추가 데이터가 없다. 서버가 "33% 완료", "66% 완료", "완료"를 일방적으로 push하면 된다.
2. **인프라 단순성** — WebSocket은 로드밸런서에서 `Connection: Upgrade` 처리가 필요하고, sticky session이나 별도 설정이 필수다. SSE는 일반 HTTP 응답이라 특별한 인프라 설정 없이 동작한다.
3. **자동 재연결** — 브라우저의 `EventSource` API가 네트워크 끊김 시 자동 재연결하며, 이 프로젝트에서는 재연결 시 Redis에 저장된 **터미널 상태(COMPLETE 또는 ERROR)를 replay**하여 끊겨도 결과를 전달한다.
4. **양방향은 오히려 리스크** — WebSocket의 양방향 특성은 이 시나리오에서 불필요한 복잡성을 추가한다. 메시지 프레이밍, 하트비트, 연결 상태 관리 등을 직접 구현해야 한다.

### 핵심 문제: 파싱 속도

이 기능은 서비스의 핵심 진입점이기 때문에, 파싱 속도가 곧 서비스 품질이었다. Gemini API 호출이 **건당 평균 9초** 소요되어, 3장 업로드 시 **약 27초를 순차적으로 대기**해야 하는 구조적 병목이 있었다. SSE로 진행률을 보여주고 있었지만, 근본적으로 총 대기 시간 자체가 길었다.

### 현재 아키텍처

```
Client → POST /parse
       → 202 Accepted + taskId (즉시 반환)
       → parsingExecutor.execute(() → parseBatchFiles())  // 비동기 제출
         └─ for (file : files)                             // 순차 처리
              GeminiAPI 호출 (~9초 블로킹)
              SSE 진행률 발행
              DB 저장
```

이미 비동기 처리 자체는 완료한 상태였다. 요청 스레드는 `taskId`를 즉시 반환하고, 실제 파싱은 `parsingExecutor` 스레드풀에서 비동기로 실행된다. SSE를 통해 진행률을 실시간으로 전달하여 사용자 경험을 개선했지만, **비동기로 실행되는 배치 내부에서의 파일 처리는 여전히 순차적**이었다. SSE가 "지금 몇 %입니다"를 알려줄 뿐, 총 소요 시간 자체는 줄어들지 않았다.

### 측정 근거

프로젝트에 내장된 AOP 기반 관측성 시스템(`ObservabilityAspect`)으로 Gemini API 호출 시간을 수집했다. 1000ms 이상 소요되는 메서드를 자동으로 `WARN` 레벨로 기록하는 구조다.

```java
// ObservabilityAspect.java — 프로젝트 내 실제 코드
@Value("${observability.execution-time.slow-threshold-ms:1000}")
private long slowThresholdMs = 1000L;

if (slowExecution) {
    log.warn(logMessage);  // JSON 구조화 로그
}

// 출력 예시:
// {"className":"GeminiService","methodName":"parseReceiptImage","durationMs":9821,"slowExecution":true}
```

수집된 로그를 기반으로 한 Gemini API 응답 시간 분포:

| 지표 | 값 |
|------|-----|
| 평균 응답 시간 | ~8,900ms |
| p90 | ~12,000ms |
| p99 | ~14,800ms |

---

## 어떤 고민

핵심 질문은 단순했다:

> **"3장의 파일을 왜 한 장씩 기다리면서 파싱하는가? 동시에 보낼 수 있지 않은가?"**

하지만 구현에는 여러 지점을 고려해야 했다:

### 1. 스레드 풀 설계

현재 `parsingExecutor`의 설정:

```java
@Bean(name = "parsingExecutor")
public Executor parsingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);       // 평상시 유지되는 스레드 수
    executor.setMaxPoolSize(10);       // 최대 동시 배치 task 수
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("parsing-");
    executor.initialize();
    return executor;
}
```

`parseBatchFiles`는 이 풀의 스레드 위에서 실행된다. 만약 파일별 병렬 처리도 같은 풀에서 실행하면, **부모 스레드가 `.join()`으로 블로킹하면서 자식 스레드의 자리를 차지하는 교착**이 발생할 수 있다. 이것을 **Thread Pool Starvation**(스레드 풀 기아)이라 한다.

### 2. 동시성 제어

순차 처리에서는 `int completed++`와 `ArrayList`로 충분했지만, 병렬 처리에서는 여러 스레드가 동시에 같은 변수에 접근한다:
- 진행률 카운터: Race condition 발생 가능
- 성공/실패 리스트: `ConcurrentModificationException` 발생 가능

### 3. SSE 진행률 발행 순서

순차 처리에서는 파일1 → 파일2 → 파일3 순서로 진행률이 올라갔지만, 병렬 처리에서는 완료 순서가 비결정적이다. 프론트엔드가 `progress %` 기반이므로 순서가 바뀌어도 문제없는지 확인이 필요했다.

### 4. 외부 API Rate Limit

Gemini API에는 분당 요청 제한이 있다. 3장을 동시에 보내면 순간적으로 3배의 요청이 발생한다. 기존에 구현해둔 부분 실패 처리(파일별로 독립 처리하되, 성공한 파일은 저장하고 실패한 파일은 에러 코드와 함께 보고)가 병렬 환경에서도 정상 동작하는지 검증이 필요했다.

---

## 해결 방안 탐색

### A. CompletableFuture + 동일 Executor

`parseBatchFiles` 내부에서 `CompletableFuture.runAsync(..., parsingExecutor)`로 파일별 병렬 실행.

| 장점 | 단점 |
|------|------|
| 기존 코드 변경 최소 | **Thread Pool Starvation** 위험 |
| 별도 인프라 불필요 | 부모+자식이 동일 풀을 공유 → 교착 가능 |

**Starvation 동작 방식:**

```
parsingExecutor (max=10):
  Thread-1: parseBatchFiles VU-1 → .join() 대기 (스레드 점유 중)
  Thread-2: parseBatchFiles VU-2 → .join() 대기 (스레드 점유 중)
  ...
  Thread-5: parseBatchFiles VU-5 → .join() 대기 (스레드 점유 중)
  Thread-6~10: 자식 CompletableFuture 실행 (5명 × 3장 = 15건 중 5건만 실행)
  큐: 나머지 10건 대기 → starvation
```

### B. CompletableFuture + ForkJoinPool.commonPool()

`parsingExecutor`는 배치 레벨에만 사용하고, 파일별 병렬 처리는 JVM 기본 `ForkJoinPool.commonPool()`에서 실행.

| 장점 | 단점 |
|------|------|
| Starvation 해소 | **스레드 수 바운딩 불가** (JVM이 관리) |
| 가장 빠른 성능 (보상 스레드 자동 생성) | `parallel()` 등 다른 작업과 풀 공유 |
| 코드 변경 최소 | Docker 환경에서 CPU 기반 parallelism이 제한적 |

### C. CompletableFuture + 전용 파일 파싱 Executor

배치 레벨과 파일 레벨을 **완전히 분리된 두 개의 Executor**로 운영.

| 장점 | 단점 |
|------|------|
| **스레드 수 명시적 제어** | Executor 빈 하나 추가 |
| Starvation 완전 해소 | 풀 사이즈 튜닝 필요 |
| 로그에서 `file-parse-N`으로 구분 가능 | — |
| CallerRunsPolicy로 backpressure 제공 | — |

### D. WebClient 리액티브 (검토 후 제외)

논블로킹 HTTP 클라이언트로 전환하면 스레드 점유 자체가 사라진다. 하지만 기존 `RestClient`/`RestTemplate` 기반 동기 호출 코드를 전면 리팩터링해야 하고, 6주 개발 기간에 리스크가 큰 변경이었다.

### E. Virtual Thread (Java 21) (검토 후 제외)

`Executors.newVirtualThreadPerTaskExecutor()` 한 줄로 블로킹 I/O 문제가 해소되지만, **프로젝트 요구사항이 Java 17**이므로 적용 불가. 향후 마이그레이션 시 고려.

---

## 내가 선택한 기술

**C안 (전용 파일 파싱 Executor)**을 최종 선택했다.

### 선택 이유

1. A안(동일 Executor)은 실제 부하 테스트에서 **Starvation으로 성능이 오히려 악화**되는 것을 확인했다 (parse avg 17.93s → 26.3s)
2. B안(commonPool)은 가장 빠르지만, **스레드 수를 제어할 수 없어 프로덕션에서 예측 불가능한 동작**을 할 수 있다
3. C안은 A안의 Starvation 문제를 해소하면서, B안의 비바운딩 문제도 해결한다

### 구현 코드

**1단계: 전용 Executor 추가**

```java
// AsyncConfig.java
@Bean(name = "fileParsingExecutor")
public Executor fileParsingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(6);       // 2 배치 × 3 파일 동시 처리
    executor.setMaxPoolSize(15);       // 최대 5 배치 × 3 파일 동시 처리
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("file-parse-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
}
```

**CallerRunsPolicy를 선택한 이유:** `AbortPolicy`(기본)는 풀이 포화되면 즉시 `RejectedExecutionException`을 던진다. 개별 요청은 최대 3건의 작업만 제출하지만, 동시 요청이 증가하거나 Gemini API 응답이 느려지면 큐가 채워져 거부가 **발생할 수 있다**. `CallerRunsPolicy`는 이때 **자연스러운 backpressure**로 작동한다: 부모 스레드(=`parsingExecutor` 스레드)가 파일 파싱을 직접 실행하면서, 새로운 배치 작업의 유입 속도가 자연스럽게 줄어든다. 트레이드오프는 **배치 레벨 동시성의 일시 감소**다. 과부하 시 거부(=데이터 재시도 필요)보다는 **처리량 저하로 흡수**하는 전략을 선택했다.

**2단계: parseBatchFiles 병렬 전환**

Before (순차 처리):
```java
void parseBatchFiles(TempExpenseMeta meta, List<File> files, String taskId) {
    int completed = 0;
    List<String> succeededFileKeys = new ArrayList<>();

    for (File file : files) {
        try {
            parseAndPersistExpenses(file, meta, rateContext);  // ~9초 블로킹
            succeededFileKeys.add(file.getS3Key());
        } catch (Exception e) { ... }
        finally {
            completed++;
            progressPublisher.publishProgress(taskId, toPercent(completed, total), ...);
        }
    }
}
```

After (병렬 처리):
```java
void parseBatchFiles(TempExpenseMeta meta, List<File> files, String taskId) {
    AtomicInteger completed = new AtomicInteger(0);
    List<String> succeededFileKeys = Collections.synchronizedList(new ArrayList<>());
    List<FailedFile> failedFiles = Collections.synchronizedList(new ArrayList<>());

    List<CompletableFuture<Void>> futures = files.stream()
            .map(file -> CompletableFuture.runAsync(() -> {
                try {
                    parseAndPersistExpenses(file, meta, finalRateContext);
                    succeededFileKeys.add(file.getS3Key());
                } catch (Exception e) { ... }
                finally {
                    int done = completed.incrementAndGet();
                    progressPublisher.publishProgress(taskId, toPercent(done, total), ...);
                }
            }, fileParsingExecutor))    // ← 전용 Executor 사용
            .toList();

    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
}
```

**동시성 대응 요약:**

| 이슈 | 순차 구조 | 병렬 구조 |
|------|----------|----------|
| progress 카운터 | `int ++` | `AtomicInteger.incrementAndGet()` |
| 성공/실패 리스트 | `ArrayList` | `Collections.synchronizedList()` (최대 3건이라 lock 경합이 무시 가능) |
| SSE 발행 순서 | 고정 (파일1→2→3) | 비결정적 (프론트엔드는 % 기반이므로 무관) |
| DB 트랜잭션 | 파일별 독립 | 동일 — `parseAndPersistExpenses` 단위 |
| 부분 실패 | 나머지 계속 처리 | 각 Future가 독립적이므로 동일 동작 |

---

## 적용 결과

### 테스트 환경

| 항목 | 값 |
|------|-----|
| Docker Compose | app(2CPU, 1G), MySQL, Redis, LocalStack, WireMock |
| WireMock Gemini 지연 | **9,000ms 고정** (실제 Gemini avg ~8,900ms 기준) |
| k6 시나리오 | ramping VUs (1→3→5→0, 3분) |
| IMAGE_COUNT | **3** (이미지 3장 업로드 → 파싱) |
| 부하 테스트 도구 | k6 (tempexpense-image-workflow.k6.js) |
| Rate Limit | WireMock은 Rate Limit을 시뮬레이션하지 않음 (별도 429 테스트는 단위 테스트로 검증) |

### 메트릭 정의

| 메트릭 | 의미 |
|--------|------|
| `parse` | k6에서 측정한 **task 단위** 소요 시간. POST /parse 요청 후 SSE로 COMPLETE 이벤트를 수신할 때까지의 시간. 즉 **3장 전체의 파싱 완료 시간**이다. |
| `iteration` | k6의 한 iteration = presigned URL 발급 + S3 업로드 + parse + confirm + cleanup 전체를 포함한 end-to-end 시간. |

> **측정값에 대한 주의:** 아래 표의 수치는 VU가 1→3→5→0으로 변화하는 **램프업 혼합 구간의 평균**이다. 4가지 방식을 **동일한 부하 조건에서 상대 비교**하기 위한 것이다. 순차 처리 parse max=26.44s는 이론값 ~27초(9초 × 3장)에 근접하여 측정의 타당성을 확인하였다.

### 4가지 방식 비교

| 메트릭 | ① 순차 처리 | ② 동일 Executor<br>(Starvation 발생) | ③ commonPool | ④ 전용 Executor<br>(최종 채택) |
|--------|-----------|-----------------------------------|-------------|--------------------------|
| **parse avg** | 17.93s | **26.30s** 🔴 | **9.18s** | **15.13s** |
| **parse p90** | 24.05s | 35.85s | **9.33s** | 18.98s |
| **parse max** | 26.44s | — | 9.43s | 23.33s |
| **iteration avg** | 18.29s | 26.72s | **9.55s** | 15.51s |
| **iteration p90** | 24.35s | 36.19s | **9.76s** | 19.33s |
| **처리량 (3분)** | 34회 | 19회 | **60회** | 38회 |
| 파싱 성공률 | 100% | 100% | 100% | 100% |
| 스레드 바운딩 | ✅ | ✅ | ❌ | ✅ |
| Starvation 위험 | — | 🔴 발생 | — | — |

### 결과 해석

**② 동일 Executor가 가장 느린 이유 — Starvation 구체적 분석:**

`parsingExecutor`의 설정은 core=5, max=10이다. k6 테스트에서 VU=5가 동시에 실행되면:

- **부모 스레드 5개**: 각 VU의 `parseBatchFiles`가 `parsingExecutor`에서 실행되며, `.join()`으로 블로킹 상태  
- **자식 작업 15개** (5 VU × 3 파일): 같은 `parsingExecutor`에 `runAsync`로 제출  
- **사용 가능한 스레드**: max=10 − 5(부모 점유) = **5개뿐**  
- **큐 대기**: 나머지 10개 작업이 큐에서 스레드 해제를 기다림

5개 스레드가 3개씩 작업을 마무리해야 부모가 풀리고, 풀린 부모 자리에 다시 자식이 들어오는 **직렬화된 병렬 처리**가 발생했다. 결과적으로 순차 처리(17.93s)보다 **더 느린 26.3s**가 측정되었다.

**③ commonPool이 가장 빠른 이유:**

실험에서 commonPool은 3장 모두 동시 처리하여 parse avg=9.18s를 달성했다. Docker 환경(2CPU)에서 `ForkJoinPool.commonPool()`의 기본 parallelism은 1이다. 그럼에도 3장이 동시에 처리된 것은, ForkJoinPool이 내부적으로 블로킹 상황에서 추가 스레드를 생성하는 메커니즘을 가지고 있기 때문으로 **추정**된다.

확실한 것은 관찰된 사실이다: commonPool은 바운딩이 어려웠고, 처리량이 가장 높았다. 반면 **스레드 생성 수를 애플리케이션에서 제어할 수 없다**는 점이 프로덕션에서의 리스크였다. JVM의 `parallel()` 스트림이나 다른 `CompletableFuture` 작업과 풀을 공유하므로, 파싱 작업이 다른 기능에 예측 불가능한 영향을 미칠 수 있다. 더 실질적인 문제는 **운영 중 장애 발생 시 원인 격리가 어렵다**는 것이다. commonPool의 스레드가 고갈되면 파싱뿐 아니라 관련 없는 기능까지 동시에 느려지며, 장애 원인을 추적하기 어려워진다.

**④ 전용 Executor가 ③보다 느린 이유:**

`ThreadPoolTaskExecutor`의 확장 전략은 core → 큐 → max 순이다. 큐(capacity=50)가 충분히 크면, 실제 동시 실행 스레드 수가 core(=6)에 머무르고 max(=15)까지 확장이 지연된다. I/O-bound 작업에서 이 **큐잉이 동시성을 제한**하여 commonPool 대비 느린 결과로 이어진다. 또한 `CallerRunsPolicy`가 작동하면 부모 스레드가 자식 작업을 직접 실행하면서 배치 레벨 동시성이 일시 감소할 수 있다.

> **핵심 트레이드오프:** 순수 성능(③ commonPool)보다 **제어 가능한 안정성(④ 전용 Executor)**을 선택했다. 전용 Executor의 성능은 풀 사이즈 튜닝(core/max/queue 비율)과 프로덕션 인스턴스 사양(CPU 코어 수)에 따라 개선 여지가 있다.

### 핵심 공식

```
순차:  total_time = sum(file₁, file₂, file₃) ≈ 9 + 9 + 9 = 27초
병렬:  total_time = max(file₁, file₂, file₃) ≈ max(9, 9, 9) = 9초 (이론값)
```

### 스레드 풀 아키텍처 (최종)

```
Client → parsingExecutor (max=10)     ← 배치 레벨 동시성 제어
           └─ fileParsingExecutor (max=15) ← 파일 레벨 동시성 제어
                ├─ file-parse-1: 파일A → GeminiAPI (~9초)
                ├─ file-parse-2: 파일B → GeminiAPI (~9초)
                └─ file-parse-3: 파일C → GeminiAPI (~9초)
```

`parsingExecutor`는 "동시에 몇 명의 사용자가 파싱할 수 있는가"를 제어하고, `fileParsingExecutor`는 "동시에 몇 장의 파일이 Gemini API를 호출할 수 있는가"를 제어한다. 두 관심사를 분리함으로써, 한쪽의 부하가 다른 쪽에 영향을 주지 않는다.

### 향후 개선: Virtual Thread (Java 21)

프로젝트가 Java 21으로 마이그레이션되면, Executor 설정을 한 줄로 변경하여 스레드 풀 관리 자체를 제거할 수 있다:

```diff
 @Bean(name = "fileParsingExecutor")
 public Executor fileParsingExecutor() {
-    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
-    executor.setCorePoolSize(6);
-    executor.setMaxPoolSize(15);
-    ...
-    return executor;
+    return Executors.newVirtualThreadPerTaskExecutor();
 }
```

Virtual Thread는 I/O 블로킹 시 OS 스레드를 **자동으로 반환**하고, 응답이 도착하면 다시 할당받는다. 스레드 풀 사이즈 튜닝, Starvation 방지, CallerRunsPolicy 같은 **모든 방어 로직이 불필요**해진다.
