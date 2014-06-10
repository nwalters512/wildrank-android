package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.wildstang.wildrank.R;
import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.data.NotesData;
import org.wildstang.wildrank.android.utils.Keys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NoteFragment extends Fragment implements OnClickListener {

	private TextView existingNotes;
	private EditText newNotes;

	private int teamNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get details about the team we are scouting from the intent
		Bundle b = getArguments();
		teamNumber = b.getInt(Keys.TEAM_NUMBER, -1);

		if (teamNumber == -1) {
			throw new IllegalArgumentException("Note Fragment must be created with a valid team number");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		saveNotes();
	}

	@Override
	public void onResume() {
		super.onResume();
		loadSavedNotes();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_notes, container, false);

		// Save references to our important views
		existingNotes = (TextView) v.findViewById(R.id.existing_notes);
		newNotes = (EditText) v.findViewById(R.id.new_notes);

		return v;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			saveNotes();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void saveNotes() {
		// Only save notes when the text field is not empty
		if (newNotes.getText().toString().trim().isEmpty()) {
			return;
		}
		try {
			NotesData data = new NotesData();
			data.setTeamNumber(teamNumber);
			data.setContent(newNotes.getText().toString());
			DataManager.getInstance().saveChangedFile(getActivity(), data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadSavedNotes() {
		// Construct a NotesData object to represent this team's notes
		NotesData notes = new NotesData();
		notes.setTeamNumber(this.teamNumber);

		// Load compiled notes synced from the flashdrive
		try {
			File compiledNotes = DataManager.getDataFileFromDirectory(notes, getActivity(), DataManager.DIRECTORY_SYNCED);
			BufferedReader br = new BufferedReader(new FileReader(compiledNotes));
			StringBuilder savedText = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				savedText.append(line).append('\n');
			}
			br.close();
			if (savedText.toString().isEmpty()) {
				existingNotes.setText("No saved notes");
			} else {
				existingNotes.setText(savedText.toString().trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
			existingNotes.setText("No saved notes");
		}

		// Load previously saved notes that have not yet been synced to the flash drive or compiled
		try {
			File unintegratedNotes = DataManager.getDataFileFromDirectory(notes, getActivity(), DataManager.DIRECTORY_QUEUE);
			BufferedReader br = new BufferedReader(new FileReader(unintegratedNotes));
			StringBuilder changedText = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				changedText.append(line).append('\n');
			}
			br.close();
			if (!changedText.toString().isEmpty()) {
				newNotes.setText(changedText.toString().trim());
			}
			newNotes.setSelection(newNotes.getText().length());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.finish) {
			saveNotes();
		}
	}
}
