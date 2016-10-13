package com.forsuredb.testapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.cursor.FSCursorRecyclerViewAdapter;
import com.fsryan.forsuredb.cursor.FSCursorViewHolder;
import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.ProfileInfoTable;

import java.util.Arrays;

import static com.forsuredb.testapp.ForSure.profileInfoTable;
import static com.google.common.base.Strings.nullToEmpty;

public class ProfileInfoTableRecyclerAdapter extends FSCursorRecyclerViewAdapter<ProfileInfoTable, ProfileInfoTableRecyclerAdapter.ViewHolder> {

    private final ProfileInfoTable api;

    public ProfileInfoTableRecyclerAdapter() {
        super();
        api = profileInfoTable().getApi();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_info_layout, parent, false);
        return new ViewHolder(v, viewType);
    }

    @Override
    protected ProfileInfoTable api() {
        return api;
    }

    public static class ViewHolder extends FSCursorViewHolder {

        private TextView idText;
        private TextView userIdText;
        private TextView emailAddressText;
        private TextView binaryDataText;
        private TextView createdText;
        private TextView modifiedText;
        private TextView deletedText;
        private TextView awesomeText;

        public ViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
        }

        @Override
        protected void initViewReferences(View view) {
            idText = (TextView) view.findViewById(R.id.profile_info_id_text);
            userIdText = (TextView) view.findViewById(R.id.foreign_user_id_text);
            emailAddressText = (TextView) view.findViewById(R.id.email_address_text);
            binaryDataText = (TextView) view.findViewById(R.id.binary_data_text);
            createdText = (TextView) view.findViewById(R.id.profile_info_created_text);
            modifiedText = (TextView) view.findViewById(R.id.profile_info_modified_text);
            deletedText = (TextView) view.findViewById(R.id.profile_info_deleted_text);
            awesomeText = (TextView) view.findViewById(R.id.awesome_text);
        }

        @Override
        protected void populateView(FSCursor cursor) {
            ProfileInfoTable api = profileInfoTable().getApi();
            idText.setText(Long.toString(api.id(cursor)));
            userIdText.setText(Long.toString(api.userId(cursor)));
            emailAddressText.setText(nullToEmpty(api.emailAddress(cursor)));
            binaryDataText.setText(Arrays.toString(api.binaryData(cursor)));
            createdText.setText(api.created(cursor).toString());
            modifiedText.setText(api.modified(cursor).toString());
            deletedText.setText(Boolean.toString(api.deleted(cursor)));
            awesomeText.setText(Boolean.toString(api.awesome(cursor)));
        }
    }
}
