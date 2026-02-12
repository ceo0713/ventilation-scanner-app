# 📱 즉시 설치 가이드 - Google Drive 사용

## ⚡ 5분 만에 링크 만들기

GitHub 없이 Google Drive로 바로 APK 다운로드 링크를 만듭니다.

---

## 1단계: APK 파일 빌드 (자동)

### 방법 A: Android Studio 없이 빌드 (추천)

아래 명령을 **하나씩** 터미널에서 실행하세요:

```bash
# 1. 프로젝트 폴더로 이동
cd "/Users/ceo/Desktop/project/3d scanner app"

# 2. Homebrew 설치 확인 (Java 설치 위해 필요)
which brew || /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 3. Java 17 설치
brew install openjdk@17

# 4. Java 경로 설정
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 5. APK 빌드
./gradlew assembleDebug

# 6. APK 파일 위치 확인
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

**빌드 성공 시:**
```
BUILD SUCCESSFUL in 1m 23s
```

**APK 파일 위치:**
```
/Users/ceo/Desktop/project/3d scanner app/app/build/outputs/apk/debug/app-debug.apk
```

---

### 방법 B: Android Studio 사용

1. Android Studio 실행
2. 프로젝트 열기: `/Users/ceo/Desktop/project/3d scanner app`
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. 빌드 완료 알림 → "locate" 클릭
5. APK 파일 위치 확인

---

## 2단계: Google Drive에 업로드 (2분)

### 1. Google Drive 접속
- https://drive.google.com 열기
- Google 계정으로 로그인

### 2. APK 업로드
1. **"새로 만들기"** → **"파일 업로드"** 클릭
2. 파일 선택:
   ```
   /Users/ceo/Desktop/project/3d scanner app/app/build/outputs/apk/debug/app-debug.apk
   ```
3. 업로드 대기 (약 30초)

### 3. 공유 링크 생성
1. 업로드된 `app-debug.apk` 파일 **우클릭**
2. **"링크 공유"** 또는 **"공유"** 클릭
3. **"액세스 권한 변경"** 클릭
4. **"링크가 있는 모든 사용자"** 선택
5. **"링크 복사"** 클릭

**링크 형식 예시:**
```
https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view?usp=sharing
```

### 4. 직접 다운로드 링크로 변환

복사한 링크를 아래 형식으로 변경:

**원본:**
```
https://drive.google.com/file/d/FILE_ID/view?usp=sharing
```

**변경 후 (직접 다운로드):**
```
https://drive.google.com/uc?export=download&id=FILE_ID
```

**예시:**
```
원본: https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view?usp=sharing
변경: https://drive.google.com/uc?export=download&id=1a2b3c4d5e6f7g8h9i0j
```

---

## 3단계: 핸드폰으로 링크 전송 (1분)

### 방법 1: QR 코드 (가장 쉬움)

1. https://www.qr-code-generator.com 접속
2. 변경된 링크 붙여넣기
3. **"Create QR Code"** 클릭
4. 핸드폰 카메라로 QR 코드 스캔

### 방법 2: 카카오톡
- 본인에게 링크 전송
- 핸드폰 카카오톡에서 링크 클릭

### 방법 3: 문자 메시지
- 본인 번호로 링크 전송
- 핸드폰에서 링크 클릭

### 방법 4: 이메일
- 본인 이메일로 링크 전송
- 핸드폰에서 이메일 열기

---

## 4단계: 핸드폰에서 설치 (1분)

### 1. 링크 열기
- Chrome 브라우저에서 링크 클릭
- APK 자동 다운로드

### 2. 출처 불명 앱 허용 (처음만)

**삼성 갤럭시:**
```
설정 → 생체 인식 및 보안 → 앱 설치
→ Chrome → "이 출처에서 허용" ON
```

**LG / 기타:**
```
설정 → 보안 → "알 수 없는 출처"
→ Chrome 또는 인터넷 브라우저 허용
```

### 3. 설치
1. 다운로드 알림 터치 (또는 다운로드 폴더)
2. `app-debug.apk` 파일 터치
3. **"설치"** 클릭
4. **"열기"** 클릭
5. 완료! 🎉

---

## 🎯 전체 과정 요약

```
APK 빌드 (2분)
    ↓
Google Drive 업로드 (1분)
    ↓
공유 링크 생성 (30초)
    ↓
핸드폰으로 링크 전송 (30초)
    ↓
핸드폰에서 다운로드 + 설치 (1분)

총 소요 시간: 5분
```

---

## 🔄 대안: Dropbox 사용

Google Drive 대신 Dropbox 사용 가능:

1. https://www.dropbox.com 접속
2. APK 업로드
3. 공유 링크 생성
4. 링크 끝에 `?dl=1` 추가 (직접 다운로드)

**예시:**
```
원본: https://www.dropbox.com/s/abc123/app-debug.apk?dl=0
변경: https://www.dropbox.com/s/abc123/app-debug.apk?dl=1
```

---

## 🔄 대안: WeTransfer 사용 (24시간 유효)

링크가 24시간만 필요하다면:

1. https://wetransfer.com 접속
2. APK 파일 업로드
3. 본인 이메일 입력
4. "Transfer" 클릭
5. 이메일에서 다운로드 링크 확인

**장점:** 
- 회원가입 불필요
- 최대 2GB (무료)

**단점:**
- 7일 후 자동 삭제

---

## ⚠️ 문제 해결

### Q: APK 빌드 시 "command not found: gradlew" 에러

**A: gradlew 권한 설정**
```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Q: Java 관련 에러

**A: Java 17 설치 확인**
```bash
java -version

# Java 없으면 설치
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

### Q: Google Drive 링크 클릭 시 미리보기만 나옴

**A: 직접 다운로드 링크로 변경**
- FILE_ID 추출 후 변경된 형식 사용
- 또는 Google Drive 앱에서 "다운로드" 버튼 클릭

### Q: 핸드폰에서 "앱이 설치되지 않음" 에러

**A: 저장 공간 확인**
```
설정 → 저장소 → 최소 500MB 확보
```

---

## 📊 방법 비교

| 방법 | 시간 | 난이도 | 유효기간 | 추천 |
|------|------|--------|----------|------|
| **Google Drive** | 5분 | ⭐ | 영구 | ✅ 1순위 |
| Dropbox | 5분 | ⭐ | 영구 | 2순위 |
| WeTransfer | 3분 | ⭐ | 7일 | 테스트용 |
| GitHub | 10분 | ⭐⭐ | 영구 | 공개 배포 |

---

## ✅ 완료!

이제 핸드폰에서 링크만 클릭하면 앱이 설치됩니다!

**최종 링크:**
```
https://drive.google.com/uc?export=download&id=YOUR_FILE_ID
```

이 링크를 카카오톡이나 QR 코드로 전송하세요! 📲
