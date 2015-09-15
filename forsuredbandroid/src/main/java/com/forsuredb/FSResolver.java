/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb;

import android.net.Uri;

import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSSaveApi;

/**
 * <p>
 *     Interface for resolving database information. {@link ForSure} is able to create these
 *     in its {@link ForSure#resolve(String)} and {@link ForSure#resolve(Uri)} methods.
 * </p>
 * @author Ryan Scott
 */
public interface FSResolver {

    /**
     * @return The {@link FSTableDescriber} that describes the table you would like to
     * resolve
     */
    FSTableDescriber table();

    /**
     * @param <T> the Type extending {@link FSSaveApi} that you would like to resolve
     * @return an object implementing the {@link FSSaveApi} extension you would like
     * to resolve
     * @throws ClassCastException if the correct cast cannot be made
     */
    <T extends FSSaveApi<Uri>> T setter();

    /**
     * @param <T> the Type extending {@link FSGetApi} that you would like to resolve
     * @return an object implementing the {@link FSGetApi} extension you would like to
     * resolve
     * @throws ClassCastException if the correct cast cannot be made
     */
    <T extends FSGetApi> T getter();
}
