#!/bin/bash

echo "=== Android 개발 환경 설정 스크립트 ==="
echo ""

# Homebrew 설치
echo "1. Homebrew 설치 중..."
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Homebrew PATH 설정
echo ""
echo "2. Homebrew PATH 설정 중..."
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"

# JDK 17 설치
echo ""
echo "3. OpenJDK 17 설치 중..."
brew install --cask temurin

# JAVA_HOME 설정
echo ""
echo "4. JAVA_HOME 환경변수 설정 중..."
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "export JAVA_HOME=\$(/usr/libexec/java_home -v 17)" >> ~/.zshrc

# 설치 확인
echo ""
echo "5. 설치 확인..."
java -version
echo ""
echo "JAVA_HOME: $JAVA_HOME"

echo ""
echo "=== 설치 완료! ==="
echo "이제 다음 명령어로 앱을 빌드할 수 있습니다:"
echo ""
echo "  cd '/Users/ceo/Desktop/project/3d scanner app'"
echo "  ./gradlew clean assembleDebug"
echo ""
