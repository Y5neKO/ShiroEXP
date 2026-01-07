package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.misc.Shiro721AttackTask;
import com.y5neko.shiroexp.util.HttpRequestInfo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Files;

/**
 * Shiro721 标签页
 * Shiro 1.4.2+ 版本的 Padding Oracle 攻击
 */
public class Shiro721Tab {
    private TextField targetUrlTextField;
    private TextField cookieTextField;
    private TextField payloadFileTextField;
    private TextField threadCountTextField;
    private TextArea logTextArea;
    private TextArea resultTextArea;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Button startButton;
    private Button stopButton;

    private File selectedPayloadFile;
    private Shiro721AttackTask currentTask;
    private Thread taskThread;

    public VBox getShiro721Tab() {
        VBox shiro721Tab = new VBox();
        shiro721Tab.setSpacing(10);
        shiro721Tab.setPadding(new Insets(10));
        shiro721Tab.getStylesheets().add("css/TextField.css");

        // =========================== 第一行：目标配置 ==========================
        HBox targetConfigBox = createTargetConfigBox();

        // =========================== 第二行：Cookie 配置 ==========================
        HBox cookieConfigBox = createCookieConfigBox();

        // =========================== 第三行：Payload 文件配置 ==========================
        HBox payloadConfigBox = createPayloadConfigBox();

        // =========================== 第四行：攻击配置 ==========================
        HBox attackConfigBox = createAttackConfigBox();

        // =========================== 第五行：操作按钮 ==========================
        HBox buttonBox = createButtonBox();

        // =========================== 进度显示区域 ==========================
        VBox progressBox = createProgressBox();

        // =========================== 日志区域 ==========================
        VBox logBox = createLogBox();

        // =========================== 结果显示区域 ==========================
        TitledPane resultPane = createResultPane();

        // =========================== 添加所有组件 ==========================
        shiro721Tab.getChildren().addAll(
            targetConfigBox,
            cookieConfigBox,
            payloadConfigBox,
            attackConfigBox,
            buttonBox,
            progressBox,
            logBox,
            resultPane
        );

        return shiro721Tab;
    }

    /**
     * 创建目标配置区域
     */
    private HBox createTargetConfigBox() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);

        Label targetUrlLabel = new Label("目标地址:");
        targetUrlLabel.setMinWidth(80);

        targetUrlTextField = new TextField();
        targetUrlTextField.setPromptText("http://example.com");
        HBox.setHgrow(targetUrlTextField, javafx.scene.layout.Priority.ALWAYS);

        box.getChildren().addAll(targetUrlLabel, targetUrlTextField);
        return box;
    }

    /**
     * 创建 Cookie 配置区域
     */
    private HBox createCookieConfigBox() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);

        Label cookieLabel = new Label("有效 Cookie:");
        cookieLabel.setMinWidth(80);

        cookieTextField = new TextField();
        cookieTextField.setPromptText("完整的 rememberMe Cookie 值（Base64 编码）");
        HBox.setHgrow(cookieTextField, javafx.scene.layout.Priority.ALWAYS);

        Button testCookieButton = new Button("测试 Cookie");
        testCookieButton.setOnAction(event -> handleTestCookie());

        box.getChildren().addAll(cookieLabel, cookieTextField, testCookieButton);
        return box;
    }

    /**
     * 创建 Payload 文件配置区域
     */
    private HBox createPayloadConfigBox() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);

        Label payloadLabel = new Label("Payload 文件:");
        payloadLabel.setMinWidth(80);

        payloadFileTextField = new TextField();
        payloadFileTextField.setPromptText("选择 Payload 文件");
        payloadFileTextField.setEditable(false);
        HBox.setHgrow(payloadFileTextField, javafx.scene.layout.Priority.ALWAYS);

        Button browseButton = new Button("浏览...");
        browseButton.setOnAction(event -> handleBrowsePayloadFile());

        box.getChildren().addAll(payloadLabel, payloadFileTextField, browseButton);
        return box;
    }

    /**
     * 创建攻击配置区域
     */
    private HBox createAttackConfigBox() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);

        Label threadsLabel = new Label("线程数:");
        threadsLabel.setMinWidth(80);

        threadCountTextField = new TextField("4");
        threadCountTextField.setPrefWidth(60);

        Label threadsHintLabel = new Label("(1-16)");
        threadsHintLabel.setStyle("-fx-text-fill: gray;");

        Button autoDetectButton = new Button("自动检测");
        autoDetectButton.setOnAction(event -> handleAutoDetectThreads());

        box.getChildren().addAll(threadsLabel, threadCountTextField, threadsHintLabel, autoDetectButton);
        return box;
    }

    /**
     * 创建操作按钮区域
     */
    private HBox createButtonBox() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);

        startButton = new Button("开始攻击");
        startButton.setOnAction(event -> handleStartAttack());

        stopButton = new Button("停止攻击");
        stopButton.setDisable(true);
        stopButton.setOnAction(event -> handleStopAttack());

        Button clearLogButton = new Button("清空日志");
        clearLogButton.setOnAction(event -> handleClearLog());

        box.getChildren().addAll(startButton, stopButton, clearLogButton);
        return box;
    }

    /**
     * 创建进度显示区域
     */
    private VBox createProgressBox() {
        VBox box = new VBox();
        box.setSpacing(5);

        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        progressLabel = new Label("准备就绪");
        progressLabel.setStyle("-fx-font-size: 12px;");

        box.getChildren().addAll(progressBar, progressLabel);
        return box;
    }

    /**
     * 创建日志显示区域
     */
    private VBox createLogBox() {
        VBox box = new VBox();
        box.setSpacing(5);

        Label logLabel = new Label("攻击日志:");
        logLabel.setStyle("-fx-font-weight: bold;");

        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setPrefHeight(200);
        logTextArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        VBox.setVgrow(logTextArea, javafx.scene.layout.Priority.ALWAYS);

        box.getChildren().addAll(logLabel, logTextArea);
        return box;
    }

    /**
     * 创建结果显示区域
     */
    private TitledPane createResultPane() {
        TitledPane pane = new TitledPane();
        pane.setText("攻击结果");
        pane.setCollapsible(true);
        pane.setExpanded(false);

        VBox box = new VBox();
        box.setSpacing(10);
        box.setPadding(new Insets(10));

        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setWrapText(true);
        resultTextArea.setPrefHeight(100);
        resultTextArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        VBox.setVgrow(resultTextArea, javafx.scene.layout.Priority.ALWAYS);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);

        Button copyButton = new Button("复制到剪贴板");
        copyButton.setOnAction(event -> handleCopyResult());

        Button saveButton = new Button("保存到文件");
        saveButton.setOnAction(event -> handleSaveResult());

        buttonBox.getChildren().addAll(copyButton, saveButton);
        box.getChildren().addAll(resultTextArea, buttonBox);

        pane.setContent(box);
        return pane;
    }

    // =========================== 事件处理方法 ===========================

    /**
     * 处理测试 Cookie 按钮
     */
    private void handleTestCookie() {
        String url = targetUrlTextField.getText().trim();
        String cookie = cookieTextField.getText().trim();

        if (url.isEmpty()) {
            appendLog("[ERROR] 请输入目标地址", "ERROR");
            return;
        }

        if (cookie.isEmpty()) {
            appendLog("[ERROR] 请输入有效 Cookie", "ERROR");
            return;
        }

        appendLog("[INFO] 正在测试 Cookie 有效性...", "INFO");
        appendLog("[INFO] 目标: " + url, "INFO");
        appendLog("[INFO] Cookie 长度: " + cookie.length() + " 字符", "INFO");

        // TODO: 实现实际的 Cookie 验证逻辑
        appendLog("[SUCCESS] Cookie 格式验证通过（功能待实现）", "SUCCESS");
    }

    /**
     * 处理浏览 Payload 文件按钮
     */
    private void handleBrowsePayloadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择 Payload 文件");

        // 显示文件选择对话框
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            this.selectedPayloadFile = selectedFile;
            payloadFileTextField.setText(selectedFile.getAbsolutePath());

            // 显示文件信息
            long fileSize = selectedFile.length();
            appendLog("[INFO] 已选择文件: " + selectedFile.getName(), "INFO");
            appendLog("[INFO] 文件大小: " + formatFileSize(fileSize), "INFO");
        }
    }

    /**
     * 处理自动检测线程数
     */
    private void handleAutoDetectThreads() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        // 默认使用 CPU 核心数，但不超过 8
        int recommendedThreads = Math.min(availableProcessors, 8);
        threadCountTextField.setText(String.valueOf(recommendedThreads));
        appendLog("[INFO] 检测到 " + availableProcessors + " 个 CPU 核心", "INFO");
        appendLog("[INFO] 推荐线程数: " + recommendedThreads, "INFO");
    }

    /**
     * 处理开始攻击按钮
     */
    private void handleStartAttack() {
        // 验证输入
        if (!validateInput()) {
            return;
        }

        // 禁用输入控件
        setInputsEnabled(false);
        clearLog();

        appendLog("[INFO] ========== 开始 Shiro721 Padding Oracle 攻击 ==========", "INFO");
        appendLog("[INFO] 目标: " + targetUrlTextField.getText().trim(), "INFO");
        appendLog("[INFO] Payload: " + selectedPayloadFile.getName(), "INFO");
        appendLog("[INFO] 线程数: " + threadCountTextField.getText().trim(), "INFO");
        appendLog("[INFO] ========== 攻击配置完成，开始执行 ==========", "INFO");

        try {
            // 创建 HTTP 请求信息
            HttpRequestInfo requestInfo = new HttpRequestInfo();
            requestInfo.setRequestURL(targetUrlTextField.getText().trim());
            requestInfo.setRequestMethod("GET");
            requestInfo.setRememberMeCookie(cookieTextField.getText().trim());

            // 获取线程数
            int threadCount = Integer.parseInt(threadCountTextField.getText().trim());

            // 创建异步攻击任务
            currentTask = new Shiro721AttackTask(requestInfo, selectedPayloadFile, threadCount);

            // 绑定进度和消息
            progressBar.progressProperty().bind(currentTask.progressProperty());
            progressLabel.textProperty().bind(currentTask.messageProperty());

            // 设置任务完成回调
            currentTask.setOnSucceeded(event -> {
                String result = currentTask.getValue();
                if (result != null) {
                    resultTextArea.setText(result);
                    TitledPane resultPane = (TitledPane) resultTextArea.getParent().getParent().getParent();
                    resultPane.setExpanded(true);
                    appendLog("[SUCCESS] ========== 攻击成功完成！===========", "SUCCESS");
                    appendLog("[INFO] 生成的恶意 Cookie 已显示在结果区域", "INFO");
                }
                setInputsEnabled(true);
                progressBar.progressProperty().unbind();
                progressLabel.textProperty().unbind();
            });

            // 设置任务失败回调
            currentTask.setOnFailed(event -> {
                Throwable ex = currentTask.getException();
                String errorMsg = ex != null ? ex.getMessage() : "未知错误";
                appendLog("[ERROR] ========== 攻击失败 ==========", "ERROR");
                appendLog("[ERROR] 错误信息: " + errorMsg, "ERROR");
                setInputsEnabled(true);
                progressBar.progressProperty().unbind();
                progressLabel.textProperty().unbind();
                progressBar.setProgress(0);
            });

            // 在新线程中启动任务
            taskThread = new Thread(currentTask);
            taskThread.setDaemon(true);
            taskThread.start();

            appendLog("[INFO] 异步攻击任务已启动", "INFO");

        } catch (Exception e) {
            appendLog("[ERROR] 启动攻击失败: " + e.getMessage(), "ERROR");
            setInputsEnabled(true);
        }
    }

    /**
     * 处理停止攻击按钮
     */
    private void handleStopAttack() {
        appendLog("[INFO] 正在停止攻击...", "INFO");

        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel();
            appendLog("[INFO] 攻击任务已取消", "INFO");
        }

        if (taskThread != null && taskThread.isAlive()) {
            taskThread.interrupt();
        }

        setInputsEnabled(true);
        progressBar.progressProperty().unbind();
        progressLabel.textProperty().unbind();
        progressBar.setProgress(0);
        progressLabel.setText("已取消");

        appendLog("[INFO] ========== 攻击已停止 ==========", "INFO");
    }

    /**
     * 处理清空日志按钮
     */
    private void handleClearLog() {
        logTextArea.clear();
    }

    /**
     * 处理复制结果按钮
     */
    private void handleCopyResult() {
        String result = resultTextArea.getText();
        if (!result.isEmpty()) {
            try {
                // 使用系统剪贴板
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(result);
                clipboard.setContents(selection, null);

                appendLog("[SUCCESS] 已复制到剪贴板（" + result.length() + " 字符）", "SUCCESS");
            } catch (Exception e) {
                appendLog("[ERROR] 复制失败: " + e.getMessage(), "ERROR");
            }
        } else {
            appendLog("[WARN] 没有可复制的内容", "WARN");
        }
    }

    /**
     * 处理保存结果按钮
     */
    private void handleSaveResult() {
        String result = resultTextArea.getText();
        if (!result.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存攻击结果");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
            );
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
            );

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    Files.write(file.toPath(), result.getBytes());
                    appendLog("[SUCCESS] 已保存到: " + file.getAbsolutePath(), "SUCCESS");
                    appendLog("[INFO] 文件大小: " + formatFileSize(file.length()), "INFO");
                } catch (Exception e) {
                    appendLog("[ERROR] 保存失败: " + e.getMessage(), "ERROR");
                }
            }
        } else {
            appendLog("[WARN] 没有可保存的内容", "WARN");
        }
    }

    // =========================== 辅助方法 ===========================

    /**
     * 验证输入
     */
    private boolean validateInput() {
        String url = targetUrlTextField.getText().trim();
        String cookie = cookieTextField.getText().trim();
        String threads = threadCountTextField.getText().trim();

        if (url.isEmpty()) {
            appendLog("[ERROR] 请输入目标地址", "ERROR");
            return false;
        }

        if (cookie.isEmpty()) {
            appendLog("[ERROR] 请输入有效 Cookie", "ERROR");
            return false;
        }

        if (selectedPayloadFile == null) {
            appendLog("[ERROR] 请选择 Payload 文件", "ERROR");
            return false;
        }

        if (!selectedPayloadFile.exists()) {
            appendLog("[ERROR] Payload 文件不存在: " + selectedPayloadFile.getAbsolutePath(), "ERROR");
            return false;
        }

        try {
            int threadCount = Integer.parseInt(threads);
            if (threadCount < 1 || threadCount > 16) {
                appendLog("[ERROR] 线程数必须在 1-16 之间", "ERROR");
                return false;
            }
        } catch (NumberFormatException e) {
            appendLog("[ERROR] 线程数必须是有效的整数", "ERROR");
            return false;
        }

        return true;
    }

    /**
     * 设置输入控件的启用状态
     */
    private void setInputsEnabled(boolean enabled) {
        targetUrlTextField.setDisable(!enabled);
        cookieTextField.setDisable(!enabled);
        payloadFileTextField.setDisable(!enabled);
        threadCountTextField.setDisable(!enabled);
        startButton.setDisable(!enabled);
        stopButton.setDisable(enabled);
    }

    /**
     * 清空日志
     */
    private void clearLog() {
        logTextArea.clear();
    }

    /**
     * 追加日志
     */
    private void appendLog(String message, String level) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logLine = "[" + timestamp + "] " + message;

        Platform.runLater(() -> {
            logTextArea.appendText(logLine + "\n");
        });
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * 更新进度
     */
    public void updateProgress(double progress, String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(message);
        });
    }

    /**
     * 攻击完成
     */
    public void onAttackComplete(String result) {
        Platform.runLater(() -> {
            resultTextArea.setText(result);
            progressBar.setProgress(1.0);
            progressLabel.setText("攻击完成");
            setInputsEnabled(true);

            // 展开结果面板
            TitledPane resultPane = (TitledPane) resultTextArea.getParent().getParent().getParent();
            resultPane.setExpanded(true);
        });
    }

    /**
     * 攻击失败
     */
    public void onAttackFailed(String error) {
        Platform.runLater(() -> {
            progressBar.setProgress(0);
            progressLabel.setText("攻击失败: " + error);
            setInputsEnabled(true);
        });
    }
}
