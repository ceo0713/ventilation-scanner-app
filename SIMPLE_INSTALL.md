# 📱 핸드폰에 앱 설치하기 - 초간단 가이드

## 🎯 목표
**링크 하나로 핸드폰에 바로 설치하기**

---

## ✨ 3단계로 끝내기 (10분)

### **1단계: GitHub 계정 만들기 (3분)**

GitHub 계정이 없다면:
1. https://github.com 접속
2. "Sign up" 클릭
3. 이메일 입력 → 비밀번호 설정 → 사용자명 입력
4. 이메일 인증 완료

**이미 계정이 있다면 → 2단계로 이동**

---

### **2단계: 프로젝트를 GitHub에 올리기 (5분)**

#### 방법 A: GitHub Desktop 사용 (클릭만으로 가능)

**2-1. GitHub Desktop 다운로드**
1. https://desktop.github.com 접속
2. "Download for macOS" 클릭
3. 다운로드 후 설치
4. GitHub 계정으로 로그인

**2-2. 프로젝트 추가**
1. GitHub Desktop 실행
2. File → Add Local Repository
3. 폴더 선택:
   ```
   /Users/ceo/Desktop/project/3d scanner app
   ```
4. "Create a repository" 클릭 (만약 에러 나면)

**2-3. GitHub에 업로드**
1. 좌측 하단에 "Publish repository" 버튼 클릭
2. Repository name: `ventilation-scanner-app`
3. ✅ **"Keep this code private" 체크 해제** (공개)
4. "Publish Repository" 클릭
5. 완료! 🎉

---

#### 방법 B: 터미널 명령 (Mac 터미널에서)

```bash
# 1. 프로젝트 폴더로 이동
cd "/Users/ceo/Desktop/project/3d scanner app"

# 2. Git 설정 (첫 사용자만)
git config --global user.name "당신의이름"
git config --global user.email "your@email.com"

# 3. 커밋
git add .
git commit -m "Initial commit: Ventilation Scanner App"

# 4. GitHub에 새 저장소 만들기
# https://github.com/new 에서 저장소 생성
# 이름: ventilation-scanner-app
# Public 선택 → Create repository

# 5. GitHub에 업로드
git remote add origin https://github.com/당신의사용자명/ventilation-scanner-app.git
git branch -M main
git push -u origin main
```

**GitHub 사용자명과 토큰 입력 요청 시:**
- Username: GitHub 사용자명
- Password: **Personal Access Token** (아래 참고)

**Personal Access Token 생성:**
1. https://github.com/settings/tokens 접속
2. "Generate new token (classic)" 클릭
3. Note: "Ventilation Scanner Upload"
4. Expiration: 90 days
5. Scopes: ✅ **repo** 체크
6. "Generate token" 클릭
7. 토큰 복사 (한 번만 표시됨!)
8. Password 입력 시 이 토큰 사용

---

### **3단계: APK 다운로드 링크 받기 (2분)**

**3-1. GitHub Actions 확인**

업로드 후 자동으로 빌드가 시작됩니다:

1. GitHub 저장소 페이지 접속:
   ```
   https://github.com/당신의사용자명/ventilation-scanner-app
   ```

2. 상단 탭에서 **"Actions"** 클릭

3. "Build APK" 워크플로우 확인
   - 🟡 노란색: 빌드 진행 중 (5-10분 대기)
   - ✅ 초록색: 빌드 성공!
   - ❌ 빨간색: 빌드 실패 (아래 "문제 해결" 참고)

**3-2. APK 다운로드**

빌드가 성공하면:

1. **"Releases"** 탭 클릭 (저장소 메인 페이지 오른쪽)
2. 최신 릴리스 (v1.0.x) 클릭
3. **Assets** 섹션에서 `app-debug.apk` 다운로드 링크 복사

**다운로드 링크 예시:**
```
https://github.com/your-username/ventilation-scanner-app/releases/download/v1.0.1/app-debug.apk
```

**3-3. 핸드폰으로 링크 보내기**

**방법 1: QR 코드**
1. https://www.qr-code-generator.com 접속
2. URL 입력: APK 다운로드 링크
3. QR 코드 생성
4. 핸드폰 카메라로 스캔

**방법 2: 문자/카카오톡**
- APK 링크를 본인에게 문자 또는 카카오톡으로 전송

**방법 3: 이메일**
- 본인 이메일로 링크 전송

---

## 📥 핸드폰에서 APK 설치하기

### **1. APK 다운로드**

핸드폰에서 위 링크 클릭:
1. Chrome 브라우저에서 링크 열기
2. `app-debug.apk` 파일 다운로드
3. "완료" 클릭

### **2. 출처 불명 앱 설치 허용**

**삼성 갤럭시:**
```
설정 → 생체 인식 및 보안 → 앱 설치
→ Chrome (또는 인터넷) → "이 출처에서 허용" ON
```

**LG:**
```
설정 → 보안 → 알 수 없는 출처
→ "알 수 없는 출처의 앱 허용" ON
```

**기타 Android:**
```
설정 → 보안 → "알 수 없는 출처" 또는 "알 수 없는 앱 설치"
→ 브라우저 앱 허용
```

### **3. APK 설치**

1. 다운로드 폴더 또는 알림에서 `app-debug.apk` 터치
2. "설치" 버튼 클릭
3. "완료" 또는 "열기" 클릭
4. 앱 서랍에서 "환기 스캐너" 실행! 🎉

---

## 🔄 앱 업데이트 방법

코드를 수정한 후:

```bash
# 터미널에서
cd "/Users/ceo/Desktop/project/3d scanner app"

git add .
git commit -m "Update: 기능 개선"
git push

# GitHub Actions가 자동으로 새 APK 빌드
# Releases에서 최신 버전 다운로드
```

또는:
- GitHub Desktop에서 "Commit to main" → "Push origin" 클릭

---

## ⚡ 더 간단한 대안: Firebase App Distribution (선택)

GitHub보다 더 간단하게 배포하고 싶다면:

### **1. Firebase 설정 (5분)**

1. https://console.firebase.google.com 접속
2. Google 계정으로 로그인
3. "프로젝트 추가" 클릭
4. 프로젝트 이름: "Ventilation Scanner"
5. 기본 설정으로 완료

### **2. App Distribution 활성화**

1. 좌측 메뉴 → "App Distribution"
2. "시작하기" 클릭
3. Android 앱 선택

### **3. APK 업로드 (매번)**

1. GitHub Actions에서 빌드된 APK 다운로드
2. Firebase Console → App Distribution
3. "Release" → "Upload" → APK 파일 선택
4. 테스터 이메일 추가 (본인 이메일)
5. "Distribute" 클릭
6. 핸드폰에서 이메일 확인 → 링크 클릭 → 설치

**장점:**
- 테스터 그룹 관리
- 자동 업데이트 알림
- 크래시 리포팅

**단점:**
- 매번 수동 업로드 필요 (자동화 가능하지만 복잡)

---

## 🎯 최종 추천 방법

### **첫 설치:**
→ **GitHub Actions** (위 3단계)
   - 한 번 설정하면 자동 빌드
   - 무료, 무제한
   - 공개 링크

### **팀/친구와 공유:**
→ **Firebase App Distribution**
   - 테스터 초대 쉬움
   - 업데이트 푸시 알림

---

## 🐛 문제 해결

### **Q: GitHub Actions 빌드가 실패해요**

**A: gradlew 권한 확인**

```bash
cd "/Users/ceo/Desktop/project/3d scanner app"
chmod +x gradlew
git add gradlew
git commit -m "Fix gradlew permissions"
git push
```

### **Q: APK 다운로드 링크가 안 보여요**

**A: Releases 수동 생성**

1. GitHub 저장소 → "Releases" 탭
2. "Create a new release" 클릭
3. Tag: v1.0.0
4. Title: First Release
5. 하단 "Assets" → APK 파일 드래그
6. "Publish release" 클릭

APK 파일 위치:
```bash
# GitHub Actions Artifacts에서 다운로드
# 또는 로컬 빌드:
./gradlew assembleDebug
# APK 위치: app/build/outputs/apk/debug/app-debug.apk
```

### **Q: 핸드폰에서 "앱이 설치되지 않음" 에러**

**A: 공간 확인 및 기존 앱 삭제**

1. 설정 → 저장소 → 최소 500MB 확보
2. 기존 "환기 스캐너" 앱 삭제 후 재설치

---

## 📊 비교표

| 방법 | 난이도 | 자동화 | 비용 | 공유 | 추천 |
|------|--------|--------|------|------|------|
| **GitHub Actions** | ⭐⭐ | ✅ | 무료 | 공개링크 | ✅ 1순위 |
| Firebase App Dist. | ⭐⭐⭐ | ❌ | 무료 | 이메일 초대 | 2순위 |
| Google Drive | ⭐ | ❌ | 무료 | 공유링크 | 임시용 |

---

## 🚀 완료!

이제 핸드폰에서 링크만 클릭하면 앱이 설치됩니다!

**최종 링크 형식:**
```
https://github.com/your-username/ventilation-scanner-app/releases/latest/download/app-debug.apk
```

이 링크를 카카오톡, 문자, 또는 QR 코드로 핸드폰에 보내세요! 📲
