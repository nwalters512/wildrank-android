package org.wildstang.wildrank.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.wildstang.wildrank.R;

public class PasswordProtectionFragment extends Fragment implements OnClickListener {

	public interface IPasswordProtectionCallbacks {
		public void passwordSuccess();
	}

	private final String CONFIGURED_PASSWORD = "9453";

	public static final String COLOR_THEME = "theme";
	public static final int THEME_DARK = 1;
	public static final int THEME_LIGHT = 2;

	private Button[] buttons = new Button[9];
	private TextView entryView;
	private TextView messageView;
	private int colorTheme;

	private StringBuilder enteredPassword = new StringBuilder();

	private IPasswordProtectionCallbacks callbacks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			colorTheme = getArguments().getInt(COLOR_THEME, THEME_LIGHT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_password_protection, container, false);
		buttons[0] = (Button) view.findViewById(R.id.button1);
		buttons[1] = (Button) view.findViewById(R.id.button2);
		buttons[2] = (Button) view.findViewById(R.id.button3);
		buttons[3] = (Button) view.findViewById(R.id.button4);
		buttons[4] = (Button) view.findViewById(R.id.button5);
		buttons[5] = (Button) view.findViewById(R.id.button6);
		buttons[6] = (Button) view.findViewById(R.id.button7);
		buttons[7] = (Button) view.findViewById(R.id.button8);
		buttons[8] = (Button) view.findViewById(R.id.button9);
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setOnClickListener(this);
		}
		entryView = (TextView) view.findViewById(R.id.password);
		messageView = (TextView) view.findViewById(R.id.message);
		messageView.setText("Enter password");
		updateColorTheme();
		return view;
	}

	public void setColorTheme(int colorTheme) {
		this.colorTheme = colorTheme;
		if (getView() != null) {
			updateColorTheme();
		}
	}

	private void updateColorTheme() {
		if (colorTheme == THEME_DARK) {
			entryView.setTextColor(getResources().getColor(R.color.white));
			messageView.setTextColor(getResources().getColor(R.color.white));
		} else {
			entryView.setTextColor(getResources().getColor(R.color.black));
			messageView.setTextColor(getResources().getColor(R.color.black));
		}
	}

	public void setCallbacks(IPasswordProtectionCallbacks callbacks) {
		this.callbacks = callbacks;
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < buttons.length; i++) {
			if (v == buttons[i]) {
				enteredPassword.append(Integer.toString(i + 1));
				updatePassword();
			}
		}
	}

	private void updatePassword() {
		StringBuilder asteriks = new StringBuilder();
		for (int i = 0; i < enteredPassword.length(); i++) {
			asteriks.append('*');
		}
		entryView.setText(asteriks.toString());
		if (enteredPassword.length() == CONFIGURED_PASSWORD.length()) {
			if (enteredPassword.toString().equals(this.CONFIGURED_PASSWORD)) {
				messageView.setText("Enter password");
				enteredPassword = new StringBuilder();
				entryView.setText("");
				callbacks.passwordSuccess();
			} else {
				messageView.setText("Wrong password! Try again.");
				enteredPassword = new StringBuilder();
				entryView.setText("");
			}
		}
	}

}
