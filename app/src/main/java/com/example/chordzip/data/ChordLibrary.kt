package com.example.chordzip.data

object ChordLibrary {
    // key: 코드 이름 (대문자로 저장), value: 운지 정보
    private val standardChords = mapOf(
        // === C 계열 ===
        "C" to ChordVoicing("C", 1, listOf(-1, 3, 2, 0, 1, 0)),
        "CM7" to ChordVoicing("CM7", 1, listOf(-1, 3, 2, 0, 0, 0)),
        "C7" to ChordVoicing("C7", 1, listOf(-1, 3, 2, 3, 1, 0)),
        "Cm" to ChordVoicing("Cm", 1, listOf(-1, 3, 5, 5, 4, 3)), // 3프렛 시작 Am 폼

        // === D 계열 ===
        "D" to ChordVoicing("D", 1, listOf(-1, -1, 0, 2, 3, 2)),
        "Dm" to ChordVoicing("Dm", 1, listOf(-1, -1, 0, 2, 3, 1)),
        "D7" to ChordVoicing("D7", 1, listOf(-1, -1, 0, 2, 1, 2)),

        // === E 계열 ===
        "E" to ChordVoicing("E", 1, listOf(0, 2, 2, 1, 0, 0)),
        "Em" to ChordVoicing("Em", 1, listOf(0, 2, 2, 0, 0, 0)),
        "E7" to ChordVoicing("E7", 1, listOf(0, 2, 0, 1, 0, 0)),

        // === F 계열 (바레 코드의 정석) ===
        // 1프렛 시작, E폼 바레
        "F" to ChordVoicing("F", 1, listOf(1, 3, 3, 2, 1, 1)),
        "Fm" to ChordVoicing("Fm", 1, listOf(1, 3, 3, 1, 1, 1)),
        "F#m" to ChordVoicing("F#m", 2, listOf(1, 3, 3, 1, 1, 1)), // 2프렛 시작 Fm 모양

        // === G 계열 ===
        "G" to ChordVoicing("G", 1, listOf(3, 2, 0, 0, 0, 3)),
        "G7" to ChordVoicing("G7", 1, listOf(3, 2, 0, 0, 0, 1)),
        // G 하이코드 (3프렛 시작, E폼)
        "Gm" to ChordVoicing("Gm", 3, listOf(1, 3, 3, 1, 1, 1)),

        // === A 계열 ===
        "A" to ChordVoicing("A", 1, listOf(-1, 0, 2, 2, 2, 0)),
        "Am" to ChordVoicing("Am", 1, listOf(-1, 0, 2, 2, 1, 0)),
        "A7" to ChordVoicing("A7", 1, listOf(-1, 0, 2, 0, 2, 0)),

        // === B 계열 (2프렛 시작, A폼 바레) ===
        "B" to ChordVoicing("B", 2, listOf(-1, 1, 3, 3, 3, 1)),
        "Bm" to ChordVoicing("Bm", 2, listOf(-1, 1, 3, 3, 2, 1))
    )

    // 코드 찾기 함수
    fun findVoicing(chordName: String): ChordVoicing {
        // 입력된 이름 정리 (공백 제거)
        val key = chordName.trim()

        // 정확한 매칭 시도
        // (대소문자 구분이 필요할 수 있으니 원본과 Capitalize된 버전 등을 체크하거나, 데이터 맵을 정교하게 관리)
        return standardChords[key]
            ?: standardChords[key.replaceFirstChar { it.uppercase() }] // 첫 글자 대문자로 변환해 재시도
            ?: ChordVoicing("", 1, listOf(-1, -1, -1, -1, -1, -1)) // 없으면 빈 코드 반환
    }

    // [확장 포인트] 나중에 사용자가 추가한 코드를 검색하는 함수는 여기에 추가
    // fun findUserChord(name: String, userChords: List<ChordVoicing>): ChordVoicing? { ... }
    // TODO: 나중에 Room DB에서 UserChord 테이블을 만들고 데이터를 가져오면, ChordLibrary의 Map과 합쳐서 검색하도록 로직만 바꾸면 됩니다.
}