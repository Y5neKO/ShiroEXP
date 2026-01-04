package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.ExploitConfig;
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
        ObservableList<String> cryptType = FXCollections.observableArrayList("CBC", "GCM");
        cryptTypeComboBox = new ComboBox<>(cryptType);
        cryptTypeComboBox.setValue("CBC");
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Shiro1.4.2版本后,Shiro的加密模式\n由AES-CBC变为AES-GCM");
        Tooltip.install(cryptTypeComboBox, tooltip);

        Button checkRememberMeButton = new Button("检测rememberMe");

        rememberMeBox.getChildren().addAll(rememberMeKeywordLabel, rememberMeKeywordTextField, rememberMeValueLabel, rememberMeValueTextField, cryptTypeLabel, cryptTypeComboBox, checkRememberMeButton);

        // ================================第三行回显链==================================
        HBox exploitEchoChainBox = new HBox();
        exploitEchoChainBox.setAlignment(Pos.CENTER);
        exploitEchoChainBox.setSpacing(10);
        exploitEchoChainBox.setPadding(new Insets(10, 0, 10, 0));

        Label exploitChainsLabel = new Label("利用链: ");
        ObservableList<String> exploitChains = FXCollections.observableArrayList("检测所有利用链", "CommonsBeanutils1");
        exploitChainComboBox = new ComboBox<>(exploitChains);
        exploitChainComboBox.setValue("检测所有利用链");

        Label echoGadgetsLabel = new Label("回显方式: ");
        ObservableList<String> echoGadgets = FXCollections.observableArrayList("检测所有回显", "AllEcho", "TomcatEcho", "SpringEcho");
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
        HBox logBox = new HBox();
        logBox.setAlignment(Pos.CENTER);
        logBox.setSpacing(10);
        logBox.setPadding(new Insets(10, 0, 0, 0));

        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setPrefHeight(100);

        logBox.getChildren().add(logTextArea);
        HBox.setHgrow(logTextArea, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(logBox, javafx.scene.layout.Priority.ALWAYS);

        // =============================处理一些绑定事件==========================================
        checkRememberMeButton.setOnMouseClicked(event -> {
            checkRememberMeButton.setDisable(true);
            // 在这里添加检测rememberMe的逻辑
            if (targetUrlTextField.getText().isEmpty()) {
                logTextArea.appendText("请输入目标地址\n");
                checkRememberMeButton.setDisable(false);
                return;
            }
            if (rememberMeKeywordTextField.getText().isEmpty()) {
                logTextArea.appendText("请输入rememberMe关键字\n");
                checkRememberMeButton.setDisable(false);
            } else {
                // 传递需要后续操作的组件到POJO类
                GlobalComponents globalComponents = new GlobalComponents(
                        rememberMeValueTextField, exploitChainComboBox,
                        echoGadgetsComboBox, logTextArea, cryptTypeComboBox
                );
                // 添加到线程池中执行，防止阻塞UI线程
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            TargetOBJ targetOBJ = new TargetOBJ(targetUrlTextField.getText());
                            targetOBJ.setRememberMeFlag(rememberMeValueTextField.getText());

                            // 应用高级配置（Cookie）
                            applyAdvancedConfig(targetOBJ, cookieTextField);

                            BruteKey.bruteKey(targetOBJ, globalComponents);
                        } catch (Exception e) {
                            final String errorMsg = e.getMessage();
                            javafx.application.Platform.runLater(() -> {
                                logTextArea.appendText("[EROR]" + (errorMsg != null && !errorMsg.isEmpty() ? errorMsg : "rememberMe 检测失败") + "\n");
                                if (errorMsg == null || errorMsg.isEmpty()) {
                                    logTextArea.appendText("[DEBUG] " + e.getClass().getSimpleName() + "\n");
                                }
                            });
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        checkRememberMeButton.setDisable(false);
                    }

                    @Override
                    protected void failed() {
                        checkRememberMeButton.setDisable(false);
                    }
                };
                new Thread(task).start();
            }
        });

        checkGadgetsButton.setOnMouseClicked(event -> {
            // 验证必填项
            if (targetUrlTextField.getText().isEmpty()) {
                logTextArea.appendText("[EROR]请先输入目标地址\n");
                return;
            }
            if (rememberMeValueTextField.getText().isEmpty()) {
                logTextArea.appendText("[EROR]请先完成 Key 检测\n");
                return;
            }

            checkGadgetsButton.setDisable(true);
            logTextArea.appendText("[INFO]正在检测回显链...\n");

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
                        java.util.List<String> validGadgets = BruteGadget.bruteGadget(targetOBJ, new BruteGadget.ProgressCallback() {
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
                                // 静默失败，不输出
                            }
                        });

                        // 更新 UI
                        javafx.application.Platform.runLater(() -> {
                            if (!validGadgets.isEmpty()) {
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

                                    // 应用高级配置到全局配置
                                    if (cookieTextField != null && !cookieTextField.getText().trim().isEmpty()) {
                                        config.setCookie(cookieTextField.getText().trim());
                                    }

                                    // 提示用户
                                    logTextArea.appendText("\n");
                                    logTextArea.appendText("========================================\n");
                                    logTextArea.appendText("[提示] 配置已自动同步到「漏洞利用」标签页\n");
                                    logTextArea.appendText("========================================\n");

                                    // 自动更新漏洞利用标签页的配置
                                    ExploitTab.updateFromConfigStatic();

                                    logTextArea.appendText("请切换到「漏洞利用」标签页进行命令执行和内存马注入\n");
                                    logTextArea.appendText("========================================\n\n");
                                }
                            }
                        });

                    } catch (Exception e) {
                        final String errorMsg = e.getMessage();
                        javafx.application.Platform.runLater(() -> {
                            logTextArea.appendText("[EROR]" + (errorMsg != null ? errorMsg : "操作失败，请检查配置") + "\n");
                            // 详细错误信息（调试用）
                            if (errorMsg == null || errorMsg.isEmpty()) {
                                logTextArea.appendText("[DEBUG] " + e.getClass().getSimpleName() + "\n");
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
        shiro550Tab.getChildren().addAll(targetUrlBox, rememberMeBox, exploitEchoChainBox, advancedConfigPane, logBox);
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

        public GlobalComponents(TextField rememberMeField, ComboBox<String> exploitChainComboBox,
                                ComboBox<String> echoGadgetsComboBox, TextArea logArea,
                                ComboBox<String> cryptTypeComboBox) {
            this.rememberMeField = rememberMeField;
            this.exploitChainComboBox = exploitChainComboBox;
            this.echoGadgetsComboBox = echoGadgetsComboBox;
            this.logArea = logArea;
            this.cryptTypeComboBox = cryptTypeComboBox;
        }
    }
}
