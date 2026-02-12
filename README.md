# 환기 스캐너 - Android 3D 스캔 + CFD 시뮬레이션 앱

핸드폰 카메라를 이용한 실내 공간 3D 스캔 및 환기 시뮬레이션 앱입니다.

## 주요 기능

### 1. 3D 공간 스캔
- **ARCore** 기반 실시간 3D 스캔
- Depth API를 활용한 포인트 클라우드 캡처
- 자동 메시 생성 (Bounding Box 기반)
- ARCore 미지원 기기에서는 간단한 박스 모델 생성

### 2. 환기 설정
- 입구(문, 창문) 및 출구(환기구) 추가
- 각 개구부의 위치, 크기, 풍속 설정
- 저장된 스캔 데이터 선택 및 관리

### 3. CFD 시뮬레이션
- **Lattice Boltzmann Method (LBM)** 기반 2D 유체 시뮬레이션
- JavaScript WebView에서 온디바이스 실행 (클라우드 불필요)
- 128x128 그리드 해상도 (실시간 시뮬레이션)
- 평균/최대 풍속, 환기율(ACH) 계산

### 4. 3D 시각화
- **Three.js** 기반 3D 렌더링
- 실내 공간 메시 표시
- 기류 벡터 화살표 오버레이
- 인터랙티브 카메라 컨트롤

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| **언어** | Kotlin |
| **3D 스캔** | ARCore (Depth API) |
| **CFD 시뮬레이션** | JavaScript LBM (D2Q9) |
| **3D 렌더링** | Three.js (WebView) |
| **데이터베이스** | Room (SQLite) |
| **UI** | Material Design 3, Navigation Component |

## 시스템 요구사항

### 권장 사양
- Android 8.0 (API 26) 이상
- ARCore 지원 기기 ([지원 목록](https://developers.google.com/ar/devices))
- 카메라 권한 필요
- 메모리: 최소 4GB RAM

### 최소 사양 (ARCore 미지원)
- Android 8.0 (API 26) 이상
- 간단한 박스 모델로 시뮬레이션 가능

## 빌드 방법

### 사전 준비
1. Android Studio Hedgehog (2023.1.1) 이상 설치
2. Android SDK 34 설치
3. Kotlin 플러그인 활성화

### 빌드 단계
```bash
# 프로젝트 클론
cd "3d scanner app"

# Gradle 빌드
./gradlew assembleDebug

# APK 설치 (기기 연결 필요)
./gradlew installDebug
```

### Android Studio에서 실행
1. Android Studio에서 프로젝트 열기
2. 기기 연결 또는 에뮬레이터 실행
3. Run 버튼 클릭 (Shift+F10)

## 사용 방법

### 1단계: 공간 스캔
1. **스캔** 탭으로 이동
2. **스캔 시작** 버튼 클릭
3. 핸드폰을 천천히 움직여 공간 촬영
4. 충분한 포인트가 수집되면 **스캔 중지**
5. **스캔 저장** 버튼으로 데이터 저장

**참고**: ARCore 미지원 기기는 "간단 모델 생성" 버튼으로 4m x 3m x 2.5m 기본 공간 생성

### 2단계: 환기 설정
1. **설정** 탭으로 이동
2. 드롭다운에서 저장된 스캔 선택
3. **입구 추가** 버튼으로 문/창문 추가
4. **출구 추가** 버튼으로 환기구 추가
5. **시뮬레이션 실행** 버튼으로 설정 저장

### 3단계: 시뮬레이션 및 결과 확인
1. **결과** 탭으로 이동
2. **시뮬레이션 시작** 버튼 클릭
3. CFD 계산 완료 대기 (약 5-10초)
4. 기류 시각화 및 통계 확인
   - 평균 풍속
   - 최대 풍속
   - 환기율 (ACH - Air Changes per Hour)

## 프로젝트 구조

```
app/src/main/
├── java/com/ventilation/scanner/
│   ├── MainActivity.kt                  # 메인 액티비티
│   ├── arcore/
│   │   ├── ARCoreManager.kt             # ARCore 세션 관리
│   │   └── MeshGenerator.kt             # 포인트 클라우드 → 메시 변환
│   ├── cfd/
│   │   └── CFDSimulator.kt              # CFD WebView 브리지
│   ├── data/
│   │   ├── ScanData.kt                  # 데이터 모델
│   │   └── RoomDatabase.kt              # Room DB 설정
│   └── ui/
│       ├── scan/ScanFragment.kt         # 스캔 화면
│       ├── config/ConfigFragment.kt     # 설정 화면
│       └── result/ResultFragment.kt     # 결과 화면
├── assets/
│   ├── cfd-lbm.js                       # LBM 시뮬레이터
│   ├── cfd-simulator.html              # CFD WebView
│   └── three-viewer.html                # 3D 뷰어
└── res/
    ├── layout/                          # XML 레이아웃
    ├── navigation/                      # 네비게이션 그래프
    └── values/                          # 문자열, 색상, 테마
```

## CFD 시뮬레이션 원리

### Lattice Boltzmann Method (LBM)
- **격자 구조**: D2Q9 (2D, 9방향 속도)
- **충돌 연산자**: BGK (Bhatnagar-Gross-Krook)
- **경계 조건**: Bounce-back (벽면), Zou-He (입/출구)
- **시간 스텝**: 500회 반복 (수렴까지)

### 시뮬레이션 한계
- ⚠️ **2D 시뮬레이션**: 높이 방향 무시 (Top-down view)
- ⚠️ **정확도**: ±20% (프리뷰 목적, 엔지니어링 등급 아님)
- ⚠️ **해상도**: 128x128 (세밀한 기류 패턴 제한)
- ✅ **장점**: 실시간, 무료, 오프라인 작동

### 정밀도 향상 방법 (향후 개선)
- 3D LBM 구현 (D3Q19)
- 그리드 해상도 증가 (256x256)
- GPU 가속 (RenderScript / WebGL2)
- 클라우드 CFD 옵션 (OpenFOAM)

## 데이터 저장

### Room Database 스키마
- **scans**: 3D 스캔 데이터 (메시, 바운딩 박스)
- **ventilation_configs**: 환기 설정 (입/출구 위치)
- **simulation_results**: CFD 결과 (속도장, 통계)

### 데이터 경로
- `/data/data/com.ventilation.scanner/databases/ventilation_scanner_database`

### 데이터 백업
- Android 자동 백업 활성화 (백업 규칙: `backup_rules.xml`)

## 성능 최적화

### ARCore 포인트 클라우드
- **Voxelization**: 0.05m 복셀 크기로 다운샘플링
- **다운샘플링**: 1000개 포인트로 제한
- **메모리**: 평균 50-100KB per scan

### CFD 시뮬레이션
- **WebView 격리**: 메인 스레드 차단 방지
- **비동기 실행**: Kotlin Coroutine 활용
- **메모리**: 128x128 그리드 = 약 2MB

### 3D 렌더링
- **Level of Detail (LOD)**: Three.js 자동 최적화
- **Wireframe + Mesh**: 공간 구조 명확화

## 알려진 문제 및 해결

### ARCore 초기화 실패
**증상**: "이 기기는 ARCore를 지원하지 않습니다" 메시지

**해결**:
1. Google Play Services for AR 설치 확인
2. ARCore 지원 기기 목록 확인
3. 간단 모델 생성 사용 (ARCore 불필요)

### WebView 블랙 스크린
**증상**: 결과 탭에서 검은 화면만 표시

**해결**:
1. Chrome WebView 업데이트
2. 하드웨어 가속 활성화 확인
3. JavaScript 활성화 확인 (자동 설정됨)

### 시뮬레이션 느림
**증상**: CFD 계산이 30초 이상 소요

**해결**:
1. 그리드 해상도 감소 (128 → 64)
2. 타임스텝 감소 (500 → 200)
3. 고성능 모드 활성화 (설정 → 배터리)

## 라이선스 및 크레딧

### 오픈소스 라이브러리
- **ARCore** - Apache 2.0 (Google)
- **Three.js** - MIT License
- **Material Design** - Apache 2.0
- **Kotlin** - Apache 2.0

### CFD 알고리즘
- Lattice Boltzmann Method 기반
- 참고: Jos Stam, "Fluid Simulation for Dummies" (2003)
- cfd-wasm 프로젝트 영감 ([msakuta/cfd-wasm](https://github.com/msakuta/cfd-wasm))

## 개발 로드맵

### v1.0 (현재)
- ✅ ARCore 3D 스캔
- ✅ 2D LBM CFD 시뮬레이션
- ✅ Three.js 3D 시각화
- ✅ SQLite 로컬 저장

### v1.1 (계획)
- [ ] 객체 인식 (YOLO26 TFLite) - 문/창문 자동 감지
- [ ] 다중 스캔 병합 (여러 방을 하나로)
- [ ] 환기 점수 계산 (건축법 기준)
- [ ] PDF 보고서 내보내기

### v2.0 (향후)
- [ ] 3D CFD (D3Q19)
- [ ] GPU 가속 (RenderScript)
- [ ] 클라우드 CFD 옵션 (OpenFOAM API)
- [ ] IoT 센서 통합 (실시간 온도/CO2)

## 기여 방법

1. Fork 프로젝트
2. Feature 브랜치 생성 (`git checkout -b feature/AmazingFeature`)
3. 변경사항 커밋 (`git commit -m 'Add AmazingFeature'`)
4. 브랜치에 Push (`git push origin feature/AmazingFeature`)
5. Pull Request 생성

## 문의 및 지원

- **이슈 트래커**: GitHub Issues
- **이메일**: support@ventilationscanner.com
- **문서**: [Wiki](https://github.com/yourrepo/wiki)

## 면책 조항

이 앱의 CFD 시뮬레이션 결과는 **참고용**이며, 엔지니어링 설계나 법적 준수 목적으로 사용하기에 충분한 정확도를 보장하지 않습니다. 정밀한 환기 분석이 필요한 경우 전문 엔지니어에게 문의하세요.

---

**Made with ❤️ for better indoor air quality**
