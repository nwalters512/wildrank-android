package org.wildstang.wildrank.android.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONObject;
import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.customviews.TeamDetailsView;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.PitData;
import org.wildstang.wildrank.android.data.TeamPictureData;
import org.wildstang.wildrank.android.fragments.PitScoutingFragment;
import org.wildstang.wildrank.android.utils.JSONBundleTools;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ScoutPitActivity extends ScoutingActivity {

    private PitScoutingFragment pitFragment;
    private TeamDetailsView teamDetails;

    private int teamNumber;
    private String teamName;
    private long teamID;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private File imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scout_pit);
        // Show the Up button in the action bar.
        setupActionBar();

        // Keep the screen on while we are scouting
        findViewById(android.R.id.content).setKeepScreenOn(true);

        Intent i = getIntent();
        teamNumber = i.getIntExtra(Keys.TEAM_NUMBER, -1);
        teamName = i.getStringExtra(Keys.TEAM_NAME);
        teamID = i.getLongExtra(Keys.TEAM_ID, -1);

        if (teamName == null || teamNumber == -1 || teamID == -1) {
            throw new IllegalArgumentException("ScoutPitActivity must be created with a valid match key, team ID, and team number");
        }

        teamDetails = (TeamDetailsView) findViewById(R.id.team_details);
        teamDetails.setTeamName(teamName);
        teamDetails.setTeamNumber(teamNumber);

        pitFragment = new PitScoutingFragment();
        Bundle args = new Bundle();
        args.putInt(Keys.TEAM_NUMBER, teamNumber);

        // Try to load previously saved data
        try {
            PitData data = new PitData(teamNumber);
            if (DataManager.loadDataIfExists(data, this, DataManager.DIRECTORY_QUEUE)) {
                args.putString(PitScoutingFragment.PREVIOUSLY_SAVED_DATA, data.getContent());
            } else if (DataManager.loadDataIfExists(data, this, DataManager.DIRECTORY_SYNCED)) {
                args.putString(PitScoutingFragment.PREVIOUSLY_SAVED_DATA, data.getContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pitFragment.setArguments(args);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.scouting_fragment, pitFragment, "pitFragment");
        ft.commit();
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.activity_scout_pit);
    }

    public void scoutingComplete() {
        // To be called by PostMatchScoutingFragment when scouting is complete

        pitFragment.writeContentsToBundle(scoutingViewsState, (ViewGroup) pitFragment.getView());

        if (!pitFragment.isComplete()) {
            Toast.makeText(this, "Some required fields are blank! Please fill in the fields highlighted in red.", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle hierarchialBundle = JSONBundleTools.createHierarchicalBundle(scoutingViewsState);
        Log.d("Bundle", hierarchialBundle.toString());
        JSONObject scoringJson = JSONBundleTools.writeBundleToJSONObject(hierarchialBundle);
        JSONObject completeJson = new JSONObject();
        try {
            completeJson.put("team_number", teamNumber);
            completeJson.put("team_name", teamName);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String scouterName = prefs.getString(Keys.SCOUTER_NAME, null);
            completeJson.put("scouter_id", scouterName);
            completeJson.put("scoring", scoringJson);
            Log.d("bundle", completeJson.toString(4));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            PitData data = new PitData();
            data.setTeamNumber(teamNumber);
            data.setContent(completeJson.toString(4));
            DataManager.getInstance().saveChangedFile(this, data);
            Toast.makeText(this, "Pit data saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Pit data saved successfullyError saving pit data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scout_pit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_take_picture:
                try {
                    takePicture();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void takePicture() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String imageFileName = teamNumber + ".jpg";
        File image = new File(storageDir + File.separator + imageFileName);
        imageFilePath = image;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Copy captured picture to private app storage
            InputStream inStream;
            OutputStream outStreamSynced;
            OutputStream outStreamQueued;
            File source = imageFilePath;
            TeamPictureData picture = new TeamPictureData(teamNumber);
            File syncedDestination = DataManager.getDataFileFromDirectory(picture, this, DataManager.DIRECTORY_SYNCED);
            File queuedDestination = DataManager.getDataFileFromDirectory(picture, this, DataManager.DIRECTORY_QUEUE);
            syncedDestination.getParentFile().mkdirs();
            queuedDestination.getParentFile().mkdirs();
            try {

                inStream = new FileInputStream(source);
                outStreamSynced = new FileOutputStream(syncedDestination);
                outStreamQueued = new FileOutputStream(queuedDestination);

                byte[] buffer = new byte[1024];
                while (inStream.read(buffer) > 0) {
                    outStreamSynced.write(buffer);
                    outStreamQueued.write(buffer);
                }
                inStream.close();
                outStreamSynced.flush();
                outStreamSynced.close();
                outStreamQueued.flush();
                outStreamQueued.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pitFragment.notifyNewPicture();
        }
    }
}
