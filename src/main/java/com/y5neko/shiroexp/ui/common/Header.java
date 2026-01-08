package com.y5neko.shiroexp.ui.common;

import com.y5neko.shiroexp.config.UpdateCache;
import com.y5neko.shiroexp.copyright.Copyright;
import com.y5neko.shiroexp.service.GitHubUpdateService;
import com.y5neko.shiroexp.ui.event.Components;
import com.y5neko.shiroexp.util.VersionComparator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

import static com.y5neko.shiroexp.config.GlobalVariable.icon;

public class Header {
    private MenuBar menuBar;

    private double xOffset = 0;
    private double yOffset = 0;

    public HBox getTitleBar(Stage primaryStage){
        /*
          设置一个网格视图作为菜单栏
         */
        // 创建一个GridPane并设置其列宽为百分比，以便它们平均分布
        GridPane gridPaneToolBar = new GridPane();gridPaneToolBar.setPadding(new Insets(0, 0, 0, 0));gridPaneToolBar.setHgap(10);gridPaneToolBar.setVgap(10); // 行之间的垂直间距
        // 为GridPane添加三列，并设置它们的百分比宽度
        ColumnConstraints columnToolBar1 = new ColumnConstraints(Region.USE_COMPUTED_SIZE, 33.3, Double.MAX_VALUE);columnToolBar1.setHgrow(Priority.ALWAYS);columnToolBar1.setPercentWidth(33.3);
        ColumnConstraints columnToolBar2 = new ColumnConstraints(Region.USE_COMPUTED_SIZE, 33.4, Double.MAX_VALUE);columnToolBar2.setHgrow(Priority.ALWAYS);columnToolBar2.setPercentWidth(33.4);
        ColumnConstraints columnToolBar3 = new ColumnConstraints(Region.USE_COMPUTED_SIZE, 33.3, Double.MAX_VALUE);columnToolBar3.setHgrow(Priority.ALWAYS);columnToolBar3.setPercentWidth(33.3);
        gridPaneToolBar.getColumnConstraints().addAll(columnToolBar1, columnToolBar2, columnToolBar3);
        HBox.setHgrow(gridPaneToolBar, Priority.ALWAYS);
        // 设置第一个网格为标题栏
        HBox toolBox = new HBox();
        toolBox.setSpacing(2);
        toolBox.setPadding(new Insets(3, 0, 2, 5));
        Image imageIcon = icon;
        ImageView imageViewIcon = new ImageView(imageIcon);
        imageViewIcon.setFitHeight(23);
        imageViewIcon.setPreserveRatio(true);
        buildMenu();
        toolBox.getChildren().add(imageViewIcon);
        toolBox.getChildren().add(menuBar);
        gridPaneToolBar.add(toolBox, 0, 0, 1, 1);
        GridPane.setHalignment(toolBox, HPos.LEFT);
        // 设置第二个网格为标题
        Label titleLabel = new Label("ShiroEXP");
        titleLabel.setFont(new Font("Consolas Bold", 20));

        gridPaneToolBar.add(titleLabel, 1, 0, 1, 1);
        GridPane.setHalignment(titleLabel, HPos.CENTER);
        GridPane.setValignment(titleLabel, VPos.CENTER);
        // 设置第三个网格为窗口操作按钮
        HBox buttonBox = new HBox();
        // 关闭按钮
        Button buttonClose = Components.getImgButton("img/CloseButton.png");
        buttonClose.setOnAction(e -> {
            primaryStage.close();
            System.exit(0);
        });
        Button buttonMin = Components.getImgButton("img/MinButton.png");
        buttonMin.setOnAction(e -> primaryStage.setIconified(true));
        Button buttonMax = Components.getImgButton("img/MaxButton.png");
        buttonMax.setOnAction(e -> {
            e.consume();
            Components.minimizeToTray(primaryStage);
        });
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(buttonClose, buttonMax, buttonMin);
        gridPaneToolBar.add(buttonBox, 2, 0, 1, 1);GridPane.setHalignment(buttonBox, HPos.RIGHT);GridPane.setValignment(buttonBox, VPos.CENTER);

        /*
          创建一个顶部模拟状态栏
         */
        HBox titleBar = new HBox();
        // 绑定拖拽事件
        titleBar.setOnMousePressed(this::handleMousePressed);
        titleBar.setOnMouseDragged(this::handleMouseDragged);
        menuBar.setOnMousePressed(this::handleMousePressed);
        menuBar.setOnMouseDragged(this::handleMouseDragged);

        // 添加一个网格视图
        titleBar.getChildren().add(gridPaneToolBar);
        titleBar.setAlignment(Pos.CENTER); // 居中布局
        titleBar.setPadding(new Insets(0, 0, 0, 0));
        titleBar.setSpacing(0);   // 设置标题栏内间距
        titleBar.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        return titleBar;
    }

    /**
     * 构建顶部菜单
     */
    private void buildMenu(){
        menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: transparent;");
        menuBar.setPadding(new Insets(0));

        // 只保留帮助菜单
        Menu helpMenu = new Menu("帮助");
        menuBar.getMenus().addAll(helpMenu);

        // 关于按钮
        MenuItem aboutButton = new MenuItem("关于");
        helpMenu.getItems().addAll(aboutButton);
        aboutButton.setOnAction(event -> showAboutDialog());

        // 检查更新按钮
        MenuItem checkUpdateButton = new MenuItem("检查更新");
        helpMenu.getItems().add(checkUpdateButton);
        checkUpdateButton.setOnAction(event -> showCheckUpdateDialog());
    }

    /**
     * 处理鼠标按下事件
     */
    private void handleMousePressed(MouseEvent event) {
        // 获取鼠标相对于窗口的坐标
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    /**
     * 处理鼠标拖动事件
     * @param event 鼠标事件
     */
    private void handleMouseDragged(MouseEvent event) {
        // 计算窗口的新位置（基于鼠标移动的距离）
        double newX = event.getScreenX() - xOffset;
        double newY = event.getScreenY() - yOffset;

        // 移动窗口到新位置
        Stage stage = (Stage) ((event.getSource() instanceof Node) ? ((Node) event.getSource()).getScene().getWindow() : null);
        if (stage != null) {
            stage.setX(newX);
            stage.setY(newY);
        }
    }

    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 ShiroEXP");
        alert.setHeaderText(String.format("ShiroEXP %s", Copyright.version));

        String content = "Apache Shiro 漏洞利用工具\n\n" +
                "功能特性:\n" +
                "- Shiro550\n" +
                "- Shiro721\n" +
                "- URLDNS依赖探测\n" +
                "- FindClassByBomb类探测\n" +
                "- 利用/回显链爆破\n" +
                "- 命令执行\n" +
                "- 内存马注入/自定义内存马\n" +
                "- 全局代理\n\n" +
                "作者: Y5neKO\n" +
                "GitHub: https://github.com/Y5neKO\n\n" +
                "免责声明:\n" +
                "本工具仅可用作学习用途和授权渗透测试，\n" +
                "使用本工具造成的后果由使用者自行承担。";

        alert.setContentText(content);
        alert.getDialogPane().setMinWidth(400);
        alert.showAndWait();
    }

    /**
     * 显示检查更新对话框
     */
    private void showCheckUpdateDialog() {
        // 检查缓存
        GitHubUpdateService.GitHubReleaseInfo cachedInfo = UpdateCache.getCachedReleaseInfo();
        if (cachedInfo != null) {
            // 缓存未过期，直接使用缓存数据
            VersionComparator.CompareResult result = VersionComparator.compare(Copyright.version, cachedInfo.getVersion());
            showUpdateResult(result, cachedInfo);
            return;
        }

        // 显示加载提示
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("检查更新");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("正在检查更新，请稍候...");
        loadingAlert.show();

        // 异步检查更新
        Task<GitHubUpdateService.GitHubReleaseInfo> checkUpdateTask = new Task<GitHubUpdateService.GitHubReleaseInfo>() {
            @Override
            protected GitHubUpdateService.GitHubReleaseInfo call() throws Exception {
                return GitHubUpdateService.checkForUpdates();
            }
        };

        checkUpdateTask.setOnSucceeded(event -> {
            loadingAlert.close();
            GitHubUpdateService.GitHubReleaseInfo releaseInfo = checkUpdateTask.getValue();

            // 保存到缓存
            UpdateCache.setCache(releaseInfo);

            VersionComparator.CompareResult result = VersionComparator.compare(Copyright.version, releaseInfo.getVersion());
            showUpdateResult(result, releaseInfo);
        });

        checkUpdateTask.setOnFailed(event -> {
            loadingAlert.close();
            Throwable error = checkUpdateTask.getException();
            showErrorDialog(error);
        });

        // 在后台线程执行
        Thread thread = new Thread(checkUpdateTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 显示更新结果
     */
    private void showUpdateResult(VersionComparator.CompareResult compareResult, GitHubUpdateService.GitHubReleaseInfo releaseInfo) {
        Alert alert;

        // 去除 Markdown 语法的更新日志
        String cleanReleaseNotes = cleanMarkdown(releaseInfo.getReleaseNotes());

        switch (compareResult) {
            case NEWER:
                // 发现新版本
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("发现新版本");
                alert.setHeaderText(null);

                String content = "发现新版本！\n\n" +
                        "当前版本: " + Copyright.version + "\n" +
                        "最新版本: " + releaseInfo.getVersion() + "\n" +
                        "发布时间: " + releaseInfo.getPublishedAt() + "\n\n" +
                        "更新日志:\n" +
                        "----------\n" +
                        (cleanReleaseNotes != null && !cleanReleaseNotes.isEmpty()
                            ? cleanReleaseNotes
                            : "暂无更新说明") + "\n" +
                        "----------\n\n" +
                        "是否前往下载？";

                alert.setContentText(content);

                ButtonType downloadButton = new ButtonType("前往下载");
                ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(downloadButton, cancelButton);

                Optional<ButtonType> buttonResult = alert.showAndWait();
                if (buttonResult.isPresent() && buttonResult.get() == downloadButton) {
                    openBrowser(releaseInfo.getHtmlUrl());
                }
                break;

            case EQUAL:
                // 已是最新版本
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("检查更新");
                alert.setHeaderText(null);

                String latestContent = "当前版本: " + Copyright.version + "\n\n" +
                        "已是最新版本\n\n" +
                        "最新版本更新日志:\n" +
                        "----------\n" +
                        (cleanReleaseNotes != null && !cleanReleaseNotes.isEmpty()
                            ? cleanReleaseNotes
                            : "暂无更新说明") + "\n" +
                        "----------";

                alert.setContentText(latestContent);
                alert.showAndWait();
                break;

            case OLDER:
                // 本地版本更新
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("检查更新");
                alert.setHeaderText(null);

                String newerContent = "当前版本: " + Copyright.version + "\n" +
                        "远程版本: " + releaseInfo.getVersion() + "\n\n" +
                        "您的版本比最新版本还要新，可能是测试版本\n\n" +
                        "最新版本更新日志:\n" +
                        "----------\n" +
                        (cleanReleaseNotes != null && !cleanReleaseNotes.isEmpty()
                            ? cleanReleaseNotes
                            : "暂无更新说明") + "\n" +
                        "----------";

                alert.setContentText(newerContent);
                alert.showAndWait();
                break;

            default:
                break;
        }
    }

    /**
     * 去除 Markdown 语法
     * 将 Markdown 格式转换为纯文本
     */
    private String cleanMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String text = markdown;

        // 去除代码块语法 (```code```)
        text = text.replaceAll("```[^\\n]*\\n([\\s\\S]*?)```", "$1");

        // 去除加粗语法 (**text** 或 __text__)
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        text = text.replaceAll("__([^_]+)__", "$1");

        // 去除斜体语法 (*text* 或 _text_)
        text = text.replaceAll("(?<!\\*)\\*(?!\\*)([^*]+)\\*(?!\\*)", "$1");
        text = text.replaceAll("(?<!_)_(?!_)([^_]+)_(?!_)", "$1");

        // 去除删除线语法 (~~text~~)
        text = text.replaceAll("~~([^~]+)~~", "$1");

        // 去除行内代码语法 (`code`)
        text = text.replaceAll("`([^`]+)`", "$1");

        // 去除链接语法
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");

        // 去除图片语法
        text = text.replaceAll("!\\[([^\\]]*)\\]\\([^\\)]+\\)", "[$1]");

        // 去除 Emoji
        text = removeEmoji(text);

        // 按行处理
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            // 去除标题语法 (# ## ### 等)
            line = line.replaceAll("^#+\\s+", "");

            // 去除无序列表语法 (- 或 * 开头)
            line = line.replaceAll("^[\\-\\*]\\s+", "• ");

            // 去除有序列表语法 (1. 2. 等)
            line = line.replaceAll("^\\d+\\.\\s+", "• ");

            // 去除引用语法 (> 开头)
            line = line.replaceAll("^>\\s+", "");

            // 去除分隔线 (--- 或 ***)
            line = line.replaceAll("^[-*]{3,}\\s*$", "----------");

            result.append(line).append("\n");
        }

        text = result.toString();

        // 去除多余的空行
        text = text.replaceAll("\\n{3,}", "\n\n");

        return text.trim();
    }

    /**
     * 去除 Emoji 表情
     */
    private String removeEmoji(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 去除常见 Emoji (Unicode 范围)
        // Emoji 的主要 Unicode 范围：
        // - Miscellaneous Symbols: U+2600–U+26FF
        // - Dingbats: U+2700–U+27BF
        // - Emoticons: U+1F600–U+1F64F
        // - Symbols and Pictographs: U+1F300–U+1F5FF
        // - Transport and Map: U+1F680–U+1F6FF
        // - Miscellaneous Symbols and Pictographs: U+1F900–U+1F9FF
        // - Supplemental Symbols and Pictographs: U+1FA00–U+1FA6F

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            int codePoint = text.codePointAt(i);

            // 跳过 Emoji Unicode 范围
            if ((codePoint >= 0x2600 && codePoint <= 0x26FF) ||      // Miscellaneous Symbols
                (codePoint >= 0x2700 && codePoint <= 0x27BF) ||      // Dingbats
                (codePoint >= 0x1F600 && codePoint <= 0x1F64F) ||     // Emoticons
                (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) ||     // Symbols and Pictographs
                (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) ||     // Transport and Map
                (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) ||     // Miscellaneous Symbols and Pictographs
                (codePoint >= 0x1FA00 && codePoint <= 0x1FA6F) ||     // Supplemental Symbols
                (codePoint >= 0x1FA70 && codePoint <= 0x1FAFF) ||     // Symbols and Pictographs Extended-A
                (codePoint >= 0x231A && codePoint <= 0x23FF)) {       // Miscellaneous Technical
                // 跳过 Emoji
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i++; // 跳过代理对的低位字符
                }
                continue;
            }

            result.appendCodePoint(codePoint);
        }

        return result.toString();
    }

    /**
     * 显示错误对话框
     */
    private void showErrorDialog(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("检查更新失败");
        alert.setHeaderText(null);

        String errorMessage = error.getMessage();
        if (errorMessage == null) {
            errorMessage = error.getClass().getSimpleName();
        }

        String content = "检查更新失败\n\n" +
                "错误信息: " + errorMessage + "\n\n" +
                "可能原因:\n" +
                "1. 网络连接不可用\n" +
                "2. GitHub API 访问受限\n" +
                "3. 代理配置错误\n\n" +
                "请稍后重试或访问 GitHub 查看最新版本";

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 打开浏览器访问指定 URL
     */
    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // 如果不支持 Desktop，提示用户手动访问
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("打开链接");
                alert.setHeaderText(null);
                alert.setContentText("请手动复制链接到浏览器:\n\n" + url);
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("打开链接失败");
            alert.setHeaderText(null);
            alert.setContentText("无法打开浏览器\n\n请手动访问:\n" + url);
            alert.showAndWait();
        }
    }
}
