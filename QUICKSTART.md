# ⚡ 핸드폰에 앱 설치 - 가장 빠른 방법

## 🎯 3분 안에 다운로드 링크 만들기

---

## 방법 1: GitHub Desktop (클릭만으로)

### **1. GitHub Desktop 설치**
1. https://desktop.github.com 다운로드
2. 설치 후 GitHub 계정으로 로그인

### **2. 프로젝트 업로드**
1. File → Add Local Repository
2. 폴더 선택: `/Users/ceo/Desktop/project/3d scanner app`
3. "Create repository" (에러 나면 클릭)
4. **"Publish repository"** 클릭
5. Repository name: `ventilation-scanner-app`
6. **"Keep this code private" 체크 해제** (공개)
7. "Publish" 클릭

### **3. APK 다운로드 링크 받기**
1. 5-10분 대기 (자동 빌드)
2. GitHub 저장소 → Releases 탭
3. APK 다운로드 링크 복사

**링크 형식:**
```
https://github.com/당신의사용자명/ventilation-scanner-app/releases/latest/download/app-debug.apk
```

---

## 방법 2: 자동 스크립트 (터미널)

```bash
# 터미널에서 실행
cd "/Users/ceo/Desktop/project/3d scanner app"
./upload-to-github.sh

# 안내에 따라 진행
```

---

## 핸드폰에서 설치

### **1. 링크 받기**
- 카카오톡, 문자, 이메일로 링크 전송
- 또는 QR 코드 생성: https://www.qr-code-generator.com

### **2. APK 다운로드**
- 핸드폰에서 링크 클릭
- Chrome에서 APK 다운로드

### **3. 설치**
1. 설정 → 보안 → "알 수 없는 출처" 허용
2. 다운로드 폴더 → APK 파일 터치
3. "설치" 클릭
4. 완료! 🎉

---

## 🔥 가장 쉬운 방법 (추천)

**GitHub 계정이 있다면:**
→ **GitHub Desktop** (위 방법 1)

**처음이라면:**
→ **자동 스크립트** (위 방법 2)

---

전체 과정: **5-10분**
핸드폰 설치: **1분**

자세한 내용은 `SIMPLE_INSTALL.md` 참고!
