package org.zerograph.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    final public static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("E");
    final public static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    final public static String INFO = "---";
    final public static String SEND = ">>>";
    final public static String RECEIVE = "<<<";
    final public static String WARNING = "~~~";
    final public static String ERROR = "!!!";

    public static void write(String message) {
        write(message, INFO);
    }

    public static void write(String message, String tag) {
        String threadName = Thread.currentThread().getName();
        Date now = new Date();
        System.out.println(String.format("%s %s %s %s %s", DAY_FORMAT.format(now).substring(0, 2), TIME_FORMAT.format(now), threadName, tag, message));
    }

}
