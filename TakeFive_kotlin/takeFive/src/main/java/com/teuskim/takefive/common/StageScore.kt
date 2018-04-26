package com.teuskim.takefive.common

class StageScore(val baseScore: Int, private val hasCostMission: Boolean, val remainCosts: Int, private val hasMaterialMission: Boolean, val remainMaterials: Int, private val hasTimeMission: Boolean, val remainTime: Int, val multiplier: Int, val isDouble: Boolean) {

    val score: Int
        get() {
            var score = baseScore
            if (hasCostMission) {
                score += remainCosts * multiplier
            }
            if (hasMaterialMission) {
                score += remainMaterials * multiplier
            }
            if (hasTimeMission) {
                score += remainTime * multiplier
            }
            if (isDouble) {
                score *= 2
            }
            return score
        }

    fun hasCostMission(): Boolean {
        return hasCostMission
    }

    fun hasMaterialMission(): Boolean {
        return hasMaterialMission
    }

    fun hasTimeMission(): Boolean {
        return hasTimeMission
    }

}
