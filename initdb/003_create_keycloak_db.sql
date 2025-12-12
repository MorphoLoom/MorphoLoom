-- Keycloak 전용 데이터베이스 생성
CREATE DATABASE keycloak
    WITH 
    OWNER = morpholoom
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

-- Keycloak 데이터베이스에 권한 부여
GRANT ALL PRIVILEGES ON DATABASE keycloak TO morpholoom;
