# 수강 신청 시스템 (Course Registration System)

## 프로젝트 개요

크리에이터(강사)가 강의를 개설하고, 클래스메이트(수강생)가 수강 신청·결제·취소를 수행하는 RESTful 백엔드 서비스입니다.  
동시에 여러 사용자가 마지막 자리에 신청하는 상황을 **비관적 락(Pessimistic Lock)** 으로 안전하게 처리하며, 정원 초과 시 **대기열(Waitlist)** 기능을 통해 자동 순번 배정이
이루어집니다.

---

## 기술 스택

| 구분        | 기술                        |
|-----------|---------------------------|
| Language  | Java 17                   |
| Framework | Spring Boot 3.5.7         |
| ORM       | Spring Data JPA, QueryDSL |
| DB & 캐싱   | H2, Redis                 |
| 인증        | `Id` 헤더 (간략 처리)           |

---

## 실행 방법(Docker 실행)

```bash
docker-compose up --build
```

서버 기동 후: `http://localhost:8080`  
H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:course_registration`, 비밀번호 없음)

---

## API 목록 및 예시

## API 목록 및 예시

> 상세 요청/응답 예시는 `src/test/java/http` 참고해주세요  
Swagger UI: http://localhost:8080/swagger-ui/index.html

### Course API

- `POST /api/courses` : 강의 등록 (CREATOR-ID 헤더)
- `GET /api/courses?status={DRAFT|OPEN|CLOSED}` : 강의 목록 조회 (상태 필터)
- `GET /api/courses/{courseId}` : 강의 상세 조회
- `PATCH /api/courses/{courseId}/open` : 강의 오픈 (CREATOR-ID 헤더)
- `PATCH /api/courses/{courseId}/close` : 강의 수강 모집 마감 (CREATOR-ID 헤더)

### Enrollment / Waitlist API

- `POST /api/courses/{courseId}/enrollments` : 수강 신청 (정원 초과 시 WAITLISTED 등록)
- `POST /api/{enrollmentId}/confirm` : 결제 확정 처리 (PENDING -> CONFIRMED)
- `POST /api/{enrollmentId}/cancel` : 수강 취소 (취소 정책 적용)
- `GET /api/enrollments/my` : 내 수강 신청 목록 (페이지네이션)
- `GET /api/enrollments/my/waitlist` : 내 대기열 목록 (페이지네이션)
- `GET /api/enrollments/my/waitlist/{waitlistId}/rank` : 내 대기 순번 조회
- `GET /api/courses/{courseId}/enrollments` : 강의별 수강생 목록 조회 (CREATOR 전용)

---

## 데이터 모델 설명

### 핵심 테이블

```
courses (강의)
├── id (PK)
├── title, description, price
├── max_capacity        -- 최대 정원
├── current_enrollment_count  -- 현재 신청 인원
├── start_date, end_date
├── status (DRAFT | OPEN | CLOSED)
├── creator_id
└── created_at, updated_at

enrollments (수강 신청)
├── id (PK)
├── course_id (FK → courses)
├── user_id
├── status (PENDING | CONFIRMED | CANCELLED)
├── confirmed_at, cancelled_at
├── created_at, updated_at
└── UNIQUE(course_id, user_id) -- 중복 신청 방지

waitlists (대기열)
├── id (PK)
├── course_id (FK → courses)
├── user_id
├── created_at          -- 대기 순번 기준
└── UNIQUE(course_id, user_id) -- 중복 신청 방지
```

### 모델링 포인트

- 수강 확정/취소 이력은 `enrollments`에서 관리하고, 대기열 순번/큐 처리는 `waitlists`에서 분리 관리합니다.
- 대기열 승급 시 `waitlists` 선두 row를 제거하고 `enrollments(PENDING)`를 생성하는 방식으로 상태 전이를 명확히 표현합니다.

---

## 요구사항 해석 및 가정

1. 인증/인가는 과제 조건에 맞춰 간소화했습니다.
    - 강사: `CREATOR-ID` 헤더
    - 수강생: `USER-ID` 헤더

2. 정원 초과 시 신청은 실패하지 않고 `WAITLISTED` 상태로 등록됩니다.
    - 구현상 `waitlists` 테이블에 등록되며, API 응답에서 대기열 등록 상태를 확인할 수 있습니다.

3. 수강 신청 취소 시 대기열 자동 승급을 수행합니다.
    - 취소로 빈 자리가 생기면, 해당 강의 대기열의 가장 먼저 들어온 사용자(선입순)가 자동으로 `PENDING` 수강 신청으로 전환됩니다.

4. 취소 정책
    - `PENDING`: 즉시 취소 가능
    - `CONFIRMED`: 결제 확정 시점 기준 N일(기본 7일) 이내 취소 가능

5. 중복 신청 방지
    - 동일 강의에 대해 활성 상태(`PENDING`, `CONFIRMED`) 신청이 있으면 재신청 불가
    - `CANCELLED` 이후에는 재신청 허용

6. 상태 전이 규칙
    - Course: `DRAFT -> OPEN -> CLOSED` (OPEN에서만 신청 가능)
    - Enrollment: `PENDING -> CONFIRMED -> CANCELLED`

7. 동시성 가정
    - 마지막 좌석/동시 취소 등 충돌이 빈번할 수 있는 구간은 DB 락 기반으로 정합성을 우선합니다.

---

## 설계 결정과 이유

### 1) 비관적 락(Pessimistic Lock) 선택 이유

수강 신청/취소는 "선착순 + 정원 제한 + 정확한 인원수"가 핵심인 도메인입니다.  
특히 마지막 좌석에 동시 요청이 몰릴 때, 트랜잭션 간 순서와 정합성이 비즈니스적으로 중요하다고 판단했습니다.

- 구현: `Course` 조회 시 `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`)로 행 잠금
- 효과:
    - 동일 강의 정원 카운트 갱신 경쟁을 직렬화
    - 초과 모집/음수 카운트 같은 데이터 불일치 방지
    - 취소 시 대기열 승급까지 하나의 일관된 트랜잭션 흐름으로 처리 가능

### 2) 낙관적 락(Optimistic Lock)과의 비교

- 낙관적 락 장점
    - 락 대기가 없어 일반적으로 처리량(throughput)에 유리
- 낙관적 락 단점(본 도메인 관점)
    - 충돌 시 재시도 로직이 필수
    - 동시 요청에서 일부 트랜잭션은 충돌로 실패/재시도되어 "순차적 선착순 처리"가 불명확해질 수 있음

즉, 낙관적 락도 "정원 초과 방지" 자체는 가능하지만,  
수강 신청처럼 순서 보장과 확정적 처리 흐름을 보장하기는 어렵다고 판단하였습니다.

### 3) 트레이드오프 판단

- 선택: 비관적 락
- 얻는 것: 높은 정합성, 명확한 처리 순서
- 잃는 것: 락 경합 시 응답 지연/처리량 저하 가능성

과제의 핵심 요구사항(정원 정확성, 동시성 안정성)에 맞춰 성능보다 정합성을 우선했습니다.

### 4) 취소 + 대기열 승급에도 락을 적용한 이유

취소 시점에도 동시 취소/동시 승급 경쟁이 발생할 수 있어,

- 정원 감소
- 대기열 선두 추출
- 신규 `PENDING` 생성  
  을 같은 정합성 경계 안에서 처리하도록 설계했습니다.

이로써 "취소 1건당 승급 1건" 규칙을 안정적으로 유지합니다.

### 5) QueryDSL 동적 쿼리 채택 이유

강의 목록 조회는 현재 `status` 필터만 사용하지만,  
향후 가격/기간/키워드/강사 등의 필터 조합이 늘어날 가능성을 고려해 QueryDSL 기반 동적 쿼리로 구현했습니다.

- 필터 확장 시 유지보수 비용 감소
- 타입 안전한 쿼리 작성으로 리팩터링 안정성 확보

### 6) 캐싱 적용 이유와 운영 방식

강의 목록/상세는 조회 빈도가 높고 변경 빈도는 상대적으로 낮아 Redis 캐시를 적용했습니다.

- 캐시 대상
    - 강의 목록 (`status`별 key)
    - 강의 상세 (`courseId` key)
- 무효화 전략
    - 강의 생성/상태 변경/수강 신청/취소 시 관련 캐시 evict
- TTL
    - 기본 3분

정합성 손상 없이 조회 성능과 DB 부하를 완화하는 목적입니다.

---

## 테스트 실행 방법

```bash
# 전체 테스트
./gradlew test

# 테스트 리포트 (HTML)
open build/reports/tests/test/index.html
```

### 테스트 구성

| 클래스                                | 유형                       | 설명                                                          |
|------------------------------------|--------------------------|-------------------------------------------------------------|
| `CourseTest`                       | 단위 테스트 (Mockito)         | 상태 전이/유효성 규칙 검증                                             |
| `EnrollmentTest`                   | 단위 테스트 (Mockito)         | 상태 전이/유효성/취소 기간 규칙 검증                                       |
| `WaitlistTest`                     | 단위 테스트 (Mockito)         | 권한 검증                                                       |
| `CourseServiceTest`                | 단위 테스트 (Mockito)         | 상태 전환, 강의 CRUD 검증                                           |
| `EnrollmentServiceTest`            | 단위 테스트 (Mockito)         | 신청/확정/취소 비즈니스 규칙 검증                                         |
| `CourseJpaRepositoryTest`          | 통합 테스트 (@DataJpaTest)    | QueryDSL 동적 쿼리(상태 필터/전체 조회) 검증                              |
| `EnrollmentJpaRepositoryTest`      | 통합 테스트 (@DataJpaTest)    | 페이지네이션, fetch join 조회 검증                                    |
| `WaitlistJpaRepositoryTest`        | 통합 테스트 (@DataJpaTest)    | 대기열 페이지네이션 및 조회 검증                                          |
| `EnrollmentServiceIntegrationTest` | 통합 테스트 (@SpringBootTest) | 동시 신청 시 정원 초과 방지,<br/> 초과 인원 대기열 적재,<br/> 동시 취소 시 승급 정합성 검증 |

---


---

## 미구현 / 제약사항

- 실서비스 수준 인증/인가(JWT, Role 기반 권한, 토큰 검증)는 미구현
- 외부 결제 연동은 미구현 (결제 확정 API로 상태 전이만 처리)
- 단일 DB 인스턴스 기준 락 전략이며, 분산 환경 확장 시 별도 전략 검토 필요
- 현재 DB는 H2 기반(개발 편의)이며 MySQL/PostgreSQL 운영 전환 시 datasource 및 dialect 수정 필요.

---

## AI 활용 범위
 
AI는 생산성 보조 도구로 제한적으로 활용했습니다.

- 일부 반복 코드/테스트 케이스 아이디어 브레인스토밍
- 동시성 통합 테스트 시나리오(스레드 구성, 검증 포인트) 초안 보조
- README 설명 구조화 및 문장 다듬기 보조
