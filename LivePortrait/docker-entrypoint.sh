#!/bin/bash
set -e

# pretrained_weights 디렉토리가 비어있거나 없으면 다운로드
if [ ! -d "/app/pretrained_weights" ] || [ -z "$(ls -A /app/pretrained_weights 2>/dev/null)" ]; then
    echo "Pretrained weights not found. Downloading from HuggingFace..."
    huggingface-cli download KwaiVGI/LivePortrait \
        --local-dir /app/pretrained_weights \
        --exclude "*.git*" "README.md" "docs" || {
        echo "Warning: Failed to download from HuggingFace. Please download manually."
        echo "Run: huggingface-cli download KwaiVGI/LivePortrait --local-dir pretrained_weights"
    }
else
    echo "Pretrained weights already exist. Skipping download."
fi

# 원래 명령어 실행
exec "$@"

