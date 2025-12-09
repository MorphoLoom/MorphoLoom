-- 테스트 계정 생성 SQL
-- 비밀번호는 BCrypt로 해시되어야 합니다.
--
-- BCrypt 해시 생성 방법:
-- 1. 온라인 도구: https://bcrypt-generator.com/
-- 2. Java 코드:
--    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
--    String hash = encoder.encode("your_password");
--    System.out.println(hash);
-- 3. Spring Boot 애플리케이션에서 임시로 생성

-- ============================================
-- 예시 계정 1: test@example.com (비밀번호: password123)
-- ============================================
-- 주의: 아래 해시는 예시입니다. 실제 사용 시 새로 생성한 해시를 사용하세요.
-- BCrypt 해시는 매번 다르게 생성되지만, 같은 비밀번호라면 모두 검증 가능합니다.
INSERT INTO users (email, password_hash, username, social_provider, created_at)
VALUES (
    'donggeun.kang@dtonic.io',
    '$2a$10$g9A.wtWWj1XeS1hdCRa14e4vDqob9qr4HDprFJLX1myqWFQldQSAe',  -- password123의 BCrypt 해시
    'admin',
    'NONE',
    NOW()
)
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 예시 계정 2: admin@example.com (비밀번호: admin123)
-- ============================================
-- 아래 해시를 실제 생성한 해시로 교체하세요
-- INSERT INTO users (email, password_hash, username, social_provider, created_at)
-- VALUES (
--     'admin@example.com',
--     '$2a$10$YOUR_BCRYPT_HASH_HERE',  -- admin123의 BCrypt 해시로 교체 필요
--     'admin',
--     'NONE',
--     NOW()
-- )
-- ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 사용자 정의 계정 생성 예시
-- ============================================
-- 원하는 이메일, 사용자명, 비밀번호로 계정을 생성하려면:
-- 1. 비밀번호의 BCrypt 해시를 생성 (위 방법 참고)
-- 2. 아래 SQL의 값들을 수정하여 실행
--
-- INSERT INTO users (email, password_hash, username, social_provider, created_at)
-- VALUES (
--     'your_email@example.com',      -- 이메일
--     '$2a$10$YOUR_BCRYPT_HASH_HERE',  -- BCrypt 해시
--     'your_username',                 -- 사용자명
--     'NONE',                          -- 소셜 제공자 (일반 가입은 'NONE')
--     NOW()                            -- 생성 시간
-- )
-- ON CONFLICT (email) DO NOTHING;

