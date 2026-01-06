package com.y5neko.shiroexp.ui.tabpane;

import com.y5neko.shiroexp.config.GlobalVariable;
import com.y5neko.shiroexp.config.AllList;
import com.y5neko.shiroexp.misc.ConfigManager;
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

        // DNSLog 域名设置
        HBox dnslogBox = new HBox();
        dnslogBox.setAlignment(Pos.CENTER);
        dnslogBox.setSpacing(10);

        Label dnslogLabel = new Label("DNSLog 域名:");
        TextField dnslogTextField = new TextField();
        dnslogTextField.setPromptText("例: dnslog.cn");
        dnslogTextField.setPrefWidth(200);

        // 加载已保存的 DNSLog 配置
        ConfigManager.loadConfig();
        String currentDnslogDomain = ConfigManager.getDnslogDomain();
        if (currentDnslogDomain != null && !currentDnslogDomain.isEmpty()) {
            dnslogTextField.setText(currentDnslogDomain);
        }

        Button saveDnslogButton = new Button("保存");
        saveDnslogButton.setOnAction(event -> {
            String domain = dnslogTextField.getText().trim();

            if (domain.isEmpty()) {
                ConfigManager.clearDnslogDomain();
                showAlert(Alert.AlertType.INFORMATION, "已清除", "DNSLog 域名已清除");
                return;
            }

            // 简单的域名格式验证
            if (!domain.matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                showAlert(Alert.AlertType.ERROR, "格式错误", "域名格式不正确\n正确格式：dnslog.cn 或 test.dnslog.cn");
                return;
            }

            // 保存配置
            ConfigManager.setDnslogDomain(domain);
            showAlert(Alert.AlertType.INFORMATION, "保存成功", "DNSLog 域名已保存：\n" + domain + "\n\nDNSLog Echo 探测将使用此域名");
        });

        dnslogBox.getChildren().addAll(dnslogLabel, dnslogTextField, saveDnslogButton);

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

        generalSettingsContent.getChildren().addAll(defaultProxyBox, dnslogBox, timeoutBox, threadBox);
        generalSettingsPane.setContent(generalSettingsContent);

        // =========================== 探测类设置 ===========================
        TitledPane detectClassSettingsPane = new TitledPane();
        detectClassSettingsPane.setText("探测类设置");
        detectClassSettingsPane.setCollapsible(true);
        detectClassSettingsPane.setExpanded(true);

        VBox detectClassContent = new VBox();
        detectClassContent.setSpacing(10);
        detectClassContent.setPadding(new Insets(10));

        // 说明文本
        Label descriptionLabel = new Label("自定义需要探测的类名，一行一个");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #666;");

        // TextArea 输入框
        TextArea customClassesTextArea = new TextArea();
        customClassesTextArea.setPromptText("输入自定义类名，一行一个\n例如: com.example.CustomClass\n以#开头的行会被视为注释");
        customClassesTextArea.setPrefRowCount(8);

        // 加载已保存的自定义类（包含注释）
        String savedContent = ConfigManager.getCustomClassesRaw();
        if (!savedContent.isEmpty()) {
            customClassesTextArea.setText(savedContent);
        }

        // 按钮和统计信息区域
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setSpacing(15);

        Button saveButton = new Button("保存自定义类");

        Label statsLabel = new Label();
        String[] savedClasses = ConfigManager.getCustomClasses();
        if (savedClasses.length > 0) {
            statsLabel.setText("当前已保存 " + savedClasses.length + " 个自定义类");
        }
        statsLabel.setStyle("-fx-text-fill: #666;");

        saveButton.setOnAction(event -> {
            String text = customClassesTextArea.getText();
            if (text == null || text.trim().isEmpty()) {
                // 清空所有自定义类
                ConfigManager.clearCustomClasses();
                URLDNSTab.updateClassList();
                showAlert(Alert.AlertType.INFORMATION, "保存成功", "已清除所有自定义类");
                statsLabel.setText("当前已保存 0 个自定义类");
                customClassesTextArea.clear(); // 清空输入框
                return;
            }

            // 去除空行，保留注释和有效类
            String[] lines = text.split("\n");
            StringBuilder filteredContent = new StringBuilder();
            int validCount = 0;
            int commentCount = 0;
            int emptyCount = 0;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    emptyCount++;
                    continue; // 跳过空行
                }

                // 添加到过滤后的内容
                if (filteredContent.length() > 0) {
                    filteredContent.append("\n");
                }
                filteredContent.append(trimmed);

                // 统计
                if (trimmed.startsWith("#")) {
                    commentCount++;
                } else {
                    validCount++;
                }
            }

            // 保存过滤后的内容
            String finalContent = filteredContent.toString();
            ConfigManager.setCustomClasses(finalContent);

            // 同步到URLDNSTab
            URLDNSTab.updateClassList();

            // 更新输入框（去除空行后）
            customClassesTextArea.setText(finalContent);

            showAlert(Alert.AlertType.INFORMATION, "保存成功",
                String.format("已保存 %d 个自定义类，%d 行注释%s",
                    validCount,
                    commentCount,
                    emptyCount > 0 ? String.format("，已去除 %d 个空行", emptyCount) : ""));
            statsLabel.setText("当前已保存 " + validCount + " 个自定义类");
        });

        // 清空按钮
        Button clearButton = new Button("清空");
        clearButton.setOnAction(event -> {
            customClassesTextArea.clear();
        });

        buttonBox.getChildren().addAll(saveButton, clearButton, statsLabel);

        detectClassContent.getChildren().addAll(descriptionLabel, customClassesTextArea, buttonBox);
        detectClassSettingsPane.setContent(detectClassContent);

        // =========================== 添加所有组件 ==========================
        settingsTab.getChildren().addAll(titleLabel, generalSettingsPane, detectClassSettingsPane);
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
