package org.wildstang.wildrank.android.data;

public class MatchData extends DataFile {

	int matchNumber;
	int teamNumber;

	public MatchData() {
		super(DataFile.Type.MATCH);
	}

	public void setMatchNumber(int matchNumber) {
		this.matchNumber = matchNumber;
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	@Override
	public String getRelativeFile() {
		return new String("/matches/" + matchNumber + "/" + teamNumber + ".json");
	}

}
