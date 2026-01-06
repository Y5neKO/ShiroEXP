package com.y5neko.shiroexp.ui.dialog;

import com.y5neko.shiroexp.misc.DNSLogParser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * DNSLog结果速查对话框
 */
public class DNSLogQuickCheckDialog extends Stage {

    private TextArea inputTextArea;
    private TextArea resultTextArea;
    private Label statsLabel;

    public DNSLogQuickCheckDialog() {
        setTitle("DNSLog结果解析");
        initUI();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        VBox root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(20));

        // ==================== 输入区域 ====================
        Label inputLabel = new Label("请粘贴DNSLog平台的完整结果：");
        inputLabel.setStyle("-fx-font-weight: bold;");

        inputTextArea = new TextArea();
        inputTextArea.setPromptText("示例：\n" +
            "38    TCPEndpoint-W9C175Ce.a96605e270.ddns.1433.eu.org.\n" +
            "37    LiveRef-zRI7MI95.a96605e270.ddns.1433.eu.org.\n" +
            "36    TemplatesImpl-jAOyF3NH.a96605e270.ddns.1433.eu.org.");
        inputTextArea.setPrefRowCount(10);

        HBox inputButtonBox = new HBox();
        inputButtonBox.setSpacing(10);

        Button parseButton = new Button("解析");

        Button clearButton = new Button("清空");

        inputButtonBox.getChildren().addAll(parseButton, clearButton);

        // ==================== 结果区域 ====================
        Label resultLabel = new Label("解析结果：");
        resultLabel.setStyle("-fx-font-weight: bold;");

        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setPrefRowCount(15);
        resultTextArea.setStyle("-fx-font-family: 'Monaco', 'Menlo', monospace; -fx-font-size: 12px;");

        HBox resultButtonBox = new HBox();
        resultButtonBox.setSpacing(10);

        Button copyButton = new Button("复制全部");

        statsLabel = new Label("等待解析...");
        statsLabel.setStyle("-fx-text-fill: #666;");

        resultButtonBox.getChildren().addAll(copyButton, statsLabel);

        // ==================== 事件处理 ====================
        parseButton.setOnAction(event -> parseDNSLog());

        clearButton.setOnAction(event -> {
            inputTextArea.clear();
            resultTextArea.clear();
            statsLabel.setText("等待解析...");
        });

        copyButton.setOnAction(event -> copyAllResults());

        // ==================== 布局组装 ====================
        root.getChildren().addAll(
            inputLabel,
            inputTextArea,
            inputButtonBox,
            new Separator(),
            resultLabel,
            resultTextArea,
            resultButtonBox
        );

        Scene scene = new Scene(root, 700, 600);
        setScene(scene);
    }

    /**
     * 解析DNSLog文本
     */
    private void parseDNSLog() {
        String input = inputTextArea.getText();
        if (input == null || input.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "输入为空", "请先粘贴DNSLog结果");
            return;
        }

        try {
            // 异步解析避免阻塞UI
            resultTextArea.setText("正在解析...");
            statsLabel.setText("解析中...");

            new Thread(() -> {
                DNSLogParser.DNSLogParseResult result = DNSLogParser.parse(input);

                Platform.runLater(() -> {
                    String formatted = result.formatAll();
                    resultTextArea.setText(formatted);

                    statsLabel.setText(
                        String.format("找到 %d 个有效依赖，%d 个未知类",
                            result.getMatchedCount(),
                            result.getUnknownCount())
                    );
                });
            }).start();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "解析失败", "解析过程中发生错误：\n" + e.getMessage());
            resultTextArea.clear();
            statsLabel.setText("解析失败");
        }
    }

    /**
     * 复制全部结果（仅复制匹配成功的完整类名）
     */
    private void copyAllResults() {
        String text = resultTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "无内容", "没有可复制的内容");
            return;
        }

        // 从结果中提取完整类名
        StringBuilder classNames = new StringBuilder();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("→")) {
                String className = line.trim().substring(1).trim();
                classNames.append(className).append("\n");
            }
        }

        if (classNames.length() == 0) {
            showAlert(Alert.AlertType.WARNING, "无匹配结果", "没有找到匹配的完整类名");
            return;
        }

        // 复制到剪贴板
        try {
            StringSelection selection = new StringSelection(classNames.toString().trim());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);

            showAlert(Alert.AlertType.INFORMATION, "复制成功",
                String.format("已复制 %d 个完整类名到剪贴板",
                    classNames.toString().split("\n").length));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "复制失败", "复制到剪贴板失败：\n" + e.getMessage());
        }
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
