
import javafx.event.EventHandler;

import java.util.Optional;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class TableAppPasswordDialogController {

    private TableAppController primaryController;
    private String dialogType;
    private EventHandler<MouseEvent> pressHandler;
    private EventHandler<MouseEvent> releaseHandler;
    private String result;
    private Stage stage;

    @FXML
    private MaterialIconView icon;
    @FXML
    private Hyperlink lblForgerPwd;
    @FXML
    private VBox parent;
    @FXML
    private AnchorPane parentHolder;
    @FXML
    private PasswordField tfEnterPwd;
    @FXML
    private TextField tfPwdUnmask;
    @FXML
    private Label errorMsg;

    public void initialize(Stage stage,TableAppController controller, String type) {
        this.stage = stage;
        this.primaryController = controller;
        this.dialogType = type;
        if(dialogType.equals("CHECK_PASSWORD") || dialogType.equals("FORGET_PASSWORD"))
        {
            parentHolder.setPrefHeight(220);
        }

        this.result = "";
        this.tfPwdUnmask.setManaged(false);

        this.pressHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MaterialIconView iconView = (MaterialIconView) event.getSource();
                iconView.setIcon(MaterialIcon.VISIBILITY_OFF);
                PasswordField pField = (PasswordField) ((StackPane) iconView.getParent()).getChildren().get(1);
                TextField tField = (TextField) ((StackPane) iconView.getParent()).getChildren().get(0);

                pField.setManaged(false);
                pField.setVisible(false);

                tField.setManaged(true);
                tField.setVisible(true);

                tField.setText(pField.getText());

            }

        };

        this.releaseHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                MaterialIconView iconView = (MaterialIconView) event.getSource();
                iconView.setIcon(MaterialIcon.VISIBILITY);
                PasswordField pField = (PasswordField) ((StackPane) iconView.getParent()).getChildren().get(1);
                TextField tField = (TextField) ((StackPane) iconView.getParent()).getChildren().get(0);

                pField.setManaged(true);
                pField.setVisible(true);

                tField.setManaged(false);
                tField.setVisible(false);

            }
        };

        icon.setOnMousePressed(pressHandler);
        icon.setOnMouseReleased(releaseHandler);

        setDialog();

    }

    @FXML
    void forgetPassword(ActionEvent event)
    {
        dialogType = "FORGET_PASSWORD";

        Label lblQuestion = new Label(AppInfo.forgetQuestion);
        lblQuestion.getStyleClass().add("label-forget-question");
        
        TextField tfAnswer = new TextField();
        tfAnswer.getStyleClass().add("txt-field");
        tfAnswer.setPrefWidth(200);
        tfAnswer.setMaxWidth(200);
        tfAnswer.setPromptText("Enter Answer");

        parent.getChildren().clear();
        parent.getChildren().addAll(lblQuestion,tfAnswer);
    }

    @FXML
    void okClicked(ActionEvent event) {

        errorMsg.setText("");

        if(dialogType.equals("CHECK_PASSWORD"))
        {
            boolean isOk = verifyCheckPassword();
            if(isOk)
            {
                result = "OK";
                stage.close();
            }
            else{
                result = "";
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Wrong Password");
                alert.setHeaderText(null);
                alert.setContentText("Do you want to Try again?");
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get().getText().equals("OK"))
                {
                    return;
                }
                else{
                    stage.close();
                }
            }
        }

        else if(dialogType.equals("SET_PASSWORD"))
        {
            boolean isOk = verifySetPassword();
            if(isOk)
            {
                result = "OK";
                stage.close();
            }
            else{
                result = "";
                return;
            }
        }
        else if(dialogType.equals("CHANGE_PASSWORD"))
        {
            boolean isOk = verifyChangePassword();
            if(isOk)
            {
                result = "OK";
                stage.close();
            }
            else{
                result = "";
                return;
            }
        }
        else if(dialogType.equals("FORGET_PASSWORD"))
        {
            if(verifyForgetPassword())
            {
                result = "OK";
                AppInfo.resetPassword();
                DBA.resetMeta();
                stage.close();
            }
            else{
                result = "";
                stage.close();
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Wrong Answer");
                alert.setHeaderText(null);
                alert.setContentText("Incorrect Answer. Cannot open App");
                alert.show();
            }
        }

    }

    private boolean verifyForgetPassword()
    {
        String answer = ((TextField)parent.getChildren().get(1)).getText();
        if(answer.trim().equals(""))
        {
            errorMsg.setText("Answer cannot be empty");
        }
        if(answer.equalsIgnoreCase(AppInfo.forgetAnswer))
        {
            return true;
        }
        return false;

    }

    private boolean verifyChangePassword() {

        String oldPwd = ((PasswordField)((StackPane)parent.getChildren().get(0)).getChildren().get(1)).getText();
        String newPwd = ((PasswordField)((StackPane)parent.getChildren().get(1)).getChildren().get(1)).getText();
        String reEnterPwd = ((PasswordField)((StackPane)parent.getChildren().get(2)).getChildren().get(1)).getText();

        if(!oldPwd.equals(AppInfo.pwd))
        {
            errorMsg.setText("Old Password is not Correct!");
            return false;
        }
        if(newPwd.isEmpty() || reEnterPwd.isEmpty())
        {
            errorMsg.setText("Password cannot be empty");
            return false;
        }
        if(!newPwd.equals(reEnterPwd))
        {
            errorMsg.setText("New Password and Re-Enter Password must be Same!");
            return false;
        }
        if(newPwd.length() < 4)
        {
            errorMsg.setText("Password length should be atleast 4 characters!");
            return false;
        }
        if(newPwd.trim().equals(""))
        {
            errorMsg.setText("Password must contain 1 Non-Space character!");
            return false;
        }
        String s = "hi";
        

        AppInfo.pwd = newPwd;
        return true;
    }

    private boolean verifySetPassword() {

        String first = "",second = "";

        first = ((PasswordField)((StackPane)parent.getChildren().get(0)).getChildren().get(1)).getText();
        second = ((PasswordField)((StackPane)parent.getChildren().get(1)).getChildren().get(1)).getText();

        if(first.isEmpty() || second.isEmpty())
        {
            errorMsg.setText("Password Cannot be empty!");
            return false;
        }
        if(first.length() < 4)
        {
            errorMsg.setText("Password length should be atleast 4 characters!");
            return false;
        }
        if(first.trim().equals(""))
        {
            errorMsg.setText("Password must contain 1 Non-Space character!");
            return false;
        }

        ChoiceBox cb = (ChoiceBox)((FlowPane)parent.getChildren().get(2)).getChildren().get(0);
        TextField tf = (TextField)((FlowPane)parent.getChildren().get(2)).getChildren().get(1);

        if(cb.getValue().toString().equals("Select Question") || tf.getText().equals(""))
        {
            errorMsg.setText("Select Question and Write Answer!");
            return false;
        }

        if(first.equals(second))
        {
            AppInfo.setPassword(first, cb.getValue().toString(), tf.getText());
            return true;
        }

        return false;

    }

    private boolean verifyCheckPassword() {

        PasswordField pf = (PasswordField)((StackPane)(parent.getChildren().get(0))).getChildren().get(1);
        String enteredPwd = pf.getText();
        pf.setText("");
        System.out.println("entered pwd: " + enteredPwd);

        String originalPwd = AppInfo.pwd;
        System.out.println("original pwd: " + originalPwd);

        if(enteredPwd.equals(originalPwd))
        {
            return true;
        }

        return false;
    }


    private void setDialog() {


        if(dialogType.equals("CHECK_PASSWORD"))
        {
            tfEnterPwd.requestFocus();
            return;
        }
        else if (dialogType.equals("SET_PASSWORD")) {
            // hyperlink removed
            parent.getChildren().remove(lblForgerPwd);

            StackPane pwdPane = createPasswordField("Re-Enter Password");
            parent.getChildren().add(pwdPane);

            FlowPane questionPane = createChoiceBox();

            parent.getChildren().add(questionPane);

        }
        else if(dialogType.equals("CHANGE_PASSWORD"))
        {
            parent.getChildren().clear();

            StackPane pwdPane1 = createPasswordField("Enter Old Password");
            parent.getChildren().add(pwdPane1);
            StackPane pwdPane2 = createPasswordField("Enter New Password");
            parent.getChildren().add(pwdPane2);
            StackPane pwdPane3 = createPasswordField("Re-Enter New Password");
            parent.getChildren().add(pwdPane3);
        }

    }

    private StackPane createPasswordField(String prompt) {

        StackPane stackPane = new StackPane();
            stackPane.getStyleClass().add("password-holder-stack");
            PasswordField pf = new PasswordField();

            pf.setPromptText(prompt);

            TextField tf = new TextField();
            tf.getStyleClass().add("password-field");
            tf.setManaged(false);
            tf.setVisible(false);

            MaterialIconView micon = new MaterialIconView(MaterialIcon.VISIBILITY);
            micon.getStyleClass().add("blue-icon");
            micon.setGlyphSize(20);

            micon.setTranslateX(110d);
            micon.setOnMousePressed(pressHandler);
            micon.setOnMouseReleased(releaseHandler);

            stackPane.getChildren().addAll(tf, pf, micon);

        return stackPane;
    }

    private FlowPane createChoiceBox()
    {
        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
        flowPane.getStyleClass().add("question-holder");

        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll("Select Question","What is your Favourite Book?","What is your Favourite Food?","What is your Pet Name?");
        ChoiceBox<String> choiceBox = new ChoiceBox<>(items);
        choiceBox.setValue("Select Question");
        choiceBox.getStyleClass().add("question-choice-box");

        TextField tf = new TextField();
        tf.getStyleClass().addAll("password-field","question-password-field");

        flowPane.getChildren().addAll(choiceBox,tf);

        return flowPane;
    }

    public String getResult() {
        return result;
    }


}
