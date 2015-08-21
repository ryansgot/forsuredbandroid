package com.forsuredb.testapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.forsuredb.FSCursor;
import com.forsuredb.FSTableDescriber;
import com.forsuredb.ForSure;
import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.ProfileInfoTable;

import java.util.Arrays;

public class TestProfileInfoCursorAdapter extends BaseAdapter {

    private final FSTableDescriber table;
    private Context context;
    private FSCursor retriever;

    public TestProfileInfoCursorAdapter(Context context) {
        table = ForSure.inst().getTable("profile_info");
        this.context = context;
        retriever = null;
    }

    @Override
    public int getCount() {
        return retriever == null ? 0 : retriever.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return retriever == null || getCount() <= position ? -1L : position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (retriever == null || !retriever.moveToPosition(position)) {
            return null;
        }
        final ProfileInfoTable api = ForSure.inst().getApi(table.getAllRecordsUri());
        return new ViewBuilder().id(api.id(retriever))
                                .userId(api.userId(retriever))
                                .emalAddress(api.emailAddress(retriever))
                                .binaryData(api.binaryData(retriever))
                                .targetLayout(view)
                                .build(context);
    }

    public void changeCursor(Cursor newCursor) {
        if (retriever != null) {
            retriever.close();
        }
        retriever = new FSCursor(newCursor);
        notifyDataSetChanged();
    }

    private class ViewBuilder {

        private long id;
        private long userId;
        private String emailAddress;
        private byte[] binaryData;
        private View targetLayout;

        public ViewBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ViewBuilder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public ViewBuilder emalAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public ViewBuilder binaryData(byte[] binaryData) {
            this.binaryData = binaryData;
            return this;
        }

        public ViewBuilder targetLayout(View targetLayout) {
            this.targetLayout = targetLayout;
            return this;
        }

        public View build(Context context) {
            ensureTargetLayoutInflated(context);
            initializeView();
            return targetLayout;
        }

        private void ensureTargetLayoutInflated(Context context) {
            if (targetLayout == null) {
                targetLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.profile_info_layout, null);
            }
        }

        private void initializeView() {
            ((TextView) targetLayout.findViewById(R.id.profile_info_id_text)).setText(Long.toString(id));
            ((TextView) targetLayout.findViewById(R.id.foreign_user_id_text)).setText(Long.toString(userId));
            ((TextView) targetLayout.findViewById(R.id.email_address_text)).setText(emailAddress);
            ((TextView) targetLayout.findViewById(R.id.binary_data_text)).setText(Arrays.toString(binaryData));
        }
    }
}
