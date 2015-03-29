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
import com.forsuredb.testapp.model.UserTableApi;

public class TestUserCursorAdapter extends BaseAdapter {

    private final UserTableApi tableApi;
    private Context context;
    private Cursor cursor;

    public TestUserCursorAdapter(Context context) {
        tableApi = (UserTableApi) ForSure.getInstance().getTable("user").getTableApi();
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
                                .globalId(tableApi.globalId(cursor))
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
        private long globalId;
        private View targetLayout;

        public ViewBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ViewBuilder globalId(long globalId) {
            this.globalId = globalId;
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
                targetLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.user_row_layout, null);
            }
        }

        private void initializeView() {
            ((TextView) targetLayout.findViewById(R.id.user_id_text)).setText(Long.toString(id));
            ((TextView) targetLayout.findViewById(R.id.user_global_id_text)).setText(Long.toString(globalId));
        }
    }
}
