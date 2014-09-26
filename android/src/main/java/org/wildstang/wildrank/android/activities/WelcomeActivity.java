package org.wildstang.wildrank.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.wildstang.wildrank.android.R;

public class WelcomeActivity extends Activity implements View.OnClickListener {

    private Button setupFlashDrive;
    private Button loadFromConfiguredFlashDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        setupFlashDrive = (Button) findViewById(R.id.setup_flash_drive);
        setupFlashDrive.setOnClickListener(this);

        loadFromConfiguredFlashDrive = (Button) findViewById(R.id.use_configured_flash_drive);
        loadFromConfiguredFlashDrive.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.setup_flash_drive:
                startActivity(new Intent(this, SetupFlashDriveActivity.class));
                break;
            case R.id.use_configured_flash_drive:
                break;
            default:
                return;
        }
    }
}
