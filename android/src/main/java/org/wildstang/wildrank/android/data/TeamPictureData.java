package org.wildstang.wildrank.android.data;

public class TeamPictureData extends DataFile {

	int teamNumber;

	public TeamPictureData() {
		super(DataFile.Type.PIT);
	}

	public TeamPictureData(int teamNumber) {
		super(DataFile.Type.PIT);
		setTeamNumber(teamNumber);
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	@Override
	public String getRelativeFile() {
		return new String("/images/" + teamNumber + ".JPG");
	}

}
