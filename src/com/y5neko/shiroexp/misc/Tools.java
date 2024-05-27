package com.y5neko.shiroexp.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tools {
    public static final String BLACK = "\033[30m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String PURPLE = "\033[35m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";
    public static final String RESET = "\033[0m";

    private static final Map<String, String> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put("BLACK", BLACK);
        COLOR_MAP.put("RED", RED);
        COLOR_MAP.put("GREEN", GREEN);
        COLOR_MAP.put("YELLOW", YELLOW);
        COLOR_MAP.put("BLUE", BLUE);
        COLOR_MAP.put("PURPLE", PURPLE);
        COLOR_MAP.put("CYAN", CYAN);
        COLOR_MAP.put("WHITE", WHITE);
    }

    /**
     * 按行读取文件内容
     * @param filePath 文件路径
     * @return 字符串数组
     */
    public static String[] multiLoadFile(String filePath){
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            return lines.toArray(new String[0]);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return new String[0];
        }
    }

    /**
     * 字节码转字符串
     * @param bytes 字节码
     * @return 字符串
     */
    public static String bytesToString(byte[] bytes){
        return new String(bytes);
    }

    /**
     * 改变字体颜色
     * @param str 字符串
     * @param color 颜色
     * @return 包含ANSI转义的字符串
     */
    public static String color(String str, String color){
        String colorCode = COLOR_MAP.getOrDefault(color, RESET);
        return colorCode + str + RESET;
    }
}
