package org.kuper.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.kuper.bot.domain.Match;
import org.kuper.bot.domain.Team;
import org.kuper.bot.domain.TeamStatus;
import org.kuper.bot.service.DataService;
import org.kuper.bot.service.EstimationService;
import org.kuper.bot.service.FitnessCalculator;

public class FitnessBot {
	
	private DataService dataService = new DataService();
	private EstimationService estimationService = new EstimationService();  
	private FitnessCalculator fitnessCalculator = new FitnessCalculator();
	
	public void readHistoricalMatchData() {
		dataService.init();
/*
		Team werder = dataService.getTeams().get(134);
		Map<String,TeamStatus> ts = werder.getStatus();
		for (TeamStatus s : ts.values()) {
			System.out.println("" + s.getSaison() + "-" + s.getSpieltag() + "X " + (s.getMatch().getHeimTeam()==134?s.getMatch().getHeimTore():s.getMatch().getGastTore()) + "-"  + s.getHeimTore() + " X" + s.getTordifferenz());
		}
*/		
//		tippeSaisons();
		tippeSpieltag(2011, 1, true);
		
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
		
		
		Map<Integer, Float> fitnessStore = new HashMap<Integer, Float>(); 
		for (int spiel = 1; spiel <= 9; spiel++) {
			
			String matchId = getMatchId(saison, spieltag, spiel);	
			Match match = matches.get(matchId);
			
			Team heimTeam = teams.get(match.getHeimTeam());
			fitnessStore.put(heimTeam.getId(), fitnessCalculator.calculateFitness(saison, spieltag, heimTeam));

			Team gastTeam = teams.get(match.getGastTeam());
			fitnessStore.put(gastTeam.getId(), fitnessCalculator.calculateFitness(saison, spieltag, gastTeam));
			
		}
		
		
		
		for (int spiel = 1; spiel <= 9; spiel++) {
				
			String matchId = getMatchId(saison, spieltag, spiel);
				
			Match match = matches.get(matchId);
				
			Team heimTeam = teams.get(match.getHeimTeam());
			float fitnessHeim = fitnessCalculator.calculateFitness(saison, spieltag, heimTeam);

			float avgOpponentHeim = fitnessCalculator.getAverageOpponentStrength(saison, spieltag, heimTeam, fitnessStore);
			System.out.println("AVG heim: " + avgOpponentHeim);
//			fitnessHeim = fitnessHeim * (1 - avgOpponentHeim/30);
			
			float serienFaktorHeim = fitnessCalculator.getSerienFaktor(saison, spieltag, heimTeam);
			if (serienFaktorHeim < 0) {
				System.err.println("SERIE: (" + spieltag + "/" + saison + ") " + heimTeam.getName()  );
			}
			
			// -------------------------
			
			Team gastTeam = teams.get(match.getGastTeam());
			float fitnessGast = fitnessCalculator.calculateFitness(saison, spieltag, gastTeam);

			
			float avgOpponentGast = fitnessCalculator.getAverageOpponentStrength(saison, spieltag, gastTeam, fitnessStore);
			System.out.println("AVG gast: " + avgOpponentGast);
//			fitnessGast = fitnessGast * (1 - avgOpponentGast/30);

			
			
			float serienFaktorGast = fitnessCalculator.getSerienFaktor(saison, spieltag, gastTeam);
			if (serienFaktorGast < 0) {
				System.err.println("SERIE: (" + spieltag + "/" + saison + ") " + gastTeam.getName()   );
			}

			
			float heimPerformance = heimVorteil + fitnessHeim + serienFaktorHeim;
			float gastPerformance = fitnessGast + serienFaktorGast;
			
			float deltaFitness = heimPerformance - gastPerformance;

//			deltaFitness = new Float(Math.round(deltaFitness * 10)) / 10;
//			deltaFitness = new Float(Math.round(deltaFitness * 8)) / 8;
			
			int norm = 8;
			deltaFitness = new Float(Math.round(deltaFitness * norm)) / norm;
			
			
			
			TeamStatus heimStatus = fitnessCalculator.getTeamStatus(heimTeam, saison, spieltag, -1);
			TeamStatus gastStatus = fitnessCalculator.getTeamStatus(gastTeam, saison, spieltag, -1);
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
