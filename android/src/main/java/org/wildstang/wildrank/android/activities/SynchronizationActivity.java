package org.wildstang.wildrank.android.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.database.ConflictResolutionResult;
import org.wildstang.wildrank.android.database.ConflictResolver;

public class SynchronizationActivity extends Activity implements View.OnClickListener {

    TextView localChangesText;
    TextView localChangesDesiredOperation;
    TextView externalChangesText;
    TextView externalChangesDesiredOperation;
    Button acceptLocalChanges;
    Button acceptExternalChanges;

    private UserConflictResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronization);

        localChangesText = (TextView) findViewById(R.id.local_body);
        localChangesDesiredOperation = (TextView) findViewById(R.id.requested_local_operation);

        externalChangesText = (TextView) findViewById(R.id.incoming_body);
        externalChangesDesiredOperation = (TextView) findViewById(R.id.local_body);

        acceptLocalChanges = (Button) findViewById(R.id.accept_local_changes);
        acceptExternalChanges = (Button) findViewById(R.id.accept_incoming_changes);

        acceptLocalChanges.setOnClickListener(this);
        acceptExternalChanges.setOnClickListener(this);

        acceptLocalChanges.setEnabled(false);
        acceptExternalChanges.setEnabled(false);

        resolver = new UserConflictResolver();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.synchronization, menu);
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

    public void onConflictResolutionRequested(Cursor originalRecord, String requestedOriginalOperation, Cursor externalRecord, String changeSinceLastSync) {
        localChangesText.setText(cursorToText(originalRecord));
        localChangesDesiredOperation.setText(requestedOriginalOperation);

        externalChangesText.setText(cursorToText(externalRecord));
        externalChangesDesiredOperation.setText(changeSinceLastSync);
    }

    private String cursorToText(Cursor c) {
        c.moveToPosition(0);
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < c.getColumnCount(); i++) {
            text.append(c.getColumnName(i) + "\n");
            switch (c.getType(i)) {
                case Cursor.FIELD_TYPE_FLOAT:
                    text.append(c.getFloat(i) + (i == c.getColumnCount() - 1 ? "" : "\n\n"));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    text.append(c.getInt(i) + (i == c.getColumnCount() - 1 ? "" : "\n\n"));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    text.append(c.getString(i) + (i == c.getColumnCount() - 1 ? "" : "\n\n"));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    text.append(c.getBlob(i) + (i == c.getColumnCount() - 1 ? "" : "\n\n"));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    text.append("Null" + (i == c.getColumnCount() - 1 ? "" : "\n\n"));
                    break;
            }
        }

        return text.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accept_incoming_changes:
                resolver.putConflictResolutionResult(ConflictResolutionResult.ResolutionStrategy.ACCEPT_INCOMING);
                break;
            case R.id.accept_local_changes:
                resolver.putConflictResolutionResult(ConflictResolutionResult.ResolutionStrategy.ACCEPT_ORIGINAL);
                break;
            default:
                return;
        }
    }

    private class UserConflictResolver extends ConflictResolver {

        @Override
        public void requestConflictResolution(Cursor originalRecord, String requestedOriginalOperation, Cursor externalRecord, String changeSinceLastSync) {
            SynchronizationActivity.this.onConflictResolutionRequested(originalRecord, requestedOriginalOperation, externalRecord, changeSinceLastSync);
        }
    }
}
