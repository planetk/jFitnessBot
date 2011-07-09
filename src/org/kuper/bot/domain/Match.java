package org.kuper.bot.domain;

public class Match {

	private int saison;
	private int spieltag;
	private int spielNr;
	private int heimTeam;
	private int gastTeam;
	private int heimTore;
	private int gastTore;
	private Integer openLigaId = null;
	
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
	public int getSpielNr() {
		return spielNr;
	}
	public void setSpielNr(int spielNr) {
		this.spielNr = spielNr;
	}
	public int getHeimTeam() {
		return heimTeam;
	}
	public void setHeimTeam(int heimTeam) {
		this.heimTeam = heimTeam;
	}
	public int getGastTeam() {
		return gastTeam;
	}
	public void setGastTeam(int gastTeam) {
		this.gastTeam = gastTeam;
	}
	public int getHeimTore() {
		return heimTore;
	}
	public void setHeimTore(int heimTore) {
		this.heimTore = heimTore;
	}
	public int getGastTore() {
		return gastTore;
	}
	public void setGastTore(int gastTore) {
		this.gastTore = gastTore;
	}

	public int getTordifferenz() {
		return heimTore - gastTore;
	}
	public void setOpenLigaId(Integer openLigaId) {
		this.openLigaId = openLigaId;
	}
	public Integer getOpenLigaId() {
		return openLigaId;
	}

}
