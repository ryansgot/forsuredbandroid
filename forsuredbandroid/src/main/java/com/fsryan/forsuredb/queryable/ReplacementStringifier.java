package com.fsryan.forsuredb.queryable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class ReplacementStringifier {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    @NonNull
    static String[] stringifyAll(@Nullable Object[] replacements) {
        if (replacements == null) {
            return new String[0];
        }

        String[] ret = new String[replacements.length];
        for (int idx = 0; idx < replacements.length; idx++) {
            ret[idx] = stringify(replacements[idx]);
        }
        return ret;
    }

    @Nullable
    static String stringify(@Nullable Object replacement) {
        if (replacement == null) {
            return null;
        }

        // By this point, Date objects have already been turned into strings.
        Class<?> cls = replacement.getClass();
        return cls == byte[].class
                ? "X'" + bytesToHex((byte[]) replacement) + '\''
                : String.valueOf(replacement);
    }

    static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
