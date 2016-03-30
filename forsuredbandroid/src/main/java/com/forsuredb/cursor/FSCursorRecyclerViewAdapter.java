package com.forsuredb.cursor;

import android.support.v7.widget.RecyclerView;

import com.forsuredb.api.FSGetApi;

public abstract class FSCursorRecyclerViewAdapter<G extends FSGetApi, VH extends FSCursorViewHolder> extends RecyclerView.Adapter<VH> {

    private FSCursor c;

    public FSCursorRecyclerViewAdapter(FSCursor c) {
        swapCursor(c);
        setHasStableIds(true);
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
        return isDataValid() && c.moveToPosition(position) ? api().id(c) : 0;
    }

    /**
     * @return The api that should be used to determine the id of the {@link FSCursor} current position
     */
    protected abstract G api();

    /**
     * <p>
     *     Swap cursors and close the old c
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
     * @param newC
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
