package com.teuskim.takefive.common

import java.util.HashMap

class PatternFounded(private val difficulty: Int) {

    private val patternCntMap: MutableMap<Pattern, Int>
    private var turnCnt: Int = 0

    private val comPointDifficulty0: Int
        get() = (getCnt(Pattern.COM_UCCCCN) * 7 + getCnt(Pattern.COM_NCCCCU) * 7
                + getCnt(Pattern.COM_NNCCCNN) * 6
                + getCnt(Pattern.COM_UNCCCNN) * 5 + getCnt(Pattern.COM_NNCCCNU) * 5
                + getCnt(Pattern.COM_UCCCNN) * 4 + getCnt(Pattern.COM_NNCCCU) * 4
                + getCnt(Pattern.COM_NNCCNN) * 3
                + getCnt(Pattern.COM_UNCCNN) * 3 + getCnt(Pattern.COM_NNCCNU) * 3
                + getCnt(Pattern.COM_UCCNNN) * 3 + getCnt(Pattern.COM_NNNCCU) * 3)

    private val userPointDifficulty0: Int
        get() = if (turnCnt < 5) {
            getCnt(Pattern.USER_NUUUUN) * 7
        } else {
            getCnt(Pattern.USER_NUUUUN) * 3
        }

    private val comPointDifficulty1: Int
        get() = (getCnt(Pattern.COM_UCCCCN) * 8 + getCnt(Pattern.COM_NCCCCU) * 8
                + getCnt(Pattern.COM_NNCCCNN) * 7
                + getCnt(Pattern.COM_UNCCCNN) * 6 + getCnt(Pattern.COM_NNCCCNU) * 6
                + getCnt(Pattern.COM_UCCCNN) * 5 + getCnt(Pattern.COM_NNCCCU) * 5
                + getCnt(Pattern.COM_NNCCNN) * 4
                + getCnt(Pattern.COM_UNCCNN) * 4 + getCnt(Pattern.COM_NNCCNU) * 4
                + getCnt(Pattern.COM_UCCNNN) * 3 + getCnt(Pattern.COM_NNNCCU) * 3)

    private val userPointDifficulty1: Int
        get() = if (turnCnt < 8) {
            getCnt(Pattern.USER_NUUUUN) * 15
        } else {
            getCnt(Pattern.USER_NUUUUN) * 7
        }

    private val comPointDifficulty2: Int
        get() = (getCnt(Pattern.COM_UCCCCN) * 14 + getCnt(Pattern.COM_NCCCCU) * 14
                + getCnt(Pattern.COM_CNCCC) * 13 + getCnt(Pattern.COM_CCNCC) * 13 + getCnt(Pattern.COM_CCCNC) * 13
                + getCnt(Pattern.COM_NNCCCNN) * 11
                + getCnt(Pattern.COM_UNCCCNN) * 9 + getCnt(Pattern.COM_NNCCCNU) * 9
                + getCnt(Pattern.COM_NCNCCN) * 9 + getCnt(Pattern.COM_NCCNCN) * 9
                + getCnt(Pattern.COM_UCCCNN) * 5 + getCnt(Pattern.COM_NNCCCU) * 5
                + getCnt(Pattern.COM_UCNCCN) * 5 + getCnt(Pattern.COM_NCNCCU) * 5
                + getCnt(Pattern.COM_UCCNCN) * 5 + getCnt(Pattern.COM_NCCNCU) * 5
                + getCnt(Pattern.COM_NNCCNN) * 5
                + getCnt(Pattern.COM_UNCCNN) * 4 + getCnt(Pattern.COM_NNCCNU) * 4 + getCnt(Pattern.COM_NCNCN) * 4
                + getCnt(Pattern.COM_UCCNNN) * 3 + getCnt(Pattern.COM_NNNCCU) * 3)

    private val userPointDifficulty2: Int
        get() = (getCnt(Pattern.USER_CUUUUN) * 13 + getCnt(Pattern.USER_NUUUUC) * 13
                + getCnt(Pattern.USER_UNUUU) * 12 + getCnt(Pattern.USER_UUNUU) * 12 + getCnt(Pattern.USER_UUUNU) * 12
                + getCnt(Pattern.USER_NNUUUNN) * 9
                + getCnt(Pattern.USER_CNUUUNN) * 4 + getCnt(Pattern.USER_NNUUUNC) * 4
                + getCnt(Pattern.USER_NUNUUN) * 4 + getCnt(Pattern.USER_NUUNUN) * 4
                + getCnt(Pattern.USER_CUUUNN) * 4 + getCnt(Pattern.USER_NNUUUC) * 4
                + getCnt(Pattern.USER_CUNUUN) * 4 + getCnt(Pattern.USER_NUNUUC) * 4
                + getCnt(Pattern.USER_CUUNUN) * 4 + getCnt(Pattern.USER_NUUNUC) * 4
                + getCnt(Pattern.USER_NNUUNN) * 4
                + getCnt(Pattern.USER_CNUUNN) * 3 + getCnt(Pattern.USER_NNUUNC) * 3 + getCnt(Pattern.USER_NUNUN) * 3
                + getCnt(Pattern.USER_CUUNNN) * 2 + getCnt(Pattern.USER_NNNUUC) * 2)

    private val comPointDifficulty3: Int
        get() = (getCnt(Pattern.COM_UCCCCN) * 15 + getCnt(Pattern.COM_NCCCCU) * 15
                + getCnt(Pattern.COM_CNCCC) * 14 + getCnt(Pattern.COM_CCNCC) * 13 + getCnt(Pattern.COM_CCCNC) * 14
                + getCnt(Pattern.COM_NNCCCNN) * 11
                + getCnt(Pattern.COM_UNCCCNN) * 9 + getCnt(Pattern.COM_NNCCCNU) * 9
                + getCnt(Pattern.COM_NCNCCN) * 9 + getCnt(Pattern.COM_NCCNCN) * 9
                + getCnt(Pattern.COM_UCCCNN) * 5 + getCnt(Pattern.COM_NNCCCU) * 5
                + getCnt(Pattern.COM_UCNCCN) * 5 + getCnt(Pattern.COM_NCNCCU) * 5
                + getCnt(Pattern.COM_UCCNCN) * 5 + getCnt(Pattern.COM_NCCNCU) * 5
                + getCnt(Pattern.COM_NNCCNN) * 5
                + getCnt(Pattern.COM_UNCCNN) * 4 + getCnt(Pattern.COM_NNCCNU) * 4 + getCnt(Pattern.COM_NCNCN) * 4
                + getCnt(Pattern.COM_UCCNNN) * 3 + getCnt(Pattern.COM_NNNCCU) * 3)

    private val userPointDifficulty3: Int
        get() = (getCnt(Pattern.USER_CUUUUN) * 14 + getCnt(Pattern.USER_NUUUUC) * 14
                + getCnt(Pattern.USER_UNUUU) * 13 + getCnt(Pattern.USER_UUNUU) * 12 + getCnt(Pattern.USER_UUUNU) * 13
                + getCnt(Pattern.USER_NNUUUNN) * 10
                + getCnt(Pattern.USER_CNUUUNN) * 8 + getCnt(Pattern.USER_NNUUUNC) * 8
                + getCnt(Pattern.USER_NUNUUN) * 8 + getCnt(Pattern.USER_NUUNUN) * 8
                + getCnt(Pattern.USER_CUUUNN) * 4 + getCnt(Pattern.USER_NNUUUC) * 4
                + getCnt(Pattern.USER_CUNUUN) * 4 + getCnt(Pattern.USER_NUNUUC) * 4
                + getCnt(Pattern.USER_CUUNUN) * 4 + getCnt(Pattern.USER_NUUNUC) * 4
                + getCnt(Pattern.USER_NNUUNN) * 4
                + getCnt(Pattern.USER_CNUUNN) * 3 + getCnt(Pattern.USER_NNUUNC) * 3 + getCnt(Pattern.USER_NUNUN) * 3
                + getCnt(Pattern.USER_CUUNNN) * 2 + getCnt(Pattern.USER_NNNUUC) * 2)

    private val comPointDifficultyHint: Int
        get() = (getCnt(Pattern.COM_UCCCCN) * 15 + getCnt(Pattern.COM_NCCCCU) * 15
                + getCnt(Pattern.COM_CNCCC) * 14 + getCnt(Pattern.COM_CCNCC) * 13 + getCnt(Pattern.COM_CCCNC) * 14
                + getCnt(Pattern.COM_NNCCCNN) * 11
                + getCnt(Pattern.COM_UNCCCNN) * 9 + getCnt(Pattern.COM_NNCCCNU) * 9
                + getCnt(Pattern.COM_NCNCCN) * 9 + getCnt(Pattern.COM_NCCNCN) * 9
                + getCnt(Pattern.COM_UCCCNN) * 5 + getCnt(Pattern.COM_NNCCCU) * 5
                + getCnt(Pattern.COM_UCNCCN) * 5 + getCnt(Pattern.COM_NCNCCU) * 5
                + getCnt(Pattern.COM_UCCNCN) * 5 + getCnt(Pattern.COM_NCCNCU) * 5
                + getCnt(Pattern.COM_NNCCNN) * 5
                + getCnt(Pattern.COM_UNCCNN) * 4 + getCnt(Pattern.COM_NNCCNU) * 4 + getCnt(Pattern.COM_NCNCN) * 4
                + getCnt(Pattern.COM_UCCNNN) * 1 + getCnt(Pattern.COM_NNNCCU) * 1)

    private val userPointDifficultyHint: Int
        get() = (getCnt(Pattern.USER_CUUUUN) * 21 + getCnt(Pattern.USER_NUUUUC) * 21
                + getCnt(Pattern.USER_UNUUU) * 20 + getCnt(Pattern.USER_UUNUU) * 19 + getCnt(Pattern.USER_UUUNU) * 20
                + getCnt(Pattern.USER_NNUUUNN) * 18
                + getCnt(Pattern.USER_CNUUUNN) * 10 + getCnt(Pattern.USER_NNUUUNC) * 10
                + getCnt(Pattern.USER_NUNUUN) * 10 + getCnt(Pattern.USER_NUUNUN) * 10
                + getCnt(Pattern.USER_CUUUNN) * 6 + getCnt(Pattern.USER_NNUUUC) * 6
                + getCnt(Pattern.USER_CUNUUN) * 6 + getCnt(Pattern.USER_NUNUUC) * 6
                + getCnt(Pattern.USER_CUUNUN) * 6 + getCnt(Pattern.USER_NUUNUC) * 6
                + getCnt(Pattern.USER_NNUUNN) * 6
                + getCnt(Pattern.USER_CNUUNN) * 5 + getCnt(Pattern.USER_NNUUNC) * 5 + getCnt(Pattern.USER_NUNUN) * 5
                + getCnt(Pattern.USER_CUUNNN) * 4 + getCnt(Pattern.USER_NNNUUC) * 4)

    private// 오목완성
            // 양쪽열린 4개
            // 오목완성
            // 양쪽열린 4개
            // 4 & 4
            // 4 & 3
            // 3 & 3
    val comPointCritical: Int
        get() {
            val point = 0
            if (difficulty == OmokManager.DIFFICULTY_0 || difficulty == OmokManager.DIFFICULTY_1) {
                if (has(Pattern.COM_CCCCC)) {
                    return 990
                } else if (has(Pattern.COM_NCCCCN)) {
                    return 800
                }
            } else {
                if (has(Pattern.COM_CCCCC)) {
                    return 990
                } else if (has(Pattern.COM_NCCCCN)) {
                    return 800
                } else {
                    val cnt4 = (getCnt(Pattern.COM_UCCCCN) + getCnt(Pattern.COM_NCCCCU)
                            + getCnt(Pattern.COM_CNCCC) + getCnt(Pattern.COM_CCNCC) + getCnt(Pattern.COM_CCCNC))
                    val cnt3 = (getCnt(Pattern.COM_NNCCCNN) + getCnt(Pattern.COM_UNCCCNN)
                            + getCnt(Pattern.COM_NNCCCNU) + getCnt(Pattern.COM_NCNCCN) + getCnt(Pattern.COM_NCCNCN))

                    if (cnt4 >= 2) {
                        return 600
                    } else if (cnt4 >= 1 && cnt3 >= 1) {
                        return 400
                    } else if (cnt3 >= 2) {
                        return 200
                    }
                }
            }
            return point
        }

    private// 오목완성
            // 오목완성
            // 양쪽열린 4개
            // 4 & 4
            // 4 & 3
            // 3 & 3
    val userPointCritical: Int
        get() {
            var point = 0
            if (difficulty == OmokManager.DIFFICULTY_0 || difficulty == OmokManager.DIFFICULTY_1) {
                if (has(Pattern.USER_UUUUU)) {
                    point = 900
                }
            } else {
                if (has(Pattern.USER_UUUUU)) {
                    point = 900
                } else if (has(Pattern.USER_NUUUUN)) {
                    point = 700
                } else {
                    val cnt4 = (getCnt(Pattern.USER_CUUUUN) + getCnt(Pattern.USER_NUUUUC)
                            + getCnt(Pattern.USER_UNUUU) + getCnt(Pattern.USER_UUNUU) + getCnt(Pattern.USER_UUUNU))
                    val cnt3 = (getCnt(Pattern.USER_NNUUUNN) + getCnt(Pattern.USER_CNUUUNN)
                            + getCnt(Pattern.USER_NNUUUNC) + getCnt(Pattern.USER_NUNUUN) + getCnt(Pattern.USER_NUUNUN))

                    if (cnt4 >= 2) {
                        point = 500
                    } else if (cnt4 >= 1 && cnt3 >= 1) {
                        point = 300
                    } else if (cnt3 >= 2) {
                        point = 100
                    }
                }
            }
            return point
        }

    private// 오목완성
            // 양쪽열린 4개
            // 4 & 4
            // 4 & 3
            // 3 & 3
    val userPointCriticalForHint: Int
        get() {
            var point = 0
            if (has(Pattern.USER_UUUUU)) {
                point = 999
            } else if (has(Pattern.USER_NUUUUN)) {
                point = 900
            } else {
                val cnt4 = (getCnt(Pattern.USER_CUUUUN) + getCnt(Pattern.USER_NUUUUC)
                        + getCnt(Pattern.USER_UNUUU) + getCnt(Pattern.USER_UUNUU) + getCnt(Pattern.USER_UUUNU))
                val cnt3 = (getCnt(Pattern.USER_NNUUUNN) + getCnt(Pattern.USER_CNUUUNN)
                        + getCnt(Pattern.USER_NNUUUNC) + getCnt(Pattern.USER_NUNUUN) + getCnt(Pattern.USER_NUUNUN))

                if (cnt4 >= 2) {
                    point = 700
                } else if (cnt4 >= 1 && cnt3 >= 1) {
                    point = 500
                } else if (cnt3 >= 2) {
                    point = 300
                }
            }
            return point
        }

    init {
        patternCntMap = HashMap()
    }

    fun getPatternCntMap(): Map<Pattern, Int> {
        return patternCntMap
    }

    fun add(pf: PatternFounded) {
        val augMap = pf.getPatternCntMap()
        for ((key, value) in augMap) {
            patternCntMap[key] = getCnt(key) + value
        }
    }

    fun increase(p: Pattern) {
        val cnt: Int
        if (patternCntMap.containsKey(p)) {
            cnt = patternCntMap[p]!! + 1
        } else {
            cnt = 1
        }
        patternCntMap[p] = cnt
    }

    fun has(p: Pattern): Boolean {
        return getCnt(p) > 0
    }

    fun getCnt(p: Pattern): Int {
        return if (patternCntMap.containsKey(p)) {
            patternCntMap[p]!!
        } else 0
    }

    fun getPoint(isComAttack: Boolean, turnCnt: Int): Int {
        this.turnCnt = turnCnt
        var point = 0
        if (isComAttack) {
            when (difficulty) {
                OmokManager.DIFFICULTY_0 -> point = comPointDifficulty0
                OmokManager.DIFFICULTY_1 -> point = comPointDifficulty1
                OmokManager.DIFFICULTY_2 -> point = comPointDifficulty2
                OmokManager.DIFFICULTY_3 -> point = comPointDifficulty3
                OmokManager.DIFFICULTY_HINT -> point = comPointDifficultyHint
            }

            point += comPointCritical

        } else {
            when (difficulty) {
                OmokManager.DIFFICULTY_0 -> point = userPointDifficulty0
                OmokManager.DIFFICULTY_1 -> point = userPointDifficulty1
                OmokManager.DIFFICULTY_2 -> point = userPointDifficulty2
                OmokManager.DIFFICULTY_3 -> point = userPointDifficulty3
                OmokManager.DIFFICULTY_HINT -> point = userPointDifficultyHint
            }

            if (difficulty == OmokManager.DIFFICULTY_HINT) {
                point += userPointCriticalForHint
            } else {
                point += userPointCritical
            }
        }
        return point
    }

    // for test
    fun logPatternsCntMap() {
        if (ConstantValues.IS_SHOW_TEST_MAP) {
            Log.test("-- logPatternsCntMap! --")
            for ((key, value) in patternCntMap) {
                Log.test("Pattern:$key, Count:$value")
            }
        }
    }

    companion object {

        var comPatternMap: MutableMap<Pattern, Array<CellState>>? = null

        init {
            comPatternMap = HashMap()
            comPatternMap!![Pattern.COM_CCCCC] = arrayOf(CellState.COM, CellState.COM, CellState.COM, CellState.COM, CellState.COM)

            comPatternMap!![Pattern.COM_NCCCCN] = arrayOf(CellState.NONE, CellState.COM, CellState.COM, CellState.COM, CellState.COM, CellState.NONE)
            comPatternMap!![Pattern.COM_UCCCCN] = arrayOf(CellState.USER, CellState.COM, CellState.COM, CellState.COM, CellState.COM, CellState.NONE)
            comPatternMap!![Pattern.COM_NCCCCU] = arrayOf(CellState.NONE, CellState.COM, CellState.COM, CellState.COM, CellState.COM, CellState.USER)

            comPatternMap!![Pattern.COM_CNCCC] = arrayOf(CellState.COM, CellState.NONE, CellState.COM, CellState.COM, CellState.COM)
            comPatternMap!![Pattern.COM_CCNCC] = arrayOf(CellState.COM, CellState.COM, CellState.NONE, CellState.COM, CellState.COM)
            comPatternMap!![Pattern.COM_CCCNC] = arrayOf(CellState.COM, CellState.COM, CellState.COM, CellState.NONE, CellState.COM)

            comPatternMap!![Pattern.COM_NNCCCNN] = arrayOf(CellState.NONE, CellState.NONE, CellState.COM, CellState.COM, CellState.COM, CellState.NONE, CellState.NONE)
            comPatternMap!![Pattern.COM_UNCCCNN] = arrayOf(CellState.USER, CellState.NONE, CellState.COM, CellState.COM, CellState.COM, CellState.NONE, CellState.NONE)
            comPatternMap!![Pattern.COM_NNCCCNU] = arrayOf(CellState.NONE, CellState.NONE, CellState.COM, CellState.COM, CellState.COM, CellState.NONE, CellState.USER)
            comPatternMap!![Pattern.COM_NCNCCN] = arrayOf(CellState.NONE, CellState.COM, CellState.NONE, CellState.COM, CellState.COM, CellState.NONE)
            comPatternMap!![Pattern.COM_NCCNCN] = arrayOf(CellState.NONE, CellState.COM, CellState.COM, CellState.NONE, CellState.COM, CellState.NONE)

            comPatternMap!![Pattern.COM_UCCCNN] = arrayOf(CellState.USER, CellState.COM, CellState.COM, CellState.COM, CellState.NONE, CellState.NONE)
            comPatternMap!![Pattern.COM_NNCCCU] = arrayOf(CellState.NONE, CellState.NONE, CellState.COM, CellState.COM, CellState.COM, CellState.USER)
            comPatternMap!![Pattern.COM_UCNCCN] = arrayOf(CellState.USER, CellState.COM, CellState.NONE, CellState.COM, CellState.COM, CellState.NONE)
            comPatternMap!![Pattern.COM_NCNCCU] = arrayOf(CellState.NONE, CellState.COM, CellState.NONE, CellState.COM, CellState.COM, CellState.USER)
            comPatternMap!![Pattern.COM_UCCNCN] = arrayOf(CellState.USER, CellState.COM, CellState.COM, CellState.NONE, CellState.COM, CellState.NONE)
            comPatternMap!![Pattern.COM_NCCNCU] = arrayOf(CellState.NONE, CellState.COM, CellState.COM, CellState.NONE, CellState.COM, CellState.USER)

            comPatternMap!![Pattern.COM_NNCCNN] = arrayOf(CellState.NONE, CellState.NONE, CellState.COM, CellState.COM, CellState.NONE, CellState.NONE)
            comPatternMap!![Pattern.COM_NCNCN] = arrayOf(CellState.NONE, CellState.COM, CellState.NONE, CellState.COM, CellState.NONE)
            comPatternMap!![Pattern.COM_UNCCNN] = arrayOf(CellState.USER, CellState.NONE, CellState.COM, CellState.COM, CellState.NONE, CellState.NONE)
            comPatternMap!![Pattern.COM_NNCCNU] = arrayOf(CellState.NONE, CellState.NONE, CellState.COM, CellState.COM, CellState.NONE, CellState.USER)

            comPatternMap!![Pattern.COM_UCCNNN] = arrayOf(CellState.USER, CellState.COM, CellState.COM, CellState.NONE, CellState.NONE, CellState.NONE)
            comPatternMap!![Pattern.COM_NNNCCU] = arrayOf(CellState.NONE, CellState.NONE, CellState.NONE, CellState.COM, CellState.COM, CellState.USER)
        }

        var userPatternMap: MutableMap<Pattern, Array<CellState>>? = null

        init {
            userPatternMap = HashMap()
            userPatternMap!![Pattern.USER_UUUUU] = arrayOf(CellState.USER, CellState.USER, CellState.USER, CellState.USER, CellState.USER)

            userPatternMap!![Pattern.USER_NUUUUN] = arrayOf(CellState.NONE, CellState.USER, CellState.USER, CellState.USER, CellState.USER, CellState.NONE)
            userPatternMap!![Pattern.USER_CUUUUN] = arrayOf(CellState.COM, CellState.USER, CellState.USER, CellState.USER, CellState.USER, CellState.NONE)
            userPatternMap!![Pattern.USER_NUUUUC] = arrayOf(CellState.NONE, CellState.USER, CellState.USER, CellState.USER, CellState.USER, CellState.COM)

            userPatternMap!![Pattern.USER_UNUUU] = arrayOf(CellState.USER, CellState.NONE, CellState.USER, CellState.USER, CellState.USER)
            userPatternMap!![Pattern.USER_UUNUU] = arrayOf(CellState.USER, CellState.USER, CellState.NONE, CellState.USER, CellState.USER)
            userPatternMap!![Pattern.USER_UUUNU] = arrayOf(CellState.USER, CellState.USER, CellState.USER, CellState.NONE, CellState.USER)

            userPatternMap!![Pattern.USER_NNUUUNN] = arrayOf(CellState.NONE, CellState.NONE, CellState.USER, CellState.USER, CellState.USER, CellState.NONE, CellState.NONE)
            userPatternMap!![Pattern.USER_CNUUUNN] = arrayOf(CellState.COM, CellState.NONE, CellState.USER, CellState.USER, CellState.USER, CellState.NONE, CellState.NONE)
            userPatternMap!![Pattern.USER_NNUUUNC] = arrayOf(CellState.NONE, CellState.NONE, CellState.USER, CellState.USER, CellState.USER, CellState.NONE, CellState.COM)
            userPatternMap!![Pattern.USER_NUNUUN] = arrayOf(CellState.NONE, CellState.USER, CellState.NONE, CellState.USER, CellState.USER, CellState.NONE)
            userPatternMap!![Pattern.USER_NUUNUN] = arrayOf(CellState.NONE, CellState.USER, CellState.USER, CellState.NONE, CellState.USER, CellState.NONE)

            userPatternMap!![Pattern.USER_CUUUNN] = arrayOf(CellState.COM, CellState.USER, CellState.USER, CellState.USER, CellState.NONE, CellState.NONE)
            userPatternMap!![Pattern.USER_NNUUUC] = arrayOf(CellState.NONE, CellState.NONE, CellState.USER, CellState.USER, CellState.USER, CellState.COM)
            userPatternMap!![Pattern.USER_CUNUUN] = arrayOf(CellState.COM, CellState.USER, CellState.NONE, CellState.USER, CellState.USER, CellState.NONE)
            userPatternMap!![Pattern.USER_NUNUUC] = arrayOf(CellState.NONE, CellState.USER, CellState.NONE, CellState.USER, CellState.USER, CellState.COM)
            userPatternMap!![Pattern.USER_CUUNUN] = arrayOf(CellState.COM, CellState.USER, CellState.USER, CellState.NONE, CellState.USER, CellState.NONE)
            userPatternMap!![Pattern.USER_NUUNUC] = arrayOf(CellState.NONE, CellState.USER, CellState.USER, CellState.NONE, CellState.USER, CellState.COM)

            userPatternMap!![Pattern.USER_NNUUNN] = arrayOf(CellState.NONE, CellState.NONE, CellState.USER, CellState.USER, CellState.NONE, CellState.NONE)
            userPatternMap!![Pattern.USER_NUNUN] = arrayOf(CellState.NONE, CellState.USER, CellState.NONE, CellState.USER, CellState.NONE)
            userPatternMap!![Pattern.USER_CNUUNN] = arrayOf(CellState.COM, CellState.NONE, CellState.USER, CellState.USER, CellState.NONE, CellState.NONE)
            userPatternMap!![Pattern.USER_NNUUNC] = arrayOf(CellState.NONE, CellState.NONE, CellState.USER, CellState.USER, CellState.NONE, CellState.COM)

            userPatternMap!![Pattern.USER_CUUNNN] = arrayOf(CellState.COM, CellState.USER, CellState.USER, CellState.NONE, CellState.NONE, CellState.NONE)
            userPatternMap!![Pattern.USER_NNNUUC] = arrayOf(CellState.NONE, CellState.NONE, CellState.NONE, CellState.USER, CellState.USER, CellState.COM)
        }
    }
}
