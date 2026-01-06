package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.config.ExploitConfig;
import com.y5neko.shiroexp.misc.Log;
import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.payloads.BruteGadget;
import com.y5neko.shiroexp.payloads.BruteKey;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

public class Shiro550Tab {
    // 需要进行后续操作的全局组件
    public TextField rememberMeValueTextField;
    public ComboBox<String> exploitChainComboBox;
    public ComboBox<String> echoGadgetsComboBox;
    public ComboBox<String> cryptTypeComboBox;
    private TextField cookieTextField;

    // 停止爆破标志和当前Task
    private volatile boolean stopBruteForce = false;
    private javafx.concurrent.Task<Void> currentBruteTask = null;

    /**
     * 追加日志并自动滚动到底部的辅助方法
     * @param logTextArea 日志文本框
     * @param text 要追加的文本
     */
    private void appendLogWithScroll(TextArea logTextArea, String text) {
        logTextArea.appendText(text);
        logTextArea.setScrollTop(Double.MAX_VALUE);
    }

    public VBox getShiro550Tab(){
        VBox shiro550Tab = new VBox();

        // ========================== 第一行输入目标地址==============================
        HBox targetUrlBox = new HBox();
        targetUrlBox.setAlignment(Pos.CENTER);
        targetUrlBox.setSpacing(10);
        targetUrlBox.setPadding(new Insets(0, 0, 10, 0));

        Label requestTypeLabel = new Label("请求方式: ");
        ObservableList<String> requestType = FXCollections.observableArrayList("GET", "POST");
        ComboBox<String> requestTypeComboBox = new ComboBox<>(requestType);
        requestTypeComboBox.setValue("GET");

        Label targetUrlLabel = new Label("目标地址: ");
        TextField targetUrlTextField = new TextField();
        HBox.setHgrow(targetUrlTextField, javafx.scene.layout.Priority.ALWAYS);

        targetUrlBox.getChildren().addAll(requestTypeLabel, requestTypeComboBox, targetUrlLabel, targetUrlTextField);

        // ================================第二行输入rememberMe相关==================================
        HBox rememberMeBox = new HBox();
        rememberMeBox.setAlignment(Pos.CENTER);
        rememberMeBox.setSpacing(10);
        rememberMeBox.setPadding(new Insets(10, 0, 10, 0));

        Label rememberMeKeywordLabel = new Label("Keyword: ");
        TextField rememberMeKeywordTextField = new TextField("rememberMe");

        Label rememberMeValueLabel = new Label("指定rememberMe: ");
        rememberMeValueTextField = new TextField();
        HBox.setHgrow(rememberMeValueTextField, javafx.scene.layout.Priority.ALWAYS);

        Label cryptTypeLabel = new Label("加密方式: ");
        ObservableList<String> cryptType = FXCollections.observableArrayList("爆破所有", "CBC", "GCM");
        cryptTypeComboBox = new ComboBox<>(cryptType);
        cryptTypeComboBox.setValue("爆破所有");
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Shiro1.4.2版本后,Shiro的加密模式\n由AES-CBC变为AES-GCM\n\n爆破所有: 同时尝试CBC和GCM模式");
        Tooltip.install(cryptTypeComboBox, tooltip);

        final Button checkRememberMeButton = new Button("检测rememberMe");

        rememberMeBox.getChildren().addAll(rememberMeKeywordLabel, rememberMeKeywordTextField, rememberMeValueLabel, rememberMeValueTextField, cryptTypeLabel, cryptTypeComboBox, checkRememberMeButton);

        // ================================第三行回显链==================================
        HBox exploitEchoChainBox = new HBox();
        exploitEchoChainBox.setAlignment(Pos.CENTER);
        exploitEchoChainBox.setSpacing(10);
        exploitEchoChainBox.setPadding(new Insets(10, 0, 10, 0));

        Label exploitChainsLabel = new Label("利用链: ");
        ObservableList<String> exploitChains = FXCollections.observableArrayList("检测所有利用链");
        exploitChains.addAll(AllList.gadgets);
        exploitChainComboBox = new ComboBox<>(exploitChains);
        exploitChainComboBox.setValue("检测所有利用链");

        Label echoGadgetsLabel = new Label("回显方式: ");
        ObservableList<String> echoGadgets = FXCollections.observableArrayList("检测所有回显");
        echoGadgets.addAll(AllList.echoGadgets);
        echoGadgetsComboBox = new ComboBox<>(echoGadgets);
        echoGadgetsComboBox.setValue("检测所有回显");

        Button checkGadgetsButton = new Button("检测回显");

        Button clearLogButton = new Button("清空日志");

        exploitEchoChainBox.getChildren().addAll(exploitChainsLabel, exploitChainComboBox, echoGadgetsLabel, echoGadgetsComboBox, checkGadgetsButton, clearLogButton);

        // ================================第四行高级配置==================================
        TitledPane advancedConfigPane = new TitledPane();
        advancedConfigPane.setText("高级配置");
        advancedConfigPane.setCollapsible(true);
        advancedConfigPane.setExpanded(false);

        VBox advancedConfigContent = new VBox();
        advancedConfigContent.setSpacing(10);
        advancedConfigContent.setPadding(new Insets(10, 0, 10, 0));

        // Cookie 配置
        HBox cookieBox = new HBox();
        cookieBox.setAlignment(Pos.CENTER);
        cookieBox.setSpacing(10);

        Label cookieLabel = new Label("Cookie: ");
        cookieTextField = new TextField();
        cookieTextField.setPromptText("例: key=value; key2=value2");
        HBox.setHgrow(cookieTextField, javafx.scene.layout.Priority.ALWAYS);

        cookieBox.getChildren().addAll(cookieLabel, cookieTextField);

        advancedConfigContent.getChildren().add(cookieBox);
        advancedConfigPane.setContent(advancedConfigContent);

        // ================================第五行日志==================================
        VBox logContainer = new VBox();
        logContainer.setSpacing(10);
        logContainer.setPadding(new Insets(10, 0, 0, 0));
        logContainer.setFillWidth(true);

        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);

        logContainer.getChildren().add(logTextArea);

        // 关键：让 TextArea 在 logContainer 中占据所有剩余垂直空间
        VBox.setVgrow(logTextArea, javafx.scene.layout.Priority.ALWAYS);

        // 关键：让 logContainer 在主 VBox (shiro550Tab) 中占据所有剩余垂直空间
        VBox.setVgrow(logContainer, javafx.scene.layout.Priority.ALWAYS);

        // =============================处理一些绑定事件==========================================
        checkRememberMeButton.setOnMouseClicked(event -> {
            // 判断当前是检测还是停止
            if ("停止检测".equals(checkRememberMeButton.getText())) {
                // 执行停止操作
                stopBruteForce = true;
                if (currentBruteTask != null) {
                    currentBruteTask.cancel();
                }
                appendLogWithScroll(logTextArea, "[INFO]用户中断检测\n");
                return;
            }

            // 验证输入
            if (targetUrlTextField.getText().isEmpty()) {
                appendLogWithScroll(logTextArea, "请输入目标地址\n");
                return;
            }
            if (rememberMeKeywordTextField.getText().isEmpty()) {
                appendLogWithScroll(logTextArea, "请输入rememberMe关键字\n");
                return;
            }

            // 切换按钮到停止状态
            checkRememberMeButton.setText("停止检测");
            stopBruteForce = false;

            // 传递需要后续操作的组件到POJO类
            final GlobalComponents globalComponents = new GlobalComponents(
                    rememberMeValueTextField, exploitChainComboBox,
                    echoGadgetsComboBox, logTextArea, cryptTypeComboBox, requestTypeComboBox, checkRememberMeButton, stopBruteForce
            );

            // 添加到线程池中执行，防止阻塞UI线程
            currentBruteTask = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        TargetOBJ targetOBJ = new TargetOBJ(targetUrlTextField.getText());
                        targetOBJ.setRememberMeFlag(rememberMeValueTextField.getText());

                        // 应用高级配置（Cookie）
                        applyAdvancedConfig(targetOBJ, cookieTextField);

                        // 获取加密方式选择
                        String selectedCryptType = cryptTypeComboBox.getValue();

                        // 调用爆破方法，传递加密模式参数
                        BruteKey.bruteKey(targetOBJ, globalComponents, selectedCryptType);
                    } catch (Exception e) {
                        final String errorMsg = e.getMessage();
                        javafx.application.Platform.runLater(() -> {
                            appendLogWithScroll(logTextArea, "[EROR]" + (errorMsg != null && !errorMsg.isEmpty() ? errorMsg : "rememberMe 检测失败") + "\n");
                            if (errorMsg == null || errorMsg.isEmpty()) {
                                appendLogWithScroll(logTextArea, "[DEBUG] " + e.getClass().getSimpleName() + "\n");
                            }
                        });
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    // 恢复按钮状态
                    checkRememberMeButton.setText("检测rememberMe");
                    currentBruteTask = null;
                }

                @Override
                protected void failed() {
                    // 恢复按钮状态
                    checkRememberMeButton.setText("检测rememberMe");
                    currentBruteTask = null;
                }

                @Override
                protected void cancelled() {
                    // 恢复按钮状态
                    checkRememberMeButton.setText("检测rememberMe");
                    currentBruteTask = null;
                }
            };
            new Thread(currentBruteTask).start();
        });

        checkGadgetsButton.setOnMouseClicked(event -> {
            // 验证必填项
            if (targetUrlTextField.getText().isEmpty()) {
                appendLogWithScroll(logTextArea, "[EROR]请先输入目标地址\n");
                return;
            }
            if (rememberMeValueTextField.getText().isEmpty()) {
                appendLogWithScroll(logTextArea, "[EROR]请先完成 Key 检测\n");
                return;
            }

            // 获取当前下拉框选择
            String selectedGadget = exploitChainComboBox.getValue();
            String selectedEcho = echoGadgetsComboBox.getValue();

            // 根据选择构建测试列表
            java.util.List<String> gadgetsToTest = new java.util.ArrayList<>();
            java.util.List<String> echosToTest = new java.util.ArrayList<>();

            if ("检测所有利用链".equals(selectedGadget)) {
                // 情况1: 检测所有利用链
                gadgetsToTest.addAll(java.util.Arrays.asList(AllList.gadgets));
            } else {
                // 情况2/3: 特定利用链
                gadgetsToTest.add(selectedGadget);
            }

            if ("检测所有回显".equals(selectedEcho)) {
                // 情况1: 检测所有回显
                echosToTest.addAll(java.util.Arrays.asList(AllList.echoGadgets));
            } else {
                // 情况2/3: 特定回显
                echosToTest.add(selectedEcho);
            }

            checkGadgetsButton.setDisable(true);
            appendLogWithScroll(logTextArea, "[INFO]正在检测回显链...\n");

            // 异步执行检测任务
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        // 创建目标对象
                        TargetOBJ targetOBJ = new TargetOBJ(targetUrlTextField.getText());
                        targetOBJ.setKey(rememberMeValueTextField.getText());
                        targetOBJ.setRememberMeFlag(rememberMeKeywordTextField.getText());

                        // 应用高级配置（Cookie）
                        applyAdvancedConfig(targetOBJ, cookieTextField);

                        // 执行回显链检测（带进度回调）
                        final TextArea logRef = logTextArea;
                        java.util.List<String> validGadgets = BruteGadget.bruteGadgetCustom(targetOBJ, targetOBJ.getKey(), gadgetsToTest, echosToTest, new BruteGadget.ProgressCallback() {
                            @Override
                            public void onProgress(String message) {
                                javafx.application.Platform.runLater(() -> logRef.appendText(message + "\n"));
                            }

                            @Override
                            public void onSuccess(String gadget, String echo) {
                                javafx.application.Platform.runLater(() -> {
                                    logRef.appendText("[SUCC]发现回显链: " + gadget + " + " + echo + "\n");
                                });
                            }

                            @Override
                            public void onFail(String gadget, String echo) {
                                javafx.application.Platform.runLater(() -> {
                                    logRef.appendText("[FAIL]回显链无效: " + gadget + " + " + echo + "\n");
                                });
                            }
                        });

                        // 更新 UI
                        javafx.application.Platform.runLater(() -> {
                            // 输出汇总结果
                            appendLogWithScroll(logTextArea, "========================================\n");
                            if (!validGadgets.isEmpty()) {
                                appendLogWithScroll(logTextArea, "[SUCC]共发现" + validGadgets.size() + "条有效回显链\n");
                                appendLogWithScroll(logTextArea, "----------\n");
                                for (String validGadget : validGadgets) {
                                    String[] parts = validGadget.split("\\+");
                                    appendLogWithScroll(logTextArea, parts[0] + " + " + parts[1] + "\n");
                                }
                                appendLogWithScroll(logTextArea, "----------\n");

                                // 自动选择第一个有效的回显链
                                String firstValid = validGadgets.get(0);
                                String[] parts = firstValid.split("\\+");
                                if (parts.length == 2) {
                                    exploitChainComboBox.getSelectionModel().select(parts[0]);
                                    echoGadgetsComboBox.getSelectionModel().select(parts[1]);

                                    // 更新全局配置
                                    ExploitConfig config = ExploitConfig.getInstance();
                                    config.setTargetUrl(targetUrlTextField.getText().trim());
                                    config.setKey(rememberMeValueTextField.getText().trim());
                                    config.setGadget(parts[0]);
                                    config.setEcho(parts[1]);
                                    config.setCryptType(cryptTypeComboBox.getValue());
                                    config.setRememberMeFlag(rememberMeKeywordTextField.getText().trim());
                                    config.setRequestType(requestTypeComboBox.getValue());

                                    // 应用高级配置到全局配置
                                    if (cookieTextField != null && !cookieTextField.getText().trim().isEmpty()) {
                                        config.setCookie(cookieTextField.getText().trim());
                                    }

                                    // 提示用户
                                    appendLogWithScroll(logTextArea, "\n");
                                    appendLogWithScroll(logTextArea, "========================================\n");
                                    appendLogWithScroll(logTextArea, "[提示] 配置已自动同步到「漏洞利用」标签页\n");
                                    appendLogWithScroll(logTextArea, "[提示] 请切换到「漏洞利用」标签页进行命令执行和内存马注入\n");
                                    appendLogWithScroll(logTextArea, "========================================\n\n");

                                    // 自动更新漏洞利用标签页的配置
                                    ExploitTab.updateFromConfigStatic();
                                }
                            } else {
                                appendLogWithScroll(logTextArea, "[FAIL]未发现有效回显链\n");
                            }
                            appendLogWithScroll(logTextArea, "========================================\n");
                        });

                    } catch (Exception e) {
                        final String errorMsg = e.getMessage();
                        javafx.application.Platform.runLater(() -> {
                            appendLogWithScroll(logTextArea, "[EROR]" + (errorMsg != null ? errorMsg : "操作失败，请检查配置") + "\n");
                            // 详细错误信息（调试用）
                            if (errorMsg == null || errorMsg.isEmpty()) {
                                appendLogWithScroll(logTextArea, "[DEBUG] " + e.getClass().getSimpleName() + "\n");
                            }
                        });
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    checkGadgetsButton.setDisable(false);
                }

                @Override
                protected void failed() {
                    checkGadgetsButton.setDisable(false);
                }
            };
            new Thread(task).start();
        });

        clearLogButton.setOnMouseClicked(event -> {
            logTextArea.clear();
        });

        // =============================最后添加所有的VBox到shiro550Tab===========================
        shiro550Tab.getChildren().addAll(targetUrlBox, rememberMeBox, exploitEchoChainBox, advancedConfigPane, logContainer);
        return shiro550Tab;
    }

    /**
     * 应用高级配置到目标对象
     * @param targetOBJ 目标对象
     * @param cookie Cookie 文本框
     */
    private void applyAdvancedConfig(TargetOBJ targetOBJ, TextField cookie) {
        // 应用 Cookie
        if (cookie != null && !cookie.getText().trim().isEmpty()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", cookie.getText().trim());
            targetOBJ.setHeaders(headers);
        }
    }

    public class GlobalComponents {
        public TextField rememberMeField;
        public ComboBox<String> exploitChainComboBox;
        public ComboBox<String> echoGadgetsComboBox;
        public TextArea logArea;
        public ComboBox<String> cryptTypeComboBox;
        public ComboBox<String> requestTypeComboBox;
        public Button checkRememberMeButton;
        public volatile boolean stopFlag;

        public GlobalComponents(TextField rememberMeField, ComboBox<String> exploitChainComboBox,
                                ComboBox<String> echoGadgetsComboBox, TextArea logArea,
                                ComboBox<String> cryptTypeComboBox, ComboBox<String> requestTypeComboBox,
                                Button checkRememberMeButton, boolean stopFlag) {
            this.rememberMeField = rememberMeField;
            this.exploitChainComboBox = exploitChainComboBox;
            this.echoGadgetsComboBox = echoGadgetsComboBox;
            this.logArea = logArea;
            this.cryptTypeComboBox = cryptTypeComboBox;
            this.requestTypeComboBox = requestTypeComboBox;
            this.checkRememberMeButton = checkRememberMeButton;
            this.stopFlag = stopFlag;
        }
    }
}
