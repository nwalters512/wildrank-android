package org.wildstang.wildrank.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.database.DatabaseContract;

public class TeamAdapter extends CursorAdapter {

    // Pit mode grays out teams that have already been pit scouted
    // General mode does not gray out any teams
    public static final int MODE_PIT = 1;
    public static final int MODE_GENERAL = 2;

    private int mode;

    public TeamAdapter(Context context, Cursor cursor, int flags, int mode) {
        super(context, cursor, flags);
        this.mode = mode;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int teamNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseContract.Team.NUMBER)));
        TextView number = (TextView) view.findViewById(R.id.number);
        number.setText("" + teamNumber);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.Team.NAME)));
        name.setFocusable(false);
        if (mode == MODE_PIT) {
            if (DataManager.isTeamPitScouted(context, teamNumber)) {
                number.setTextColor(context.getResources().getColor(R.color.gray));
                name.setTextColor(context.getResources().getColor(R.color.gray));
            } else {
                number.setTextColor(context.getResources().getColor(R.color.black));
                name.setTextColor(context.getResources().getColor(R.color.black));
            }
        } else if (mode == MODE_GENERAL) {
            // Don't gray out anything in these modes
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item_team, viewGroup, false);
        bindView(v, context, cursor);
        viewGroup.setFocusable(true);
        return v;
    }

}
