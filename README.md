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

2. 대기열(Waitlist) 기능의 해석
</br>과제에서 제시된 '대기열'의 의미를 다음과 같이 두 가지로 가설을 세워 검토했습니다.

- 비즈니스형 대기열: 정원 초과 시 수강 신청 기회를 얻기 위해 순번을 대기하는 기능
- 기술형 대기열: 대규모 트래픽 발생 시 시스템 부하를 방지하기 위해 요청을 큐(Queue)에 담아 순차 처리하는 기능

본 과제는 'CRUD + 비즈니스 규칙형' 과제이며, 선택 구현 사항에 대기열이 포함된 점을 고려하여 첫 번째 가설(비즈니스형 대기열)로 정의하고 구현했습니다. 수강 신청의 정원이 가득 찼을 때 사용자가 신청을 포기하지 않고 순번을 기다릴 수 있게 함으로써 사용자 경험을 개선하는 것에 목적을 두었습니다.

3. 정원 초과 시 신청은 실패하지 않고 `WAITLISTED` 상태로 등록됩니다.
    - 구현상 `waitlists` 테이블에 등록되며, API 응답에서 대기열 등록 상태를 확인할 수 있습니다.

4. 수강 신청 취소 시 대기열 자동 승급을 수행합니다.
    - 취소로 빈 자리가 생기면, 해당 강의 대기열의 가장 먼저 들어온 사용자(선입순)가 자동으로 `PENDING` 수강 신청으로 전환됩니다.

5. 취소 정책
    - `PENDING`: 즉시 취소 가능
    - `CONFIRMED`: 결제 확정 시점 기준 N일(기본 7일) 이내 취소 가능

6. 중복 신청 방지
    - 동일 강의에 대해 신청 이력이 있으면 재신청 불가

7. 상태 전이 규칙
    - Course: `DRAFT -> OPEN -> CLOSED` (OPEN에서만 신청 가능)
    - Enrollment: `PENDING -> CONFIRMED -> CANCELLED`

8. 동시성 가정
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

[추가적인 고민]

비관적 락은 트랜잭션이 락을 획득할 때까지 DB 커넥션을 점유하며 대기하므로, 요청이 급증하는 실제 운영 환경에서는 성능 저하의 원인이 될 수 있다고 생각합니다.

본 과제는 단일 DB 환경에서의 정합성 보장을 우선하여 비관적 락을 채택했으나, 향후 대규모 트래픽 대응 및 서버 스케일 아웃(Scale-out) 상황을 가정한다면 다음과 같은 개선 방향을 고려할 수 있습니다.
- Java synchronized의 한계:

애플리케이션 단에서 대기를 유도할 수 있으나, 여러 인스턴스로 서버가 확장될 경우 정합성을 보장할 수 없습니다.

- 분산 락(Distributed Lock) 도입:

Redis를 활용하여 DB 외부에서 락을 관리함으로써, DB 커넥션 부하를 줄이고 분산 환경에서도 안정적으로 동시성을 제어하는 구조로 고도화가 가능합니다.

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

### 6) 페이징 조회 최적화 (N+1 및 성능 고려)
수강 신청 내역(Enrollment) 조회 시, 연관된 강의(Course) 정보를 함께 보여주어야 하므로 성능 최적화를 위해 다음과 같은 결정을 내렸습니다.

- Fetch Join을 통한 N+1 문제 해결:

기본적으로 Lazy Loading을 사용하되, 목록 조회 시에는 join fetch를 사용하여 단 한 번의 쿼리로 연관 엔티티를 조회하도록 구현했습니다. 이를 통해 조회된 내역의 개수만큼 추가 쿼리가 발생하는 N+1 문제를 방지했습니다.

- 카운트 쿼리(Count Query) 분리 및 최적화:

JPA의 Page 처리 시, 기본적으로 데이터 조회 쿼리와 동일한 조인이 카운트 쿼리에도 포함되는 문제가 있습니다.</br>
Enrollment의 개수를 세는 데에는 사실상 Course와의 조인이 불필요하므로, @Query의 countQuery 속성을 사용하여 조인이 없는 가벼운 카운트 쿼리를 별도로 작성했습니다.
이를 통해 데이터가 방대해질 경우 발생할 수 있는 불필요한 조인 비용을 제거하고 전체적인 조회 성능을 개선했습니다.

### 7) 캐싱 적용 이유와 운영 방식

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

1) 실서비스 수준의 인증/인가 및 결제 보안
- JWT 기반 토큰 검증, Role 기반 권한 제어 등 실서비스 수준의 보안 모델은 제외하고 헤더를 통한 식별 방식으로 간소화했습니다.
- 외부 PG사 연동 대신 결제 확정 API를 통한 상태 전이 로직으로 대체했습니다.

2) 인프라 확장성 및 데이터베이스
- 현재 단일 DB 인스턴스 기준의 비관적 락 전략을 사용하고 있으며, 분산 서버 환경으로 확장 시 Redis 기반 분산 락 등으로의 전환이 필요합니다.
- H2 인메모리 DB를 사용 중이므로 실제 운영 환경(MySQL/PostgreSQL) 전환 시 설정 변경이 필요합니다.

3) 시스템 자정 작용을 위한 관리 정책 (Life Cycle Management)
</br>실제 운영 환경에서는 데이터의 무결성과 리소스 효율을 위해 다음과 같은 데이터 정리(Clean-up) 정책이 필수적이나, 본 과제에서는 설계 범위로만 고려하였습니다.

- 결제 만료 처리: PENDING 상태의 신청자가 일정 시간(예: 24시간) 내에 결제를 완료하지 않을 경우, 자동으로 CANCELLED 처리하여 자리를 해소하고 대기열 순번을 넘겨주는 로직이 필요합니다.

- 대기열 자동 정리: 강의 시작일이 도래하거나 종료된 강의에 대해서는 대기열(WAITLISTED)의 의미가 사라지므로, 시스템 부하 방지를 위해 해당 대기자들을 일괄 정리하는 정책이 수반되어야 합니다.
  
- 구현 제안: 만약 이를 구현한다면, Spring Scheduler를 활용해 일정 주기마다 만료 대상을 조회하여 처리하는 Polling 방식을 도입하여 자동화할 수 있을 것으로 판단됩니다. 본 과제에서는 명확한 비즈니스 운영 정책이 정의되지 않은 프로토타입 단계임을 고려하여 핵심 도메인 로직 구현에 집중하였습니다.
---

## AI 활용 범위
 
AI는 생산성 보조 도구로 제한적으로 활용했습니다.

- 일부 반복 코드/테스트 케이스 아이디어 브레인스토밍
- 동시성 통합 테스트 시나리오(스레드 구성, 검증 포인트) 초안 보조
- README 설명 구조화 및 문장 다듬기 보조
