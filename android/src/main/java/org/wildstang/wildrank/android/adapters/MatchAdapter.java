package org.wildstang.wildrank.android.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.competitionmodels.CompetitionMatch;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.utils.Keys;

public class MatchAdapter extends CursorAdapter {

    public MatchAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView number = (TextView) view.findViewById(R.id.number);
        TextView blueAlliance = (TextView) view.findViewById(R.id.blue_alliance);
        TextView redAlliance = (TextView) view.findViewById(R.id.red_alliance);
        int matchNumber = CompetitionMatch.matchNumberFromMatchKey(cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.KEY)));
        number.setText("" + matchNumber);
        number.setFocusable(false);
        String blueAllianceString = cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.BLUE_1)) + "  " + cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.BLUE_2)) + "  "
                + cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.BLUE_3));
        String redAllianceString = cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.RED_1)) + "  " + cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.RED_2)) + "  "
                + cursor.getString(cursor.getColumnIndex(DatabaseContract.Match.RED_3));
        blueAlliance.setText(blueAllianceString);
        redAlliance.setText(redAllianceString);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String configuredTeamString = prefs.getString(Keys.CONFIGURED_TEAM, null);
        int teamNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(configuredTeamString)));
        if (DataManager.isMatchScouted(context, matchNumber, teamNumber)) {
            number.setTextColor(context.getResources().getColor(R.color.gray));
            redAlliance.setTextColor(context.getResources().getColor(R.color.gray));
            blueAlliance.setTextColor(context.getResources().getColor(R.color.gray));
        } else {
            number.setTextColor(context.getResources().getColor(R.color.black));
            redAlliance.setTextColor(context.getResources().getColor(R.color.red));
            blueAlliance.setTextColor(context.getResources().getColor(R.color.blue));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_match, viewGroup, false);
    }
}
