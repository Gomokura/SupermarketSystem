package com.supermarket.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    private static final SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat YMD_HMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat YMDHMS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String formatYMD(Date date) {
        return date != null ? YMD.format(date) : null;
    }

    public static String formatYMDHMS(Date date) {
        return date != null ? YMD_HMS.format(date) : null;
    }

    public static String formatYMDHMS() {
        return YMDHMS.format(new Date());
    }

    public static Date now() {
        return new Date();
    }
}
