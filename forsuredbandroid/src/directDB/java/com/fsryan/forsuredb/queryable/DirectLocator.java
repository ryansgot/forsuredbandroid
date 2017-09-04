package com.fsryan.forsuredb.queryable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.api.FSJoin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectLocator {

    public final String table;
    public final long id;

    private List<FSJoin> joins;

    public DirectLocator(@NonNull String table) {
        this(table, 0L);
    }

    public DirectLocator(@NonNull String table, long id) {
        this.table = table;
        this.id = id;
    }

    public void addJoins(@Nullable List<FSJoin> joins) {
        if (joins == null) {
            return;
        }
        if (this.joins == null) {
            this.joins = new ArrayList<>(joins.size());
        }
        this.joins.addAll(joins);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectLocator that = (DirectLocator) o;

        if (id != that.id) return false;
        if (!table.equals(that.table)) return false;
        return getJoins().equals(that.getJoins());
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + getJoins().hashCode();
        return result;
    }

    @NonNull
    public List<FSJoin> getJoins() {
        return joins == null ? Collections.<FSJoin>emptyList() : joins;
    }

    public boolean forSpecficRecord() {
        return id > 0;
    }
}
