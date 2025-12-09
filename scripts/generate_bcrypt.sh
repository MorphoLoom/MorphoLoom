#!/bin/bash
# BCrypt 해시 생성 스크립트
# 사용법: ./generate_bcrypt.sh [비밀번호]

PASSWORD=${1:-"password123"}

echo "BCrypt 해시 생성 중..."
echo ""

# Maven을 사용하여 BCrypt 해시 생성
cd "$(dirname "$0")/../Morpholoom" || exit 1

mvn compile exec:java -Dexec.mainClass="com.project.Morpholoom.util.BcryptGenerator" \
    -Dexec.args="$PASSWORD" -q 2>/dev/null || {
    echo "Maven 실행 실패. 다음 방법을 시도하세요:"
    echo "1. Maven이 설치되어 있는지 확인"
    echo "2. 프로젝트 디렉토리에서 직접 실행:"
    echo "   cd Morpholoom"
    echo "   mvn compile exec:java -Dexec.mainClass=\"com.project.Morpholoom.util.BcryptGenerator\" -Dexec.args=\"$PASSWORD\""
    echo ""
    echo "또는 온라인 도구 사용: https://bcrypt-generator.com/"
}

