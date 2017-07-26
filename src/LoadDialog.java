import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class LoadDialog extends JDialog {
	AppMode selectedMode;
	
	public LoadDialog(JFrame frame) {
		super(frame, true);
		
		setTitle("Loading Options");
		
		final HBox hbox = new HBox();
		
		final ToggleGroup group = new ToggleGroup();
        final RadioButton modeUnist = new RadioButton("unist");
        modeUnist.setToggleGroup(group);
        modeUnist.setUserData(AppMode.UNIST_PS3);
        final RadioButton modeUniel = new RadioButton("uniel");
        modeUniel.setToggleGroup(group);
        modeUniel.setUserData(AppMode.UNIEL_STEAM);
        
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    selectedMode = (AppMode) group.getSelectedToggle().getUserData();
                }                
            }
        });
        
        hbox.getChildren().addAll(modeUnist, modeUniel);
        
        BorderPane border = new BorderPane();
        
        border.setTop(hbox);
	}
	
	
}
