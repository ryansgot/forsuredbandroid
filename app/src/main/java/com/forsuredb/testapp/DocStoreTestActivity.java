package com.forsuredb.testapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.forsuredb.testapp.model.DocStoreDoublePropertyExtension;
import com.forsuredb.testapp.model.DocStoreIntPropertyExtension;
import com.forsuredb.testapp.model.DocStoreTestBase;
import com.fsryan.forsuredb.api.SaveResult;

import java.util.Calendar;
import java.util.UUID;

import static com.forsuredb.testapp.ForSure.docStoreTestTable;
import static com.google.common.base.Strings.nullToEmpty;

public class DocStoreTestActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private static final String LOG_TAG = DocStoreTestActivity.class.getSimpleName();

    private final Calendar currentCalendar = Calendar.getInstance();
    private Spinner typeSelectionSpinner;
    private EditText nameEntryText;
    private EditText valueEntryText;
    private Button dateButton;
    private Button timeButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_store_test);

        typeSelectionSpinner = (Spinner) findViewById(R.id.type_spinner);
        valueEntryText = (EditText) findViewById(R.id.value_text_entry);
        nameEntryText = (EditText) findViewById(R.id.name_text_entry);
        dateButton = (Button) findViewById(R.id.date_button);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = currentCalendar.get(Calendar.YEAR);
                int month = currentCalendar.get(Calendar.MONTH);
                int dom = currentCalendar.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(DocStoreTestActivity.this, DocStoreTestActivity.this, year, month, dom).show();
            }
        });
        timeButton = (Button) findViewById(R.id.time_button);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
                int min = currentCalendar.get(Calendar.MINUTE);
                new TimePickerDialog(DocStoreTestActivity.this, DocStoreTestActivity.this, hour, min, false).show();
            }
        });
        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logResult(docStoreTestTable().set()
                        .object(createNewFromStateAndUi())
                        .save());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        menu.removeItem(R.id.action_test_doc_store);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_test_relational:
                startActivity(new Intent(this, TestActivity.class));
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeButton.setText(hourOfDay + ":" + minute);
        currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        currentCalendar.set(Calendar.MINUTE, minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        dateButton.setText(year + "/" + monthOfYear + "/" + dayOfMonth);
        currentCalendar.set(Calendar.YEAR, year);
        currentCalendar.set(Calendar.MONTH, monthOfYear);
        currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    private DocStoreTestBase createNewFromStateAndUi() {
        switch (typeSelectionSpinner.getSelectedItemPosition()) {
            case 0: return createDocStoreIntExtensionFromStateAndUi();
            case 1: return createDocStoreDoubleExtensionFromStateAndUi();
        }
        return null;
    }

    private DocStoreTestBase createDocStoreIntExtensionFromStateAndUi() {
        String valueStr = nullToEmpty(valueEntryText.getText().toString());
        String nameStr = nullToEmpty(nameEntryText.getText().toString());
        int value = 0;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Cannot create integer from " + valueStr, Toast.LENGTH_LONG).show();
        }
        return new DocStoreIntPropertyExtension(UUID.randomUUID().toString(), nameStr, currentCalendar.getTime(), value);
    }

    private DocStoreTestBase createDocStoreDoubleExtensionFromStateAndUi() {
        String valueStr = nullToEmpty(valueEntryText.getText().toString());
        String nameStr = nullToEmpty(nameEntryText.getText().toString());
        double value = 0;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Cannot create double from " + valueStr, Toast.LENGTH_LONG).show();
        }
        return new DocStoreDoublePropertyExtension(UUID.randomUUID().toString(), nameStr, currentCalendar.getTime(), value);
    }

    private void logResult(SaveResult<Uri> result) {
        Log.d(LOG_TAG, "SaveResult<Uri>{inserted=" + result.inserted() + ", exception=" + result.exception() + ", rowsAffected=" + result.rowsAffected() + "}");
    }
}
