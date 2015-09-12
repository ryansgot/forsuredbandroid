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

import android.util.Log;

import com.forsuredb.api.FSLogger;
import com.google.common.base.Strings;

/*package*/ class ADBFSLogger implements FSLogger {

    private final String logTag;

    public ADBFSLogger(String logTag) {
        this.logTag = logTag;
    }

    @Override
    public void e(String message) {
        Log.e(logTag, Strings.nullToEmpty(message));
    }

    @Override
    public void i(String message) {
        Log.i(logTag, Strings.nullToEmpty(message));
    }

    @Override
    public void w(String message) {
        Log.w(logTag, Strings.nullToEmpty(message));
    }

    @Override
    public void o(String message) {
        Log.d(logTag, Strings.nullToEmpty(message));
    }
}
