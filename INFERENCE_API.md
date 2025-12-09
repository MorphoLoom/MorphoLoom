# Inference API Documentation

## 개요
`recursing_keldysh` 컨테이너에서 Python 추론 스크립트를 실행하는 REST API입니다.

## API 엔드포인트

### 1. 추론 실행
- **URL**: `POST /api/v1/inference/execute`
- **Content-Type**: `application/json`

#### 요청 본문
```json
{
    "sourcePath": "/app/storage/user/1/images/test.png",
    "drivingPath": "/app/storage/user/1/videos/test.mp4",
    "userId": 1
}
```

#### 응답 예시 (성공)
```json
{
    "success": true,
    "message": "추론이 성공적으로 실행되었습니다.",
    "executedCommand": "docker exec recursing_keldysh python inference.py -s /app/storage/user/1/images/test.png -d /app/storage/user/1/videos/test.mp4",
    "output": "추론 실행 결과 출력...",
    "error": null
}
```

#### 응답 예시 (실패)
```json
{
    "success": false,
    "message": "추론 실행 중 오류가 발생했습니다.",
    "executedCommand": "docker exec recursing_keldysh python inference.py -s /app/storage/user/1/images/test.png -d /app/storage/user/1/videos/test.mp4",
    "output": null,
    "error": "오류 메시지..."
}
```

### 2. 서비스 상태 확인
- **URL**: `GET /api/v1/inference/status`

#### 응답 예시
```json
{
    "success": true,
    "message": "추론 서비스가 정상적으로 실행 중입니다.",
    "executedCommand": "docker ps --filter name=recursing_keldysh",
    "output": "컨테이너 실행 중",
    "error": null
}
```

## 사용 방법

### cURL을 사용한 테스트
```bash
# 1. 서비스 상태 확인
curl -X GET http://localhost:18080/api/v1/inference/status

# 2. 추론 실행
curl -X POST http://localhost:18080/api/v1/inference/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/app/storage/user/1/images/test.png",
    "drivingPath": "/app/storage/user/1/videos/test.mp4",
    "userId": 1
  }'
```

## 주의사항

1. **컨테이너 상태**: `recursing_keldysh` 컨테이너가 실행 중이어야 합니다.
2. **파일 경로**: 소스 이미지와 드라이빙 비디오 파일이 컨테이너 내에서 접근 가능한 경로에 있어야 합니다.
3. **권한**: 파일에 대한 읽기 권한이 있어야 합니다.
4. **타임아웃**: 명령 실행 시간이 10분을 초과하면 타임아웃됩니다.

## 오류 코드

- **200**: 성공
- **400**: 잘못된 요청 파라미터
- **500**: 서버 오류 또는 추론 실행 실패
- **503**: 서비스 사용 불가 (컨테이너 미실행)