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

public class FitnessBot {
	
	private DataService dataService = new DataService();

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
		
		Map<String, Match> matches = dataService.getMatches();
		Map<Integer, Team> teams = dataService.getTeams();
	
		float heimVorteil = 0.8f;
		
		int gesamtPunkte = 0;
		
		for (int spieltag = 1; spieltag <= 34; spieltag++) {
			
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
				
				Team gastTeam = teams.get(match.getGastTeam());
				TeamStatus gastStatus = gastTeam.getStatus().get(getSpieltagId(saison, spieltag, -1));
				if (gastStatus == null) {
					gastStatus = TeamStatus.getLiga2Status(gastTeam, saison - 1, 34);
				}
				float fitnessSaisonGast = gastStatus.getFitness();

				float heimPerformance = heimVorteil + fitnessSaisonHeim;
				float gastPerformance = fitnessSaisonGast;
				
				float deltaFitness = heimPerformance - gastPerformance;


// 4837		
//				float summeTore = 2.78f; // => 385

// 4356
//				float summeTore = 3.5f; // => 344

// 4989				
//				float summeTore = 2.9f; // => 375

// 4996				
				float summeTore = 3.01f; // => 371

// 4937				
//				float summeTore = getEstimatedGoals( heimTore, heimGegentore, gastTore, gastGegentore); // => 391

//				float summeTore = getEstimatedGoals2(heimStatus.getTorschnitt(), gastStatus.getTorschnitt()); // => 391
//				float summeTore = getEstimatedGoals( heimStatus.getTorschnitt(), heimStatus.getGegentorschnitt(), gastStatus.getTorschnitt(), gastStatus.getGegentorschnitt()); // => 391
				
// 4703				
//				float summeTore = (new Float( heimTore + heimGegentore + gastTore + gastGegentore) / (2 * getSpielCount(spieltag,-1)));
				
//				float summeTore = (new Float( heimTore + heimGegentore + gastTore + gastGegentore) / (2 * getSpielCount(spieltag,-1)));
//				float summeTore = 1.5f + (new Float( heimTore + heimGegentore + gastTore + gastGegentore) / (8 * getSpielCount(spieltag,-1)));

//				System.err.println("SUMME: " + summeTore);
				
				
				float toreHeim = (summeTore + deltaFitness) / 2;
				float toreGast = (summeTore - deltaFitness) / 2;
				
				int th = Math.round(toreHeim);
				int tg = Math.round(toreGast);

				int spielPunkte = getTippPunkte(th, tg, match.getHeimTore(), match.getGastTore());
				
				if (sendTipp) {
					sendTipp(match, th, tg);
				}
				
	/*
				System.out.println("" + heimTeam.getName() + "-"
						+ gastTeam.getName() + " "
						+ th + ":" 
						+ tg + " ("
						+ match.getHeimTore() + ":"
						+ match.getGastTore() + ") "
						+ spielPunkte
				);
	*/
				spieltagsPunkte = spieltagsPunkte + spielPunkte;
				gesamtPunkte = gesamtPunkte + spielPunkte;
			}
			
	//		System.out.println("---------------------------------------");
	//		System.out.println("Spieltag: " + spieltagsPunkte);
	//		System.out.println("---------------------------------------");
			
		}
		
		System.out.println("=======================================");
		System.out.println("Saison: " + gesamtPunkte);
		System.out.println("=======================================");
		
		return gesamtPunkte;
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

	private int fac(int n) {
		return n>1?n*fac(n-1):1; 
	}

	private float getPoissionProbability(float t, int n) {
		return new Float((Math.pow(t, n) / fac(n) * Math.pow(Math.exp(1),(-1.0*t))));
	}

	private float getEstimatedGoals(float team1GoalsShot, float team1GoalsConc, float team2GoalsShot, float team2GoalsConc) {
		float totalGoals=0.0f;
		float maxProbability = 0.0f;

		for (int n=0; n <= 6; n++) {
			for (int m=0; m <= 6; m++) {
				for (int o=0; o <= 6; o++) {
					for (int p=0; p <= 6; p++) {
						float probability = getPoissionProbability(team1GoalsShot, n)
										  * getPoissionProbability(team1GoalsConc, m)
										  * getPoissionProbability(team2GoalsShot, o)
										  * getPoissionProbability(team2GoalsConc, p);
						if (probability > maxProbability) {
							maxProbability = probability;
							totalGoals = new Float(n + m + o + p);
						}
					}	
				}	
			}			
		}
		System.err.println("GOAL: " + totalGoals );
		return totalGoals;
	}

	private float getEstimatedGoals2(float team1Goals, float team2Goals) {

		/*
		float totalGoals=0.0f;
		float maxProbability = 0.0f;

		for (int n=0; n <= 6; n++) {
			
			float p = getPoissionProbability(3, n);
			System.err.println("PROB: (" + n + "): " + p);
			
			for (int m=0; m <= 6; m++) {

				float probability = getPoissionProbability(team1Goals, n)
								  * getPoissionProbability(team2Goals, m);
				
				
				if (probability >= maxProbability) {
					//System.err.println("PROB: (" + n + "/" + m + "): " + probability);
					maxProbability = probability;
					totalGoals = new Float(n + m);
				}	
			}			
		}
		return totalGoals;
		*/
		return 2.0f + 0.33f * (team1Goals + team2Goals);
	}

}
