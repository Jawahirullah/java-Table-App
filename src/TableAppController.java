import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TableAppController {

    private Stage stage;
    private Button previousClickedListItem;
    private String activeButton = "";
    private Button activeToolButton = null;
    private StageCreator stageCreator;
    private boolean showNoTableAvail = false;
    private String selectedTheme = "";
    private ContextMenu visibilityContextMenu;
    private boolean isVisibilityContextShowing;

    @FXML
    private StackPane updateRowHolder;
    @FXML
    private Button btnOk;
    @FXML
    private Button btnNewTable;
    @FXML
    private Button btnAddRow;
    @FXML
    private AnchorPane tableHolder;
    @FXML
    private TableView tableview;
    @FXML
    private VBox listview;
    @FXML
    private Label txtTableTitle;
    @FXML
    private Label txtNoTable;
    @FXML
    private StackPane okBtnHolder;
    @FXML
    private Button btnSettings;
    @FXML
    private FlowPane tfHolder;
    @FXML
    private Button btnNoTableAvailable;
    @FXML
    private CheckBox cbIsLocked;
    @FXML
    private MenuItem menuSetChangePwd;
    @FXML
    private Menu menuTheme;
    @FXML
    private ChoiceBox<String> choiceVisibility;
    @FXML
    private StackPane visibilityOptionHolder;
    @FXML
    private Label errorMsg;


    public void initialize(Stage primaryStage, StageCreator stageCreator) {
        this.stage = primaryStage;
        this.stageCreator = stageCreator;

        this.previousClickedListItem = null;
        TableManager.tableView = this.tableview;
        this.selectedTheme = AppInfo.appTheme;
        setSettings();

        visibilityContextMenu = new ContextMenu();
        visibilityContextMenu.setAutoHide(false);
        visibilityContextMenu.setOnShown(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent arg0) {
                System.out.println("showing");
               isVisibilityContextShowing = true;
                
            }  
        });
        visibilityContextMenu.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent arg0) {
                System.out.println("hiding");
                isVisibilityContextShowing = false;
            }  
        });

        choiceVisibility.setContextMenu(visibilityContextMenu);

        Label placeholder = new Label("NO DATAS IN THE TABLE");
        placeholder.getStyleClass().add("placeholder");
        this.tableview.setPlaceholder(placeholder);

        btnNoTableAvailable.setManaged(false);
        showNoTableAvail = false;

        try (ResultSet rs = DBA.showTables()) {

            while (rs.next()) {
                if (rs.getString(1).equalsIgnoreCase("hello_table_meta")) {
                    continue;
                }
                addListItem(rs.getString(1));
            }
            if (listview.getChildren().size() == 1) {
                btnNoTableAvailable.setVisible(true);
                btnNoTableAvailable.setManaged(true);
                showNoTableAvail = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in creating listview");
        }
    }

    void addListItem(String name) {
        TableManager.allTables.add(name.trim());

        Button listItem = new Button(name.trim().toUpperCase());
        listItem.getStyleClass().add("list-item");

        listItem.setOnAction((e) -> {
            if(TableManager.tableName.equalsIgnoreCase(listItem.getText()))
            {
                return;
            }
            if (tableHolder.isVisible() == false) {
                txtNoTable.setVisible(false);
                tableHolder.setVisible(true);
            }
            if(previousClickedListItem != null)
            {
                previousClickedListItem.setStyle("-fx-backgroun-color: rgb(254, 254, 254);");
            }

            previousClickedListItem = (Button) e.getSource();
            previousClickedListItem.setStyle("-fx-background-color: #ffe936;");
            resetActiveButton(previousClickedListItem.getText(), false);
            showTable(previousClickedListItem.getText());
            setColumnVisibility();
            txtTableTitle.setText(previousClickedListItem.getText());

        });

        MenuItem menu = new MenuItem("Delete Table");
        ContextMenu contextMenu = new ContextMenu(menu);
        listItem.setContextMenu(contextMenu);

        menu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                Alert dialog = new Alert(AlertType.CONFIRMATION);
                dialog.setTitle("Delete Table");
                dialog.setHeaderText(null);

                dialog.getButtonTypes().remove(0);
                ButtonType btnYes = ButtonType.YES;
                dialog.getButtonTypes().add(0, btnYes);

                MenuItem menu = ((MenuItem) e.getSource());

                dialog.setContentText(
                        "Are you sure you want to delete\n'" + listItem.getText().toLowerCase() + "' table ?");
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.get() == ButtonType.YES) {
                    DBA.deleteTable(listItem.getText());
                    listview.getChildren().remove(listItem);

                    if (listview.getChildren().size() == 1) {
                        btnNoTableAvailable.setManaged(true);
                        btnNoTableAvailable.setVisible(true);
                        showNoTableAvail = true;
                    }

                    resetActiveButton(TableManager.tableName, true);
                    refreshTableView(listItem.getText());
                    TableManager.removeTable(listItem.getText());
                }

            }

        });

        listview.getChildren().add(listItem);
        if (listview.getChildren().size() != 1) {
            if (btnNoTableAvailable.isVisible()) {
                btnNoTableAvailable.setVisible(false);
                btnNoTableAvailable.setManaged(false);
                showNoTableAvail = false;
            }
        }

    }

    @FXML
    void searchTable(StringProperty prop, String oldValue, String newValue) {

        oldValue = oldValue.toLowerCase().trim();
        newValue = newValue.toLowerCase().trim();

        if (oldValue.equals("") && newValue.equals("")) {
            return;
        } else if (newValue.startsWith(oldValue)) {
            for (Node btn : listview.getChildren()) {
                String txt = ((Button) btn).getText().toLowerCase();
                if (!txt.startsWith(newValue)) {
                    btn.setVisible(false);
                    btn.setManaged(false);
                }
            }
        } else {
            for (Node btn : listview.getChildren()) {
                String txt = ((Button) btn).getText().toLowerCase();
                if (txt.startsWith(newValue)) {
                    if (!btn.isVisible()) {
                        btn.setVisible(true);
                        btn.setManaged(true);
                    }
                } else {
                    if (btn.isVisible()) {
                        btn.setVisible(false);
                        btn.setManaged(false);
                    }
                }
            }
        }
        if (showNoTableAvail) {
            btnNoTableAvailable.setVisible(true);
            btnNoTableAvailable.setManaged(true);
        } else {
            btnNoTableAvailable.setVisible(false);
            btnNoTableAvailable.setManaged(false);
        }

    }

    @FXML
    void createNewTable(ActionEvent event) {

        Scene scene = stageCreator.createStage("TableAppNewTable.fxml");

        Stage newTableStage = new Stage();
        newTableStage.setScene(scene);
        newTableStage.setTitle("Create Table");
        newTableStage.setResizable(false);
        newTableStage.initModality(Modality.APPLICATION_MODAL);
        newTableStage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                newTableStage.setX(stage.getX() + stage.getWidth() / 2 - newTableStage.getWidth() / 2);
                newTableStage.setY(stage.getY() + stage.getHeight() / 2 - newTableStage.getHeight() / 2);
            }
        });
        ((TableAppNewTableController) stageCreator.getFxmlLoader().getController()).initialize(stage, this);
        newTableStage.showAndWait();

    }

    void resetActiveButton(String table, boolean isConform) {
        if(isConform == false && TableManager.isSameTable(table))
        {
            return;
        }
        if (activeButton.equals("")) {
            return;
        } else if (activeButton.equals("addButton")) {
            refreshTfHolder(table, true);
            okBtnHolder.setVisible(false);
        } else if (activeButton.equals("deleteButton")) {
            tableview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            okBtnHolder.setVisible(false);
        } else if (activeButton.equals("editButton")) {
            tableview.setEditable(false);
            tableview.getColumns().forEach(col -> {
                ((TableColumn) col).setEditable(false);
                ((TableColumn) col).setCellFactory(TableManager.callback);
            });
        }
        else if(activeButton.equals("visibilityButton"))
        {
            visibilityOptionHolder.setVisible(false);
            isVisibilityContextShowing = false;
        }

        styleToolButton(false);
        activeButton = "";
        activeToolButton = null;
    }

    boolean showTable(String table) {
        return TableManager.showTableView(table);
    }


    // this method check whether the currently selected table is similar to
    // currently showing table
    // isConform represents do the tfHolder definitely refresh it
    // if both are false it doesn't refresh the tfHolder and returns false
    // if any one of these is true it refreshes the tfHolder and returns true;

    @FXML
    void okClicked(ActionEvent event) {

        if ((this.activeButton).equals("addButton")) 
        {
            if (checkTfHolder()) 
            {
                ObservableList<Node> list = tfHolder.getChildren();
                String[] datas = new String[list.size() + 1];
                datas[0] = Integer.toString(++TableManager.maxno);
                for (int i = 0; i < list.size(); i++) {
                    datas[i + 1] = ((TextField) list.get(i)).getText();
                }

                TableManager.addRow(datas);
                DBA.addRecord(datas);
                refreshTfHolder();
                errorMsg.setText("");
            }
            else{
                errorMsg.setText("Datas are mismatching to Columns!");
                return;
            }
        }
        else if(activeButton.equals("deleteButton")) 
        {
            int deletedRows = TableManager.deleteRow();
            resetActiveButton(TableManager.tableName, true);
        } 
        else if (activeButton.equals("saveButton") || activeButton.equals("editButton")) {
            resetActiveButton(TableManager.tableName, true);
        }

        errorMsg.setText("");

    }

    boolean checkTfHolder() {

        boolean result = true;
        int i = 1;
        for (Node node : tfHolder.getChildren()) {
            String txt = ((TextField) node).getText();
            if (!isSameType(txt, i)) {
                result = false;
                break;
            }
            i++;
        }

        return result;
    }

    boolean refreshTfHolder(String table, boolean isConform) {
        boolean isTfRefreshed = false;

        if (!TableManager.isSameTable(table) || isConform) {
            tfHolder.getChildren().clear();
            isTfRefreshed = true;
        }

        return isTfRefreshed;
    }

    void refreshTfHolder(){

        ObservableList<Node> tfs = tfHolder.getChildren();
        var varHolder = new Object(){ int i = 1; };

        for(Node node : tfs)
        {
            TextField tf = (TextField)node;
            tf.setText("");
            if(varHolder.i++ == 1)
            tf.requestFocus();
        }
    }

    boolean isSameType(String txt, int columnNo) {
        Map<String, String> regex = Map.of("INT", "\\d+", "VARCHAR", ".+", "FLOAT", "([0-9]+)(\\.[0-9]+)?", "DATE",
                "^[0-3][0-9]-[0-3][0-9]-[0-9]{4}$");

        String currentRegex = regex.get(TableManager.columnTypes[columnNo]);
        Pattern pattern = Pattern.compile(currentRegex);
        Matcher matcher = pattern.matcher(txt);

        boolean isValid = matcher.matches();

        return isValid;
    
    }

    // ------------------------Tool Buttons Logic--------------------------

    @FXML
    void addRow(ActionEvent event) 
    {
        boolean toContinue = toolButtonClicked("addButton", (Button) event.getSource());

        if (toContinue == false) {
            return;
        } 

        TextField[] tfs = new TextField[TableManager.columnHeadings.length];

        for (int index = 1; index < (TableManager.columnHeadings).length; index++) {
            tfs[index-1] = new TextField();

            if (TableManager.columnTypes[index].equals("DATE"))
                tfs[index-1].setPromptText((TableManager.columnHeadings)[index].toUpperCase() + " as dd-mm-yyyy");
            else
                tfs[index-1].setPromptText((TableManager.columnHeadings)[index].toUpperCase());

            tfs[index-1].getStyleClass().add("txt-field");
            tfs[index-1].setPrefWidth(145d);

            tfs[index-1].setOnMouseClicked(new EventHandler<Event>() {
                @Override
                public void handle(Event arg0) {
                    errorMsg.setText("");
                }    
            });

            tfs[index-1].focusedProperty().addListener((obs, oldVal, newVal) -> {
                errorMsg.setText("");
            });

            tfHolder.getChildren().add(tfs[index-1]);

        }

        okBtnHolder.setVisible(true);
        setOkBtnTxt("ADD");
        tfHolder.getChildren().get(0).requestFocus();

    }

    @FXML
    void deleteRow(ActionEvent event)
    {
        boolean toContinue = toolButtonClicked("deleteButton", (Button) event.getSource());

        if (toContinue == false) {
            return;
        }

        tableview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        okBtnHolder.setVisible(true);
        setOkBtnTxt("DELETE");
    }

    @FXML
    void editRow(ActionEvent event)
    {
        boolean toContinue = toolButtonClicked("editButton", (Button) event.getSource());

        if (toContinue == false) {
            return;
        }

        tableview.setEditable(true);
        ObservableList<TableColumn<Map<String, String>, String>> columns = tableview.getColumns();

        for (TableColumn<Map<String, String>, String> column : columns) {
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setEditable(true);

            column.setOnEditCommit((CellEditEvent<Map<String, String>, String> e) -> {

                boolean hasDataEntered = false;

                Map<String, String> row = e.getRowValue();
                String newValue = e.getNewValue();
                String colTitle = e.getTableColumn().getText().toLowerCase();

                if (isSameType(newValue, TableManager.getColumnIndex(colTitle))) {
                    DBA.AlterRow(row, newValue, TableManager.getColumnIndex(colTitle));
                    row.put(colTitle, e.getNewValue());
                    hasDataEntered = true;
                    errorMsg.setText("");
                } else {
                    System.out.println("wrong");
                    row.put(colTitle, e.getOldValue());
                    e.getTableColumn().setVisible(false);
                    e.getTableColumn().setVisible(true);
                    errorMsg.setText("Entered Data is mismatching to Column " + colTitle);
                }
                System.out.println(hasDataEntered);

                if (hasDataEntered) {
                    // Toast.showToast("DATA MODIFIED SUCCUSSFULLY", stage);
                    okClicked(event);
                }
            });

            // Toast.showToast("Double Click the Table Cell And \n Press Enter to Insert Data", stage);

        }

    }

    void setColumnVisibility()
    {
        visibilityContextMenu.getItems().clear();

        for(int i = 1; i < TableManager.columnHeadings.length; i++)
        {
            CheckMenuItem item = new CheckMenuItem(TableManager.columnHeadings[i].toUpperCase());
            item.setSelected(true);
            visibilityContextMenu.getItems().add(item);

            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e)
                {
                    int selectedItem = visibilityContextMenu.getItems().indexOf((CheckMenuItem)e.getSource()) + 1;
                    if(item.isSelected())
                    {
                        ((TableColumn)tableview.getColumns().get(selectedItem)).setVisible(true);
                    }
                    else{
                        ((TableColumn)tableview.getColumns().get(selectedItem)).setVisible(false);
                    }
                }
            });

        }
    }

    @FXML
    void showVisibilityContext(MouseEvent event)
    {
        if(isVisibilityContextShowing)
        {
            System.out.println("context showing");
            visibilityContextMenu.hide();
            return;
        }  
        System.out.println("context not showing");
        visibilityContextMenu.show(choiceVisibility, Side.BOTTOM, 0, 0);
    }

    @FXML
    void showVisibilityOption(ActionEvent event)
    {
        boolean toContinue = toolButtonClicked("visibilityButton", (Button) event.getSource());

        if (toContinue == false) {
            return;
        }

        visibilityOptionHolder.setVisible(true);
        isVisibilityContextShowing = false;

    }

    boolean toolButtonClicked(String btnName, Button button) {
        if (activeButton.equals(btnName)) {
            resetActiveButton(TableManager.tableName, true);
            return false;
        }
        resetActiveButton(TableManager.tableName, true);
        this.activeButton = btnName;
        this.activeToolButton = button;
        styleToolButton(true);

        return true;
    }

    // --------------------------------------------------------------

    void styleToolButton(boolean toenable) {

        if (toenable) {

            activeToolButton.setStyle("-fx-background-color: #FFE936;");
            StackPane container = (StackPane) activeToolButton.getParent();
            MaterialIconView icon = (MaterialIconView) container.getChildren().get(1);
            // icon.setStyle("-fx-fill: -fx-main-color;");
        } else {

            activeToolButton.setStyle("-fx-background-color: transparent;");
            StackPane container = (StackPane) activeToolButton.getParent();
            MaterialIconView icon = (MaterialIconView) container.getChildren().get(1);
            // icon.setStyle("-fx-fill: white;");
        }

    }

    void setOkBtnTxt(String txt) {
        // refreshTfHolder(TableManager.tableName, true);
        okBtnHolder.setVisible(true);
        ((Label) okBtnHolder.getChildren().get(1)).setText(txt);

    }

    void refreshTableView(String deletedTable) {
        if (TableManager.tableName.equals(deletedTable)) {
            txtNoTable.setVisible(true);
            tableHolder.setVisible(false);
            txtTableTitle.setText("");
        }
    }

    private void setSettings() {

        if (AppInfo.locked) {
            cbIsLocked.setSelected(true);
        } else {
            cbIsLocked.setSelected(false);
        }

        if (AppInfo.pwdSet) {
            menuSetChangePwd.setText("Change Password");
            ;
        } else {
            menuSetChangePwd.setText("Set Password");
        }

        ToggleGroup radioGroup = new ToggleGroup();

        Stream.of("Default", "Theme 1", "Theme 2", "Theme 3", "Theme 4").forEach(theme -> {
            RadioMenuItem radio = new RadioMenuItem(theme);
            radio.setToggleGroup(radioGroup);
            radio.getStyleClass().add("menu-item");
            if(theme.equals(AppInfo.appTheme))
            {
                radio.setSelected(true);
            }
            menuTheme.getItems().add(radio);
        });

        radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> changed, Toggle oldValue, Toggle newValue)
            {
                if(newValue == null)
                return;
                
                RadioMenuItem radio = (RadioMenuItem)newValue;
                String theme = radio.getText();
                String cssName = "Theme " + theme.charAt(theme.length() -1) + ".css";
                if(theme.charAt(theme.length() - 1) == 't')
                {
                    cssName = "Default.css";
                }
                

                if(selectedTheme.equals(theme))
                {
                    return;
                }
                else if(!selectedTheme.equals("Default") && theme.equals("Default") )
                {
                    stage.getScene().getStylesheets().remove(1);
                }
                else if(selectedTheme.equals("Default") && !theme.equals("Default"))
                {
                    stage.getScene().getStylesheets().add(this.getClass().getResource(cssName).toExternalForm());
                }
                else if(!selectedTheme.equals("Default") && !theme.equals("Default"))
                {
                    stage.getScene().getStylesheets().remove(1);
                    stage.getScene().getStylesheets().add(this.getClass().getResource(cssName).toExternalForm());
                }
                selectedTheme = theme; 
                AppInfo.appTheme = cssName.replace(".css", "");

                System.out.println("selected Theme : " + selectedTheme);
                System.out.println("App theme file : " + AppInfo.appTheme);
                System.out.println("stylesheets size : " + stage.getScene().getStylesheets().size());
                
            }
            
        });

    }

    @FXML
    void  themeChanged(ActionEvent event)
    {
    
    }

    @FXML
    void openSettings(ActionEvent event) {
        btnSettings.getContextMenu().show(btnSettings, Side.LEFT, 15, 15);
    }

    @FXML
    void isLockedClicked(ActionEvent event)
    {
        if (cbIsLocked.isSelected()) {
            AppInfo.locked = true;

            if (!AppInfo.pwdSet) {
                // show dialogue of enter and reente password and forget question
                // after user filled successfully turn menu2 to change password
                String result = setAppPassword();
                if (result.equals("OK")) {
                    AppInfo.pwdSet = true;
                    btnSettings.getContextMenu().getItems().get(1).setText("Change Password");
                } else {
                    AppInfo.locked = false;
                    cbIsLocked.setSelected(false);
                }
            } else {
                AppInfo.locked = true;
            }
        } else {
            AppInfo.locked = false;
        }

    }

    @FXML
    void setOrChangePasswordClicked(ActionEvent event)
    {
        if (menuSetChangePwd.getText().equalsIgnoreCase("Set Password")) {
            String result = setAppPassword();
            if (result.equals("OK")) {
                AppInfo.pwdSet = true;
                menuSetChangePwd.setText("Change Password");
            }

        }
        else {
            changeAppPassword();
        }

    }

    String setAppPassword() {
        Stage setPwdStage = createPasswordDialogStage("SET_PASSWORD");
        setPwdStage.showAndWait();

        TableAppPasswordDialogController controller = ((TableAppPasswordDialogController) stageCreator.getFxmlLoader()
                .getController());
        String result = controller.getResult();

        return result;
    }

    String changeAppPassword() {
        Stage changeAppStage = createPasswordDialogStage("CHANGE_PASSWORD");
        changeAppStage.showAndWait();

        TableAppPasswordDialogController controller = ((TableAppPasswordDialogController) stageCreator.getFxmlLoader()
                .getController());
        String result = controller.getResult();

        return result;
    }

    Stage createPasswordDialogStage(String dialogType) {
        Scene scene = stageCreator.createStage("TableAppPasswordDialog.fxml");

        Stage pwdStage = new Stage();
        pwdStage.setScene(scene);

        if (dialogType.equals("SET_PASSWORD"))
            pwdStage.setTitle("Set Password");
        else
            pwdStage.setTitle("Change Password");

        pwdStage.setResizable(false);
        pwdStage.initModality(Modality.APPLICATION_MODAL);
        pwdStage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                pwdStage.setX(stage.getX() + stage.getWidth() / 2 - pwdStage.getWidth() / 2);
                pwdStage.setY(stage.getY() + stage.getHeight() / 2 - pwdStage.getHeight() / 2);
            }
        });
        ((TableAppPasswordDialogController) stageCreator.getFxmlLoader().getController()).initialize(pwdStage, this,
                dialogType);

        return pwdStage;
    }

    // -------------------------PDF related actions------------------------------

    @FXML
    void saveAsPdf(ActionEvent event) {
        resetActiveButton(TableManager.tableName, true);

        if (activeButton.equals("saveButton")) {
            return;
        }

        this.activeButton = "saveButton";

        activeToolButton = (Button) event.getSource();
        styleToolButton(true);

        String fileLocation = chooseDirectory();

        if (fileLocation == null) {
            System.out.println("File not saved");
            okClicked(event);
            return;
        }

        fileLocation += "\\" + TableManager.tableName + ".pdf";

        float[] columnWidths = new float[TableManager.columnHeadings.length - 1];
        ObservableList<Map<String, String>> rows = tableview.getItems();

        Document document = new Document();
        document.setMargins(30, 30, 30, 30);

        try (FileOutputStream os = new FileOutputStream(new File(fileLocation))) {

            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();

            // ----------Setting creation date 0f the PDF-----------

            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

            Paragraph dateTxt = new Paragraph("Created Date : " + date.format(formatter));
            dateTxt.setAlignment(Element.ALIGN_RIGHT);
            Font fontDate = new Font();
            fontDate.setSize(12);
            dateTxt.setFont(fontDate);
            dateTxt.setSpacingAfter(30f);
            document.add(dateTxt);

            // --------adding title of the table-------------

            Font fontTitle = new Font();
            fontTitle.setSize(18);
            fontTitle.setColor(BaseColor.RED);
            fontTitle.setStyle(Font.BOLD);
            Paragraph title = new Paragraph(TableManager.tableName, fontTitle);
            title.setSpacingAfter(25f);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // ---------------adding table to the document------------------

            PdfPTable table = new PdfPTable(TableManager.columnHeadings.length - 1);

            PdfPCell cell = null;

            for (int index = 1; index < TableManager.columnHeadings.length; index++) {
                Font f = new Font();
                f.setColor(BaseColor.WHITE);
                f.setSize(12f);

                Phrase phrase = new Phrase(TableManager.columnHeadings[index].toUpperCase(), f);

                cell = new PdfPCell(phrase);
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(153, 128, 250));
                cell.setPaddingLeft(10f);
                cell.setPaddingTop(5f);
                cell.setPaddingBottom(7f);

                cell.setBorderWidth(.5f);
                cell.setBorderColor(BaseColor.GRAY);

                table.addCell(cell);

            }

            var varHolder = new Object() {
                int rowIndex = 1;
            };

            for (Map<String, String> row : rows) {
                for (int i = 1; i < TableManager.columnHeadings.length; i++) {
                    cell = new PdfPCell(Phrase.getInstance(row.get(TableManager.columnHeadings[i])));
                    cell.setPaddingLeft(10f);
                    cell.setPaddingTop(5f);
                    cell.setPaddingBottom(7f);

                    cell.setBorderWidth(.5f);
                    cell.setBorderColor(BaseColor.GRAY);

                    System.out.println(cell.getPhrase().getFont().getSize());
                    if (varHolder.rowIndex % 2 == 0) {
                        cell.setBackgroundColor(new BaseColor(247, 244, 255));
                    }

                    table.addCell(cell);
                }
                varHolder.rowIndex++;
            }

            document.add(table);
            document.close();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("pdf created successfully");
        okClicked(event);
    }

    private String chooseDirectory() {
        Stage directoryChooserStage = new Stage();
        directoryChooserStage.setTitle("Choose Directory");

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File("C:\\Users\\jawah\\Desktop\\My_files"));

        File selectedDirectory = chooser.showDialog(stage);

        System.out.println(selectedDirectory + "\\" + TableManager.tableName + ".pdf");

        if (selectedDirectory == null) {
            return null;
        }
        return selectedDirectory.toString();
    }
}