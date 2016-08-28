package com.forsuredb.testapp;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.forsuredb.testapp.adapter.DocStoreTestAdapter;
import com.forsuredb.testapp.model.DocStoreDoublePropertyExtension;
import com.forsuredb.testapp.model.DocStoreIntPropertyExtension;
import com.forsuredb.testapp.model.DocStoreTestBase;
import com.forsuredb.testapp.model.DocStoreTestTable;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.fosuredb.cursor.FSCursor;
import com.fsryan.fosuredb.cursor.FSCursorLoader;
import com.fsryan.fosuredb.cursor.FSCursorRecyclerViewAdapter;
import com.fsryan.fosuredb.cursor.FSCursorViewHolder;

import java.util.Calendar;
import java.util.UUID;

import static com.forsuredb.testapp.ForSure.docStoreTestTable;
import static com.google.common.base.Strings.nullToEmpty;

public class DocStoreTestActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private static final String LOG_TAG = DocStoreTestActivity.class.getSimpleName();
    private static final int DOC_STORE_INT_PROPERTY_EXTENSION_LOADER_ID = DocStoreTestActivity.class.hashCode();
    private static final int DOC_STORE_DOUBLE_PROPERTY_EXTENSION_LOADER_ID = DOC_STORE_INT_PROPERTY_EXTENSION_LOADER_ID + 1;

    private final Calendar currentCalendar = Calendar.getInstance();
    private Spinner typeSelectionSpinner;
    private EditText nameEntryText;
    private EditText valueEntryText;
    private Button dateButton;
    private Button timeButton;
    private Button saveButton;

    /*package*/ DocStoreTestAdapter<DocStoreIntPropertyExtension> docStoreIntPropertyExtensionAdapter;
    /*package*/ DocStoreTestAdapter<DocStoreDoublePropertyExtension> docStoreDoublePropertyExtensionAdapter;
    private DocStoreIntPropertyExtensionLoader dsIntPropertyLoader;
    private DocStoreDoublePropertyExtensionLoader dsDoublePropertyLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_store_test);

        typeSelectionSpinner = (Spinner) findViewById(R.id.type_spinner);
        valueEntryText = (EditText) findViewById(R.id.value_text_entry);
        nameEntryText = (EditText) findViewById(R.id.name_text_entry);
        initButtons();
        initRecyclers();
        dsIntPropertyLoader = new DocStoreIntPropertyExtensionLoader();
        dsDoublePropertyLoader = new DocStoreDoublePropertyExtensionLoader();
        getLoaderManager().initLoader(DOC_STORE_INT_PROPERTY_EXTENSION_LOADER_ID, null, dsIntPropertyLoader);
        getLoaderManager().initLoader(DOC_STORE_DOUBLE_PROPERTY_EXTENSION_LOADER_ID, null, dsDoublePropertyLoader);
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

    private void initButtons() {
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

    private void logResult(SaveResult<Uri> result) {
        Log.d(LOG_TAG, "SaveResult<Uri>{inserted=" + result.inserted() + ", exception=" + result.exception() + ", rowsAffected=" + result.rowsAffected() + "}");
    }

    private void initRecyclers() {
        docStoreIntPropertyExtensionAdapter = new DocStoreTestAdapter<>(DocStoreIntPropertyExtension.class, new DocStoreTestAdapter.ViewHolderFactory<DocStoreIntPropertyExtension>() {
            @Override
            public DocStoreTestAdapter.ViewHolder<DocStoreIntPropertyExtension> create(View v, int viewType, DocStoreTestTable api, Class<DocStoreIntPropertyExtension> docStoreTestBaseExtensionClass) {
                return new DocStoreTestAdapter.ViewHolder<DocStoreIntPropertyExtension>(v, viewType, api, docStoreTestBaseExtensionClass) {
                    @Override
                    protected void populateRemainingViews(DocStoreIntPropertyExtension obj) {
                        valueText.setText(Integer.toString(obj.getValue()));
                    }
                };
            }
        });
        docStoreDoublePropertyExtensionAdapter = new DocStoreTestAdapter<>(DocStoreDoublePropertyExtension.class, new DocStoreTestAdapter.ViewHolderFactory<DocStoreDoublePropertyExtension>() {
            @Override
            public DocStoreTestAdapter.ViewHolder<DocStoreDoublePropertyExtension> create(View v, int viewType, DocStoreTestTable api, Class<DocStoreDoublePropertyExtension> docStoreTestBaseExtensionClass) {
                return new DocStoreTestAdapter.ViewHolder<DocStoreDoublePropertyExtension>(v, viewType, api, docStoreTestBaseExtensionClass) {
                    @Override
                    protected void populateRemainingViews(DocStoreDoublePropertyExtension obj) {
                        valueText.setText(Double.toString(obj.getValue()));
                    }
                };
            }
        });

        initRecycler(R.id.doc_store_test_base_int_property_recycler, docStoreIntPropertyExtensionAdapter);
        initRecycler(R.id.doc_store_test_base_double_property_recycler, docStoreDoublePropertyExtensionAdapter);
    }

    private <G extends FSGetApi, VH extends FSCursorViewHolder> void initRecycler(int viewId, FSCursorRecyclerViewAdapter<G, VH> adapter) {
        RecyclerView recycler = (RecyclerView) findViewById(viewId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.scrollToPosition(0);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);
    }

    private class DocStoreIntPropertyExtensionLoader implements LoaderManager.LoaderCallbacks<FSCursor> {

        @Override
        public Loader<FSCursor> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "DocStoreIntPropertyExtensionLoader.onCreateLoader");
            return new FSCursorLoader<>(DocStoreTestActivity.this, docStoreTestTable()
                    .find()
                            .byClassName(DocStoreIntPropertyExtension.class.getName())
                            .andFinally()
                    .order()
                            .byModified(OrderBy.Order.DESC)
                            .andFinally());
        }

        @Override
        public void onLoadFinished(Loader<FSCursor> loader, FSCursor data) {
            Log.i(LOG_TAG, "DocStoreIntPropertyExtensionLoader.onLoadFinished");
            if (data != null && data.moveToFirst()) {
                docStoreIntPropertyExtensionAdapter.changeCursor(data);
            } else {
                Log.i(LOG_TAG, "data was null or empty");
            }
        }

        @Override
        public void onLoaderReset(Loader<FSCursor> loader) {
            Log.i(LOG_TAG, "DocStoreIntPropertyExtensionLoader.onLoaderReset");
        }
    }

    private class DocStoreDoublePropertyExtensionLoader implements LoaderManager.LoaderCallbacks<FSCursor> {

        @Override
        public Loader<FSCursor> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "DocStoreDoublePropertyExtensionLoader.onCreateLoader");
            return new FSCursorLoader<>(DocStoreTestActivity.this, docStoreTestTable()
                    .find()
                            .byClassName(DocStoreDoublePropertyExtension.class.getName())
                            .andFinally()
                    .order()
                            .byModified(OrderBy.Order.DESC)
                            .andFinally());
        }

        @Override
        public void onLoadFinished(Loader<FSCursor> loader, FSCursor data) {
            Log.i(LOG_TAG, "DocStoreDoublePropertyExtensionLoader.onLoadFinished");
            if (data != null && data.moveToFirst()) {
                docStoreDoublePropertyExtensionAdapter.changeCursor(data);
            } else {
                Log.i(LOG_TAG, "data was null or empty");
            }
        }

        @Override
        public void onLoaderReset(Loader<FSCursor> loader) {
            Log.i(LOG_TAG, "DocStoreDoublePropertyExtensionLoader.onLoaderReset");
        }
    }
}
