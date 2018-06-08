package org.remote.invocation.starter.utils;
/**
 * 操作系统相关
 *
 * @author liucheng
 * @create 2018-06-06 16:16
 **/
public class OS {

    public static String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static Boolean isOSWIN() {
        return getOS().contains("windows");
    }
}
