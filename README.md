# 🎸 ChordZip (코드집)

> **"나만의 기타 코드 운지법을 기록하고 관리하는 스마트한 세트리스트 매니저"**

![App Icon](app/src/main/ic_launcher-playstore.png)
## 📖 프로젝트 소개
**ChordZip**은 기존의 정형화된 코드 사전 앱들과 달리, **사용자가 직접 커스텀한 운지법(Fretboard)**을 기록할 수 있는 기타 연주자들을 위한 필수 유틸리티 앱입니다.

"이 노래의 이 부분에서는 코드를 이렇게 잡아야 소리가 예쁜데..." 하며 악보 구석에 메모하던 경험, 다들 있으시죠? ChordZip은 그 메모를 스마트폰 속으로 가져왔습니다. 노래별, 파트별로 나만의 코드를 정리하고, 공연 D-day와 연습 목표까지 한 번에 관리하세요.

## ✨ 주요 기능 (Key Features)

### 1. 🎼 커스텀 운지법 저장 (Interactive Fretboard)
- 단순히 이름만 적는 것이 아니라, **6줄 기타 프렛(Fret) 위에 직접 점을 찍어** 나만의 운지법을 시각적으로 저장합니다.
- 노래의 특정 파트(Verse, Chorus 등)마다 서로 다른 코드 보이싱을 기록할 수 있습니다.

### 2. 📂 체계적인 곡 관리 (Setlist Management)
- **노래(Song) > 파트(Part) > 코드(Chord)** 로 이어지는 3단 계층 구조로 곡을 디테일하게 정리합니다.
- 드래그 앤 드롭(Drag & Drop)으로 곡 순서와 파트 순서를 자유롭게 변경하여 공연 세트리스트를 짭니다.
- 자주 연주하는 곡은 '즐겨찾기'로 상단에 고정할 수 있습니다.

### 3. 📅 연습 목표 관리 (Goal & D-Day)
- 다가오는 합주나 공연 날짜를 D-Day로 설정합니다.
- 메인 화면 상단에서 남은 날짜와 목표를 매일 확인하며 동기를 부여받습니다.

### 4. 🛠️ 연주 편의 기능
- **유튜브 연동:** 원곡 링크를 저장해두고 연습할 때 바로 감상할 수 있습니다.
- **메타데이터:** 카포(Capo) 위치, 튜닝(Tuning), BPM 정보를 곡마다 따로 메모합니다.

### 5. 💾 완벽한 오프라인 지원 (Local Database)
- **Room Database**를 적용하여 인터넷 연결 없이도 모든 데이터를 안전하게 저장합니다.
- 앱을 종료하거나 폰을 재시작해도 데이터는 영구적으로 보존됩니다.

---

## 🛠️ 기술 스택 (Tech Stack)

이 프로젝트는 최신 안드로이드 개발 트렌드와 아키텍처를 준수하여 개발되었습니다.

| 구분 | 기술 | 설명 |
|:---:|:---:|:---|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white) | 100% Kotlin 기반 개발 |
| **UI** | ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=android&logoColor=white) | 선언형 UI 프레임워크 사용 (XML 없는 100% Compose) |
| **Architecture** | **MVVM** | ViewModel과 UIState 패턴을 통한 단방향 데이터 흐름(UDF) 구현 |
| **DI** | **Hilt** | 의존성 주입(Dependency Injection)을 통한 결합도 감소 |
| **Async** | **Coroutines & Flow** | 비동기 처리 및 실시간 데이터 스트림 관측 |
| **Database** | **Room** | SQLite 추상화 라이브러리를 통한 로컬 데이터 영구 저장 |
| **Design** | **Material 3** | 최신 안드로이드 디자인 가이드라인 준수 |

---

## 🏗️ 아키텍처 및 데이터 구조

### 데이터베이스 모델링 (ERD Concept)
- **Song Entity:** 제목, 가수, 즐겨찾기 여부, 메타데이터(BPM, Capo 등) 저장
- **Converters:** `List<Part>`와 `List<Chord>`와 같은 복잡한 객체를 JSON 형식으로 직렬화하여 DB에 저장
- **DDayInfo Entity:** 사용자별 1개의 목표 설정을 관리하는 별도 테이블 운용

### 데이터 흐름
`Room Database (DAO)` ➡ `ViewModel (StateFlow)` ➡ `UI (Compose)`
- **SSOT (Single Source of Truth):** 모든 데이터의 원본은 Room DB이며, UI는 DB의 상태를 관찰(Observe)하여 자동으로 갱신됩니다.

---

## 🚀 설치 및 실행 방법

1. 이 저장소를 클론합니다.
   ```bash
   git clone [https://github.com/YourUsername/ChordZip.git](https://github.com/YourUsername/ChordZip.git)
2. Android Studio (Koala 이상 권장)에서 프로젝트를 엽니다.
3. Gradle Sync가 완료될 때까지 기다립니다.
4. 에뮬레이터 또는 실제 기기(Android 11 이상 권장)를 연결하고 Run 버튼을 누릅니다.

## 🔮 추후 업데이트 예정 (Roadmap)
- [ ] 구글 로그인을 통한 기기 간 데이터 동기화 (Firebase 연동)
- [ ] 다른 사용자와 코드 악보 공유하기 기능
- [ ] 메트로놈 기능 고도화

## 👨‍💻 Developer: [PancakeMapleSyrup]
## 📧 Email: [sodaus3ro@gmail.com]
## 🐙 GitHub: [https://github.com/PancakeMapleSyrup]