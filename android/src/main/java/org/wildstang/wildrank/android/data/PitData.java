package org.wildstang.wildrank.android.data;

public class PitData extends DataFile {

	int teamNumber;

	public PitData() {
		super(DataFile.Type.PIT);
	}

	public PitData(int teamNumber) {
		super(DataFile.Type.PIT);
		setTeamNumber(teamNumber);
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	@Override
	public String getRelativeFile() {
		return new String("/pit/" + teamNumber + ".json");
	}

}
