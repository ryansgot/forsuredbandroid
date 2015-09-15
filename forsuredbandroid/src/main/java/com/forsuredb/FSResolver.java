package com.forsuredb;

import android.net.Uri;

import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSSaveApi;

public interface FSResolver {
    FSTableDescriber table();
    <T extends FSSaveApi<Uri>> T setter();
    <T extends FSGetApi> T getter();
}
