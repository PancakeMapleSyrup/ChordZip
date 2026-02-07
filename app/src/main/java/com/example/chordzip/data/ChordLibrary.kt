package com.example.chordzip.data

object ChordLibrary {
    // key: 코드 이름 (대문자로 저장), value: 운지 정보
    private val standardChords = mapOf(
        // === C 계열 ===
        "C" to ChordVoicing("C", 1, listOf(-1, 3, 2, 0, 1, 0)),
        "Cm" to ChordVoicing("Cm", 1, listOf(-1, 3, 5, 5, 4, 3)),
        "C7" to ChordVoicing("C7", 1, listOf(-1, 3, 2, 3, 1, 0)),
        "CM7" to ChordVoicing("CM7", 1, listOf(-1, 3, 2, 0, 0, 0)),
        "Cm7" to ChordVoicing("Cm7", 1, listOf(-1, 3, 1, 3, 1, -1)),
        "Csus4" to ChordVoicing("Csus4", 1, listOf(-1, 3, 3, 0, 1, 1)),
        "C#m7" to ChordVoicing("C#m7", 4, listOf(-1, 1, 3, 3, 0, 0)),

        // === D 계열 ===
        "D" to ChordVoicing("D", 1, listOf(-1, -1, 0, 2, 3, 2)),
        "Dm" to ChordVoicing("Dm", 1, listOf(-1, -1, 0, 2, 3, 1)),
        "D7" to ChordVoicing("D7", 1, listOf(-1, -1, 0, 2, 1, 2)),
        "Dm7" to ChordVoicing("Dm7", 1, listOf(-1, -1, 0, 2, 1, 1)),
        "DM7" to ChordVoicing("DM7", 1, listOf(-1, -1, 0, 2, 2, 2)),

        // === E 계열 ===
        "E" to ChordVoicing("E", 1, listOf(0, 2, 2, 1, 0, 0)),
        "Em" to ChordVoicing("Em", 1, listOf(0, 2, 2, 0, 0, 0)),
        "E7" to ChordVoicing("E7", 1, listOf(0, 2, 0, 1, 0, 0)),
        "Em7" to ChordVoicing("Em7", 1, listOf(0, 2, 2, 0, 3, 0)),
        "EM7" to ChordVoicing("EM7", 1, listOf(0, 2, 1, 1, 0, 0)),

        // === F 계열 ===
        "F" to ChordVoicing("F", 1, listOf(1, 3, 3, 2, 1, 1)),
        "Fm" to ChordVoicing("Fm", 1, listOf(1, 3, 3, 1, 1, 1)),
        "F7" to ChordVoicing("F7", 1, listOf(1, 3, 1, 2, 1, 1)),
        "Fm7" to ChordVoicing("Fm7", 1, listOf(1, 3, 1, 1, 1, 1)),
        "FM7" to ChordVoicing("FM7", 1, listOf(1, -1, 2, 2, 1, -1)),
        "F#m" to ChordVoicing("F#m", 2, listOf(1, 3, 3, 1, 1, 1)),

        // === G 계열 ===
        "G" to ChordVoicing("G", 1, listOf(3, 2, 0, 0, 0, 3)),
        "G7" to ChordVoicing("G7", 1, listOf(3, 2, 0, 0, 0, 1)),
        "GM7" to ChordVoicing("GM7", 1, listOf(3, 2, 0, 0, 0, 2)),
        "Gm" to ChordVoicing("Gm", 3, listOf(1, 3, 3, 1, 1, 1)),
        "G#m7" to ChordVoicing("G#m7", 1, listOf(4,-1,4,4,0,0)),

        // === A 계열 ===
        "A" to ChordVoicing("A", 1, listOf(-1, 0, 2, 2, 2, 0)),
        "Am" to ChordVoicing("Am", 1, listOf(-1, 0, 2, 2, 1, 0)),
        "A7" to ChordVoicing("A7", 1, listOf(-1, 0, 2, 0, 2, 0)),
        "Am7" to ChordVoicing("Am7", 1, listOf(-1, 0, 2, 0, 1, 0)),
        "AM7" to ChordVoicing("AM7", 1, listOf(-1, 0, 2, 1, 2, 0)),

        // === B 계열  ===
        "B" to ChordVoicing("B", 1, listOf(-1, 2, 4, 4, 4, 2)),
        "Bm" to ChordVoicing("Bm", 1, listOf(-1, 2, 4, 4, 3, 2)),
        "B7" to ChordVoicing("B7", 1, listOf(-1, 2, 1, 2, 0, 2)),
        "Bm7" to ChordVoicing("Bm7", 1, listOf(-1, 2, 4, 2, 3, 2)),
        "BM7" to ChordVoicing("BM7", 1, listOf(-1, 2, 4, 3, 4, 2)),
        "Bm11" to ChordVoicing("Bm11", 1, listOf(-1, 2, 0, 2, 3, 0)),
    )

    // 사용자 지정 코드 저장소 (메모리 저장용)
    // 앱이 실행되는 동안 사용자가 만든 코드를 여기에 저장합니다.
    private val userChords = mutableMapOf<String, ChordVoicing>()

    // 코드 정렬 우선순위
    // 숫자가 낮을수록 리스트 상단에 표시됩니다.
    private val suffixPriority = mapOf(
        "" to 0,        // Major Triad (예: C) - 가장 기본
        "m" to 1,       // Minor Triad (예: Cm)
        "min" to 1,
        "7" to 2,       // Dominant 7th (예: C7) - 가장 많이 쓰이는 7화음
        "m7" to 3,      // Minor 7th (예: Cm7)
        "min7" to 3,
        "M7" to 4,      // Major 7th (예: CM7)
        "maj7" to 4,
        "sus4" to 5,    // Suspended (예: Csus4)
        "sus2" to 5,
        "dim" to 6,     // Diminished
        "dim7" to 6,
        "aug" to 7,     // Augmented
        "+" to 7,
        "6" to 8,       // 6th
        "m6" to 8
        // 그 외(9, 11, 13 등 텐션)는 후순위로 자동 배치
    )

    // 코드 찾기 함수 (표준 + 사용자 정의 통합 검색)
    fun findVoicing(chordName: String): ChordVoicing {
        val key = chordName.trim()

        // 1순위: 사용자 정의 코드에서 찾기 (사용자가 덮어썼을 수도 있으므로)
        userChords[key]?.let { return it }

        // 2순위: 표준 코드에서 찾기
        return standardChords[key]
            ?: standardChords[key.replaceFirstChar { it.uppercase() }]
            ?: ChordVoicing("", 1, listOf(-1, -1, -1, -1, -1, -1))
    }

    // 사용자 코드 추가 기능
    fun addCustomChord(name: String, startFret: Int, positions: List<Int>) {
        val newChord = ChordVoicing(name, startFret, positions)
        userChords[name] = newChord
    }

    fun getChordsByRoot(root: String): List<ChordVoicing> {
        // 표준 코드와 사용자 코드를 합침
        val allChords = standardChords.values + userChords.values

        return allChords
            .filter { chord ->
                // 정확히 해당 근음으로 시작하는지 확인 (C 검색 시 C# 제외)
                // 예: 근음이 "C"일 때 -> "C", "Cm" (O) / "C#", "C#m" (X)
                if (root.length == 1) {
                    // C, D, E 등 한 글자 근음인 경우: 두 번째 글자가 #이나 b이면 제외
                    chord.name.startsWith(root) &&
                            (chord.name.length == 1 || (chord.name[1] != '#' && chord.name[1] != 'b'))
                } else {
                    // C#, Bb 등 두 글자 근음인 경우: 그냥 startsWith 체크
                    chord.name.startsWith(root)
                }
            }
            .sortedWith(Comparator { o1, o2 ->
                // 정렬 로직 실행
                compareChordNames(o1.name, o2.name, root)
            })
            .distinctBy { it.name } // 중복 이름 제거 (사용자 코드가 우선순위를 가짐)
    }

    // 코드 이름 비교 함수
    private fun compareChordNames(name1: String, name2: String, root: String): Int {
        // 1. 접미사(Suffix) 추출 (예: "Cm7" -> "m7")
        val suffix1 = name1.substringAfter(root)
        val suffix2 = name2.substringAfter(root)

        // 2. 우선순위 점수 가져오기 (없으면 99점)
        val p1 = suffixPriority[suffix1] ?: 99
        val p2 = suffixPriority[suffix2] ?: 99

        // 3. 우선순위 비교
        if (p1 != p2) {
            return p1 - p2
        }

        // 4. 우선순위가 같다면 (둘 다 99점 등) 알파벳 순서로 정렬
        return suffix1.compareTo(suffix2)
    }
}