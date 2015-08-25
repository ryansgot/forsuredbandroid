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
import com.forsuredb.testapp.model.UserTable;
import com.google.common.base.Strings;

import java.math.BigDecimal;

public class TestUserCursorAdapter extends BaseAdapter {

    private final FSTableDescriber table;
    private Context context;
    private FSCursor retriever;

    public TestUserCursorAdapter(Context context) {
        table = ForSure.inst().getTable("user");
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
        UserTable api = ForSure.inst().getApi(table.getAllRecordsUri());
        return new ViewBuilder().id(api.id(retriever))
                                .globalId(api.globalId(retriever))
                                .loginCount(api.loginCount(retriever))
                                .appRating(api.appRating(retriever))
                                .competitorAppRating(api.competitorAppRating(retriever))
                                .created(api.created(retriever).toString())
                                .modified(api.modified(retriever).toString())
                                .deleted(api.deleted(retriever))
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
        private long globalId;
        private int loginCount;
        private double appRating;
        private BigDecimal competitorAppRating;
        private View targetLayout;
        private String created;
        private String modified;
        private boolean deleted;

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

        public ViewBuilder created(String created) {
            this.created = created;
            return this;
        }

        public ViewBuilder modified(String modified) {
            this.modified = modified;
            return this;
        }

        public ViewBuilder deleted(boolean deleted) {
            this.deleted = deleted;
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
            ((TextView) targetLayout.findViewById(R.id.user_created_text)).setText(Strings.nullToEmpty(created));
            ((TextView) targetLayout.findViewById(R.id.user_modified_text)).setText(Strings.nullToEmpty(modified));
            ((TextView) targetLayout.findViewById(R.id.user_deleted_text)).setText(Boolean.toString(deleted));
        }
    }
}
