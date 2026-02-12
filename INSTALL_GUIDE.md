# 핸드폰에 앱 설치하기 - 완전 가이드

## 📱 준비물
- Mac (현재 컴퓨터)
- Android 핸드폰 (Android 8.0 이상)
- USB 케이블 (핸드폰 충전 케이블)
- 인터넷 연결 (Android Studio 다운로드용)

---

## 🚀 방법 1: Android Studio 사용 (가장 쉬움)

### **1단계: Android Studio 설치 (20분)**

#### 1-1. 다운로드
1. 브라우저에서 접속: https://developer.android.com/studio
2. "Download Android Studio" 초록 버튼 클릭
3. 약관 동의 체크 → "Download Android Studio for Mac" 클릭
4. DMG 파일 다운로드 대기 (~1GB, 5-10분)

#### 1-2. 설치
1. 다운로드한 `android-studio-*.dmg` 파일 더블클릭
2. Android Studio 아이콘을 Applications 폴더로 드래그
3. Applications 폴더에서 Android Studio 실행
4. "Open" 클릭 (보안 경고 나오면)

#### 1-3. 초기 설정
1. **Welcome 화면**: "Next" 클릭
2. **Install Type**: "Standard" 선택 → "Next"
3. **UI Theme**: 원하는 테마 선택 → "Next"
4. **Verify Settings**: "Next" 클릭
5. **License Agreement**: 각 항목 "Accept" → "Finish"
6. SDK 다운로드 대기 (~2-3GB, 10-15분)
7. "Finish" 클릭

---

### **2단계: 프로젝트 열기 (5분)**

#### 2-1. Android Studio에서 프로젝트 열기
1. Android Studio 메인 화면
2. "Open" 버튼 클릭 (또는 File → Open)
3. 폴더 선택:
   ```
   /Users/ceo/Desktop/project/3d scanner app
   ```
4. "Open" 클릭

#### 2-2. Gradle 동기화 대기
- 화면 하단에 "Gradle Sync" 진행 표시
- "Build: Successful" 메시지 대기 (2-5분)
- 에러 발생 시 → 아래 "문제 해결" 섹션 참고

---

### **3단계: 핸드폰 준비 (3분)**

#### 3-1. 개발자 옵션 활성화

**모든 Android 기기 공통:**
1. **설정** 앱 열기
2. **휴대전화 정보** (또는 **디바이스 정보**) 선택
3. **빌드 번호** 찾기:
   - 삼성: 설정 → 휴대전화 정보 → 소프트웨어 정보 → **빌드 번호**
   - LG: 설정 → 휴대전화 정보 → 소프트웨어 정보 → **빌드 번호**
   - 기타: 설정 → 휴대전화 정보 → **빌드 번호**
4. **빌드 번호를 7번 연속 터치**
5. "개발자 모드가 활성화되었습니다" 메시지 확인
6. 비밀번호/패턴 입력 (요청 시)

#### 3-2. USB 디버깅 활성화
1. 설정 → **개발자 옵션** (또는 **Developer options**)
2. **USB 디버깅** 토글 ON
3. 경고 팝업 → "확인" 클릭

#### 3-3. USB 케이블 연결
1. USB 케이블로 핸드폰과 Mac 연결
2. 핸드폰 화면 잠금 해제
3. 팝업 확인:
   - "USB 디버깅을 허용하시겠습니까?"
   - ✅ **"항상 이 컴퓨터에서 허용"** 체크
   - **"확인"** 클릭
4. "USB 용도 선택" 팝업 (나오면):
   - **"파일 전송"** 또는 **"MTP"** 선택

---

### **4단계: 앱 실행 (1분)**

#### 4-1. Android Studio에서 기기 확인
1. Android Studio 상단 툴바 확인
2. 기기 선택 드롭다운에서 핸드폰 모델명 표시 확인
   - 예: "Samsung SM-G991N" 또는 "Pixel 6"
3. 기기가 안 보이면:
   - Mac 터미널 열기 (Command + Space → "터미널")
   - 입력: `~/Library/Android/sdk/platform-tools/adb devices`
   - 핸드폰이 "device" 상태로 표시되는지 확인

#### 4-2. 앱 실행
1. Android Studio 상단 툴바에서:
   - 초록색 ▶️ **"Run"** 버튼 클릭
   - (또는 메뉴: Run → Run 'app')
   - (또는 키보드: Control + R)
2. "Select Deployment Target" 창:
   - 핸드폰 기기 선택
   - "OK" 클릭
3. 빌드 진행 표시 (화면 하단)
   - "Building..." → "Installing APK..." → "Launching app..."
4. 핸드폰에서 앱 자동 실행 확인! 🎉

---

## 🔧 문제 해결

### **문제 1: Gradle Sync 실패**

**증상**: "Gradle sync failed" 에러

**해결**:
```bash
# 터미널에서 실행
cd "/Users/ceo/Desktop/project/3d scanner app"

# Gradle Wrapper 생성
# (Android Studio의 Gradle을 사용)
# File → Invalidate Caches → Invalidate and Restart
```

또는:
1. Android Studio → Preferences (Command + ,)
2. Build, Execution, Deployment → Gradle
3. "Use Gradle from" → "gradle-wrapper.properties file" 선택
4. Apply → OK
5. File → Sync Project with Gradle Files

---

### **문제 2: 기기가 Android Studio에 안 보임**

**증상**: 드롭다운에 핸드폰 이름이 없음

**해결 1: ADB 재시작**
```bash
# 터미널에서 실행
~/Library/Android/sdk/platform-tools/adb kill-server
~/Library/Android/sdk/platform-tools/adb start-server
~/Library/Android/sdk/platform-tools/adb devices
```

출력 예시 (정상):
```
List of devices attached
abc123456789    device
```

출력 예시 (문제):
```
List of devices attached
abc123456789    unauthorized
```
→ 핸드폰에서 USB 디버깅 허용 팝업 다시 확인

**해결 2: USB 케이블 재연결**
1. USB 케이블 뺐다가 다시 연결
2. 핸드폰 잠금 해제
3. "USB 디버깅 허용" 팝업 다시 확인

**해결 3: 다른 USB 포트 사용**
- Mac의 다른 USB 포트에 연결
- USB 허브 사용 중이면 Mac에 직접 연결

---

### **문제 3: "Installation failed" 에러**

**증상**: 빌드는 성공했지만 설치 실패

**해결 1: 기존 앱 삭제**
```bash
# 터미널에서 실행
~/Library/Android/sdk/platform-tools/adb uninstall com.ventilation.scanner
```

**해결 2: 핸드폰 공간 확인**
- 설정 → 저장소 → 최소 500MB 이상 확보

**해결 3: ARCore 설치 확인**
1. Google Play 스토어 열기
2. "Google Play Services for AR" 검색
3. 설치 또는 업데이트

---

### **문제 4: 앱이 실행되지만 검은 화면**

**증상**: 앱은 열리지만 화면이 검정색

**해결**: ARCore 미지원 기기일 수 있음
1. 앱 내에서 Toast 메시지 확인
2. "간단 모델 생성" 버튼 사용 (ARCore 불필요)

---

## 📦 방법 2: APK 파일로 직접 설치 (고급)

Android Studio 없이 APK만 만들어서 설치하는 방법입니다.

### **사전 준비: Gradle Wrapper 생성**

Android Studio를 **한 번만** 열어서 Gradle Wrapper를 생성합니다:

1. Android Studio에서 프로젝트 열기 (위 Step 2 참고)
2. Terminal 탭 (하단) 클릭
3. 명령 실행:
   ```bash
   ./gradlew wrapper --gradle-version=8.2
   ```
4. 완료 후 Android Studio 종료 가능

### **Step 1: APK 빌드**

```bash
# 터미널에서 프로젝트 폴더로 이동
cd "/Users/ceo/Desktop/project/3d scanner app"

# Debug APK 빌드
./gradlew assembleDebug

# APK 위치 확인
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

빌드 완료 시 APK 파일:
```
app/build/outputs/apk/debug/app-debug.apk
```

### **Step 2: APK를 핸드폰으로 전송**

**방법 A: AirDrop (Mac ↔ iPhone은 불가)**
- Android ↔ Mac은 AirDrop 미지원
- 다른 방법 사용 필요

**방법 B: USB로 직접 전송**
```bash
# ADB로 설치
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**방법 C: 이메일/클라우드**
1. APK 파일을 이메일로 자신에게 전송
2. 또는 Google Drive / Dropbox 업로드
3. 핸드폰에서 다운로드
4. 파일 앱에서 APK 파일 터치 → 설치

**방법 D: 웹서버 (로컬 네트워크)**
```bash
# 간단한 HTTP 서버 실행 (APK 폴더에서)
cd app/build/outputs/apk/debug
python3 -m http.server 8000

# 핸드폰 브라우저에서 접속 (Mac IP 확인 필요)
# http://192.168.x.x:8000/app-debug.apk
```

### **Step 3: 핸드폰에서 APK 설치**

1. **출처 불명 앱 허용**:
   - 설정 → 생체 인식 및 보안 (또는 보안)
   - "알 수 없는 앱 설치" → 파일 앱(또는 Chrome) 허용

2. **APK 설치**:
   - 다운로드한 `app-debug.apk` 파일 터치
   - "설치" 버튼 클릭
   - 완료 후 "열기" 또는 앱 서랍에서 실행

---

## 🎯 각 방법 비교

| 방법 | 난이도 | 시간 | 장점 | 단점 |
|------|--------|------|------|------|
| **Android Studio** | ⭐⭐ | 30분 | 가장 쉬움, 디버깅 가능 | 용량 큼 (5GB) |
| **APK 직접 설치** | ⭐⭐⭐⭐ | 10분 | 빠름 | Gradle 설정 필요 |

---

## 💡 추천 흐름

**첫 테스트:**
→ **Android Studio 방법** (디버깅 필요시 편리)

**이후 배포:**
→ APK 직접 설치 (다른 사람에게 공유)

---

## 📞 추가 도움

**에러 발생 시:**
1. 에러 메시지 전체 복사
2. Android Studio → Help → Collect Logs
3. 또는 터미널 출력 확인

**ARCore 지원 확인:**
- https://developers.google.com/ar/devices
- 본인 핸드폰 모델 검색

**카메라 권한 확인:**
- 설정 → 앱 → 환기 스캐너 → 권한 → 카메라 허용

---

이제 핸드폰에서 3D 스캔 + 환기 시뮬레이션을 테스트할 수 있습니다! 🚀
