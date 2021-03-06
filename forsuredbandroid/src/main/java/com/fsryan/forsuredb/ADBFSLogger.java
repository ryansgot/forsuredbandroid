/*
   forsuredbandroid, an android companion to the forsuredb project

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
package com.fsryan.forsuredb;

import android.util.Log;

import com.fsryan.forsuredb.api.FSLogger;

/*package*/ class ADBFSLogger implements FSLogger {

    private final String logTag;

    public ADBFSLogger(String logTag) {
        this.logTag = logTag;
    }

    @Override
    public void e(String message, Object... objects) {
        Log.e(logTag, message == null ? "" : String.format(message, objects));
    }

    @Override
    public void i(String message, Object... objects) {
        Log.i(logTag, message == null ? "" : String.format(message, objects));
    }

    @Override
    public void w(String message, Object... objects) {
        Log.w(logTag, message == null ? "" : String.format(message, objects));
    }

    @Override
    public void o(String message, Object... objects) {
        Log.d(logTag, message == null ? "" : String.format(message, objects));
    }
}
