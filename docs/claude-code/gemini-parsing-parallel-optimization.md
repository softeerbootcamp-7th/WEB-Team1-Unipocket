# Gemini API 영수증 파싱 병렬화: 같은 스레드풀에서 병렬 처리하면 왜 더 느려질까?

---

## 문제 배경

Unipocket은 교환학생을 위한 여행 가계부 서비스다. 사용자가 영수증 이미지(최대 3장)를 올리면 Gemini AI API가 가맹점명·금액·통화 등을 자동으로 추출한다.

문제는 **Gemini API 호출이 건당 약 9초**가 걸린다는 것이었다. 3장을 순차 처리하면 약 27초를 기다려야 한다.

```
파일1 → Gemini (~9초) → 완료
                         파일2 → Gemini (~9초) → 완료
                                                  파일3 → Gemini (~9초) → 완료
총 소요: ~27초
```

이미 비동기 처리(`@Async` + `parsingExecutor`)는 적용한 상태여서, 사용자에게는 SSE로 진행률을 실시간 전달하고 있었다. 하지만 **배치 내부에서 파일을 하나씩 처리하는 구조**는 그대로였다.

> 9초짜리 I/O 작업이 3개면, 동시에 보내면 9초에 끝나지 않을까?

자연스러운 의문이었고, `CompletableFuture`로 병렬 처리를 시도했다.

---

## 어떤 고민

### 첫 번째 시도: 기존 Executor에서 병렬 처리

가장 단순한 접근이었다. 이미 `parsingExecutor`라는 스레드풀이 있으니까, 여기에 `CompletableFuture.runAsync(..., parsingExecutor)`로 파일별 작업을 넣으면 되지 않을까?

```java
// parseBatchFiles 메서드 안에서
List<CompletableFuture<Void>> futures = files.stream()
    .map(file -> CompletableFuture.runAsync(() -> {
        parseAndPersistExpenses(file, meta, rateContext);
    }, parsingExecutor))   // 같은 parsingExecutor 사용
    .toList();

CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
```

k6 부하 테스트를 돌려봤다. 결과는 **순차 처리보다 더 느려졌다.**

### 왜 더 느려졌는가 — Thread Pool Starvation

`parsingExecutor`의 설정은 core=5, max=10이다. 5명의 사용자가 동시에 요청하면:

```
parsingExecutor (max=10):
  Thread-1~5: 각 사용자의 parseBatchFiles 실행 중 → .join()으로 대기 (스레드 점유)
  Thread-6~10: 자식 CompletableFuture 실행 (5명 × 3장 = 15건 중 5건만 실행 가능)
  큐: 나머지 10건이 스레드 반환을 기다림
```

**부모가 자식의 완료를 기다리는데, 자식이 실행될 스레드를 부모가 차지하고 있는 상황**이다. 이것이 Thread Pool Starvation(스레드풀 기아)이다. 병렬화가 오히려 순차 처리보다 느려지는 역설적인 결과가 나왔다.

이 경험을 통해 **"같은 풀에서 부모가 자식을 기다리면 안 된다"**는 것을 배웠고, 풀을 분리해야 한다는 결론에 도달했다.

> 이 Starvation 현상은 코드만 봐서는 예측하기 어려웠다. 실제로 k6 부하 테스트를 돌리고 나서야 "왜 더 느려졌지?"를 추적하면서 발견하게 되었다.

---

## 해결 방안 탐색

| 방안 | 장점 | 단점 |
|------|------|------|
| **A. 같은 Executor 사용** | 코드 변경 최소 | Starvation 발생 (실측으로 확인) |
| **B. ForkJoinPool.commonPool()** | 가장 빠른 성능 | 스레드 수 제어 불가, 다른 기능과 풀 공유 |
| **C. 전용 파일 파싱 Executor** | 스레드 수 명시적 제어, Starvation 해소 | Executor 빈 하나 추가 |

- **A안**은 실제 부하 테스트에서 Starvation이 발생하여 탈락
- **B안**은 빠르지만, 프로덕션에서 스레드 수를 제어할 수 없어 예측이 어려움
- **C안**은 배치 레벨(사용자 수)과 파일 레벨(파일 수)의 동시성을 분리하여 관리 가능

### "풀을 더 키우면 되지 않나?"

단순히 `parsingExecutor`의 `maxPoolSize`를 늘리면 Starvation이 완화되는 것은 맞다. 하지만 하나의 풀로 관리하면, **"동시 사용자 수"와 "동시 Gemini 호출 수"를 독립적으로 제어할 수 없다.** 풀 사이즈를 정하려면 `max = 동시 사용자 × (1 + 파일수)`처럼 두 변수의 곱으로 계산해야 하고, 어느 한쪽이 변하면 다시 튜닝해야 한다.

이 서비스에서 파일은 최대 3장이다. 즉 **부모 1개가 항상 자식 3개를 생성하는 1:3 구조**다. 이 비율이 고정되어 있으므로, 배치 동시성과 파일 동시성을 분리하여 각각 독립적으로 관리하는 것이 자연스럽다고 판단했다.

---

## 내가 선택한 기술

**C안: 전용 파일 파싱 Executor**를 선택했다.

배치 수준의 동시성(`parsingExecutor`)과 파일 수준의 동시성(`fileParsingExecutor`)을 분리했다.

```
사용자 요청 → parsingExecutor (max=10)       ← "몇 명이 동시에 파싱할 수 있는가"
               └─ fileParsingExecutor (max=15) ← "몇 장이 동시에 Gemini를 호출하는가"
                    ├─ 파일A → Gemini (~9초)
                    ├─ 파일B → Gemini (~9초)
                    └─ 파일C → Gemini (~9초)
```

### 전용 Executor 설정

```java
@Bean(name = "fileParsingExecutor")
public Executor fileParsingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(6);
    executor.setMaxPoolSize(15);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("file-parse-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
}
```

### 거부 정책: CallerRunsPolicy

스레드풀이 가득 찼을 때 어떻게 할 것인가? 두 가지를 고민했다.

- **AbortPolicy** (기본값): 즉시 예외를 던진다. 빠르지만, 해당 파일의 파싱 결과가 유실된다.
- **CallerRunsPolicy**: 호출한 쪽의 스레드에서 직접 실행한다. 느리지만, 작업이 누락되지 않는다.

이 서비스에서는 **부분 성공/부분 실패**가 중요했다. 3장 중 2장이 성공하고 1장이 실패하면, 성공한 2장은 저장하고 실패한 1장만 에러로 알려준다. 만약 스레드풀 포화로 작업이 거부되면 그 파일은 파싱 시도조차 못 하게 된다.

그래서 `CallerRunsPolicy`를 선택했다. 풀이 가득 차면 부모 스레드가 직접 파일을 처리한다. 전체 처리량은 일시적으로 줄어들 수 있지만, **모든 파일이 파싱 시도는 된다**는 보장을 얻었다.

### 병렬 처리 코드

```java
void parseBatchFiles(TempExpenseMeta meta, List<File> files, String taskId) {
    AtomicInteger completed = new AtomicInteger(0);
    List<String> succeededFileKeys = Collections.synchronizedList(new ArrayList<>());
    List<FailedFile> failedFiles = Collections.synchronizedList(new ArrayList<>());

    List<CompletableFuture<Void>> futures = files.stream()
            .map(file -> CompletableFuture.runAsync(() -> {
                try {
                    parseAndPersistExpenses(file, meta, rateContext);
                    succeededFileKeys.add(file.getS3Key());
                } catch (Exception e) {
                    failedFiles.add(new FailedFile(file.getS3Key(), resolveErrorCode(e)));
                } finally {
                    int done = completed.incrementAndGet();
                    progressPublisher.publishProgress(taskId, toPercent(done, total), ...);
                }
            }, fileParsingExecutor))  // ← 전용 Executor 사용
            .toList();

    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    // 이후 성공/실패 결과에 따라 SSE 완료 또는 에러 이벤트 발행
}
```

순차 구조에서는 `int++`와 `ArrayList`로 충분했지만, 병렬 구조에서는 여러 스레드가 동시에 접근하므로 `AtomicInteger`와 `Collections.synchronizedList()`로 변경했다.

---

## 적용 결과

### 테스트 환경

Docker Compose로 앱(2CPU, 1G), MySQL, Redis, LocalStack(S3 mock), WireMock(Gemini mock)을 구성했다.

WireMock에 Gemini API의 고정 지연을 설정하여 실제 API 응답 시간(~9초)을 시뮬레이션했다:

```json
// wiremock/mappings/gemini-generate-content-success.json
{
  "request": { "method": "POST", "urlPathPattern": "/models/.+:generateContent" },
  "response": {
    "status": 200,
    "fixedDelayMilliseconds": 9000,
    "jsonBody": { "candidates": [{ "content": { "parts": [{ "text": "..." }] } }] }
  }
}
```

### 테스트 실행

```bash
# 1. Docker 환경 기동
./gradlew clean bootJar -x test
docker compose -f docker-compose.test.yml build app
docker compose -f docker-compose.test.yml up -d

# 2. k6 부하 테스트 실행 (이미지 3장, VU 1→3→5→0, 3분)
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e IMAGE_COUNT=3 \
  -e PARSE_TIMEOUT_SECONDS=180 \
  -e CONFIRM_TIMEOUT_SECONDS=120 \
  -e PRESIGNED_URL_HOST_OVERRIDE=http://localhost:4567 \
  -e START_VUS=1 \
  -e STAGE_1_DURATION=30s -e STAGE_1_TARGET=3 \
  -e STAGE_2_DURATION=2m  -e STAGE_2_TARGET=5 \
  -e STAGE_3_DURATION=30s -e STAGE_3_TARGET=0 \
  load-tests/tempexpense-image-workflow.k6.js
```

### 세 가지 방식 비교

동일한 k6 부하 조건에서 세 가지 방식의 상대적 성능을 비교했다.

| 메트릭 | ① 순차 처리 | ② 같은 Executor 병렬<br>(Starvation) | ③ 전용 Executor 병렬<br>(최종 채택) |
|--------|-----------|-----------------------------------|-------------------------------|
| **parse avg** | 17.93s | **26.30s** 🔴 | **15.13s** |
| **parse p90** | 24.05s | 35.85s | 18.98s |
| **parse max** | 26.44s | — | 23.33s |
| **처리량 (3분)** | 34회 | 19회 | **38회** |
| 파싱 성공률 | 100% | 100% | 100% |
| Starvation | — | 🔴 발생 | — |

### 핵심 발견

1. **같은 Executor에서 병렬 처리하면 오히려 느려진다.** 부모 스레드가 `.join()`으로 풀을 점유한 채 자식 스레드를 기다리면 Starvation이 발생한다. 순차 처리(17.93s)보다 더 느린 26.3s가 측정되었다.

2. **풀을 분리하면 Starvation이 해소된다.** 전용 Executor로 분리하자 parse avg가 17.93s → 15.13s로 개선되고, 처리량이 34회 → 38회로 증가했다. 순차 처리 max=26.44s가 병렬 처리 max=23.33s로 단축된 것이 관찰된다.

3. **동시성 자료구조로의 전환이 필수다.** 병렬 구조에서는 `int++`와 `ArrayList` 대신 `AtomicInteger`와 `synchronizedList`를 사용해야 한다.

### 배운 점

"Executor에 `CompletableFuture`로 넣으면 알아서 병렬로 되겠지"라는 생각이 얼마나 위험한지 직접 체감했다. 부하 테스트를 돌리기 전까지는 코드만 봐서는 Starvation을 예측하기 어려웠다. k6 같은 도구로 실제 동시 사용자 환경을 재현하고 측정하는 과정이 중요하다는 것을 배웠다.
