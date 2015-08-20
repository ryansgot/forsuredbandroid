package com.forsuredb.testapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.forsuredb.FSTableDescriber;
import com.forsuredb.ForSure;
import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.UserTable;

import java.math.BigDecimal;

public class TestUserCursorAdapter extends BaseAdapter {

    private final FSTableDescriber table;
    private Context context;
    private Cursor cursor;

    public TestUserCursorAdapter(Context context) {
        table = ForSure.inst().getTable("user");
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
        UserTable api = ForSure.inst().getApi(table.getAllRecordsUri());
        return new ViewBuilder().id(api.id(cursor))
                                .globalId(api.globalId(cursor))
                                .loginCount(api.loginCount(cursor))
                                .appRating(api.appRating(cursor))
                                .competitorAppRating(api.competitorAppRating(cursor))
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
        private int loginCount;
        private double appRating;
        private BigDecimal competitorAppRating;
        private View targetLayout;

        public ViewBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ViewBuilder globalId(long globalId) {
            this.globalId = globalId;
            return this;
        }

        public ViewBuilder loginCount(int loginCount) {
            this.loginCount = loginCount;
            return this;
        }

        public ViewBuilder appRating(double appRating) {
            this.appRating = appRating;
            return this;
        }

        public ViewBuilder competitorAppRating(BigDecimal competitorAppRating) {
            this.competitorAppRating = competitorAppRating;
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
            ((TextView) targetLayout.findViewById(R.id.login_count_text)).setText(Integer.toString(loginCount));
            ((TextView) targetLayout.findViewById(R.id.app_rating_text)).setText(Double.toString(appRating));
            ((TextView) targetLayout.findViewById(R.id.competitor_app_rating)).setText(competitorAppRating.toString());
        }
    }
}
