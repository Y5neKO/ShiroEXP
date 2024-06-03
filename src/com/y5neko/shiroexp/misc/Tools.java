package com.y5neko.shiroexp.misc;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import org.apache.tika.parser.txt.CharsetDetector;



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
     * 读取配置文件项
     * @param configFile 配置文件路径
     * @param propName 配置名称
     * @return 配置内容
     */
    public static String getProperty(String configFile, String propName) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(new File(configFile))){
            Properties prop = new Properties();
            prop.load(fileInputStream);

            return prop.getProperty(propName);
        }
    }

    /**
     * 字节码转字符串
     * @param bytes 字节码
     * @return 字符串
     */
    public static String bytesToString(byte[] bytes){
//        CharsetDetector charsetDetector = new CharsetDetector();
//        charsetDetector.setText(byteArray);
//
//        CharsetMatch match = charsetDetector.detect();
//        String encoding = match.getName();
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

    /**
     * 合并字节码
     * @param a 字节码a
     * @param b 字节码b
     * @return 合并后的字节码
     */
    public static byte[] byteMerger(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * 填充字节码
     * @param s 字节码
     * @return 填充后的字节码
     */
    private static byte[] pad(byte[] s) {
        s = byteMerger(s, charToByte((char)(16 - s.length % 16)));
        return s;
    }

    /**
     * 字符转字节码
     * @param c 字符
     * @return 字节码
     */
    private static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte)((c & 0xFF00) >> 8);
        b[1] = (byte)(c & 0xFF);
        return b;
    }

    /**
     * CBC加密
     * @param key AES密钥
     * @param data 需要加密的数据
     * @return 加密后的数据
     */
    public static String CBC_Encrypt(String key, String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String ALGORITHM = "AES";
        String TRANSFORMATION = "AES/CBC/PKCS5Padding";

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // 解码base64
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] checkdata_raw = decoder.decode(data);
        byte[] key_raw = decoder.decode(key);

        // 生成AES密钥
        SecretKeySpec keySpec = new SecretKeySpec(key_raw, ALGORITHM);

        // 生成IV向量
        byte[] ivBytes = new byte[16]; // IV长度和块大小相同（对于AES是16字节）
        new SecureRandom().nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // 首先加密得到校验数据字节码
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encrypted_raw = cipher.doFinal(checkdata_raw);
        // 然后拼接向量字节码和校验数据字节码，得到最终数据
        byte[] payload_raw = byteMerger(ivBytes, encrypted_raw);

        // base64编码一次
        return Base64.getEncoder().encodeToString(payload_raw);
    }

    /**
     * GCM加密
     * @param key AES密钥
     * @param checkdata 需要加密的数据
     * @return 加密后的数据
     */
    public static String GCM_Encrypt(String key, String checkdata) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String ALGORITHM = "AES";
        String TRANSFORMATION = "AES/GCM/NoPadding";

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // 解码base64
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] checkdata_raw = decoder.decode(checkdata);
        byte[] key_raw = decoder.decode(key);

        // 生成AES密钥
        SecretKeySpec keySpec = new SecretKeySpec(key_raw, ALGORITHM);

        // 生成IV向量
        byte[] ivBytes = new byte[16]; // 对于GCM是12字节
        new SecureRandom().nextBytes(ivBytes);

        // 生成GCM签名
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, ivBytes);     //TAG长度一般为128
//        System.out.println(Arrays.toString(gcmParameterSpec.getIV()));

        // 首先加密得到校验数据字节码
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        byte[] encrypted_raw = cipher.doFinal(pad(checkdata_raw));
        // 然后拼接GCM认证标签字节码
        byte[] payload_raw = byteMerger(ivBytes, encrypted_raw);

        return Base64.getEncoder().encodeToString(payload_raw);
    }

    /**
     * 生成随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        if (length < 1) {
            throw new IllegalArgumentException("Length must be a positive integer");
        }

        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            builder.append(randomChar);
        }

        return builder.toString();
    }

    /**
     * 读取文件为字节数组
     * @param filePath 读取文件路径
     * @return 文件的字节数组
     */
    public static byte[] fileToBytes(String filePath) {
        File file1 = new File(filePath);
        try {
            byte[] bytes = Files.readAllBytes(file1.toPath());
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从输入字符串中提取包含在$$$和$$$中的子字符串。
     * @param input 输入字符串
     * @return 包含在$$$和$$$中的子字符串列表
     */
    public static String extractStrings(String input) {
        List<String> results = new ArrayList<>();
        // 定义正则表达式模式
        String regex = "\\$\\$\\$(.*?)\\$\\$\\$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 查找并提取匹配的子字符串
        while (matcher.find()) {
            results.add(matcher.group(1));
        }

        return results.get(0);
    }

    public static void addHeader(Map<String, String> headers, String key, String value) {
        headers.put(key, value);
    }

    public static void main(String[] args) {
        byte[] test = fileToBytes("C:\\Tools\\BlueTeamTools0.92\\outputShiroBytes_sum.ser");
//        System.out.println(bytesToString(test));
        Object o;

        try (FileInputStream fileIn = new FileInputStream("C:\\Tools\\BlueTeamTools0.92\\outputShiroBytes_sum.ser");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {

            Object person = in.readObject();
            System.out.println("Deserialized Person:");
            System.out.println(person);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
