package com.fsryan.forsuredb.queryable;

import android.support.annotation.NonNull;

/**
 * <p>In order to not lose type information when sending data across to the ContentProvider as
 * strings, double the number of strings you send across, but in the form:
 * [type1, value1, type2, value2, ...] so that, on the other side, you can deserialize in a
 * type-aware manner.
 * <p>A couple things to remember:
 * <ul>
 *     <li>byte[] objects are converted to hex strings (strings length 2x the array length)</li>
 *     <li>doubles are converted to their bit-for-bit signed longs</li>
 *     <li>floats are converted to their bit-for-bit signed ints</li>
 * </ul>
 */
abstract class ReplacementSerializer {

    @NonNull
    static String[] serializeAll(Object[] objects) {
        if (objects == null) {
            return new String[0];
        }

        String[] ret = new String[objects.length * 2];
        for (int idx = 0; idx < ret.length; idx += 2) {
            final Object obj = objects[idx / 2];
            if (obj == null) {
                continue;
            }

            final int retIdx = idx + 1;
            final char token = tokenOf(obj.getClass());
            ret[idx] = Character.toString(token);
            switch (token) {
                case 'I':
                case 'L':
                case 'S':
                    ret[retIdx] = String.valueOf(obj);
                    break;
                case 'F':
                    ret[retIdx] = String.valueOf(Float.floatToIntBits((float) obj));
                    break;
                case 'D':
                    ret[retIdx] = String.valueOf(Double.doubleToLongBits((double) obj));
                    break;
                case 'b':
                    final String stringified = ReplacementStringifier.stringify(obj);
                    ret[retIdx] = stringified.substring(2, stringified.length() - 1);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot serialize type: " + obj.getClass());
            }
        }
        return ret;
    }

    private static char tokenOf(@NonNull Class<?> cls) {
        if (cls == Integer.class) {
            return 'I';
        }
        if (cls == Long.class) {
            return 'L';
        }
        if (cls == Float.class) {
            return 'F';
        }
        if (cls == Double.class) {
            return 'D';
        }
        if (cls == String.class) {
            return 'S';
        }
        if (cls == byte[].class) {
            return 'b';
        }
        throw new IllegalArgumentException("cannot serialize item of type: " + cls);
    }

    @NonNull
    static Object[] deserializeAll(String[] allSerialized) {
        if (allSerialized == null) {
            return new Object[0];
        }

        Object[] ret = new Object[allSerialized.length / 2];
        for (int idx = 0; idx < allSerialized.length; idx += 2) {
            final int retIdx = idx / 2;
            final String val = allSerialized[idx + 1];
            final char token = allSerialized[idx].charAt(0);
            switch (token) {
                case 'I':
                    ret[retIdx] = Integer.parseInt(val);
                    break;
                case 'L':
                    ret[retIdx] = Long.parseLong(val);
                    break;
                case 'F':
                    ret[retIdx] = Float.intBitsToFloat(Integer.parseInt(val));
                    break;
                case 'D':
                    ret[retIdx] = Double.longBitsToDouble(Long.parseLong(val));
                    break;
                case 'S':
                    ret[retIdx] = val;
                    break;
                case 'b':
                    ret[retIdx] = fromHex(val);
                    break;
                default:
                    throw new IllegalArgumentException("cannot deserialize item--unrecognized token: " + token);
            }
        }
        return ret;
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
