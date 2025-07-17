package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.object.TargetOBJ;
import com.y5neko.shiroexp.payloads.BruteKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Shiro550Tab {
    // 需要进行后续操作的全局组件
    public TextField rememberMeValueTextField;
    public ComboBox<String> exploitChainComboBox;
    public ComboBox<String> echoGadgetsComboBox;
    public ComboBox<String> cryptTypeComboBox;

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

        // ================================第四行日志==================================
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
                            BruteKey.bruteKey(targetOBJ, globalComponents);
                        } catch (Exception e) {
//                            logTextArea.appendText(e.getMessage() + "\n");
                            e.printStackTrace();
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
            logTextArea.appendText("检测回显的逻辑\n");
        });

        clearLogButton.setOnMouseClicked(event -> {
            logTextArea.clear();
        });

        // =============================最后添加所有的VBox到shiro550Tab===========================
        shiro550Tab.getChildren().addAll(targetUrlBox,rememberMeBox, exploitEchoChainBox, logBox);
        return shiro550Tab;
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
