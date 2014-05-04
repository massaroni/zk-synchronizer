package com.mass.codec;

public final class Base64 {
    private Base64() {
    }

    public static String encodeURLSafe(final String str) {
        final byte[] data = str == null ? null : str.getBytes();
        return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(data);
    }
}
