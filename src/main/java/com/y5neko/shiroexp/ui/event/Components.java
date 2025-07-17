package com.y5neko.shiroexp.ui.event;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;

import static com.y5neko.shiroexp.config.GlobalVariable.icon;

public class Components {
    /**
     * 获取图片按钮
     * @param imgPath 图片路径
     * @return 图片按钮
     */
    public static Button getImgButton(String imgPath) {
        Button button = new Button();
        Image imageClose = new Image(imgPath);
        ImageView imageViewClose = new ImageView(imageClose);
        imageViewClose.setFitHeight(20);
        imageViewClose.setPreserveRatio(true);
        button.setGraphic(imageViewClose);
        button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // 仅显示图形内容
        return button;
    }

    /**
     * 将JavaFX Stage最小化到系统托盘
     * @param primaryStage JavaFX Stage对象
     */
    public static void minimizeToTray(Stage primaryStage) {
        Platform.setImplicitExit(false);
        // 隐藏JavaFX Stage
        primaryStage.hide();

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(icon, null);

        TrayIcon trayIcon = getTrayIcon(primaryStage, bufferedImage);
        trayIcon.setImageAutoSize(true);

        // 检查系统托盘是否可用并添加图标
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * 获取TrayIcon对象
     * @param primaryStage JavaFX Stage对象
     * @param bufferedImage BufferedImage对象
     * @return TrayIcon对象
     */
    private static TrayIcon getTrayIcon(Stage primaryStage, BufferedImage bufferedImage) {
        PopupMenu popup = new PopupMenu();
        java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        popup.add(exitItem);

        // 创建TrayIcon并添加事件监听器
        TrayIcon trayIcon = new TrayIcon(bufferedImage, "ShiroEXP", popup);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // 双击托盘图标时恢复窗口
                    Platform.runLater(primaryStage::show);
                    SystemTray.getSystemTray().remove(trayIcon);
                }
            }
        });
        return trayIcon;
    }
}
