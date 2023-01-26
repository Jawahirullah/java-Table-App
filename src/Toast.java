import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class Toast {


    public static void showToast(final String msg,final Stage stage)
    {
        final Popup popup = createPopup(msg);
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                popup.setX(stage.getX() + stage.getWidth()/2.15);
                popup.setY(stage.getY() + 85);
            }
        });

        popup.show(stage);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4),e -> {
            if(popup.isShowing())
            popup.hide();
        }));

        timeline.setCycleCount(1);
        timeline.play();
    }



    private static Popup createPopup(final String message)
    {
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        Label label = new Label(message);

        label.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                popup.hide();
            }
        });

        label.getStylesheets().add("TableStyle.css");
        label.getStyleClass().add("toast");
        label.setAlignment(Pos.CENTER);
        popup.getContent().add(label);

        return popup;
        
    }
}