package com.y5neko.shiroexp.misc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    /**
     * 控制台日志
     * @param type 日志类型
     * @param msg 日志内容
     * @return 日志
     */
    public static String buffer_logging(String type, String msg) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTime = now.format(formatter);
        formattedDateTime = String.format("[%s] ", Tools.color(formattedDateTime, "CYAN"));

        switch (type) {
            case "INFO":
                return formattedDateTime + "[" + Tools.color("INFO", "BLUE") + "] " + msg;
            case "EROR":
                return formattedDateTime + "[" + Tools.color("EROR", "RED") + "] " + msg;
            case "WARN":
                return formattedDateTime + "[" + Tools.color("WARN", "YELLOW") + "] " + msg;
            case "FAIL":
                return formattedDateTime + "[" + Tools.color("FAIL", "RED") + "] " + msg;
            case "SUCC":
                return formattedDateTime + "[" + Tools.color("SUCC", "GREEN") + "] " + msg;
            case "NULL":
                return formattedDateTime + "[" + Tools.color("NULL", "RED") + "] " + msg;
            default:
                return formattedDateTime + "[" + Tools.color("INFO", "BLUE") + "] " + msg;
        }
    }
}
