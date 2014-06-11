package org.wildstang.wildrank.android.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.utils.Keys;

public class MatchDetailsView extends RelativeLayout {

    private TextView teamNumberView;
    private TextView matchNumberView;
    private TextView allianceColorView;

    public MatchDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_view_match_details, this, true);

        teamNumberView = (TextView) findViewById(R.id.team_number);
        matchNumberView = (TextView) findViewById(R.id.match_number);
        allianceColorView = (TextView) findViewById(R.id.alliance_color);
    }

    public void setTeamNumber(int number) {
        teamNumberView.setText(Integer.toString(number));
    }

    public void setMatchNumber(int number) {
        matchNumberView.setText(Integer.toString(number));
    }

    public void setAllianceColor(String allianceColor) {
        if (allianceColor.equals(Keys.ALLIANCE_COLOR_RED)) {
            allianceColorView.setText(R.string.alliance_color_red);
            allianceColorView.setTextColor(getResources().getColor(R.color.red));

        } else {
            allianceColorView.setText(R.string.alliance_color_blue);
            allianceColorView.setTextColor(getResources().getColor(R.color.blue));
        }
    }

}
