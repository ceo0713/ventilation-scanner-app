#!/bin/bash

# 환기 스캐너 앱 GitHub 자동 업로드 스크립트

echo "🚀 환기 스캐너 앱을 GitHub에 업로드합니다..."
echo ""

# 사용자 정보 입력
read -p "GitHub 사용자명을 입력하세요: " GITHUB_USERNAME
read -p "GitHub 이메일을 입력하세요: " GITHUB_EMAIL

# Git 설정
echo ""
echo "📝 Git 설정 중..."
git config user.name "$GITHUB_USERNAME"
git config user.email "$GITHUB_EMAIL"

# 저장소 이름
REPO_NAME="ventilation-scanner-app"

echo ""
echo "📦 커밋 준비 중..."
git add .
git commit -m "Initial commit: Ventilation Scanner App with auto-build"

echo ""
echo "✅ 커밋 완료!"
echo ""
echo "⚠️  다음 단계를 진행하세요:"
echo ""
echo "1. GitHub에서 새 저장소 만들기:"
echo "   → https://github.com/new 접속"
echo "   → Repository name: $REPO_NAME"
echo "   → Public 선택"
echo "   → 'Create repository' 클릭"
echo ""
echo "2. Personal Access Token 생성 (아직 없다면):"
echo "   → https://github.com/settings/tokens 접속"
echo "   → 'Generate new token (classic)' 클릭"
echo "   → Note: 'Ventilation Scanner Upload'"
echo "   → Scopes: ✅ repo 체크"
echo "   → 'Generate token' 클릭"
echo "   → 토큰 복사 (저장하세요!)"
echo ""
echo "3. 아래 명령 실행:"
echo ""
echo "   git remote add origin https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "   (Username: $GITHUB_USERNAME)"
echo "   (Password: Personal Access Token 입력)"
echo ""
echo "4. 업로드 완료 후:"
echo "   → https://github.com/$GITHUB_USERNAME/$REPO_NAME/actions 에서 빌드 확인"
echo "   → 5-10분 후 https://github.com/$GITHUB_USERNAME/$REPO_NAME/releases 에서 APK 다운로드"
echo ""
echo "5. APK 다운로드 링크:"
echo "   → https://github.com/$GITHUB_USERNAME/$REPO_NAME/releases/latest/download/app-debug.apk"
echo ""
echo "이 링크를 핸드폰으로 보내세요! 📱"
