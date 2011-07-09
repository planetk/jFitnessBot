package org.kuper.bot.domain;

public class MatchDay {
	
	private int saison;
	private int dayNo;

	public MatchDay(int saison, int dayNo) {
		this();
		this.saison = saison;
		this.dayNo = dayNo;
	}
	
	public MatchDay() {
		super();
	}
	
	public int getSaison() {
		return saison;
	}
	public void setSaison(int saison) {
		this.saison = saison;
	}
	public int getDayNo() {
		return dayNo;
	}
	public void setDayNo(int dayNo) {
		this.dayNo = dayNo;
	}

	public MatchDay getWithOffset(int offset) {
		int dayNo = this.dayNo + offset;
		int saison = this.saison;
		while (dayNo < 1) {
			saison--;
			dayNo=dayNo+34;
		}
		while (dayNo > 34) {
			saison++;
			dayNo=dayNo-34;
		}
		MatchDay result = new MatchDay();
		result.setDayNo(dayNo);
		result.setSaison(saison);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dayNo;
		result = prime * result + saison;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchDay other = (MatchDay) obj;
		if (dayNo != other.dayNo)
			return false;
		if (saison != other.saison)
			return false;
		return true;
	}
	
}
