package com.teuskim.takefive.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PatternFounded {
	
	private static Map<Pattern, CellState[]> comPatternMap;
	static {
		comPatternMap = new HashMap<Pattern, CellState[]>();
		comPatternMap.put(Pattern.COM_CCCCC, new CellState[]{CellState.COM,CellState.COM,CellState.COM,CellState.COM,CellState.COM});
		
		comPatternMap.put(Pattern.COM_NCCCCN, new CellState[]{CellState.NONE,CellState.COM,CellState.COM,CellState.COM,CellState.COM,CellState.NONE});
		comPatternMap.put(Pattern.COM_UCCCCN, new CellState[]{CellState.USER,CellState.COM,CellState.COM,CellState.COM,CellState.COM,CellState.NONE});
		comPatternMap.put(Pattern.COM_NCCCCU, new CellState[]{CellState.NONE,CellState.COM,CellState.COM,CellState.COM,CellState.COM,CellState.USER});
		
		comPatternMap.put(Pattern.COM_CNCCC, new CellState[]{CellState.COM,CellState.NONE,CellState.COM,CellState.COM,CellState.COM});
		comPatternMap.put(Pattern.COM_CCNCC, new CellState[]{CellState.COM,CellState.COM,CellState.NONE,CellState.COM,CellState.COM});
		comPatternMap.put(Pattern.COM_CCCNC, new CellState[]{CellState.COM,CellState.COM,CellState.COM,CellState.NONE,CellState.COM});
		
		comPatternMap.put(Pattern.COM_NNCCCNN, new CellState[]{CellState.NONE,CellState.NONE,CellState.COM,CellState.COM,CellState.COM,CellState.NONE,CellState.NONE});
		comPatternMap.put(Pattern.COM_UNCCCNN, new CellState[]{CellState.USER,CellState.NONE,CellState.COM,CellState.COM,CellState.COM,CellState.NONE,CellState.NONE});
		comPatternMap.put(Pattern.COM_NNCCCNU, new CellState[]{CellState.NONE,CellState.NONE,CellState.COM,CellState.COM,CellState.COM,CellState.NONE,CellState.USER});
		comPatternMap.put(Pattern.COM_NCNCCN, new CellState[]{CellState.NONE,CellState.COM,CellState.NONE,CellState.COM,CellState.COM,CellState.NONE});
		comPatternMap.put(Pattern.COM_NCCNCN, new CellState[]{CellState.NONE,CellState.COM,CellState.COM,CellState.NONE,CellState.COM,CellState.NONE});
		
		comPatternMap.put(Pattern.COM_UCCCNN, new CellState[]{CellState.USER,CellState.COM,CellState.COM,CellState.COM,CellState.NONE,CellState.NONE});
		comPatternMap.put(Pattern.COM_NNCCCU, new CellState[]{CellState.NONE,CellState.NONE,CellState.COM,CellState.COM,CellState.COM,CellState.USER});
		comPatternMap.put(Pattern.COM_UCNCCN, new CellState[]{CellState.USER,CellState.COM,CellState.NONE,CellState.COM,CellState.COM,CellState.NONE});
		comPatternMap.put(Pattern.COM_NCNCCU, new CellState[]{CellState.NONE,CellState.COM,CellState.NONE,CellState.COM,CellState.COM,CellState.USER});
		comPatternMap.put(Pattern.COM_UCCNCN, new CellState[]{CellState.USER,CellState.COM,CellState.COM,CellState.NONE,CellState.COM,CellState.NONE});
		comPatternMap.put(Pattern.COM_NCCNCU, new CellState[]{CellState.NONE,CellState.COM,CellState.COM,CellState.NONE,CellState.COM,CellState.USER});
		
		comPatternMap.put(Pattern.COM_NNCCNN, new CellState[]{CellState.NONE,CellState.NONE,CellState.COM,CellState.COM,CellState.NONE,CellState.NONE});
		comPatternMap.put(Pattern.COM_NCNCN, new CellState[]{CellState.NONE,CellState.COM,CellState.NONE,CellState.COM,CellState.NONE});
		comPatternMap.put(Pattern.COM_UNCCNN, new CellState[]{CellState.USER,CellState.NONE,CellState.COM,CellState.COM,CellState.NONE,CellState.NONE});
		comPatternMap.put(Pattern.COM_NNCCNU, new CellState[]{CellState.NONE,CellState.NONE,CellState.COM,CellState.COM,CellState.NONE,CellState.USER});
		
		comPatternMap.put(Pattern.COM_UCCNNN, new CellState[]{CellState.USER,CellState.COM,CellState.COM,CellState.NONE,CellState.NONE,CellState.NONE});
		comPatternMap.put(Pattern.COM_NNNCCU, new CellState[]{CellState.NONE,CellState.NONE,CellState.NONE,CellState.COM,CellState.COM,CellState.USER});
	}
	
	private static Map<Pattern, CellState[]> userPatternMap;
	static {
		userPatternMap = new HashMap<Pattern, CellState[]>();
		userPatternMap.put(Pattern.USER_UUUUU, new CellState[]{CellState.USER,CellState.USER,CellState.USER,CellState.USER,CellState.USER});
		
		userPatternMap.put(Pattern.USER_NUUUUN, new CellState[]{CellState.NONE,CellState.USER,CellState.USER,CellState.USER,CellState.USER,CellState.NONE});
		userPatternMap.put(Pattern.USER_CUUUUN, new CellState[]{CellState.COM,CellState.USER,CellState.USER,CellState.USER,CellState.USER,CellState.NONE});
		userPatternMap.put(Pattern.USER_NUUUUC, new CellState[]{CellState.NONE,CellState.USER,CellState.USER,CellState.USER,CellState.USER,CellState.COM});
		
		userPatternMap.put(Pattern.USER_UNUUU, new CellState[]{CellState.USER,CellState.NONE,CellState.USER,CellState.USER,CellState.USER});
		userPatternMap.put(Pattern.USER_UUNUU, new CellState[]{CellState.USER,CellState.USER,CellState.NONE,CellState.USER,CellState.USER});
		userPatternMap.put(Pattern.USER_UUUNU, new CellState[]{CellState.USER,CellState.USER,CellState.USER,CellState.NONE,CellState.USER});
		
		userPatternMap.put(Pattern.USER_NNUUUNN, new CellState[]{CellState.NONE,CellState.NONE,CellState.USER,CellState.USER,CellState.USER,CellState.NONE,CellState.NONE});
		userPatternMap.put(Pattern.USER_CNUUUNN, new CellState[]{CellState.COM,CellState.NONE,CellState.USER,CellState.USER,CellState.USER,CellState.NONE,CellState.NONE});
		userPatternMap.put(Pattern.USER_NNUUUNC, new CellState[]{CellState.NONE,CellState.NONE,CellState.USER,CellState.USER,CellState.USER,CellState.NONE,CellState.COM});
		userPatternMap.put(Pattern.USER_NUNUUN, new CellState[]{CellState.NONE,CellState.USER,CellState.NONE,CellState.USER,CellState.USER,CellState.NONE});
		userPatternMap.put(Pattern.USER_NUUNUN, new CellState[]{CellState.NONE,CellState.USER,CellState.USER,CellState.NONE,CellState.USER,CellState.NONE});
		
		userPatternMap.put(Pattern.USER_CUUUNN, new CellState[]{CellState.COM,CellState.USER,CellState.USER,CellState.USER,CellState.NONE,CellState.NONE});
		userPatternMap.put(Pattern.USER_NNUUUC, new CellState[]{CellState.NONE,CellState.NONE,CellState.USER,CellState.USER,CellState.USER,CellState.COM});
		userPatternMap.put(Pattern.USER_CUNUUN, new CellState[]{CellState.COM,CellState.USER,CellState.NONE,CellState.USER,CellState.USER,CellState.NONE});
		userPatternMap.put(Pattern.USER_NUNUUC, new CellState[]{CellState.NONE,CellState.USER,CellState.NONE,CellState.USER,CellState.USER,CellState.COM});
		userPatternMap.put(Pattern.USER_CUUNUN, new CellState[]{CellState.COM,CellState.USER,CellState.USER,CellState.NONE,CellState.USER,CellState.NONE});
		userPatternMap.put(Pattern.USER_NUUNUC, new CellState[]{CellState.NONE,CellState.USER,CellState.USER,CellState.NONE,CellState.USER,CellState.COM});
		
		userPatternMap.put(Pattern.USER_NNUUNN, new CellState[]{CellState.NONE,CellState.NONE,CellState.USER,CellState.USER,CellState.NONE,CellState.NONE});
		userPatternMap.put(Pattern.USER_NUNUN, new CellState[]{CellState.NONE,CellState.USER,CellState.NONE,CellState.USER,CellState.NONE});
		userPatternMap.put(Pattern.USER_CNUUNN, new CellState[]{CellState.COM,CellState.NONE,CellState.USER,CellState.USER,CellState.NONE,CellState.NONE});
		userPatternMap.put(Pattern.USER_NNUUNC, new CellState[]{CellState.NONE,CellState.NONE,CellState.USER,CellState.USER,CellState.NONE,CellState.COM});
		
		userPatternMap.put(Pattern.USER_CUUNNN, new CellState[]{CellState.COM,CellState.USER,CellState.USER,CellState.NONE,CellState.NONE,CellState.NONE});
		userPatternMap.put(Pattern.USER_NNNUUC, new CellState[]{CellState.NONE,CellState.NONE,CellState.NONE,CellState.USER,CellState.USER,CellState.COM});
	}
	
	private Map<Pattern, Integer> patternCntMap;
	
	private int difficulty;
	private int turnCnt;
	
	public PatternFounded(int difficulty) {
		this.difficulty = difficulty;
		patternCntMap = new HashMap<Pattern, Integer>();
	}
	
	public Map<Pattern, Integer> getPatternCntMap() {
		return patternCntMap;
	}
	
	public static Map<Pattern, CellState[]> getComPatternMap() {
		return comPatternMap;
	}
	
	public static Map<Pattern, CellState[]> getUserPatternMap() {
		return userPatternMap;
	}

	public void add(PatternFounded pf) {
		Map<Pattern, Integer> augMap = pf.getPatternCntMap();
		for (Entry<Pattern, Integer> entry : augMap.entrySet()) {
			patternCntMap.put(entry.getKey(), (getCnt(entry.getKey()) + entry.getValue()));
		}
	}
	
	public void increase(Pattern p) {
		int cnt;
		if (patternCntMap.containsKey(p)) {
			cnt = patternCntMap.get(p) + 1;
		} else {
			cnt = 1;
		}
		patternCntMap.put(p, cnt);
	}
	
	public boolean has(Pattern p) {
		return (getCnt(p) > 0);
	}
	
	public int getCnt(Pattern p) {
		if (patternCntMap.containsKey(p)) {
			return patternCntMap.get(p);
		}
		return 0;
	}

	public int getPoint(boolean isComAttack, int turnCnt) {
		this.turnCnt = turnCnt;
		int point = 0;
		if (isComAttack) {
			switch (difficulty) {
			case OmokManager.DIFFICULTY_0:		point = getComPointDifficulty0();		break;
			case OmokManager.DIFFICULTY_1:		point = getComPointDifficulty1();		break;
			case OmokManager.DIFFICULTY_2:		point = getComPointDifficulty2();		break;
			case OmokManager.DIFFICULTY_3:		point = getComPointDifficulty3();		break;
			case OmokManager.DIFFICULTY_HINT:	point = getComPointDifficultyHint();	break;
			}
			
			point += getComPointCritical();
			
		} else {
			switch (difficulty) {
			case OmokManager.DIFFICULTY_0:		point = getUserPointDifficulty0();		break;
			case OmokManager.DIFFICULTY_1:		point = getUserPointDifficulty1();		break;
			case OmokManager.DIFFICULTY_2:		point = getUserPointDifficulty2();		break;
			case OmokManager.DIFFICULTY_3:		point = getUserPointDifficulty3();		break;
			case OmokManager.DIFFICULTY_HINT:	point = getUserPointDifficultyHint();	break;
			}
			
			if (difficulty == OmokManager.DIFFICULTY_HINT) {
				point += getUserPointCriticalForHint();
			} else {
				point += getUserPointCritical();
			}
		}
		return point;
	}
	
	private int getComPointDifficulty0() {
		return getCnt(Pattern.COM_UCCCCN) * 7		+ getCnt(Pattern.COM_NCCCCU) * 7
				+ getCnt(Pattern.COM_NNCCCNN) * 6
				+ getCnt(Pattern.COM_UNCCCNN) * 5	+ getCnt(Pattern.COM_NNCCCNU) * 5
				+ getCnt(Pattern.COM_UCCCNN) * 4	+ getCnt(Pattern.COM_NNCCCU) * 4
				+ getCnt(Pattern.COM_NNCCNN) * 3
				+ getCnt(Pattern.COM_UNCCNN) * 3	+ getCnt(Pattern.COM_NNCCNU) * 3
				+ getCnt(Pattern.COM_UCCNNN) * 3	+ getCnt(Pattern.COM_NNNCCU) * 3;
	}
	
	private int getUserPointDifficulty0() {
		if (turnCnt < 5) {
			return getCnt(Pattern.USER_NUUUUN) * 7;			
		} else {
			return getCnt(Pattern.USER_NUUUUN) * 3; 
		}
	}
	
	private int getComPointDifficulty1() {
		return getCnt(Pattern.COM_UCCCCN) * 8		+ getCnt(Pattern.COM_NCCCCU) * 8
				+ getCnt(Pattern.COM_NNCCCNN) * 7
				+ getCnt(Pattern.COM_UNCCCNN) * 6	+ getCnt(Pattern.COM_NNCCCNU) * 6
				+ getCnt(Pattern.COM_UCCCNN) * 5	+ getCnt(Pattern.COM_NNCCCU) * 5
				+ getCnt(Pattern.COM_NNCCNN) * 4
				+ getCnt(Pattern.COM_UNCCNN) * 4	+ getCnt(Pattern.COM_NNCCNU) * 4
				+ getCnt(Pattern.COM_UCCNNN) * 3	+ getCnt(Pattern.COM_NNNCCU) * 3;
	}
	
	private int getUserPointDifficulty1() {
		if (turnCnt < 8) {
			return getCnt(Pattern.USER_NUUUUN) * 15;
		} else {
			return getCnt(Pattern.USER_NUUUUN) * 7;
		}
	}
	
	private int getComPointDifficulty2() {
		return getCnt(Pattern.COM_UCCCCN) * 14		+ getCnt(Pattern.COM_NCCCCU) * 14
				+ getCnt(Pattern.COM_CNCCC) * 13	+ getCnt(Pattern.COM_CCNCC) * 13	+ getCnt(Pattern.COM_CCCNC) * 13
				+ getCnt(Pattern.COM_NNCCCNN) * 11
				+ getCnt(Pattern.COM_UNCCCNN) * 9	+ getCnt(Pattern.COM_NNCCCNU) * 9
				+ getCnt(Pattern.COM_NCNCCN) * 9	+ getCnt(Pattern.COM_NCCNCN) * 9
				+ getCnt(Pattern.COM_UCCCNN) * 5	+ getCnt(Pattern.COM_NNCCCU) * 5
				+ getCnt(Pattern.COM_UCNCCN) * 5	+ getCnt(Pattern.COM_NCNCCU) * 5
				+ getCnt(Pattern.COM_UCCNCN) * 5	+ getCnt(Pattern.COM_NCCNCU) * 5
				+ getCnt(Pattern.COM_NNCCNN) * 5
				+ getCnt(Pattern.COM_UNCCNN) * 4	+ getCnt(Pattern.COM_NNCCNU) * 4	+ getCnt(Pattern.COM_NCNCN) * 4
				+ getCnt(Pattern.COM_UCCNNN) * 3	+ getCnt(Pattern.COM_NNNCCU) * 3;
	}
	
	private int getUserPointDifficulty2() {
		return getCnt(Pattern.USER_CUUUUN) * 13	+ getCnt(Pattern.USER_NUUUUC) * 13
				+ getCnt(Pattern.USER_UNUUU) * 12	+ getCnt(Pattern.USER_UUNUU) * 12	+ getCnt(Pattern.USER_UUUNU) * 12
				+ getCnt(Pattern.USER_NNUUUNN) * 9
				+ getCnt(Pattern.USER_CNUUUNN) * 4	+ getCnt(Pattern.USER_NNUUUNC) * 4
				+ getCnt(Pattern.USER_NUNUUN) * 4	+ getCnt(Pattern.USER_NUUNUN) * 4
				+ getCnt(Pattern.USER_CUUUNN) * 4	+ getCnt(Pattern.USER_NNUUUC) * 4
				+ getCnt(Pattern.USER_CUNUUN) * 4	+ getCnt(Pattern.USER_NUNUUC) * 4
				+ getCnt(Pattern.USER_CUUNUN) * 4	+ getCnt(Pattern.USER_NUUNUC) * 4
				+ getCnt(Pattern.USER_NNUUNN) * 4
				+ getCnt(Pattern.USER_CNUUNN) * 3	+ getCnt(Pattern.USER_NNUUNC) * 3	+ getCnt(Pattern.USER_NUNUN) * 3
				+ getCnt(Pattern.USER_CUUNNN) * 2	+ getCnt(Pattern.USER_NNNUUC) * 2;
	}
	
	private int getComPointDifficulty3() {
		return getCnt(Pattern.COM_UCCCCN) * 15		+ getCnt(Pattern.COM_NCCCCU) * 15
				+ getCnt(Pattern.COM_CNCCC) * 14	+ getCnt(Pattern.COM_CCNCC) * 13	+ getCnt(Pattern.COM_CCCNC) * 14
				+ getCnt(Pattern.COM_NNCCCNN) * 11
				+ getCnt(Pattern.COM_UNCCCNN) * 9	+ getCnt(Pattern.COM_NNCCCNU) * 9
				+ getCnt(Pattern.COM_NCNCCN) * 9	+ getCnt(Pattern.COM_NCCNCN) * 9
				+ getCnt(Pattern.COM_UCCCNN) * 5	+ getCnt(Pattern.COM_NNCCCU) * 5
				+ getCnt(Pattern.COM_UCNCCN) * 5	+ getCnt(Pattern.COM_NCNCCU) * 5
				+ getCnt(Pattern.COM_UCCNCN) * 5	+ getCnt(Pattern.COM_NCCNCU) * 5
				+ getCnt(Pattern.COM_NNCCNN) * 5
				+ getCnt(Pattern.COM_UNCCNN) * 4	+ getCnt(Pattern.COM_NNCCNU) * 4	+ getCnt(Pattern.COM_NCNCN) * 4
				+ getCnt(Pattern.COM_UCCNNN) * 3	+ getCnt(Pattern.COM_NNNCCU) * 3;
	}
	
	private int getUserPointDifficulty3() {
		return getCnt(Pattern.USER_CUUUUN) * 14	+ getCnt(Pattern.USER_NUUUUC) * 14
				+ getCnt(Pattern.USER_UNUUU) * 13	+ getCnt(Pattern.USER_UUNUU) * 12	+ getCnt(Pattern.USER_UUUNU) * 13
				+ getCnt(Pattern.USER_NNUUUNN) * 10
				+ getCnt(Pattern.USER_CNUUUNN) * 8	+ getCnt(Pattern.USER_NNUUUNC) * 8
				+ getCnt(Pattern.USER_NUNUUN) * 8	+ getCnt(Pattern.USER_NUUNUN) * 8
				+ getCnt(Pattern.USER_CUUUNN) * 4	+ getCnt(Pattern.USER_NNUUUC) * 4
				+ getCnt(Pattern.USER_CUNUUN) * 4	+ getCnt(Pattern.USER_NUNUUC) * 4
				+ getCnt(Pattern.USER_CUUNUN) * 4	+ getCnt(Pattern.USER_NUUNUC) * 4
				+ getCnt(Pattern.USER_NNUUNN) * 4
				+ getCnt(Pattern.USER_CNUUNN) * 3	+ getCnt(Pattern.USER_NNUUNC) * 3	+ getCnt(Pattern.USER_NUNUN) * 3
				+ getCnt(Pattern.USER_CUUNNN) * 2	+ getCnt(Pattern.USER_NNNUUC) * 2;
	}
	
	private int getComPointDifficultyHint() {
		return getCnt(Pattern.COM_UCCCCN) * 15		+ getCnt(Pattern.COM_NCCCCU) * 15
				+ getCnt(Pattern.COM_CNCCC) * 14	+ getCnt(Pattern.COM_CCNCC) * 13	+ getCnt(Pattern.COM_CCCNC) * 14
				+ getCnt(Pattern.COM_NNCCCNN) * 11
				+ getCnt(Pattern.COM_UNCCCNN) * 9	+ getCnt(Pattern.COM_NNCCCNU) * 9
				+ getCnt(Pattern.COM_NCNCCN) * 9	+ getCnt(Pattern.COM_NCCNCN) * 9
				+ getCnt(Pattern.COM_UCCCNN) * 5	+ getCnt(Pattern.COM_NNCCCU) * 5
				+ getCnt(Pattern.COM_UCNCCN) * 5	+ getCnt(Pattern.COM_NCNCCU) * 5
				+ getCnt(Pattern.COM_UCCNCN) * 5	+ getCnt(Pattern.COM_NCCNCU) * 5
				+ getCnt(Pattern.COM_NNCCNN) * 5
				+ getCnt(Pattern.COM_UNCCNN) * 4	+ getCnt(Pattern.COM_NNCCNU) * 4	+ getCnt(Pattern.COM_NCNCN) * 4
				+ getCnt(Pattern.COM_UCCNNN) * 1	+ getCnt(Pattern.COM_NNNCCU) * 1;
	}
	
	private int getUserPointDifficultyHint() {
		return getCnt(Pattern.USER_CUUUUN) * 21	+ getCnt(Pattern.USER_NUUUUC) * 21
				+ getCnt(Pattern.USER_UNUUU) * 20	+ getCnt(Pattern.USER_UUNUU) * 19	+ getCnt(Pattern.USER_UUUNU) * 20
				+ getCnt(Pattern.USER_NNUUUNN) * 18
				+ getCnt(Pattern.USER_CNUUUNN) * 10	+ getCnt(Pattern.USER_NNUUUNC) * 10
				+ getCnt(Pattern.USER_NUNUUN) * 10	+ getCnt(Pattern.USER_NUUNUN) * 10
				+ getCnt(Pattern.USER_CUUUNN) * 6	+ getCnt(Pattern.USER_NNUUUC) * 6
				+ getCnt(Pattern.USER_CUNUUN) * 6	+ getCnt(Pattern.USER_NUNUUC) * 6
				+ getCnt(Pattern.USER_CUUNUN) * 6	+ getCnt(Pattern.USER_NUUNUC) * 6
				+ getCnt(Pattern.USER_NNUUNN) * 6
				+ getCnt(Pattern.USER_CNUUNN) * 5	+ getCnt(Pattern.USER_NNUUNC) * 5	+ getCnt(Pattern.USER_NUNUN) * 5
				+ getCnt(Pattern.USER_CUUNNN) * 4	+ getCnt(Pattern.USER_NNNUUC) * 4;
	}
	
	private int getComPointCritical() {
		int point = 0;
		if (difficulty == OmokManager.DIFFICULTY_0 || difficulty == OmokManager.DIFFICULTY_1) {
			if (has(Pattern.COM_CCCCC)) {  // 오목완성
				return 990;
			} else if (has(Pattern.COM_NCCCCN)) {  // 양쪽열린 4개
				return 800;
			}
		} else {
			if (has(Pattern.COM_CCCCC)) {  // 오목완성
				return 990;
			} else if (has(Pattern.COM_NCCCCN)) {  // 양쪽열린 4개
				return 800;
			} else {
				int cnt4 = getCnt(Pattern.COM_UCCCCN)+getCnt(Pattern.COM_NCCCCU)
							+getCnt(Pattern.COM_CNCCC)+getCnt(Pattern.COM_CCNCC)+getCnt(Pattern.COM_CCCNC);
				int cnt3 = getCnt(Pattern.COM_NNCCCNN)+getCnt(Pattern.COM_UNCCCNN)
							+getCnt(Pattern.COM_NNCCCNU)+getCnt(Pattern.COM_NCNCCN)+getCnt(Pattern.COM_NCCNCN);
				
				if (cnt4 >= 2) {  // 4 & 4
					return 600;
				} else if (cnt4 >= 1 && cnt3 >= 1) {  // 4 & 3
					return 400;
				} else if (cnt3 >= 2) {  // 3 & 3
					return 200;
				}
			}
		}
		return point;
	}
	
	private int getUserPointCritical() {
		int point = 0;
		if (difficulty == OmokManager.DIFFICULTY_0 || difficulty == OmokManager.DIFFICULTY_1) {
			if (has(Pattern.USER_UUUUU)) {  // 오목완성
				point = 900;
			}
		} else {
			if (has(Pattern.USER_UUUUU)) {  // 오목완성
				point = 900;
			} else if (has(Pattern.USER_NUUUUN)) {  // 양쪽열린 4개
				point = 700;
			} else {
				int cnt4 = getCnt(Pattern.USER_CUUUUN)+getCnt(Pattern.USER_NUUUUC)
							+getCnt(Pattern.USER_UNUUU)+getCnt(Pattern.USER_UUNUU)+getCnt(Pattern.USER_UUUNU);
				int cnt3 = getCnt(Pattern.USER_NNUUUNN)+getCnt(Pattern.USER_CNUUUNN)
							+getCnt(Pattern.USER_NNUUUNC)+getCnt(Pattern.USER_NUNUUN)+getCnt(Pattern.USER_NUUNUN);
				
				if (cnt4 >= 2) {  // 4 & 4
					point = 500;
				} else if (cnt4 >= 1 && cnt3 >= 1) {  // 4 & 3
					point = 300;
				} else if (cnt3 >= 2) {  // 3 & 3
					point = 100;
				}
			}
		}
		return point;
	}
	
	private int getUserPointCriticalForHint() {
		int point = 0;
		if (has(Pattern.USER_UUUUU)) {  // 오목완성
			point = 999;
		} else if (has(Pattern.USER_NUUUUN)) {  // 양쪽열린 4개
			point = 900;
		} else {
			int cnt4 = getCnt(Pattern.USER_CUUUUN)+getCnt(Pattern.USER_NUUUUC)
						+getCnt(Pattern.USER_UNUUU)+getCnt(Pattern.USER_UUNUU)+getCnt(Pattern.USER_UUUNU);
			int cnt3 = getCnt(Pattern.USER_NNUUUNN)+getCnt(Pattern.USER_CNUUUNN)
						+getCnt(Pattern.USER_NNUUUNC)+getCnt(Pattern.USER_NUNUUN)+getCnt(Pattern.USER_NUUNUN);
			
			if (cnt4 >= 2) {  // 4 & 4
				point = 700;
			} else if (cnt4 >= 1 && cnt3 >= 1) {  // 4 & 3
				point = 500;
			} else if (cnt3 >= 2) {  // 3 & 3
				point = 300;
			}
		}
		return point;
	}
	
	// for test
	public void logPatternsCntMap() {
		if (ConstantValues.IS_SHOW_TEST_MAP) {
			Log.test("-- logPatternsCntMap! --");
			for (Entry<Pattern, Integer> entry : patternCntMap.entrySet()) {
				Log.test("Pattern:"+entry.getKey()+", Count:"+entry.getValue());
			}
		}
	}
}
