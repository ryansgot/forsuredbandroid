package com.forsuredb.testapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.DocStoreDoublePropertyExtension;
import com.forsuredb.testapp.model.DocStoreIntPropertyExtension;
import com.forsuredb.testapp.model.DocStoreTestBase;
import com.forsuredb.testapp.model.DocStoreTestTable;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.cursor.BaseFSCursorRecyclerAdapter;
import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.cursor.FSCursorViewHolder;

import static com.forsuredb.testapp.ForSure.docStoreTestTable;

public class DocStoreTestAdapter extends BaseFSCursorRecyclerAdapter<DocStoreTestAdapter.ViewHolder> {

    private final DocStoreTestTable api = docStoreTestTable().getApi();

    public DocStoreTestAdapter() {
        super(true);
    }

    @Override
    protected FSGetApi api() {
        return api;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_store_test_item, parent, false);
        return new ViewHolder(v, viewType, api);
    }

    public static class ViewHolder extends FSCursorViewHolder {

        private final DocStoreTestTable api;
        private TextView classNameText;
        private TextView uuidText;
        private TextView nameText;
        private TextView dateText;
        private TextView valueText;

        public ViewHolder(View itemView, int viewType, DocStoreTestTable api) {
            super(itemView, viewType);
            this.api = api;
        }

        @Override
        protected void initViewReferences(View targetLayout) {
            classNameText = (TextView) targetLayout.findViewById(R.id.class_name_text);
            uuidText = (TextView) targetLayout.findViewById(R.id.uuid_text);
            nameText = (TextView) targetLayout.findViewById(R.id.name_text);
            dateText = (TextView) targetLayout.findViewById(R.id.date_text);
            valueText = (TextView) targetLayout.findViewById(R.id.value_text);
        }

        @Override
        protected void populateView(FSCursor cursor) {
            DocStoreTestBase obj = api.get(cursor);

            classNameText.setText(obj.getClass().getSimpleName());
            uuidText.setText(obj.getUuid());
            nameText.setText(obj.getName());
            dateText.setText(obj.getDate().toString());

            if (obj instanceof DocStoreIntPropertyExtension) {
                valueText.setText(Integer.toString(((DocStoreIntPropertyExtension) obj).getValue()));
            } else {
                valueText.setText(Double.toString(((DocStoreDoublePropertyExtension) obj).getValue()));
            }
        }
    }
}
