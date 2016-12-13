package com.fsryan.forsuredb.cursor;

import android.support.v7.widget.RecyclerView;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

/**
 * <p>
 *     This works best in conjunction with an {@link FSCursorLoader}, keeping a reference to your
 *     adapter and updating the adapter with the new {@link FSCursor} in the
 *     {@link android.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.content.Loader, Object)}
 *     implementation.
 * </p>
 * <p>
 *     You should extend this class to make a cursor recycler adapter for your {@link RecyclerView}.
 *     It's a relatively minimal implementation, but it makes the implementation of your extension
 *     quite easy.
 * </p>
 * <p>
 *     You will not need to implement {@link #onBindViewHolder(FSCursorViewHolder, int)}, as it is
 *     implemented here, but you do need to implement
 *     {@link #onCreateViewHolder(android.view.ViewGroup, int)}
 * </p>
 * @param <VH> An extension of the {@link FSCursorViewHolder} class
 */
public abstract class BaseFSCursorRecyclerAdapter<VH extends FSCursorViewHolder> extends RecyclerView.Adapter<VH> {

    private FSCursor c;
    private boolean queryContainsIdField;

    /**
     * @param queryContainsIdField should be true if the {@link FSGetApi} extension's
     *                             {@link FSGetApi#id(Retriever)} returned by your implementation of
     *                             the {@link #api()} method will be able to get the id of the
     *                             record.
     */
    public BaseFSCursorRecyclerAdapter(boolean queryContainsIdField) {
        this(null, queryContainsIdField);
    }

    /**
     *
     * @param c The initial {@link FSCursor} instance to use
     * @param queryContainsIdField should be true if the {@link FSGetApi} extension's
     *                             {@link FSGetApi#id(Retriever)} returned by your implementation of
     *                             the {@link #api()} method will be able to get the id of the
     *                             record.
     */
    public BaseFSCursorRecyclerAdapter(FSCursor c, boolean queryContainsIdField) {
        this.queryContainsIdField = queryContainsIdField;
        swapCursor(c);
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!isDataValid()) {
            throw new IllegalStateException("Cannot create ViewHolder for RecyclerViewCursorAdapter that does not have any data");
        }
        if (!c.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position: " + position + "; item count = " + getItemCount());
        }
        viewHolder.populateView(c);
    }

    @Override
    public int getItemCount() {
        return isDataValid() ? c.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (!isDataValid() || !c.moveToPosition(position)) {
            return 0;
        }
        return queryContainsIdField ? api().id(c) : position;
    }

    public void setQueryContainsIdField(boolean queryContainsIdField) {
        this.queryContainsIdField = queryContainsIdField;
    }

    /**
     * @return The api that should be used to determine the id of the {@link FSCursor} current position
     */
    protected abstract FSGetApi api();

    /**
     * <p>
     *     Swap cursors and close the old cursor.
     * </p>
     * @param newC the new {@link FSCursor} data
     */
    public void changeCursor(FSCursor newC) {
        final FSCursor oldCursor = swapCursor(newC);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    /**
     * <p>
     *     Swap new {@link FSCursor} with existing and return the existing c
     * </p>
     * @param newC the new cursor to swap in
     * @return
     */
    public FSCursor swapCursor(FSCursor newC) {
        if (newC == c) {
            return null;
        }

        final FSCursor oldCursor = c;
        c = newC;
        notifyDataSetChanged();

        return oldCursor;
    }

    private boolean isDataValid() {
        return c != null && !c.isClosed() && c.moveToFirst();
    }
}
