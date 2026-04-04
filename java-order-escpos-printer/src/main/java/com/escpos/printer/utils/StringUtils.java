package com.escpos.printer.utils;

public class StringUtils {
    /**
     * @param s
     * @return true if the given string is null or blank
     */
    public static boolean IsNullOrBlank(String s) {
        return s == null || s.isBlank();
    }
}
