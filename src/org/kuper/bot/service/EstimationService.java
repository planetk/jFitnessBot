package org.kuper.bot.service;

import org.kuper.bot.domain.TeamStatus;

public class EstimationService {

	public float getEstimatedGoals(TeamStatus heimStatus, TeamStatus gastStatus) {

//		return 2.5f;   // 4624
//		return 2.6f;   // 4721
//		return 2.7f;   // 4847
//		return 2.8f;   // 4913 // 5116
//		return 2.9f;   // 4989 // 5174
//		return 2.925f; // 4981
//		return 2.95f;  // 5011 // 5188
//		return 2.975f; // 5013
//		return 2.98f;  // 5016 // 5190
		return 2.99f;  // 5008 // 5193	
//		return 3.0f;   // 5004 // 5187
//		return 3.05f;  // 4962
//		return 3.1f;   // 4913 // 5091
//		return 3.2f;   // 4743
//		return 3.3f;   // 4661
	
		
		
		
		
		// ??? // 5134
//		float x = 2.5f + (heimStatus.getTorschnitt() + gastStatus.getTorschnitt() ) / 6;
//		System.err.println("X: " + x);
//		return x;

		// ??? // 5189
		// return getEstimatedGoals( heimStatus.getTorschnitt(), heimStatus.getGegentorschnitt(), gastStatus.getTorschnitt(), gastStatus.getGegentorschnitt()); // => 391
//		return getEstimatedGoals3( heimStatus.getTorschnitt(), gastStatus.getTorschnitt()); // => 391
			
		
//						float summeTore = getEstimatedGoals( heimTore, heimGegentore, gastTore, gastGegentore); // => 391
//						float summeTore = getEstimatedGoals2(heimStatus.getTorschnitt(), gastStatus.getTorschnitt()); // => 391
//						float summeTore = getEstimatedGoals( heimStatus.getTorschnitt(), heimStatus.getGegentorschnitt(), gastStatus.getTorschnitt(), gastStatus.getGegentorschnitt()); // => 391
						
//						float summeTore = (new Float( heimTore + heimGegentore + gastTore + gastGegentore) / (2 * getSpielCount(spieltag,-1)));
						
//						float summeTore = (new Float( heimTore + heimGegentore + gastTore + gastGegentore) / (2 * getSpielCount(spieltag,-1)));
//						float summeTore = 1.5f + (new Float( heimTore + heimGegentore + gastTore + gastGegentore) / (8 * getSpielCount(spieltag,-1)));

	}

	private int fac(int n) {
		return n>1?n*fac(n-1):1; 
	}

	private float getPoissionProbability(float t, int n) {
		return new Float((Math.pow(t, n) / fac(n) * Math.pow(Math.exp(1),(-t))));
	}

	protected float getEstimatedGoals(float team1GoalsShot, float team1GoalsConc, float team2GoalsShot, float team2GoalsConc) {
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
							totalGoals = 2.75f + new Float(n + m + o + p) / 20;
						}
					}	
				}	
			}			
		}
		System.err.println("GOAL: " + totalGoals );
		return totalGoals;
	}

	protected float getEstimatedGoals2(float team1Goals, float team2Goals) {

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

	
	protected float getEstimatedGoals3(float team1GoalsShot, float team2GoalsShot) {
		float totalGoals=0.0f;
		float maxProbability = 0.0f;

		for (int n=0; n <= 6; n++) {
			float probability = getPoissionProbability(team1GoalsShot, n);
			System.out.println("PROB HEIM SCORES " + n + ": " + String.format("%.4f",probability) );
		}

		for (int n=0; n <= 6; n++) {
			for (int m=0; m <= 6; m++) {
				float probability = getPoissionProbability(team1GoalsShot, n)
								  * getPoissionProbability(team2GoalsShot, m);
				if (probability > maxProbability) {
					maxProbability = probability;
					totalGoals = new Float(n + m);
				}	
			}	
		}
		
	//	System.err.println("GOAL: " + totalGoals );
		return totalGoals;
	}

	
	public float estimateHomeAdavantage() {
//		return 0.4f; //4905 // 4996
//		return 0.5f; //4927 // 5130
//		return 0.6f; //4920 // 5170
		return 0.7f; //4955 // 5193
//		return 0.8f; //5016 // 5159
//		return 0.9f; //5018 // 5146
//		return 1.0f; //4934 // 5074
		
	}
	
	
	
	
}
