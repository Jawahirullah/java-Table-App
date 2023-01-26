

import java.util.LinkedHashMap;
import java.util.Map;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TableAppNewTableController {

    private Stage primaryStage;
    private TableAppController primaryController;

    @FXML
    private FlowPane parent;
    @FXML
    private Label errorMsg;
    @FXML
    private TextField tfTableName;


    public void initialize(Stage stage,TableAppController controller)
    {
        primaryStage = stage;
        primaryController = controller;
        addNewRow(new ActionEvent());

    }

    @FXML
    void addNewRow(ActionEvent event) {

        HBox container = new HBox();
        container.getStyleClass().add("new-row-container");

        TextField columnName = new TextField();
        columnName.setPromptText("Column Name");

        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll("SELECT TYPE","NUMBER","LETTERS","FLOATING NUMBER","DATE");
        ChoiceBox<String> columnType = new ChoiceBox<>(items);
        columnType.setValue("SELECT TYPE");

        MaterialIconView deleteIcon = new MaterialIconView(MaterialIcon.DELETE);
        deleteIcon.setSize("24px");
        deleteIcon.setMouseTransparent(true);

        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteIcon);

        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e)
            {
                Button btn =  (Button)e.getSource();
                parent.getChildren().remove(btn.getParent());
            }
        });

        columnName.getStyleClass().addAll("txt-field","txt-field-new-row");
        columnType.getStyleClass().add("new-row-choice-box");
        deleteButton.getStyleClass().add("delete-icon-btn");
        deleteIcon.getStyleClass().add("delete-icon");

        container.getChildren().addAll(columnName,columnType,deleteButton);

        parent.getChildren().add(container);
    }

    @FXML
    void createTable(ActionEvent event) {
        errorMsg.setText("");

        Map<String,String> columns = new LinkedHashMap<String,String>();
        
        boolean result = checkInput(columns);

        if(result)
        {
            DBA.createTable(tfTableName.getText().trim(),columns);
            primaryController.addListItem(tfTableName.getText().trim());
            ((Stage)parent.getScene().getWindow()).close();
        }
        else{
            return;
        }

    }


    private boolean checkInput(Map<String,String> map)
    {
        String tableName = tfTableName.getText().trim();

        if(tableName.equals(""))
        {
            errorMsg.setText("Enter table name");
            return false;
        }

        if(!tableName.matches("[A-Za-z0-9]+"))
        {
            errorMsg.setText("Table Name Supports Alphanumeric Characters Only");
            return false;
        }

        if(tableName.equalsIgnoreCase("hello_table_meta"))
        {
            errorMsg.setText("Invalid column Name. Try Another!");
            return false;
        }

        for (String name : TableManager.allTables) {
            if(tableName.equalsIgnoreCase(name))
            {
                errorMsg.setText(("Table Name '" + tableName + "' already exists"));
                return false;
            }
        }

        ObservableList<Node> hboxes = parent.getChildren();
        if(hboxes.size() == 0)
        {
            errorMsg.setText("Table must contain columns!");
            return false;
        }   

        for(Node node: hboxes){

            HBox container = (HBox)node;
            

            TextField tf = (TextField)container.getChildren().get(0);
            ChoiceBox cb = (ChoiceBox)container.getChildren().get(1);

            if(tf.getText().trim().equals(""))
            {
                errorMsg.setText("Text Fields cannot be empty");
                return false;
            }
            if(tf.getText().trim().equalsIgnoreCase("unique_no"))
            {
                errorMsg.setText("Invalid column Name");
                return false;
            }

            if(cb.getValue().toString().equals("SELECT TYPE"))
            {
                errorMsg.setText("Select Proper Data Type for the Columns");
                return false;
            }

            map.put(tf.getText().trim(),cb.getValue().toString());

        }

        return true;
    }

}

