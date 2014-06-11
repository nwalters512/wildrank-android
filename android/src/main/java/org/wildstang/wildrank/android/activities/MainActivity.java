package org.wildstang.wildrank.android.activities;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.database.DatabaseContentProvider;
import org.wildstang.wildrank.android.fragments.MatchScoutingMainFragment;
import org.wildstang.wildrank.android.fragments.NotesMainFragment;
import org.wildstang.wildrank.android.fragments.PasswordProtectionFragment;
import org.wildstang.wildrank.android.fragments.PasswordProtectionFragment.IPasswordProtectionCallbacks;
import org.wildstang.wildrank.android.fragments.PitScoutingMainFragment;
import org.wildstang.wildrank.android.fragments.TeamSummariesMainFragment;
import org.wildstang.wildrank.android.fragments.WhiteboardFragment;
import org.wildstang.wildrank.android.tasks.TaskFragment;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo.ProgressUpdaterState;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;
import org.wildstang.wildrank.android.tasks.TaskFragmentSynchronizeWithFlashDrive;
import org.wildstang.wildrank.android.utils.Keys;

public class MainActivity extends Activity implements TaskFragment.TaskCallbacks, OnItemClickListener, OnSharedPreferenceChangeListener {

    private TaskFragmentSynchronizeWithFlashDrive synchronizeWithUSBTaskFragment;
    private ProgressDialog progress;

    private String[] modeNames = {"Match Scouting", "Pit Scouting", "Notes", "Team Summaries", "Whiteboard"};
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private PasswordProtectionFragment drawerPassword;
    private ActionBarDrawerToggle drawerToggle;

    // Keys for storing instance state
    private static final String PIT_NAVIGATION_SPINNER_POSITION = "spinner_position";
    private static final String NOTES_NAVIGATION_SPINNER_MODE = "notes_spinner_mode";
    private static final String NOTES_NAVIGATION_SPINNER_MATCH = "notes_spinner_match";
    private static final String SUMMARIES_NAVIGATION_SPINNER_MODE = "summaries_spinner_mode";
    private static final String SUMMARIES_NAVIGATION_SPINNER_MATCH = "summaries_spinner_match";
    private int pitNavSpinnerPosition = 0;
    private int notesNavSpinnerMode = 0;
    private int notesNavSpinnerMatch = 0;
    private int summariesNavSpinnerMode = 0;
    private int summariesNavSpinnerMatch = 0;

    boolean superUserMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load nav spinner state
        if (savedInstanceState != null) {
            pitNavSpinnerPosition = savedInstanceState.getInt(PIT_NAVIGATION_SPINNER_POSITION);
            notesNavSpinnerMode = savedInstanceState.getInt(NOTES_NAVIGATION_SPINNER_MODE, 0);
            notesNavSpinnerMatch = savedInstanceState.getInt(NOTES_NAVIGATION_SPINNER_MATCH, 0);
            summariesNavSpinnerMode = savedInstanceState.getInt(SUMMARIES_NAVIGATION_SPINNER_MODE, 0);
            summariesNavSpinnerMatch = savedInstanceState.getInt(SUMMARIES_NAVIGATION_SPINNER_MATCH, 0);
        }

        // Load all default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        // This tells our event details downloader to ignore non-QM matches
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Keys.DOWNLOAD_QM_ONLY, true).commit();

        // Determine if we are in Super User Mode
        superUserMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Keys.SUPER_USER_MODE, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        boolean isSetupComplete = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Keys.SETUP_COMPLETE, false);

        getActionBar().setTitle("");

        // See if any fragments were retained across an orientation change
        FragmentManager fm = getFragmentManager();
        synchronizeWithUSBTaskFragment = (TaskFragmentSynchronizeWithFlashDrive) fm.findFragmentByTag("synchronizeWithUSB");

		/*
         * If any Fragment is non-null, then it is currently being retained
		 * across a configuration change. We should allow that TaskFragment to
		 * continue whatever it was working on
		 */
        initializeNavigationDrawer();
        if (synchronizeWithUSBTaskFragment == null) {
            if (!isSetupComplete) {
                Intent i = new Intent(this, SetupActivity.class);
                startActivityForResult(i, SetupActivity.REQUEST_CODE_FINISHED);
            } else {
                loadConfiguredModeFragment();
            }
        }
    }

    /*
     * Dismisses any ProgressDialog that is currently showing
     */
    @Override
    public void onPause() {
        super.onPause();
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (superUserMode) {
            enableSuperUserMode();
        } else {
            disableSuperUserMode();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PIT_NAVIGATION_SPINNER_POSITION, pitNavSpinnerPosition);
        outState.putInt(NOTES_NAVIGATION_SPINNER_MODE, notesNavSpinnerMode);
        outState.putInt(NOTES_NAVIGATION_SPINNER_MATCH, notesNavSpinnerMatch);
        outState.putInt(SUMMARIES_NAVIGATION_SPINNER_MODE, summariesNavSpinnerMode);
        outState.putInt(SUMMARIES_NAVIGATION_SPINNER_MATCH, summariesNavSpinnerMatch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_settings:
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.action_synchronize_with_flash_drive:
                this.synchronizeWithFlashDrive();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Keys.SUPER_USER_MODE)) {
            superUserMode = sharedPreferences.getBoolean(key, false);
        }
    }

    @Override
    public void onPreExecute(TaskType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProgressUpdate(TaskType type, ProgressUpdateInfo info) {
        if (progress == null) {
            progress = new ProgressDialog(this);
            progress.setCancelable(false);
            progress.setTitle("Loading...");
            progress.setMessage(info.message);
        }
        if (type == TaskType.TASK_SYNCHRONIZE_WITH_FLASH_DRIVE) {
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        if (info.state == ProgressUpdaterState.COMPLETE) {
            progress.dismiss();
            progress = null;
        } else {
            progress.show();
        }

    }

    @Override
    public void onCancelled(TaskType type) {
        // Nothing to do here
    }

    @Override
    public void onPostExecute(TaskType type) {
        if (type == TaskType.TASK_SYNCHRONIZE_WITH_FLASH_DRIVE) {
            DataManager.prepareForEject();
            startActivity(new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
            Toast.makeText(this, "Scroll down, press \"Unmount\", press back button.", Toast.LENGTH_LONG).show();
            resetFragments();
        }
    }

    /*
     * Displays a warning if the flash drive is not connected and prompts the
     * user to connect one
     */
    private void displayFlashDriveWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set title
        alertDialogBuilder.setTitle("Connect a Flash Drive");

        // Set dialog message
        alertDialogBuilder
                .setMessage(
                        "Please connect a flash drive and try again.\n\nIf the flash drive is already conntected and you still can't sync, you may have to manually remount. Click \"Open Settings\" below, scroll to the bottom, and unmount and remount the USB storage device. Then press the back button.")
                .setCancelable(false).setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                synchronizeWithFlashDrive();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).setNeutralButton("Open settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
            }
        });
        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Show it
        alertDialog.show();
    }

    private void resetFragments() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (fm.findFragmentByTag("synchronizeWithUSB") != null) {
            ft.remove(fm.findFragmentByTag("synchronizeWithUSB"));
        }
        ft.commit();
    }

    /*
     * Initializes a TaskFragment that synchronizes internal storage with the
     * flash drive. Used to load any previously collected scouting/match data
     * from the flash drive after loading match and pit lists
     */
    private void synchronizeWithFlashDrive() {
        resetFragments();
        if (DataManager.isFlashDriveConnected(this)) {
            FragmentManager fm = getFragmentManager();
            synchronizeWithUSBTaskFragment = new TaskFragmentSynchronizeWithFlashDrive();
            fm.beginTransaction().add(synchronizeWithUSBTaskFragment, "synchronizeWithUSB").commit();
        } else {
            displayFlashDriveWarning();
        }

    }

	/* Navigation Drawer stuff */

    private void initializeNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerPassword = (PasswordProtectionFragment) getFragmentManager().findFragmentById(R.id.password_protection);
        drawerPassword.setCallbacks(new IPasswordProtectionCallbacks() {

            @Override
            public void passwordSuccess() {
                drawerList.setVisibility(View.VISIBLE);

            }
        });
        drawerPassword.setColorTheme(PasswordProtectionFragment.THEME_DARK);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_drawer_item, modeNames));
        // Set the list's click listener
        drawerList.setOnItemClickListener(this);
        // Add the toggle to the Action Bar
        drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                drawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely open
             * state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.app_name);
            }

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!superUserMode) {
                    drawerList.setVisibility(View.GONE);
                }
                if (drawerList.getCheckedItemCount() > 0) {
                    getActionBar().setTitle(modeNames[drawerList.getCheckedItemPosition()]);
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    private void enableSuperUserMode() {
        superUserMode = true;
        if (drawerPassword != null) {
            getFragmentManager().beginTransaction().hide(drawerPassword).commit();
        }
        if (drawerList != null) {
            drawerList.setVisibility(View.VISIBLE);
        }
    }

    private void disableSuperUserMode() {
        superUserMode = false;
        if (drawerPassword != null) {
            getFragmentManager().beginTransaction().show(drawerPassword).commit();
        }
        if (drawerList != null) {
            drawerList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = null;
        int tabletMode = -1;
        switch (position) {
            case 0:
                //Match scouting
                fragment = new MatchScoutingMainFragment();
                resetActionBar();
                tabletMode = Keys.TABLET_MODE_MATCH;
                break;
            case 1:
                // Pit scouting
                fragment = new PitScoutingMainFragment();
                resetActionBar();
                setupActionBarForPit();
                tabletMode = Keys.TABLET_MODE_PIT;
                break;
            case 2:
                // Notes
                fragment = new NotesMainFragment();
                resetActionBar();
                setupActionBarForNotes();
                tabletMode = Keys.TABLET_MODE_NOTES;
                break;
            case 3:
                // Team summaries
                fragment = new TeamSummariesMainFragment();
                resetActionBar();
                setupActionBarForSummaries();
                tabletMode = Keys.TABLET_MODE_TEAM_SUMMARIES;
                break;
            case 4:
                // Team summaries
                fragment = new WhiteboardFragment();
                resetActionBar();
                tabletMode = Keys.TABLET_MODE_WHITEBOARD;
                break;
        }

        if (tabletMode != -1) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Keys.TABLET_MODE, tabletMode).commit();
        }

        if (fragment != null) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "mainFragment").commit();
            setTitle(modeNames[position]);
            drawerList.setItemChecked(position, true);

        }
        drawerLayout.closeDrawer(findViewById(R.id.left_drawer));
    }

    private void setupActionBarForPit() {
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        final String[] dropdownValues = {"All teams", "Assigned teams"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, dropdownValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                ((PitScoutingMainFragment) getFragmentManager().findFragmentByTag("mainFragment")).onNavigationItemSelected(itemPosition, itemId);
                pitNavSpinnerPosition = itemPosition;
                return true;
            }

        });
        actionBar.setSelectedNavigationItem(pitNavSpinnerPosition);
    }

    private void setupActionBarForNotes() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        // Manually set layout params so we don't overlap the title
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        View customNav = LayoutInflater.from(actionBar.getThemedContext()).inflate(R.layout.action_bar_filter_by_team, null); // layout which contains your button.
        actionBar.setCustomView(customNav, lp);
        Spinner notesMode = (Spinner) actionBar.getCustomView().findViewById(R.id.mode);
        Spinner matchNumber = (Spinner) actionBar.getCustomView().findViewById(R.id.match_number);

        // Set up spinner for notes mode
        final String[] modes = {"All teams", "Teams from match"};
        ArrayAdapter<String> modesAdapter = new ArrayAdapter<>(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, modes);
        modesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notesMode.setAdapter(modesAdapter);
        notesMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                notesNavSpinnerMode = position;
                notesModeSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        notesMode.setSelection(notesNavSpinnerMode);

        // Set up spinner for match number
        // Get number of matches
        String eventKey = PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.CONFIGURED_EVENT, "null");
        Cursor countCursor = getContentResolver().query(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/" + eventKey + "/match"), new String[]{"count(*) AS count"}, null, null,
                null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        String[] matchNumberList = new String[count];
        // Construct list of match numbers
        for (int i = 0; i < matchNumberList.length; i++) {
            matchNumberList[i] = "" + (i + 1);
        }
        ArrayAdapter<String> matchNumberAdapter = new ArrayAdapter<>(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, matchNumberList);
        matchNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matchNumber.setAdapter(matchNumberAdapter);
        matchNumber.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                notesNavSpinnerMatch = position;
                notesMatchSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        matchNumber.setSelection(notesNavSpinnerMatch);

    }

    private void notesModeSelected(int spinnerPosition) {
        Spinner matchNumber = (Spinner) getActionBar().getCustomView().findViewById(R.id.match_number);
        switch (spinnerPosition) {
            case 0:
                matchNumber.setVisibility(View.INVISIBLE);
                if (!(getFragmentManager().findFragmentByTag("mainFragment") instanceof NotesMainFragment)) {
                    Fragment fragment = new NotesMainFragment();
                    getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "mainFragment").commit();
                }
                ((NotesMainFragment) getFragmentManager().findFragmentByTag("mainFragment")).setMode(NotesMainFragment.MODE_ALL).update();
                break;
            case 1:
                matchNumber.setVisibility(View.VISIBLE);
                notesMatchSelected(notesNavSpinnerMatch);
                break;
        }
    }

    private void notesMatchSelected(int spinnerPosition) {
        if (((Spinner) getActionBar().getCustomView().findViewById(R.id.mode)).getSelectedItemPosition() != 1) {
            return;
        }
        int matchNumber = spinnerPosition + 1;
        if (!(getFragmentManager().findFragmentByTag("mainFragment") instanceof NotesMainFragment)) {
            Fragment fragment = new NotesMainFragment();
            Bundle args = new Bundle();
            args.putInt(NotesMainFragment.NOTES_MODE, NotesMainFragment.MODE_CURRENT_MATCH);
            args.putInt(NotesMainFragment.MATCH_NUMBER, matchNumber);
            fragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "mainFragment").commit();
        } else {
            ((NotesMainFragment) getFragmentManager().findFragmentByTag("mainFragment")).setMode(NotesMainFragment.MODE_CURRENT_MATCH).setMatchNumber(matchNumber).update();
        }
    }

    private void setupActionBarForSummaries() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        // Manually set layout params so we don't overlap the title
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        View customNav = LayoutInflater.from(actionBar.getThemedContext()).inflate(R.layout.action_bar_filter_by_team, null); // layout which contains your button.
        actionBar.setCustomView(customNav, lp);
        Spinner mode = (Spinner) actionBar.getCustomView().findViewById(R.id.mode);
        Spinner matchNumber = (Spinner) actionBar.getCustomView().findViewById(R.id.match_number);

        // Set up spinner for summaries mode
        final String[] modes = {"All teams", "Teams from match"};
        ArrayAdapter<String> modesAdapter = new ArrayAdapter<>(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, modes);
        modesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mode.setAdapter(modesAdapter);
        mode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                summariesNavSpinnerMode = position;
                summariesModeSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mode.setSelection(summariesNavSpinnerMode);

        // Set up spinner for match number
        // Get number of matches
        String eventKey = PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.CONFIGURED_EVENT, "null");
        Cursor countCursor = getContentResolver().query(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/" + eventKey + "/match"), new String[]{"count(*) AS count"}, null, null,
                null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        String[] matchNumberList = new String[count];
        // Construct list of match numbers
        for (int i = 0; i < matchNumberList.length; i++) {
            matchNumberList[i] = "" + (i + 1);
        }
        ArrayAdapter<String> matchNumberAdapter = new ArrayAdapter<>(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, matchNumberList);
        matchNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matchNumber.setAdapter(matchNumberAdapter);
        matchNumber.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                summariesNavSpinnerMatch = position;
                summariesMatchSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        matchNumber.setSelection(summariesNavSpinnerMatch);

    }

    private void summariesModeSelected(int spinnerPosition) {
        Spinner matchNumber = (Spinner) getActionBar().getCustomView().findViewById(R.id.match_number);
        switch (spinnerPosition) {
            case 0:
                matchNumber.setVisibility(View.INVISIBLE);
                if (!(getFragmentManager().findFragmentByTag("mainFragment") instanceof TeamSummariesMainFragment)) {
                    Fragment fragment = new TeamSummariesMainFragment();
                    getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "mainFragment").commit();
                }
                ((TeamSummariesMainFragment) getFragmentManager().findFragmentByTag("mainFragment")).setMode(TeamSummariesMainFragment.MODE_ALL).update();
                break;
            case 1:
                Log.d("summariesModeSelected", "should be visible");
                matchNumber.setVisibility(View.VISIBLE);
                summariesMatchSelected(summariesNavSpinnerMatch);
                break;
        }
    }

    private void summariesMatchSelected(int spinnerPosition) {
        if (((Spinner) getActionBar().getCustomView().findViewById(R.id.mode)).getSelectedItemPosition() != 1) {
            return;
        }
        int matchNumber = spinnerPosition + 1;
        if (!(getFragmentManager().findFragmentByTag("mainFragment") instanceof TeamSummariesMainFragment)) {
            Fragment fragment = new TeamSummariesMainFragment();
            Bundle args = new Bundle();
            args.putInt(TeamSummariesMainFragment.SUMMARIES_MODE, TeamSummariesMainFragment.MODE_CURRENT_MATCH);
            args.putInt(TeamSummariesMainFragment.MATCH_NUMBER, matchNumber);
            fragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "mainFragment").commit();
        } else {
            ((TeamSummariesMainFragment) getFragmentManager().findFragmentByTag("mainFragment")).setMode(TeamSummariesMainFragment.MODE_CURRENT_MATCH).setMatchNumber(matchNumber).update();
        }
    }

    private void resetActionBar() {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActionBar().setDisplayShowCustomEnabled(false);
    }

    private void setTitle(String title) {
        getActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupActivity.REQUEST_CODE_FINISHED) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                // Mark that setup is complete
                prefs.edit().putBoolean(Keys.SETUP_COMPLETE, true).commit();

                // Initialize navigation drawer stuff
                initializeNavigationDrawer();

                loadConfiguredModeFragment();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.finish();
            }
        } else if (requestCode == MatchScoutingMainFragment.MATCH_SCOUTING_FINISHED && resultCode == MatchScoutingMainFragment.MATCH_SCOUTING_SUCCESSFUL) {
            Fragment fragment = getFragmentManager().findFragmentByTag("mainFragment");
            if (fragment != null && fragment instanceof MatchScoutingMainFragment) {
                ((MatchScoutingMainFragment) fragment).advanceToNextMatch();
            }
        }
    }

    private void loadConfiguredModeFragment() {
        int tabletMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(Keys.TABLET_MODE, Keys.TABLET_MODE_MATCH);
        Fragment fragment;
        switch (tabletMode) {
            case Keys.TABLET_MODE_MATCH:
                fragment = new MatchScoutingMainFragment();
                resetActionBar();
                break;
            case Keys.TABLET_MODE_PIT:
                fragment = new PitScoutingMainFragment();
                resetActionBar();
                setupActionBarForPit();
                break;
            case Keys.TABLET_MODE_NOTES:
                fragment = new NotesMainFragment();
                resetActionBar();
                setupActionBarForNotes();
                break;
            case Keys.TABLET_MODE_TEAM_SUMMARIES:
                fragment = new TeamSummariesMainFragment();
                resetActionBar();
                setupActionBarForSummaries();
                break;
            case Keys.TABLET_MODE_WHITEBOARD:
                fragment = new WhiteboardFragment();
                resetActionBar();
                break;
            default:
                fragment = new MatchScoutingMainFragment();
                resetActionBar();
                break;
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "mainFragment").commit();
        setTitle(modeNames[tabletMode - 1]);
        drawerList.setItemChecked(tabletMode - 1, true);
    }
}
