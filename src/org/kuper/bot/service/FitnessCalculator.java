package org.kuper.bot.service;

import java.util.Map;

import org.kuper.bot.domain.Team;
import org.kuper.bot.domain.TeamStatus;

public class FitnessCalculator {
	
	public float calculateFitness(int saison, int spieltag,
			Team team) {

		return weigthedSeasonsFitness(saison, spieltag, team);

	}
	
	@Deprecated
	protected float calculateWeigthedAverageFitness(int saison, int spieltag,
			Team team) {
		int anzahlSpielTage = 70;
		float minGewicht = 0.1f;
		float steigung = (minGewicht-1.0f) / anzahlSpielTage;
		
		float summeFitness = 0.0f;
		float summeGewichte = 0.0f;
		for (int n=0; n < anzahlSpielTage; n++) {
			TeamStatus status = team.getStatus().get(getSpieltagId(saison, spieltag, -(n+1)));
			if (status==null) {
				status = TeamStatus.getLiga2Status(team, saison, 1);
			}
			float fitnessTag = status.getFitness();
			float gewichtung = (1.0f+steigung*n);
			summeFitness = summeFitness + gewichtung * fitnessTag;
			summeGewichte = summeGewichte + gewichtung;
		}
		return summeFitness / summeGewichte;
	}
	
	protected float weigthedSeasonsFitness(int saison, int spieltag, Team team) {

		TeamStatus status = getTeamStatus(team, saison, spieltag, -1);
		float fitnessSaison = status.getFitness();

		TeamStatus statusVor1 = getTeamStatus(team, saison-1, 34, 0);
		float fitnessVor1 = statusVor1.getFitness();

		TeamStatus statusVor2 = getTeamStatus(team, saison-2, 34, 0);
		float fitnessVor2 = statusVor2.getFitness();

		float totalFitness = 0.0f;

		if (spieltag < 3) {
			float fitnessVor2Gewichtet = 0.1f * fitnessVor2;
			float fitnessVor1Gewichtet = 0.8f * fitnessVor1;
			float fitnessSaisonGewichtet = 0.1f * fitnessSaison;
			totalFitness = fitnessSaisonGewichtet + fitnessVor1Gewichtet + fitnessVor2Gewichtet;
		} else if (spieltag < 4) {
			float fitnessVor2Gewichtet = 0.2f * fitnessVor2;
			float fitnessVor1Gewichtet = 0.5f * fitnessVor1;
			float fitnessSaisonGewichtet = 0.3f * fitnessSaison;
			totalFitness = fitnessSaisonGewichtet + fitnessVor1Gewichtet + fitnessVor2Gewichtet;
		} else if (spieltag > 32) {
			float fitnessVor2Gewichtet = 0.1f * fitnessVor2;
			float fitnessVor1Gewichtet = 0.3f * fitnessVor1;
			float fitnessSaisonGewichtet = 0.6f * fitnessSaison;
			totalFitness = fitnessSaisonGewichtet + fitnessVor1Gewichtet + fitnessVor2Gewichtet;
		} else {
			float fitnessVor2Gewichtet = 0.2f * fitnessVor2;
			float fitnessVor1Gewichtet = 0.3f * fitnessVor1;
			float fitnessSaisonGewichtet = 0.5f * fitnessSaison;
			totalFitness = fitnessSaisonGewichtet + fitnessVor1Gewichtet + fitnessVor2Gewichtet;
		}
		return totalFitness;
		
	}

	public float getSerienFaktor(int saison, int spieltag, Team team) {
		float result = 0.0f;
		
		if (spieltag > 4) {
			TeamStatus status = getTeamStatus(team, saison, spieltag, -1);
			int tendenz = status.getTendenz();
			if (tendenz > 0) {
				result = -0.02f;
				for (int n=-4; n < -1; n++) {
					status = getTeamStatus(team, saison, spieltag, n);
					if (status.getTendenz() != tendenz) {
						result = 0.0f;
						break;
					}
				}
			}
		}
		
		return result;
	}


	
	public float getAverageOpponentStrength(int saison, int spieltag, Team team, Map<Integer, Float> fitnessStore) {
		float result = 0.0f;
		for (int i = 1; i < spieltag; i++) {
			TeamStatus status = getTeamStatus(team, saison, spieltag, -1);
			result = result + fitnessStore.get(status.getOpponent());
		}
		return result / spieltag;
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

	public TeamStatus getTeamStatus(Team team, int saison, int spieltag, int offset) {
		TeamStatus status = team.getStatus().get(getSpieltagId(saison, spieltag, offset));
		if (status==null) {
			status = TeamStatus.getLiga2Status(team, saison, 1);
		}
		return status;
	}


}
