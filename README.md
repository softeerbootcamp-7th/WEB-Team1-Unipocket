## 교환학생 지출의 모든 것, 통합 가계부 유니포켓 **UniPocket** 프로젝트 공간입니다!

> 국내 카드부터 해외 카드까지 한 번에 관리하는 교환학생 가계부

---

## 서비스 정보

### 기본 설명

해외 카드까지 한 번에 관리할 수 있는 교환학생 전용 가계부 서비스입니다.

국내 카드 내역은 자동 연동하고, 해외 카드 내역은 업로드 방식으로 통합 관리합니다.

### 타겟 사용자

한국 카드와 해외 카드, 현금 등 여러 통화를 함께 사용하는 교환학생

### 주요 기능

#### 1. 지출 기록 및 자동화

- **다양한 입력 방식 지원**: 영수증 OCR 인식, 은행 앱 스크린샷 분석, 거래 내역 파일(CSV/Excel) 업로드 및 직접 입력 기능 제공
- **지출 내역 자동 분류**: 업로드된 데이터를 바탕으로 날짜, 거래처, 카테고리, 결제 수단 자동 추출/분류

#### 2. 데이터 다차원 필터링 및 조회

- 기간(날짜), 거래처, 카테고리, 결제 수단 기준으로 상세 내역을 빠르게 필터링하여 조회 가능

#### 3. 개인화된 지출 대시보드 (위젯)

- 결제 수단별/카테고리별 지출 현황을 시각화된 위젯으로 구성해 메인 화면에서 한눈에 확인 가능

#### 4. 여행 및 특정 목적별 지출 관리

- 여행 기간 중 지출을 별도 세션으로 분리하여 일상 지출과 독립적으로 관리 가능

#### 5. 동일 국가 교환학생 간 지출 비교

- 동일 국가/지역 체류 교환학생 평균 지출과 내 지출을 비교해 소비 수준을 객관적으로 점검 가능

### 서비스 주소

- https://www.unipocket.co.kr/

---

## 🧑🏻‍💻 팀원 소개

<table>
  <thead>
    <tr>
      <th align="center" colspan="3">💻 Frontend</th>
      <th align="center" colspan="3">🛠️ Backend</th>
    </tr>
  </thead>
  <tbody>
    <tr>

<!-- Frontend -->
<td align="center" width="200">
    <a href="https://github.com/1jiwoo27">
    <img src="https://github.com/1jiwoo27.png" alt="엄지우" width="120"  />
    <br />
    <sub><b>엄지우</b></sub>
    </a>
</td>
<td align="center" width="200">
    <a href="https://github.com/Kjiw0n">
    <img src="https://github.com/Kjiw0n.png" alt="김지원" width="120"  />
    <br />
    <sub><b>김지원</b></sub>
    </a>
</td>
<td align="center" width="200">
    <a href="https://github.com/minngyuseong">
    <img src="https://github.com/minngyuseong.png" alt="성민규" width="120"  />
    <br />
    <sub><b>성민규</b></sub>
    </a>
</td>

<!-- Backend -->
<td align="center" width="200">
    <a href="https://github.com/AnarchyDeve">
        <img src="https://github.com/AnarchyDeve.png" alt="김동균" width="120"  />
        <br />
        <sub><b>김동균</b></sub>
    </a>
</td>
<td align="center" width="200">
    <a href="https://github.com/kcw2205">
        <img src="https://github.com/kcw2205.png" alt="김찬우" width="120"  />
        <br />
        <sub><b>김찬우</b></sub>
    </a>
</td>

</tr>

  </tbody>
</table>

<br/>

## 🔧 Stacks

### Frontend

> 서비스의 사용자 인터페이스(UI)와 상호작용 로직을 담당합니다.

<img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" /> <img src="https://img.shields.io/badge/pnpm-F69220?style=for-the-badge&logo=pnpm&logoColor=white" />

<img src="https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=white" /> <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />

<img src="https://img.shields.io/badge/TanStack%20Query-FF4154?style=for-the-badge&logo=reactquery&logoColor=white" /> <img src="https://img.shields.io/badge/TanStack%20Router-315CF5?style=for-the-badge&logo=tanstack&logoColor=white" />

<img src="https://img.shields.io/badge/TailwindCSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" /> <img src="https://img.shields.io/badge/shadcn/ui-000000?style=for-the-badge&logo=shadcnui&logoColor=white" />

<details>
<summary>Frontend 디렉토리 구조</summary>

```
frontend/
├── src/
│   ├── api/              # API 모듈 (account-books, auth, cards, expenses 등)
│   ├── assets/           # 이미지/아이콘/정적 리소스
│   ├── components/       # 공통 및 도메인 UI 컴포넌트
│   ├── constants/        # 상수 정의
│   ├── data/             # 프론트 샘플/정적 데이터
│   ├── hooks/            # 커스텀 훅
│   ├── lib/              # 유틸리티
│   ├── pages/            # 페이지 컴포넌트
│   ├── routes/           # TanStack Router 라우트 정의
│   ├── stores/           # 전역 상태(zustand)
│   ├── styles/           # 전역 스타일
│   ├── test/             # 프론트 단위 테스트
│   └── types/            # 타입 정의
├── index.html
├── package.json
├── pnpm-lock.yaml
└── vite.config.ts
```

</details>

<br/>

### Backend

> 서비스의 비즈니스 로직, 데이터베이스, API를 관리합니다.

<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white" /> <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" /> <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" />

<img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=spring&logoColor=white" /> <img src="https://img.shields.io/badge/MyBatis-000000?style=for-the-badge&logoColor=white" /> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />

<details>
<summary>Backend 디렉토리 구조</summary>

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/genesis/unipocket/
│   │   │   ├── accountbook/
│   │   │   ├── analysis/
│   │   │   ├── auth/
│   │   │   ├── exchange/
│   │   │   ├── expense/
│   │   │   ├── media/
│   │   │   ├── tempexpense/
│   │   │   ├── travel/
│   │   │   ├── user/
│   │   │   ├── widget/
│   │   │   └── UnipocketApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test-unit.yml
│   │       └── application-test-it.yml
│   └── test/
│       └── java/com/genesis/unipocket/
├── docs/
├── build.gradle
├── settings.gradle
├── gradlew
└── gradlew.bat
```

</details>

<br/>

### Deploy

> CI/CD 자동화 및 클라우드 환경에서 서비스 배포를 관리합니다.

<img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" /> <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white" />

---

## 시스템 구성도

추가 예정

---

## Github & 서비스 링크

- Wiki: https://github.com/softeerbootcamp-7th/WEB-Team1-Unipocket/wiki
- Figma: https://www.figma.com/design/fC3n8H8ialRaNYWbI1coXA/-1%E1%84%90%E1%85%B5%E1%86%B7--Final-Design---Handoff?m=dev
- 서비스 기획안: https://www.figma.com/slides/CzLAOcxVJT5eNuchdMZbgB/%EC%A0%9C%EB%84%A4%EC%8B%9C%EC%8A%A4-%EC%84%9C%EB%B9%84%EC%8A%A4-%EA%B8%B0%ED%9A%8D%EC%95%88?t=edg8re9TN70qPRtK-0

---

## 실행 가이드

### 1) 사전 준비

- Frontend: Node.js 20+ , pnpm
- Backend: Java 17

### 2) Frontend 실행

```bash
cd frontend
pnpm install
pnpm dev
```

- 기본 실행 주소: `http://localhost:5173`
- 필요 시 `.env.local`에 아래 값을 설정해 API 연동
  - `VITE_API_BASE_URL`
  - `VITE_API_PROXY_TARGET`
  - `VITE_CDN_URL`
  - `VITE_COOKIE_DOMAIN`
  - `VITE_USER_ID`

### 3) Backend 실행

```bash
cd backend
./gradlew bootRun
```

Windows PowerShell에서는 아래 명령을 사용합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

- 로컬에서 `dev` 프로필로 실행하려면 환경변수(`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` 등)가 필요합니다.
- 테스트 실행

```bash
cd backend
./gradlew unitTest
./gradlew integrationTest
```

### 4) 전체 로컬 개발 권장 순서

1. Backend 실행
2. Frontend 실행
3. 브라우저에서 `http://localhost:5173` 접속
