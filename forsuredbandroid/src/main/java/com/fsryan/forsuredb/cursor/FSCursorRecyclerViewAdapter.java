package com.fsryan.forsuredb.cursor;

import com.fsryan.forsuredb.api.FSGetApi;

/**
 * <p>
 *     This still exists for the sake of backwards compatibility. The G parameter is not needed,
 *     and thus, is confusing. This class will go away soon. Use {@link BaseFSCursorRecyclerAdapter}
 *     instead.
 * </p>
 * @param <G> An extension of the {@link FSGetApi} class
 * @param <VH> An extension of the {@link FSCursorViewHolder} class
 * @see BaseFSCursorRecyclerAdapter
 */
@Deprecated
public abstract class FSCursorRecyclerViewAdapter<G extends FSGetApi, VH extends FSCursorViewHolder> extends BaseFSCursorRecyclerAdapter<VH> {

    public FSCursorRecyclerViewAdapter() {
        this(null);
    }

    public FSCursorRecyclerViewAdapter(FSCursor c) {
        super(c, true);
        setHasStableIds(true);
    }

    /**
     * @return The api that should be used to determine the id of the {@link FSCursor} current
     * position
     */
    protected abstract G api();
}
