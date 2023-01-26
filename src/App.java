import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application{

    private TableAppController primaryController;
    private TableAppPasswordDialogController pwdController;
    private Stage stage;
   
    @Override
    public void start(Stage primaryStage) throws Exception {

        this.stage = primaryStage;

        StageCreator stageCreator = new StageCreator();
        
        if(showPasswordDialogue(stageCreator))
        { 
            Scene scene = stageCreator.createStage("TableAppInterface.fxml");
            // scene.getStylesheets().add("TableStyle.css");
            // get theme from database and if theme is other than default add theme css here
            FXMLLoader primaryControllerLoader = stageCreator.getFxmlLoader();
    
            stage.setScene(scene);
            stage.setTitle("Hello Tables");
            // stage.getIcons().add(new Image("TableAppIcon.png"));
    
            stage.setOnCloseRequest(e->{
                AppInfo.storeAppInfo();
                DBA.closeConnection();
                System.out.println("connection closed");
    
            });
            primaryController = ((TableAppController)primaryControllerLoader.getController());
            primaryController.initialize(stage,stageCreator);
        
            stage.show();
        }
        else{
            DBA.closeConnection();
            System.out.println("connection closed");
        }
    }


    private boolean showPasswordDialogue(StageCreator stageCreator)
    {
        String[] metaData = DBA.getMetaData();
        AppInfo.createPassword(metaData);

        if(AppInfo.locked == true)
        {
            Scene pwdScene = stageCreator.createStage("TableAppPasswordDialog.fxml");
            Stage pwdStage = new Stage();
            pwdStage.setScene(pwdScene);
            pwdStage.setTitle("Enter Password");
            FXMLLoader loader = stageCreator.getFxmlLoader();
            pwdController = (TableAppPasswordDialogController)loader.getController();
            pwdController.initialize(pwdStage,primaryController, "CHECK_PASSWORD");

            pwdStage.showAndWait();
            if(pwdController.getResult().equals("OK"))
            {
                return true;
            }
            else 
            {
                return false;
            }
        }

        return true;

    }
    
    public static void main(String[] args) throws Exception {
        DBA.createConnection();
        launch(args);
    }

}

// create table students(no int,name varchar(20),age int);

// insert into students values(1, 'Jawahir', 20),(2, 'Kamal', 19),(3, 'Hashim', 15),(4, 'James', 18),(5, 'Manoj', 18);

// insert into students (name) values('jawa'),('kamal'),('raj'),('bhuvi'),('santhosh');
