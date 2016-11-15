package com.adam.aslfms.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class derived from the answer listed here
 *
 * @link http://stackoverflow.com/a/4846511/50913
 */
public class MD5 {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "MD5";

    /**
     * Static constant used for loading digest instance
     */
    private static final String MD5 = "MD5";

    /**
     * MD5 has a string.
     *
     * @param s the string to hash
     * @return the hashed string
     */
    public static String getHashString(final String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
            throw new RuntimeException("MD5 message digest not available", e);
        }
    }
}

