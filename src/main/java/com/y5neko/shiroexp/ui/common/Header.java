package com.y5neko.shiroexp.ui.common;

import com.y5neko.shiroexp.copyright.Copyright;
import com.y5neko.shiroexp.ui.event.Components;
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("检查更新");
        alert.setHeaderText(null);
        alert.setContentText("当前已是最新版本 v0.2");
        alert.showAndWait();
    }
}
