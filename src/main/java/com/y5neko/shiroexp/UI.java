package com.y5neko.shiroexp;

import com.y5neko.shiroexp.ui.common.Center;
import com.y5neko.shiroexp.ui.common.Footer;
import com.y5neko.shiroexp.ui.common.Header;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.scenicview.ScenicView;

import static com.y5neko.shiroexp.config.GlobalVariable.icon;

public class UI extends Application {
    // 设置一个BorderPane作为根视图
    BorderPane root = new BorderPane();

    private double dragStartX, dragStartY;
    private double initWidth, initHeight;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // =============================================================Step 1: 创建一个菜单栏=============================================================
        HBox titleBar = new Header().getTitleBar(primaryStage);
        root.setTop(titleBar);


        // =============================================================Step 2: 创建一个中间容器=============================================================
        // 设置一个VBox作为中间主要展示内容
        VBox centerBox = new Center().getCenterBox();
        root.setCenter(centerBox);


        // =============================================================Step 3: 创建一个底部栏=============================================================
        HBox bottomBar = new Footer().getBottomBar();
        root.setBottom(bottomBar);


        // =============================================================Step 4: 外观设计=============================================================
        // 圆角设计
        // 动态绑定 clip 的宽高到 root 的宽高（自动适应窗口大小变化）
        Rectangle clipRectangle = new Rectangle();
        clipRectangle.setArcWidth(20);
        clipRectangle.setArcHeight(20);

        // 创建一个 StackPane 来做边框容器，达成边框效果（root现在不是根容器了）
        StackPane borderedPane = new StackPane();
        borderedPane.setPadding(new Insets(1)); // 设置边框宽度（内边距）
        borderedPane.setStyle("-fx-background-color: lightgray; -fx-background-radius: 11;");
        borderedPane.getChildren().add(root);

        // 绑定宽度和高度到 root 的尺寸
        clipRectangle.widthProperty().bind(root.widthProperty());
        clipRectangle.heightProperty().bind(root.heightProperty());
        root.setClip(clipRectangle);


        // =============================================================Step 5: 处理Scene和Stage=============================================================
        // 开始处理Stage和放入Scene（root现在不是根容器了）
        Scene scene = new Scene(borderedPane);
        scene.setFill(Color.TRANSPARENT);   // 圆角|透明
        scene.getStylesheets().add("css/Style.css");
        scene.getStylesheets().add("css/Tabs.css"); // 加载CSS
        // 设置stage为无状态栏型
        primaryStage.initStyle(StageStyle.TRANSPARENT); // 圆角|透明
        primaryStage.setScene(scene);
        primaryStage.setTitle("ShiroEXP");
        primaryStage.setHeight(800);
        primaryStage.setWidth(1000);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
//        ScenicView.show(scene);


        // ============================================================Step 6: 处理一些绑定事件==========================================================
        scene.setOnMousePressed(event -> {
            dragStartX = event.getScreenX();
            dragStartY = event.getScreenY();
            initWidth = primaryStage.getWidth();
            initHeight = primaryStage.getHeight();
        });
        scene.setOnMouseDragged(event -> {
            double deltaX = event.getScreenX() - dragStartX;
            double deltaY = event.getScreenY() - dragStartY;

            if (event.getSceneX() > (scene.getWidth() - 40) &&
                    event.getSceneY() > (scene.getHeight() - 40)) {
                primaryStage.setWidth(initWidth + deltaX);
                primaryStage.setHeight(initHeight + deltaY);
            } else if (event.getSceneX() > (scene.getWidth() - 5)) {
                primaryStage.setWidth(initWidth + deltaX);
            } else if (event.getSceneY() > (scene.getHeight() - 5)) {
                primaryStage.setHeight(initHeight + deltaY);
            }
        });
        scene.setOnMouseMoved(event -> {
            if (event.getSceneX() > (scene.getWidth() - 40) &&
                    event.getSceneY() > (scene.getHeight() - 40)) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });
    }
}
