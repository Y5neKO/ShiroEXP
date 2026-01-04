package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.GlobalVariable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 设置标签页
 * 提供应用配置和关于信息
 */
public class SettingsTab {
    public VBox getSettingsTab() {
        VBox settingsTab = new VBox();
        settingsTab.setSpacing(15);
        settingsTab.setPadding(new Insets(20));

        // =========================== 标题 ==========================
        Label titleLabel = new Label("设置");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // =========================== 通用设置 ==========================
        TitledPane generalSettingsPane = new TitledPane();
        generalSettingsPane.setText("通用设置");
        generalSettingsPane.setCollapsible(true);
        generalSettingsPane.setExpanded(true);

        VBox generalSettingsContent = new VBox();
        generalSettingsContent.setSpacing(10);
        generalSettingsContent.setPadding(new Insets(10));

        // 默认代理设置
        HBox defaultProxyBox = new HBox();
        defaultProxyBox.setAlignment(Pos.CENTER);
        defaultProxyBox.setSpacing(10);

        Label defaultProxyLabel = new Label("默认代理: ");
        TextField defaultProxyTextField = new TextField();
        defaultProxyTextField.setPromptText("127.0.0.1:8080");
        defaultProxyTextField.setPrefWidth(200);

        // 加载当前全局代理
        String currentProxy = GlobalVariable.getGlobalProxy();
        if (currentProxy != null && !currentProxy.isEmpty()) {
            defaultProxyTextField.setText(currentProxy);
        }

        Button saveProxyButton = new Button("保存");
        saveProxyButton.setOnAction(event -> {
            String proxy = defaultProxyTextField.getText().trim();
            if (proxy.isEmpty()) {
                GlobalVariable.clearGlobalProxy();
                showAlert(Alert.AlertType.INFORMATION, "已清除", "全局代理已清除");
            } else {
                // 验证格式
                if (!proxy.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$")) {
                    showAlert(Alert.AlertType.ERROR, "格式错误", "代理格式应为：ip:port\n例如：127.0.0.1:8080");
                    return;
                }
                GlobalVariable.setGlobalProxy(proxy);
                showAlert(Alert.AlertType.INFORMATION, "保存成功", "全局代理已设置：\n" + proxy + "\n\n所有请求将自动使用此代理");
            }
        });

        defaultProxyBox.getChildren().addAll(defaultProxyLabel, defaultProxyTextField, saveProxyButton);

        // 超时设置
        HBox timeoutBox = new HBox();
        timeoutBox.setAlignment(Pos.CENTER);
        timeoutBox.setSpacing(10);

        Label timeoutLabel = new Label("请求超时(秒): ");
        TextField timeoutTextField = new TextField("30");
        timeoutTextField.setPrefWidth(100);

        timeoutBox.getChildren().addAll(timeoutLabel, timeoutTextField);

        // 线程数设置
        HBox threadBox = new HBox();
        threadBox.setAlignment(Pos.CENTER);
        threadBox.setSpacing(10);

        Label threadLabel = new Label("最大线程数: ");
        TextField threadTextField = new TextField("5");
        threadTextField.setPrefWidth(100);

        threadBox.getChildren().addAll(threadLabel, threadTextField);

        generalSettingsContent.getChildren().addAll(defaultProxyBox, timeoutBox, threadBox);
        generalSettingsPane.setContent(generalSettingsContent);

        // =========================== 外观设置 ==========================
        TitledPane appearancePane = new TitledPane();
        appearancePane.setText("外观设置");
        appearancePane.setCollapsible(true);
        appearancePane.setExpanded(false);

        VBox appearanceContent = new VBox();
        appearanceContent.setSpacing(10);
        appearanceContent.setPadding(new Insets(10));

        // 主题选择
        HBox themeBox = new HBox();
        themeBox.setAlignment(Pos.CENTER);
        themeBox.setSpacing(10);

        Label themeLabel = new Label("主题: ");
        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("浅色", "深色");
        themeComboBox.setValue("浅色");
        themeComboBox.setPromptText("选择主题");

        Button applyThemeButton = new Button("应用");
        applyThemeButton.setOnAction(event -> {
            showAlert(Alert.AlertType.INFORMATION, "主题切换", "主题切换功能暂不支持");
        });

        themeBox.getChildren().addAll(themeLabel, themeComboBox, applyThemeButton);

        // 字体大小
        HBox fontSizeBox = new HBox();
        fontSizeBox.setAlignment(Pos.CENTER);
        fontSizeBox.setSpacing(10);

        Label fontSizeLabel = new Label("字体大小: ");
        Slider fontSizeSlider = new Slider(10, 20, 12);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(2);
        fontSizeSlider.setMinorTickCount(1);
        fontSizeSlider.setSnapToTicks(true);

        fontSizeBox.getChildren().addAll(fontSizeLabel, fontSizeSlider);

        appearanceContent.getChildren().addAll(themeBox, fontSizeBox);
        appearancePane.setContent(appearanceContent);

        // =========================== 添加所有组件 ==========================
        settingsTab.getChildren().addAll(titleLabel, generalSettingsPane, appearancePane);
        return settingsTab;
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
