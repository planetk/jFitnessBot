package org.kuper.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.kuper.bot.domain.Match;
import org.kuper.bot.domain.Team;
import org.kuper.bot.domain.TeamStatus;
import org.kuper.bot.service.DataService;
import org.kuper.bot.service.EstimationService;

public class FitnessBot {
	
	private DataService dataService = new DataService();
	private EstimationService estimationService = new EstimationService();  

	public void readHistoricalMatchData() {
		dataService.init();
/*
		Team werder = dataService.getTeams().get(134);
		Map<String,TeamStatus> ts = werder.getStatus();
		for (TeamStatus s : ts.values()) {
			System.out.println("" + s.getSaison() + "-" + s.getSpieltag() + "X " + (s.getMatch().getHeimTeam()==134?s.getMatch().getHeimTore():s.getMatch().getGastTore()) + "-"  + s.getHeimTore() + " X" + s.getTordifferenz());
		}
*/		
		tippeSaisons();
//		tippeSaison(2010, true);
//		tippeSaison(2010, false);
		
	}
	
	public int tippeSaisons() {
		int punkte = 0;
		for (int saison = 1999; saison <= 2010; saison++) {
			punkte = punkte + tippeSaison(saison, false);
		}
		System.out.println("=======================================");
		System.out.println("Summe: " + punkte);
		System.out.println("=======================================");
		return punkte;
	}
	
	

	private int tippeSaison(int saison, boolean sendTipp) {
		
		int gesamtPunkte = 0;
		
		for (int spieltag = 1; spieltag <= 34; spieltag++) {
		
			gesamtPunkte = gesamtPunkte + tippeSpieltag(saison, spieltag, sendTipp);
		}
			
		System.out.println("=======================================");
		System.out.println("Saison: " + gesamtPunkte);
		System.out.println("=======================================");
		
		return gesamtPunkte;
	}

	private int tippeSpieltag(int saison, int spieltag, boolean sendTipp) {
		
		Map<String, Match> matches = dataService.getMatches();
		Map<Integer, Team> teams = dataService.getTeams();
	
		float heimVorteil = estimationService.estimateHomeAdavantage();
		
		int spieltagsPunkte = 0;
			
		for (int spiel = 1; spiel <= 9; spiel++) {
				
			String matchId = getMatchId(saison, spieltag, spiel);
				
			Match match = matches.get(matchId);
				
			Team heimTeam = teams.get(match.getHeimTeam());
			TeamStatus heimStatus = heimTeam.getStatus().get(getSpieltagId(saison, spieltag, -1));
			// TODO: proper handling for 2 Liga Mannschaften
			if (heimStatus==null) {
				heimStatus = TeamStatus.getLiga2Status(heimTeam, saison - 1, 34);
			}
			float fitnessSaisonHeim = heimStatus.getFitness();

			TeamStatus heimStatusVorSaison = heimTeam.getStatus().get(getSpieltagId(saison-1, 34));
			// TODO: proper handling for 2 Liga Mannschaften
			if (heimStatusVorSaison==null) {
				heimStatusVorSaison = TeamStatus.getLiga2Status(heimTeam, saison - 1, 34);
			}
			float fitnessVorSaisonHeim = heimStatusVorSaison.getFitness();

			TeamStatus heimStatusVorVorSaison = heimTeam.getStatus().get(getSpieltagId(saison-2, 34));
			// TODO: proper handling for 2 Liga Mannschaften
			if (heimStatusVorVorSaison==null) {
				heimStatusVorVorSaison = TeamStatus.getLiga2Status(heimTeam, saison - 2, 34);
			}
			float fitnessVorVorSaisonHeim = heimStatusVorVorSaison.getFitness();
			
			float fitnessVorVorsaisonGewichtetHeim = 0.2f * (17-spieltag/2)*fitnessVorVorSaisonHeim/17;
			float fitnessVorsaisonGewichtetHeim = 0.8f * (17-spieltag/2)*fitnessVorSaisonHeim/17;
			float fitnessSaisonGewichtetHeim = ((spieltag/2-1)*fitnessSaisonHeim)/17;

			Team gastTeam = teams.get(match.getGastTeam());
			TeamStatus gastStatus = gastTeam.getStatus().get(getSpieltagId(saison, spieltag, -1));
			if (gastStatus == null) {
				gastStatus = TeamStatus.getLiga2Status(gastTeam, saison - 1, 34);
			}
			float fitnessSaisonGast = gastStatus.getFitness();

			TeamStatus gastStatusVorSaison = gastTeam.getStatus().get(getSpieltagId(saison-1, 34));
			// TODO: proper handling for 2 Liga Mannschaften
			if (gastStatusVorSaison==null) {
				gastStatusVorSaison = TeamStatus.getLiga2Status(gastTeam, saison - 1, 34);
			}
			float fitnessVorSaisonGast = gastStatusVorSaison.getFitness();

			TeamStatus gastStatusVorVorSaison = gastTeam.getStatus().get(getSpieltagId(saison-2, 34));
			// TODO: proper handling for 2 Liga Mannschaften
			if (gastStatusVorVorSaison==null) {
				gastStatusVorVorSaison = TeamStatus.getLiga2Status(gastTeam, saison - 2, 34);
			}
			float fitnessVorVorSaisonGast = gastStatusVorVorSaison.getFitness();

			float fitnessVorVorsaisonGewichtetGast = 0.2f * (17-spieltag/2)*fitnessVorVorSaisonGast/17;
			float fitnessVorsaisonGewichtetGast = 0.8f * (17-spieltag/2)*fitnessVorSaisonGast/17;
			float fitnessSaisonGewichtetGast = ((spieltag/2-1)*fitnessSaisonGast)/17;
			
			
			
			float heimPerformance = heimVorteil + fitnessSaisonGewichtetHeim + fitnessVorsaisonGewichtetHeim + fitnessVorVorsaisonGewichtetHeim;
			float gastPerformance = fitnessSaisonGewichtetGast + fitnessVorsaisonGewichtetGast + fitnessVorVorsaisonGewichtetGast;
				
			float deltaFitness = heimPerformance - gastPerformance;


			float estimatedGoals = estimationService.getEstimatedGoals(heimStatus, gastStatus);
				
			float toreHeim = (estimatedGoals + deltaFitness) / 2;
			float toreGast = (estimatedGoals - deltaFitness) / 2;
				
			int th = Math.round(toreHeim);
			int tg = Math.round(toreGast);

			int spielPunkte = getTippPunkte(th, tg, match.getHeimTore(), match.getGastTore());
				
			if (sendTipp) {
				sendTipp(match, th, tg);
			}
				
	
				System.out.println("" + heimTeam.getName() + "-"
						+ gastTeam.getName() + " "
						+ th + ":" 
						+ tg + " ("
						+ match.getHeimTore() + ":"
						+ match.getGastTore() + ") "
						+ "["
						+ String.format("%.4f", deltaFitness)
	/*
						+ String.format("%.2f", heimPerformance)
						+ ":"
		 				+ String.format("%.2f", gastPerformance)
		*/ 				
						+ "] "
						+ spielPunkte
				);
	
			spieltagsPunkte = spieltagsPunkte + spielPunkte;
		}
			
		//	System.out.println("---------------------------------------");
			System.out.println("Spieltag (" + spieltag + "/" + saison + "): " + spieltagsPunkte);
			System.out.println("---------------------------------------");
			
		
		return spieltagsPunkte;
	}

	
	
	
	
	
	private void sendTipp(Match match, int th, int tg) {
		try {
		    // Construct data
		    String data = "match_id=" + match.getOpenLigaId() + "&token=3d9pm7cj8re2r8frc6nce3o5&result=" + th + ":" + tg;

		    // Send data
		    URL url = new URL("http://botliga.de/api/guess");
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = rd.readLine()) != null) {
		        System.out.println("<<<" + line);
		    }
		    wr.close();
		    rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getSpieltagId(int saison, int spieltag, int offset) {
		int tag = spieltag + offset;
		int sai = saison;
		while (tag < 1) {
			sai--;
			tag=tag+34;
		}
		while (tag > 34) {
			sai++;
			tag=tag-34;
		}
		return getSpieltagId(sai,tag);
	}

	private String getSpieltagId(int saison, int spieltag) {
		return "" + saison + "|" + spieltag;
	}

	private String getMatchId(int saison, int spieltag, int spielNr) {
		return getSpieltagId(saison, spieltag) + "|" + spielNr;
	}

	public int getTippPunkte(int tippHeimTore, int tippGastTore, int heimTore, int gastTore) {
		
		if (heimTore == tippHeimTore && gastTore == tippGastTore) {
			return 5; // Genauer Treffer
		} else if (heimTore == gastTore && tippHeimTore == tippGastTore) {
			return 2; // Unentschieden 
		} else if ((heimTore - gastTore) == (tippHeimTore - tippGastTore)) {
			return 3; // korekte Tordifferenz
		} else if (heimTore > gastTore && tippHeimTore > tippGastTore) {
			return 2; // korrekte Tendenz heimsieg
		} else if (heimTore < gastTore && tippHeimTore < tippGastTore) {
			return 2; // korrekte Tendenz gastsieg
		} else { 
			return 0;
		}
	}


}
