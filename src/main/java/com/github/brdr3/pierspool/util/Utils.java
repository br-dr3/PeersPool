package com.github.brdr3.pierspool.util;
public class Utils {
    
    public static void cleanBuffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }
    }
}
