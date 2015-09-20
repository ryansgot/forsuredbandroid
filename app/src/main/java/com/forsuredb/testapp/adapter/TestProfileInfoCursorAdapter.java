package com.forsuredb.testapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.forsuredb.api.Retriever;
import com.forsuredb.testapp.R;
import com.forsuredb.testapp.model.ProfileInfoTable;
import com.google.common.base.Strings;

import java.util.Arrays;

import static com.forsuredb.testapp.ForSure.profileInfoTable;

public class TestProfileInfoCursorAdapter extends BaseAdapter {

    private Context context;
    private Retriever retriever;

    public TestProfileInfoCursorAdapter(Context context) {
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

        ProfileInfoTable profileInfoTable = profileInfoTable().getApi();
        return new ViewBuilder().id(profileInfoTable.id(retriever))
                                .userId(profileInfoTable.userId(retriever))
                                .emalAddress(profileInfoTable.emailAddress(retriever))
                                .binaryData(profileInfoTable.binaryData(retriever))
                                .created(profileInfoTable.created(retriever).toString())
                                .modified(profileInfoTable.modified(retriever).toString())
                                .deleted(profileInfoTable.deleted(retriever))
                                .targetLayout(view)
                                .build(context);
    }

    public void changeCursor(Retriever newCursor) {
        if (retriever != null) {
            retriever.close();
        }
        if (newCursor != null) {
            retriever = newCursor;
            notifyDataSetChanged();
        }
    }

    private class ViewBuilder {

        private long id;
        private long userId;
        private String emailAddress;
        private byte[] binaryData;
        private View targetLayout;
        private String created;
        private String modified;
        private boolean deleted;

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
                targetLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.profile_info_layout, null);
            }
        }

        private void initializeView() {
            ((TextView) targetLayout.findViewById(R.id.profile_info_id_text)).setText(Long.toString(id));
            ((TextView) targetLayout.findViewById(R.id.foreign_user_id_text)).setText(Long.toString(userId));
            ((TextView) targetLayout.findViewById(R.id.email_address_text)).setText(emailAddress);
            ((TextView) targetLayout.findViewById(R.id.binary_data_text)).setText(Arrays.toString(binaryData));
            ((TextView) targetLayout.findViewById(R.id.profile_info_created_text)).setText(Strings.nullToEmpty(created));
            ((TextView) targetLayout.findViewById(R.id.profile_info_modified_text)).setText(Strings.nullToEmpty(modified));
            ((TextView) targetLayout.findViewById(R.id.profile_info_deleted_text)).setText(Boolean.toString(deleted));
        }
    }
}
