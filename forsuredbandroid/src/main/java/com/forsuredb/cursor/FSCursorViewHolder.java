package com.forsuredb.cursor;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class FSCursorViewHolder extends RecyclerView.ViewHolder {

    protected int viewType;

    public FSCursorViewHolder(View itemView) {
        this(itemView, 0);
    }

    public FSCursorViewHolder(View itemView, int viewType) {
        super(itemView);
        initViewReferences(itemView);
        this.viewType = viewType;
    }

    /**
     * @param view Must set up all of the holder's child View references using view.findViewById
     */
    protected abstract void initViewReferences(View view);

    /**
     * <p>
     *     It is up to you to determine what {@link com.forsuredb.api.FSGetApi}
     *     class to use to get data out of the {@link FSCursor}. A {@link FSCursor} is a
     *     {@link com.forsuredb.api.Retriever}.
     * </p>
     * @param cursor A cursor at the correct position for this view.
     */
    protected abstract void populateView(FSCursor cursor);
}
