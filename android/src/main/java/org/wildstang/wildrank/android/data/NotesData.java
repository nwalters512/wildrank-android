package org.wildstang.wildrank.android.data;

public class NotesData extends DataFile {

	int teamNumber;

	public NotesData() {
		super(DataFile.Type.PIT);
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	@Override
	public String getRelativeFile() {
		return new String("/notes/" + teamNumber + ".txt");
	}

}
