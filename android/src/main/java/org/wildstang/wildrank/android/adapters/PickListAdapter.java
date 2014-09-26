package org.wildstang.wildrank.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.database.DatabaseContract;
import org.wildstang.wildrank.android.dragndrop.DragNDropCursorAdapter;

public class PickListAdapter extends DragNDropCursorAdapter {

    public PickListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int handler, int moveToTop, int moveToBottom) {
        super(context, layout, cursor, from, to, handler, moveToTop, moveToBottom);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.list_item_draggable_team, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.ranking)).setText("" + (cursor.getPosition() + 1));
        ((TextView) view.findViewById(R.id.number)).setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.Team.TEAM_KEY)));
        view.findViewById(R.id.number).setFocusable(false);
        view.setFocusable(false);
        if(true){
        /*if (cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.PICK_LIST_PICKED)) == 0) {
            int tier = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Team.PICK_LIST_TIER));
            switch (tier) {
                case 1:
                    view.setBackgroundColor(context.getResources().getColor(R.color.pick_list_tier_1));
                    break;
                case 2:
                    view.setBackgroundColor(context.getResources().getColor(R.color.pick_list_tier_2));
                    break;
                case 3:
                    view.setBackgroundColor(context.getResources().getColor(R.color.pick_list_tier_3));
                    break;
                case 4:
                    view.setBackgroundColor(context.getResources().getColor(R.color.pick_list_tier_4));
                    break;
                case 5:
                    view.setBackgroundColor(context.getResources().getColor(R.color.pick_list_tier_5));
                    break;
                default:
                    view.setBackgroundColor(context.getResources().getColor(R.color.white));
                    break;
            }*/
            ((TextView) view.findViewById(R.id.ranking)).setTextColor(context.getResources().getColor(R.color.black));
            ((TextView) view.findViewById(R.id.number)).setTextColor(context.getResources().getColor(R.color.black));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.black));
            ((TextView) view.findViewById(R.id.ranking)).setTextColor(context.getResources().getColor(R.color.white));
            ((TextView) view.findViewById(R.id.number)).setTextColor(context.getResources().getColor(R.color.white));
        }
    }
}
