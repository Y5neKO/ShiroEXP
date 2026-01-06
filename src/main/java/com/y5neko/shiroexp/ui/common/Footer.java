package com.y5neko.shiroexp.ui.common;

import com.y5neko.shiroexp.copyright.Copyright;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Footer {
    public HBox getBottomBar(){
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(2, 5, 2, 5));
        bottomBox.setStyle("-fx-background-color: #99ccff;");
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        Label bottomLabel = new Label(String.format("v%s Powered by Y5neKO", Copyright.version));
        bottomLabel.setFont(Font.font(bottomLabel.getFont().getFamily(), FontWeight.BOLD, bottomLabel.getFont().getSize()));
        bottomBox.getChildren().add(bottomLabel);
        return bottomBox;
    }
}
