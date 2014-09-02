package org.wildstang.wildrank.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.activities.ScoutPitActivity;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.TeamPictureData;
import org.wildstang.wildrank.android.interfaces.IScoutingFragmentHost;
import org.wildstang.wildrank.android.utils.ImageTools;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.File;

public class PitScoutingFragment extends ScoutingFragment implements OnClickListener {

    public static final String PREVIOUSLY_SAVED_DATA = "previousData";

    int teamNumber;

    public PitScoutingFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        teamNumber = getArguments().getInt(Keys.TEAM_NUMBER);
        View view = inflater.inflate(R.layout.fragment_scout_pit, container, false);
        view.findViewById(R.id.finish).setOnClickListener(this);
        Bundle b = ((IScoutingFragmentHost) getActivity()).getScoutingViewStateBundle();
        if (b != null) {
            super.restoreViewsFromBundle(b, (ViewGroup) view);
        }
        if (getArguments().getString(PREVIOUSLY_SAVED_DATA) != null) {
            restoreViewsFromJSON(getArguments().getString(PREVIOUSLY_SAVED_DATA), (ViewGroup) view);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finish) {
            ((ScoutPitActivity) getActivity()).scoutingComplete();
        }
    }

    private void loadTeamPicture() {
        TeamPictureData picture = new TeamPictureData(teamNumber);
        File image = DataManager.getDataFileFromDirectory(picture, getActivity(), DataManager.DIRECTORY_SYNCED);
        if (image.exists()) {
            ((ImageView) getView().findViewById(R.id.team_picture)).setImageBitmap(ImageTools.decodeSampledBitmapFromFile(image, 200, 200));
        }
    }

    public void notifyNewPicture() {
        loadTeamPicture();
    }

    @Override
    public void onResume() {
        super.onResume();
        NoteFragment f = new NoteFragment();
        Bundle args = new Bundle();
        args.putInt(Keys.TEAM_NUMBER, teamNumber);
        f.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.notes_container, f, "notes").commit();
        loadTeamPicture();
    }

}
