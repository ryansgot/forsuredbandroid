package com.forsuredb.testapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forsuredb.cursor.FSCursor;
import com.forsuredb.cursor.FSCursorRecyclerViewAdapter;
import com.forsuredb.cursor.FSCursorViewHolder;
import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.UserTable;

import static com.forsuredb.testapp.ForSure.userTable;

public class UserTableRecyclerAdapter extends FSCursorRecyclerViewAdapter<UserTable, UserTableRecyclerAdapter.ViewHolder> {

    private final UserTable api;

    public UserTableRecyclerAdapter() {
        super(null);
        api = userTable().getApi();
    }

    @Override
    protected UserTable api() {
        return api;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row_layout, parent);
        return new ViewHolder(v, viewType);
    }

    public static class ViewHolder extends FSCursorViewHolder {

        private TextView idText;
        private TextView globalIdText;
        private TextView loginCountText;
        private TextView appRatingText;
        private TextView competitorAppRatingText;
        private TextView createdText;
        private TextView modifiedText;
        private TextView deletedText;

        public ViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
        }

        @Override
        protected void initViewReferences(View targetLayout) {
            idText = (TextView) targetLayout.findViewById(R.id.user_id_text);
            globalIdText = (TextView) targetLayout.findViewById(R.id.user_global_id_text);
            loginCountText = (TextView) targetLayout.findViewById(R.id.login_count_text);
            appRatingText = (TextView) targetLayout.findViewById(R.id.app_rating_text);
            competitorAppRatingText = (TextView) targetLayout.findViewById(R.id.competitor_app_rating);
            createdText = (TextView) targetLayout.findViewById(R.id.user_created_text);
            modifiedText = (TextView) targetLayout.findViewById(R.id.user_modified_text);
            deletedText = (TextView) targetLayout.findViewById(R.id.user_deleted_text);
        }

        @Override
        protected void populateView(FSCursor cursor) {
            UserTable api = userTable().getApi();
            idText.setText(Long.toString(api.id(cursor)));
            globalIdText.setText(Long.toString(api.globalId(cursor)));
            loginCountText.setText(Integer.toString(api.loginCount(cursor)));
            appRatingText.setText(Double.toString(api.appRating(cursor)));
            competitorAppRatingText.setText(api.competitorAppRating(cursor).toString());
            createdText.setText(api.created(cursor).toString());
            modifiedText.setText(api.modified(cursor).toString());
            deletedText.setText(Boolean.toString(api.deleted(cursor)));
        }
    }
}
