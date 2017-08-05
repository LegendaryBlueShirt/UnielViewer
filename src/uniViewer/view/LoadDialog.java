package uniViewer.view;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import uniViewer.AppMode;
import uniViewer.interfaces.UnielCharacter;
import uniViewer.model.UnielCharacterImpl;

public class LoadDialog extends Dialog<ButtonType> {
	private boolean folderOk = false;
	public ObjectBinding<AppMode> binding;
	
	@FXML
    RadioButton modeUniel;        
    @FXML
    RadioButton modeUnist;
    @FXML
    TextField uniHome;
    
    @FXML protected void handleLoadAction(ActionEvent event) {
    		if(folderOk) {
    			setResult(ButtonType.OK);
    		}
        close();
    }
    
    @FXML protected void handleCancelAction(ActionEvent event) {
    		setResult(ButtonType.CLOSE);
    		close();
    }
    
    DirectoryChooser fileChooser = new DirectoryChooser();
    @FXML protected void showFolderChooser(ActionEvent event) {
	    	fileChooser.setTitle("Open Resource File");
	    	fileChooser.setInitialDirectory(new File("."));
	    File selectedFolder = fileChooser.showDialog(getDialogPane().getScene().getWindow());
	    if(selectedFolder != null) {
	    		uniHome.setText(selectedFolder.getPath());
	    }
    }
    
    public File getFolder() {
    		return new File(uniHome.getText());
    }
    
    public AppMode getMode() {
    		return binding.getValue();
    }
	
	public LoadDialog() {
		super();
		
		setTitle("Loading Options");
		setResult(ButtonType.CANCEL);
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoadDialog.fxml"));
			loader.setController(this);
			Parent root = loader.load();
			//Parent root = FXMLLoader.load(getClass().getResource("LoadDialog.fxml"));
			//root.setController(this);
	        getDialogPane().setContent(root);
	        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            Node closeButton = getDialogPane().lookupButton(ButtonType.CLOSE);
            closeButton.managedProperty().bind(closeButton.visibleProperty());
            closeButton.setVisible(false);
	        
	        binding = new ObjectBinding<AppMode>() {
		        	{
		        		super.bind(modeUniel.selectedProperty(), modeUnist.selectedProperty());
		        	}

				@Override
				protected AppMode computeValue() {
					if(modeUnist.isSelected()) {
						return AppMode.UNIST_PS3;
					} else if(modeUniel.isSelected()) {
						return AppMode.UNIEL_STEAM;
					}
					return AppMode.UNKNOWN;
				}
	        };
	        binding.addListener(new ChangeListener<AppMode>(){
				@Override
				public void changed(ObservableValue<? extends AppMode> observable, AppMode oldValue, AppMode newValue) {
					uniHome.setText(uniHome.getText());
				}});;
			uniHome.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					File validatedFolder = validateFolder(new File(newValue));
					if(validatedFolder == null) {
						folderOk = false;
					} else {
						uniHome.setText(validatedFolder.getPath());
						folderOk = true;
					}
				}});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private File validateFolder(File folder) {
		File packFile, tempFolder;
		switch(getMode()) {
		case UNIST_PS3:
			List<UnielCharacter> stcharacters = UnielCharacterImpl.getStCharacters();
			for(UnielCharacter character: stcharacters) {
				tempFolder = new File(folder, "dummy");
				do {
					tempFolder = tempFolder.getParentFile();
					packFile = new File(tempFolder, character.getFile(UnielCharacter.PACKFILE_COMPRESSED).getPath());
				} while(!packFile.exists() && (tempFolder.getParentFile() != null));
				if(!packFile.exists()) {
					return null;
				}
				folder = tempFolder;
			}
			break;
		case UNIEL_STEAM:
			List<UnielCharacter> characters = UnielCharacterImpl.getCharacters();
			for(UnielCharacter character: characters) {
				tempFolder = new File(folder, "dummy");
				do {
					tempFolder = tempFolder.getParentFile();
					packFile = new File(tempFolder, character.getFile(UnielCharacter.FILE_DATA).getPath());
				} while(!packFile.exists() && (tempFolder.getParentFile() != null));
				if(!packFile.exists()) {
					return null;
				}
				folder = tempFolder;
			}
			break;
		case UNKNOWN:
			return null;
		default:
			break;
		}
		return folder;
	}
}
