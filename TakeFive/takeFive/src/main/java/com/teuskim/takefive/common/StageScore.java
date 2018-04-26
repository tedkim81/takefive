package com.teuskim.takefive.common;

public class StageScore {

	private int baseScore;
	private boolean hasCostMission;
	private int remainCosts;
	private boolean hasMaterialMission;
	private int remainMaterials;
	private boolean hasTimeMission;
	private int remainTime;
	private int multiplier;
	private boolean isDouble;
	
	public StageScore(int baseScore, boolean hasCostMission, int remainCosts, boolean hasMaterialMission, int remainMaterials
			, boolean hasTimeMission, int remainTime, int multiplier, boolean isDouble) {
		this.baseScore = baseScore;
		this.hasCostMission = hasCostMission;
		this.remainCosts = remainCosts;
		this.hasMaterialMission = hasMaterialMission;
		this.remainMaterials = remainMaterials;
		this.hasTimeMission = hasTimeMission;
		this.remainTime = remainTime;
		this.multiplier = multiplier;
		this.isDouble = isDouble;
	}

	public int getBaseScore() {
		return baseScore;
	}

	public boolean hasCostMission() {
		return hasCostMission;
	}

	public int getRemainCosts() {
		return remainCosts;
	}

	public boolean hasMaterialMission() {
		return hasMaterialMission;
	}

	public int getRemainMaterials() {
		return remainMaterials;
	}

	public boolean hasTimeMission() {
		return hasTimeMission;
	}

	public int getRemainTime() {
		return remainTime;
	}

	public int getMultiplier() {
		return multiplier;
	}

	public boolean isDouble() {
		return isDouble;
	}
	
	public int getScore() {
		int score = baseScore;
		if (hasCostMission) {
			score += (remainCosts * multiplier);
		}
		if (hasMaterialMission) {
			score += (remainMaterials * multiplier);
		}
		if (hasTimeMission) {
			score += (remainTime * multiplier);
		}
		if (isDouble) {
			score *= 2;
		}
		return score;
	}
	
}
