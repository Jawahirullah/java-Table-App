import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class StageCreator {

    private FXMLLoader loader;
    
    public Scene createStage(String fxml)
    {
        loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = null;

        try {
            root = loader.load();
            System.out.println(root.getProperties());
        } catch (IOException e) {
            
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("Default.css").toExternalForm());
        if(!AppInfo.appTheme.equals("Default"))
        {
            scene.getStylesheets().add(this.getClass().getResource(AppInfo.appTheme + ".css").toExternalForm());
        }

        return scene;
    }

    public FXMLLoader getFxmlLoader()
    {
        return loader;
    }

}
