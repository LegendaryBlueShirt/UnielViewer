package uniViewer.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.text.Text;
 
public class LoadDialogController {
	@FXML
    RadioButton modeUniel;        
    @FXML
    RadioButton modeUnist; 
    
    @FXML private Text actiontarget;
    
    @FXML protected void handleSubmitButtonAction(ActionEvent event) {
        actiontarget.setText("Sign in button pressed");
    }

}