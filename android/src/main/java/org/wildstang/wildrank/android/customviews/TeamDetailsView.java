package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wildstang.wildrank.R;

public class TeamDetailsView extends RelativeLayout {

	private TextView teamNumberView;
	private TextView teamNameView;

	private int teamNumber;
	private String teamName;

	public TeamDetailsView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.custom_view_team_details, this, true);

		teamNumberView = (TextView) findViewById(R.id.team_number);
		teamNameView = (TextView) findViewById(R.id.team_name);
	}

	public void setTeamNumber(int number) {
		this.teamNumber = number;
		teamNumberView.setText(Integer.toString(teamNumber));
	}

	public void setTeamName(String name) {
		this.teamName = name;
		teamNameView.setText(teamName);
	}

}
