# LivePortrait Dockerfile
FROM nvidia/cuda:11.8.0-cudnn8-runtime-ubuntu22.04

# Python 3.9 설치
RUN apt-get update && apt-get install -y \
    python3.10 \
    python3.10-dev \
    python3-pip \
    git \
    ffmpeg \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Python 대안 설정 (기존 python3 심볼릭 링크 충돌 방지)
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.10 1 && \
    update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.10 1

    RUN cd /usr/local/cuda/lib64 && \
    ln -sf libnvrtc.so.11.8.89 libnvrtc.so && \
    ln -sf libnvrtc.so.11.8.89 libnvrtc.so.11    
# 작업 디렉토리 설정
WORKDIR /app

# requirements 파일 복사
COPY requirements.txt requirements_base.txt ./

# PyTorch 및 의존성 설치
# CUDA 11.8용 PyTorch 설치
RUN pip3 install --no-cache-dir torch==2.3.0 torchvision==0.18.0 torchaudio==2.3.0 --index-url https://download.pytorch.org/whl/cu118

# 나머지 의존성 설치
RUN pip3 install --no-cache-dir -r requirements_base.txt && \
    pip3 install --no-cache-dir -r requirements.txt

# HuggingFace CLI 설치 (pretrained weights 다운로드용)
RUN pip3 install --no-cache-dir "huggingface_hub[cli]"

# 전체 프로젝트 복사
COPY . .

# pretrained weights 다운로드 (선택사항 - 주석 해제하여 사용)
RUN huggingface-cli download KwaiVGI/LivePortrait --local-dir pretrained_weights --exclude "*.git*" "README.md" "docs" || true

# 포트 노출
EXPOSE 7860

# 기본 명령어 (Gradio 앱 실행)
# CMD ["python", "app.py", "--server_name", "0.0.0.0", "--server_port", "7860"]
CMD ["bash"]

