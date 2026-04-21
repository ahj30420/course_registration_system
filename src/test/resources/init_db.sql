-- =========================================
-- 데이터베이스 초기화 스크립트
-- =========================================

-- 기존 테이블 삭제
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS waitlists CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS courses CASCADE;

SET REFERENTIAL_INTEGRITY FALSE;

-- =========================================
-- 테이블 생성
-- =========================================

CREATE TABLE courses
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    title                    VARCHAR(100)  NOT NULL,
    description              VARCHAR(1000) NOT NULL,
    price                    BIGINT        NOT NULL,
    capacity                 INT           NOT NULL,
    current_enrollment_count INT           NOT NULL,
    creator_id               BIGINT        NOT NULL,
    start_date               DATE          NOT NULL,
    end_date                 DATE          NOT NULL,
    status                   VARCHAR(20)   NOT NULL,
    created_at               DATETIME,
    updated_at               DATETIME
);

CREATE TABLE enrollments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    confirm_at DATETIME,
    cancel_at  DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT uk_enrollment_course_user UNIQUE (course_id, user_id),
    FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE waitlists (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id  BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (course_id) REFERENCES courses (id)
);

-- =========================================
-- 더미 데이터 삽입
-- =========================================


INSERT INTO courses (title, description, price, capacity, current_enrollment_count, creator_id, start_date, end_date,
                     status, created_at,
                     updated_at)
VALUES ('자바 입문', '자바 기초 강의', 10000, 30, 0, 1, '2026-04-20', '2026-05-20', 'OPEN', NOW(), NOW()),
       ('스프링 부트', '스프링 핵심 강의', 20000, 25, 0, 2, '2026-04-25', '2026-05-25', 'OPEN', NOW(), NOW()),
       ('JPA 실전', 'JPA 활용 강의', 30000, 20, 0, 1, '2026-05-01', '2026-06-01', 'DRAFT', NOW(), NOW()),
       ('QueryDSL', 'QueryDSL 심화', 25000, 15, 0, 3, '2026-05-10', '2026-06-10', 'DRAFT', NOW(), NOW()),
       ('자료구조', 'CS 기초 강의', 15000, 40, 0, 2, '2026-04-18', '2026-05-18', 'OPEN', NOW(), NOW()),
       ('알고리즘', '코딩 테스트 대비', 18000, 35, 0, 3, '2026-04-22', '2026-05-22', 'OPEN', NOW(), NOW()),
       ('운영체제', 'OS 개념 강의', 22000, 20, 0, 1, '2026-05-05', '2026-06-05', 'CLOSED', NOW(), NOW()),
       ('데이터베이스', 'DB 설계 및 SQL', 27000, 30, 0, 2, '2026-04-28', '2026-05-28', 'OPEN', NOW(), NOW()),
       ('네트워크', '네트워크 기초', 16000, 25, 0, 3, '2026-05-03', '2026-06-03', 'DRAFT', NOW(), NOW()),
       ('클린 코드', '코드 품질 개선', 19000, 20, 0, 1, '2026-04-30', '2026-05-30', 'OPEN', NOW(), NOW());


INSERT INTO enrollments (course_id, user_id, status, created_at, updated_at)
VALUES (1, 1, 'CONFIRMED', '2026-04-01 10:00:00', NOW()),
       (2, 1, 'PENDING', '2026-04-02 10:00:00', NOW()),
       (5, 1, 'CONFIRMED', '2026-04-03 10:00:00', NOW()),
       (6, 1, 'PENDING', '2026-04-04 10:00:00', NOW()),
       (8, 1, 'CONFIRMED', '2026-04-05 10:00:00', NOW()),
       (1, 2, 'CONFIRMED', '2026-04-02 10:00:00', NOW()),
       (2, 2, 'PENDING', '2026-04-03 10:00:00', NOW()),
       (5, 2, 'CONFIRMED', '2026-04-04 10:00:00', NOW()),
       (6, 2, 'PENDING', '2026-04-05 10:00:00', NOW()),
       (8, 2, 'CONFIRMED', '2026-04-06 10:00:00', NOW());


INSERT INTO waitlists (course_id, user_id, created_at, updated_at)
VALUES (3, 1, '2026-04-10 10:00:00', NOW()),
       (4, 1, '2026-04-11 10:00:00', NOW());