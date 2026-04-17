-- =========================================
-- 데이터베이스 초기화 스크립트
-- =========================================

-- 기존 테이블 삭제
DROP TABLE IF EXISTS courses;

-- =========================================
-- 테이블 생성
-- =========================================

CREATE TABLE courses
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    price       BIGINT        NOT NULL,
    capacity    INT           NOT NULL,
    creator_id  BIGINT        NOT NULL,
    start_date  DATE          NOT NULL,
    end_date    DATE          NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    created_at  DATETIME,
    updated_at  DATETIME
);

-- =========================================
-- 더미 데이터 삽입
-- =========================================

-- COURSES
INSERT INTO courses (title, description, price, capacity, creator_id, start_date, end_date, status, created_at,
                     updated_at)
VALUES ('자바 입문', '자바 기초 강의', 10000, 30, 1, '2026-04-20', '2026-05-20', 'OPEN', NOW(), NOW()),
       ('스프링 부트', '스프링 핵심 강의', 20000, 25, 2, '2026-04-25', '2026-05-25', 'OPEN', NOW(), NOW()),
       ('JPA 실전', 'JPA 활용 강의', 30000, 20, 1, '2026-05-01', '2026-06-01', 'DRAFT', NOW(), NOW()),
       ('QueryDSL', 'QueryDSL 심화', 25000, 15, 3, '2026-05-10', '2026-06-10', 'DRAFT', NOW(), NOW()),
       ('자료구조', 'CS 기초 강의', 15000, 40, 2, '2026-04-18', '2026-05-18', 'OPEN', NOW(), NOW()),
       ('알고리즘', '코딩 테스트 대비', 18000, 35, 3, '2026-04-22', '2026-05-22', 'OPEN', NOW(), NOW()),
       ('운영체제', 'OS 개념 강의', 22000, 20, 1, '2026-05-05', '2026-06-05', 'CLOSED', NOW(), NOW()),
       ('데이터베이스', 'DB 설계 및 SQL', 27000, 30, 2, '2026-04-28', '2026-05-28', 'OPEN', NOW(), NOW()),
       ('네트워크', '네트워크 기초', 16000, 25, 3, '2026-05-03', '2026-06-03', 'DRAFT', NOW(), NOW()),
       ('클린 코드', '코드 품질 개선', 19000, 20, 1, '2026-04-30', '2026-05-30', 'OPEN', NOW(), NOW());