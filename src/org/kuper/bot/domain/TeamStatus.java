package org.kuper.bot.domain;

public class TeamStatus {
	
	
	private int team = 0;

	private int tordifferenz = 0;
	private int heimTore = 0;
	private int heimGegentore = 0;
	private int auswaertsTore = 0;
	private int auswaertsGegentore = 0;
	private int saison = 0;
	private int spieltag = 0;
	private Match match;

	public TeamStatus(TeamStatus status) {
		this();
		team = status.getTeam();
		tordifferenz = status.getTordifferenz();
		heimTore = status.getHeimTore();
		heimGegentore = status.getHeimGegentore();
		auswaertsTore = status.getAuswaertsTore();
		auswaertsGegentore = status.getAuswaertsGegentore();
		saison = status.getSaison();
		spieltag = status.getSpieltag();
		match = status.getMatch();
	}

	public TeamStatus() {
		super();
	}

	public static TeamStatus getLiga2Status(Team team, int saison, int spieltag) {
		TeamStatus liga2Status = new TeamStatus();
		liga2Status.setTordifferenz(-spieltag);
		liga2Status.setHeimTore(2);
		liga2Status.setHeimGegentore(1);
		liga2Status.setAuswaertsTore(2);
		liga2Status.setAuswaertsGegentore(2);
		liga2Status.setSaison(saison);
		liga2Status.setSpieltag(spieltag);		
		return liga2Status;
	}
	
	public int getTeam() {
		return team;
	}
	public void setTeam(int team) {
		this.team = team;
	}
	public int getHeimTore() {
		return heimTore;
	}
	public void setHeimTore(int heimTore) {
		this.heimTore = heimTore;
	}
	public int getHeimGegentore() {
		return heimGegentore;
	}
	public void setHeimGegentore(int heimGegentore) {
		this.heimGegentore = heimGegentore;
	}
	public int getAuswaertsTore() {
		return auswaertsTore;
	}
	public void setAuswaertsTore(int auswaertsTore) {
		this.auswaertsTore = auswaertsTore;
	}
	public int getAuswaertsGegentore() {
		return auswaertsGegentore;
	}
	public void setAuswaertsGegentore(int auswaertsGegentore) {
		this.auswaertsGegentore = auswaertsGegentore;
	}
	public int getSaison() {
		return saison;
	}
	public void setSaison(int saison) {
		this.saison = saison;
	}
	public int getSpieltag() {
		return spieltag;
	}
	public void setSpieltag(int spieltag) {
		this.spieltag = spieltag;
	}
	public int getTordifferenz() {
		return tordifferenz;
	}
	public void setTordifferenz(int tordifferenz) {
		this.tordifferenz = tordifferenz;
	}
	public Match getMatch() {
		return match;
	}
	public void setMatch(Match match) {
		this.match = match;
	}
	
	public int getTore() {
		return getHeimTore() + getAuswaertsTore();
	}

	public int getGegentore() {
		return getHeimGegentore() + getAuswaertsGegentore();
	}
	
	public float getTorschnitt() {
		return getSchnitt(getTore());
	}

	public float getGegentorschnitt() {
		return getSchnitt(getGegentore());
	}

	private float getSchnitt(int tore) {
		return (new Float(tore)) / spieltag;
	}
	
	public float getFitness() {
		return (new Float(tordifferenz)) / spieltag;
//		return ((float)tordifferenz) / spieltag;
	}

	public int getTendenz() {
		if (match.getHeimTeam() == this.team) {
			return Integer.signum(match.getHeimTore() - match.getGastTore());
		} else {
			return Integer.signum(match.getGastTore() - match.getHeimTore());			
		}
	}

	public int getOpponent() {
		if (match.getHeimTeam() == this.team) {
			return match.getGastTeam();
		} else {
			return match.getHeimTeam();
		}
	}
	

}