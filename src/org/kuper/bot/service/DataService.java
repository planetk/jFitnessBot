package org.kuper.bot.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kuper.bot.domain.Match;
import org.kuper.bot.domain.Team;
import org.kuper.bot.domain.TeamStatus;


public class DataService {
	
	private Map<String, Match> matches; 
	private Map<Integer, Team> teams;
	private Map<String,TeamStatus> teamStatus;
	
	public void init() {
		matches = loadMatches();
		teams = loadTeams();
		teamStatus = calculateStatus(matches, teams);
	}

	public Map<Integer, Team> loadTeams() {
		try {
			Map<Integer, Team> result = new HashMap<Integer, Team>();
			
			InputStream inputStream = ClassLoader.getSystemResourceAsStream("teams.csv");

			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
	        String line;

	        int cnt=0;
	        while ((line = in.readLine()) != null) {
	        	cnt++;
	        	String[] fields = line.split(";", 2);
	        	if (fields.length != 2) {
	        		throw new java.lang.RuntimeException("teams contains invalid line: " + cnt);
	        	}
	        	
	        	Team team = new Team();
	        	team.setId(Integer.parseInt(fields[0]));
	        	team.setName(fields[1]);
	        	
	        	result.put(Integer.parseInt(fields[0]), team);
	        }
	
	        return result;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Map<String, Match> loadMatches() {
		
		try {
			Map<String, Match> result = new LinkedHashMap<String, Match>();
			
			InputStream inputStream = ClassLoader.getSystemResourceAsStream("matches.csv");
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
	        String line;
	        //ignore 1st. Line
	        in.readLine();
	
	        int cnt=1;
	        while ((line = in.readLine()) != null) {
	        	cnt++;
	        	String[] fields = line.split(";", 8);
	        	if (fields.length != 8) {
	        		throw new java.lang.RuntimeException("matches contains invalid line: " + cnt);
	        	}
	        	
	        	Match match = new Match();
	        	match.setSaison(Integer.parseInt(fields[0]));
	        	match.setSpieltag(Integer.parseInt(fields[1]));
	        	match.setSpielNr(Integer.parseInt(fields[2]));
	        	match.setHeimTeam(Integer.parseInt(fields[3]));
	        	match.setGastTeam(Integer.parseInt(fields[4]));
	        	match.setHeimTore(Integer.parseInt(fields[5]));
	        	match.setGastTore(Integer.parseInt(fields[6]));
	        	if (!"?".equals(fields[7])) {
	        		match.setOpenLigaId(Integer.parseInt(fields[7]));
	        	}
	        	
	        	result.put(getMatchId(match), match);
	        }
	
	        return result;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Map<String, TeamStatus> calculateStatus(Map<String, Match> matches,
			Map<Integer, Team> teams) {

		Map<String, TeamStatus> result = new HashMap<String, TeamStatus>();
		
		Map<Integer, TeamStatus> statusBuffer = null;
		
		int saison = 0;
		for (Match match: matches.values()) {
			
			if(saison != match.getSaison()) {
				saison = match.getSaison();
				statusBuffer = new HashMap<Integer, TeamStatus>();  
			}

			// ----------------
			
			Team heimTeam = teams.get(match.getHeimTeam());
			
			TeamStatus bufferStatus;
			TeamStatus status = calculateTeamStatus(statusBuffer, match, heimTeam, true);
			
			heimTeam.getStatus().put(getSpieltagId(match.getSaison(), match.getSpieltag()), status);
			
			result.put(getTeamStatusId(match.getSaison(), match.getSpieltag(), match.getHeimTeam()), status);
			
			//-----------
			
			Team gastTeam = teams.get(match.getGastTeam());

			status = calculateTeamStatus(statusBuffer, match, gastTeam, false);
			
			gastTeam.getStatus().put(getSpieltagId(match.getSaison(), match.getSpieltag()), status);

			result.put(getTeamStatusId(match.getSaison(), match.getSpieltag(), match.getGastTeam()), status);

			// ----------------
		}
		
		return result;
	}

	private TeamStatus calculateTeamStatus(
			 Map<Integer, TeamStatus> statusBuffer, Match match, Team team, boolean isHeim) {
		TeamStatus bufferStatus = statusBuffer.get(team.getId());
		if (bufferStatus==null) {
			bufferStatus = new TeamStatus();
		}

		if (isHeim) {
			bufferStatus.setTeam(match.getHeimTeam());
			bufferStatus.setTordifferenz(bufferStatus.getTordifferenz() +  match.getTordifferenz());
			bufferStatus.setHeimTore(bufferStatus.getHeimTore() + match.getHeimTore());
			bufferStatus.setHeimGegentore(bufferStatus.getHeimGegentore() + match.getGastTore());
		} else {
			bufferStatus.setTeam(match.getGastTeam());
			bufferStatus.setTordifferenz(bufferStatus.getTordifferenz() - match.getTordifferenz());
			bufferStatus.setAuswaertsTore(bufferStatus.getAuswaertsTore() + match.getGastTore());
			bufferStatus.setAuswaertsGegentore(bufferStatus.getAuswaertsGegentore() + match.getHeimTore());
		}

		statusBuffer.put(team.getId(), bufferStatus);
					
		TeamStatus status = new TeamStatus(bufferStatus);
		status.setSaison(match.getSaison());
		status.setSpieltag(match.getSpieltag());
		status.setMatch(match);
		return status;
	}

	private String getTeamStatusId(int saison, int spieltag, int team) {
		return getSpieltagId(saison, spieltag) + "|" + team;
	}
	

	
	private String getSpieltagId(int saison, int spieltag) {
		return "" + saison + "|" + spieltag;
	}

	private String getMatchId(Match match) {
		return getMatchId(match.getSaison(), match.getSpieltag(), match.getSpielNr());
	}

	private String getMatchId(int saison, int spieltag, int spielNr) {
		return getSpieltagId(saison, spieltag) + "|" + spielNr;
	}

	public Map<String, Match> getMatches() {
		return matches;
	}

	public Map<Integer, Team> getTeams() {
		return teams;
	}

	public Map<String, TeamStatus> getTeamStatus() {
		return teamStatus;
	}

}
