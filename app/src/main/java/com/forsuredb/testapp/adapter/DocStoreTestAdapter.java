package com.forsuredb.testapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.DocStoreTestBase;
import com.forsuredb.testapp.model.DocStoreTestTable;
import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.cursor.FSCursorRecyclerViewAdapter;
import com.fsryan.forsuredb.cursor.FSCursorViewHolder;

import static com.forsuredb.testapp.ForSure.docStoreTestTable;

public class DocStoreTestAdapter<T extends DocStoreTestBase> extends FSCursorRecyclerViewAdapter<DocStoreTestTable, DocStoreTestAdapter.ViewHolder<T>> {

    public interface ViewHolderFactory<T extends DocStoreTestBase> {
        DocStoreTestAdapter.ViewHolder<T> create(View v, int viewType, DocStoreTestTable api, Class<T> docStoreTestBaseExtensionClass);
    }


    private final Class<T> docStoreTestBaseExtensionClass;
    private final ViewHolderFactory<T> holderFactory;
    private final DocStoreTestTable api;

    public DocStoreTestAdapter(Class<T> docStoreTestBaseExtensionClass, ViewHolderFactory<T> holderFactory) {
        super();
        this.docStoreTestBaseExtensionClass = docStoreTestBaseExtensionClass;
        this.holderFactory = holderFactory;
        api = docStoreTestTable().getApi();
    }

    @Override
    protected DocStoreTestTable api() {
        return api;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_store_test_item, parent, false);
        return holderFactory.create(v, viewType, api, docStoreTestBaseExtensionClass);
    }

    public static abstract class ViewHolder<T extends DocStoreTestBase> extends FSCursorViewHolder {

        private final DocStoreTestTable api;
        private final Class<T> docStoreTestBaseExtensionClass;
        protected TextView uuidText;
        protected TextView nameText;
        protected TextView dateText;
        protected TextView valueText;

        public ViewHolder(View itemView, int viewType, DocStoreTestTable api, Class<T> docStoreTestBaseExtensionClass) {
            super(itemView, viewType);
            this.api = api;
            this.docStoreTestBaseExtensionClass = docStoreTestBaseExtensionClass;
        }

        @Override
        protected void initViewReferences(View targetLayout) {
            uuidText = (TextView) targetLayout.findViewById(R.id.uuid_text);
            nameText = (TextView) targetLayout.findViewById(R.id.name_text);
            dateText = (TextView) targetLayout.findViewById(R.id.date_text);
            valueText = (TextView) targetLayout.findViewById(R.id.value_text);
        }

        @Override
        protected void populateView(FSCursor cursor) {
            T obj = api.getAs(docStoreTestBaseExtensionClass, cursor);
            uuidText.setText(obj.getUuid());
            nameText.setText(obj.getName());
            dateText.setText(obj.getDate().toString());
            populateRemainingViews(obj);
        }

        protected abstract void populateRemainingViews(T obj);
    }
}
