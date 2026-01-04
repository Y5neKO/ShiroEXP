package com.y5neko.shiroexp.ui.tabpane;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Shiro721 标签页
 * Shiro 1.4.2+ 版本的漏洞利用
 */
public class Shiro721Tab {
    private TextField targetUrlTextField;
    private TextArea logTextArea;

    public VBox getShiro721Tab() {
        VBox shiro721Tab = new VBox();
        shiro721Tab.setSpacing(10);
        shiro721Tab.setPadding(new Insets(10));
        shiro721Tab.getStylesheets().add("css/TextField.css");

        // =========================== 第一行：目标配置 ==========================
        HBox targetConfigBox = new HBox();
        targetConfigBox.setAlignment(Pos.CENTER);
        targetConfigBox.setSpacing(10);

        Label targetUrlLabel = new Label("目标地址: ");
        targetUrlTextField = new TextField();
        targetUrlTextField.setPromptText("http://example.com");
        HBox.setHgrow(targetUrlTextField, javafx.scene.layout.Priority.ALWAYS);

        targetConfigBox.getChildren().addAll(targetUrlLabel, targetUrlTextField);

        // =========================== 第二行：Cookie 配置 ==========================
        HBox cookieConfigBox = new HBox();
        cookieConfigBox.setAlignment(Pos.CENTER);
        cookieConfigBox.setSpacing(10);

        Label cookieLabel = new Label("有效 Cookie: ");
        TextField cookieTextField = new TextField();
        cookieTextField.setPromptText("rememberMe=valid_cookie_value");
        HBox.setHgrow(cookieTextField, javafx.scene.layout.Priority.ALWAYS);

        Button testCookieButton = new Button("测试 Cookie");
        testCookieButton.setOnAction(event -> {
            logTextArea.appendText("[INFO]测试 Cookie 功能待实现\n");
        });

        cookieConfigBox.getChildren().addAll(cookieLabel, cookieTextField, testCookieButton);

        // =========================== 第三行：攻击配置 ==========================
        HBox attackConfigBox = new HBox();
        attackConfigBox.setAlignment(Pos.CENTER);
        attackConfigBox.setSpacing(10);

        Label gadgetLabel = new Label("利用链: ");
        ComboBox<String> gadgetComboBox = new ComboBox<>();
        gadgetComboBox.setPromptText("选择利用链");

        Label attackTypeLabel = new Label("攻击模式: ");
        ComboBox<String> attackTypeComboBox = new ComboBox<>();
        attackTypeComboBox.setPromptText("选择攻击模式");

        Button attackButton = new Button("开始攻击");

        attackConfigBox.getChildren().addAll(gadgetLabel, gadgetComboBox, attackTypeLabel, attackTypeComboBox, attackButton);

        // =========================== 说明区域 ==========================
        TitledPane infoPane = new TitledPane();
        infoPane.setText("关于 Shiro721");
        infoPane.setCollapsible(true);
        infoPane.setExpanded(false);

        TextArea infoTextArea = new TextArea();
        infoTextArea.setEditable(false);
        infoTextArea.setText(
            "Shiro721 是 Apache Shiro 1.4.2+ 版本中存在的一个漏洞。\n\n" +
            "漏洞描述：\n" +
            "Shiro 1.4.2+ 版本中，当用户使用有效的 rememberMe Cookie 时，\n" +
            "攻击者可以通过 Padding Oracle 攻击来解密 Cookie 并构造恶意 payload。\n\n" +
            "利用条件：\n" +
            "1. 目标使用 Shiro 1.4.2+ 版本\n" +
            "2. 需要一个有效的 rememberMe Cookie\n" +
            "3. 目标支持 AES-GCM 加密模式\n\n" +
            "注意：此功能需要较长时间进行 Padding Oracle 攻击，请耐心等待。\n"
        );
        infoPane.setContent(infoTextArea);

        // =========================== 日志区域 ==========================
        HBox logBox = new HBox();
        logBox.setAlignment(Pos.CENTER);
        logBox.setSpacing(10);

        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setPrefHeight(200);
        logBox.getChildren().add(logTextArea);
        HBox.setHgrow(logTextArea, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(logBox, javafx.scene.layout.Priority.ALWAYS);

        // =========================== 添加所有组件 ==========================
        shiro721Tab.getChildren().addAll(targetConfigBox, cookieConfigBox, attackConfigBox, infoPane, logBox);
        return shiro721Tab;
    }
}
