package com.forsuredb;

import android.net.Uri;

import com.forsuredb.api.FSFilter;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSRecordResolver;
import com.forsuredb.api.FSSaveApi;
import com.forsuredb.api.Retriever;

public class AndroidRecordResolver<F extends FSFilter<Uri>> implements FSRecordResolver<Uri, F> {

    private final FSTableDescriber table;
    private final ContentProviderQueryable cpq;
    private F filter;

    /*package*/ AndroidRecordResolver(FSTableDescriber table, ContentProviderQueryable cpq) {
        this.table = table;
        this.cpq = cpq;
        this.filter = table.getNewFilter(this);
    }

    @Override
    public <T extends FSSaveApi<Uri>> T setter() {
        return (T) table.set(cpq);
    }

    @Override
    public <T extends FSGetApi> T getter() {
        return (T) table.get();
    }

    @Override
    public <T extends Retriever> T retrieve() {
        return (T) cpq.query(null, filter.selection(), null);
    }

    @Override
    public F find() {
        filter = table.getNewFilter(this);
        return filter;
    }

    @Override
    public F also() {
        return filter;
    }

    public FSTableDescriber table() {
        return table;
    }
}
