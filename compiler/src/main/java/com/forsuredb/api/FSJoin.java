package com.forsuredb.api;

public interface FSJoin {

    /**
     * <p>
     *     Corresponds to some of the types of joins that are possible. Future versions may
     *     have more joins, but the most common is the {@link #INNER} type.
     * </p>
     */
    enum Type {
        /**
         * <p>
         *     It is debatable whether this is useful, given the choices that have been made
         *     in the forsuredb project thus far
         * </p>
         */
        NATURAL,
        LEFT,

        /**
         * <p>
         *     Probably the most common, and if you're wondering which {@link FSJoin.Type}
         *     you should use, then it's probably this one.
         * </p>
         */
        INNER,
        OUTER,
        CROSS;
    }

    /**
     * @return the {@link FSJoin.Type} of this join
     */
    Type type();

    /**
     * @return the name of the table that is the parent in this relationship. This need not be
     * the table you're querying.
     * @see #childTable()
     */
    String parentTable();

    /**
     * @return The column in the parent table that should correspond to the {@link #childColumn()}.
     * These columns need not have the same name.
     * @see #childColumn()
     */
    String parentColumn();

    /**
     * @return the name of the table that is the child in this relationship.
     * @see #parentTable()
     */
    String childTable();

    /**
     * @return The column in the child table that should correspond to the {@link #parentColumn()}.
     * These columns need not have the same name.
     * @see #parentColumn()
     */
    String childColumn();
}
