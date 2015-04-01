package com.forsuredb.testapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.forsuredb.ForSure;
import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.ProfileInfoTableApi;

public class TestProfileInfoCursorAdapter extends BaseAdapter {

    private final ProfileInfoTableApi tableApi;
    private Context context;
    private Cursor cursor;

    public TestProfileInfoCursorAdapter(Context context) {
        tableApi = ForSure.getInstance().getTableApi(ProfileInfoTableApi.class);
        this.context = context;
        cursor = null;
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return cursor == null || getCount() <= position ? -1L : position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }
        return new ViewBuilder().id(tableApi.id(cursor))
                                .userId(tableApi.userId(cursor))
                                .emalAddress(tableApi.emailAddress(cursor))
                                .targetLayout(view)
                                .build(context);
    }

    public void changeCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    private class ViewBuilder {

        private long id;
        private long userId;
        private String emailAddress;
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
        }
    }
}
