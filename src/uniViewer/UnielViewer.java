package uniViewer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uniViewer.interfaces.SpriteSource;
import uniViewer.interfaces.UnielCharacter;
import uniViewer.model.Hantei6DataFile;
import uniViewer.model.NameOverride;
import uniViewer.model.UnielCharacterImpl;
import uniViewer.util.AnimHelper;
import uniViewer.util.GzipHelper;
import uniViewer.util.SteamHelper;
import uniViewer.util.UnPac;
import uniViewer.util.UnielDecrypt;
import uniViewer.view.LoadDialog;
import uniViewer.view.ViewerWindow;

public class UnielViewer extends Application {
	static AppMode currentAppMode = AppMode.UNIST_PS3;
	static SpriteSource spriteSource;
	static Hantei6DataFile dataFile;
	static File unielHome;
	static UnPac.PacFile pacFile;
	static ComboBox<UnielCharacter> characterSelect;
	
	private void setAppMode(AppMode appMode) {
		currentAppMode = appMode;
		switch(currentAppMode) {
		case UNIEL_STEAM:
			characters = UnielCharacterImpl.getCharacters();
			break;
		case UNIST_PS3:
			characters = UnielCharacterImpl.getStCharacters();
			break;
		case UNKNOWN:
			break;
		}
		
		sequenceSelect.getItems().clear();
		characterSelect.getItems().clear();
		characterSelect.getItems().addAll(characters);
		characterSelect.setDisable(true);
		sequenceSelect.setDisable(true);
		characterSelect.getSelectionModel().select(0);
	}
	
	private void loadCharacter(UnielCharacter character) {
		if(unielHome == null)
			return;
		
		File namesFile = character.getFile(UnielCharacter.ANIM_NAME_OVERRIDE);
		try {
			InputStream is = getClass().getResourceAsStream("/"+namesFile.getName());
			byte[] data = new byte[is.available()];
			is.read(data);
			is.close();

			String str = new String(data, "UTF-8");
			NameOverride.setOverrideData(str);
		}catch(Exception e) {
			e.printStackTrace();
			NameOverride.setOverrideData("{}");
		}
		
		try {
			switch(currentAppMode) {
			case UNIEL_STEAM:
				loadCharacterUniel(character);
				break;
			case UNIST_PS3:
				loadCharacterUnist(character);
				break;
			case UNKNOWN:
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sequenceSelect.getItems().clear();
		for(Entry<Integer, Hantei6DataFile.Sequence> sequence: dataFile.mSequences.entrySet()) {
			sequenceSelect.getItems().add(sequence.getValue());
		}
		characterSelect.setDisable(false);
		sequenceSelect.setDisable(false);
		sequenceSelect.getSelectionModel().select(0);
	}
	
	private void loadCharacterUnist(UnielCharacter character)throws IOException {
		File packFile = new File(unielHome, character.getFile(UnielCharacter.PACKFILE).getPath());
		if(!packFile.exists()) {
			GzipHelper.inflate(new File(unielHome, character.getFile(UnielCharacter.PACKFILE_COMPRESSED).getPath()), packFile);
		}
		UnPac.PacFile pacFile = new UnPac.PacFile(packFile);
		UnielSpriteLoader spriteLoader = new UnielSpriteLoader(ByteBuffer.wrap(pacFile.getFile(character.getFile(UnielCharacter.FILE_SPRITES).getPath())));
		if(spriteLoader.needsCgarc) {
			spriteLoader.loadSheets(ByteBuffer.wrap(pacFile.getFile(character.getFile(UnielCharacter.FILE_SPRITES_CG).getPath())));
		}
		
		spriteLoader.loadPalettes(ByteBuffer.wrap(pacFile.getFile(character.getFile(UnielCharacter.FILE_PALETTE).getPath())));
		byte[] dataBytes = pacFile.getFile(character.getFile(UnielCharacter.FILE_DATA).getPath());
		dataFile = new Hantei6DataFile(ByteBuffer.wrap(dataBytes));
		spriteSource = spriteLoader;
	}
	
	public void loadCharacterUniel(UnielCharacter character)throws IOException {
		byte[] data;
		data = UnielDecrypt.decrypt(new File(unielHome, character.getFile(UnielCharacter.FILE_SPRITES).getPath()));
		UnielSpriteLoader spriteLoader = new UnielSpriteLoader(ByteBuffer.wrap(data));
		
		data = UnielDecrypt.decrypt(character.getFile(UnielCharacter.FILE_PALETTE));
		spriteLoader.loadPalettes(ByteBuffer.wrap(data));
		
		FileInputStream fis = new FileInputStream(character.getFile(UnielCharacter.FILE_DATA));
		byte[] dataBytes = new byte[fis.available()];
		fis.read(dataBytes);
		fis.close();
		dataFile = new Hantei6DataFile(ByteBuffer.wrap(dataBytes));
		spriteSource = spriteLoader;
	}
	
	static EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>(){
		@Override
		public void handle(KeyEvent event) {
			int currentIndex = sequenceSelect.getSelectionModel().getSelectedIndex();
		    switch(event.getCode()) {
			    case UP:
			    	try{
			    		sequenceSelect.getSelectionModel().select(currentIndex-1);
			    	} catch(Exception e) {
			    		sequenceSelect.getSelectionModel().select(currentIndex);
			    	}
			    	
			    	break;
			    case DOWN:
			    	try{
			    		sequenceSelect.getSelectionModel().select(currentIndex+1);
			    	} catch(Exception e) {
			    		sequenceSelect.getSelectionModel().select(currentIndex);
			    	}
			    	
			    	break;
			    case LEFT:
			    	if(currentFrame > 0)
			    		currentFrame--;
			    	animating = false;
			    	break;
			    case RIGHT:
			    	if((currentFrame+1) < sequenceData.frames.length) {
			    		currentFrame++;
			    	}
			    	animating = false;
			    	break;
			    case SPACE:
			    	animating = !animating;
			    	break;
			    default:
		    }
		}
	};
	
	static int currentFrame = 0;
	static boolean animating = false;
	static int sequenceTime = 0;
	//static int currentSequence = 0;
	static volatile Hantei6DataFile.Sequence sequenceData;
	static MenuBar menu;
	static List<UnielCharacter> characters = new ArrayList<UnielCharacter>();
	static ComboBox<Hantei6DataFile.Sequence> sequenceSelect;
	
	void createMenu(final ViewerWindow view) {
		menu = new MenuBar();
		
		Menu fileMenu = new Menu("File");
		MenuItem loadDirectory = new MenuItem("Load Directory");
		loadDirectory.setOnAction(event -> showDirectoryChooser());
		fileMenu.getItems().add(loadDirectory);
		
		Menu viewMenu = new Menu("View");
		MenuItem resetPosition = new MenuItem("Reset Position");
		resetPosition.setOnAction(event -> view.resetPosition());
		viewMenu.getItems().add(resetPosition);
		
		menu.getMenus().addAll(fileMenu, viewMenu);
		
		characterSelect = new ComboBox<UnielCharacter>();
		characterSelect.valueProperty().addListener(new ChangeListener<UnielCharacter>() {
			@Override
			public void changed(ObservableValue<? extends UnielCharacter> observable, UnielCharacter oldValue,
					UnielCharacter newValue) {
				if(newValue == null)
					return;
				if(characterSelect.isDisabled())
					return;
				loadCharacter(newValue);
				currentFrame = 0;
				//currentSequence = 0;
				sequenceData = dataFile.mSequences.get(0);
				view.setSpriteSource(spriteSource);
			}});
		
		sequenceSelect = new ComboBox<Hantei6DataFile.Sequence>();
		sequenceSelect.valueProperty().addListener(new ChangeListener<Hantei6DataFile.Sequence>() {
			@Override
			public void changed(ObservableValue<? extends Hantei6DataFile.Sequence> observable, Hantei6DataFile.Sequence oldValue, Hantei6DataFile.Sequence newValue) {
				if(newValue == null)
					return;
				if(sequenceSelect.isDisabled())
					return;
				sequenceData = newValue;
				currentFrame = 0;
				sequenceTime = 0;
			}});
		
		characterSelect.setDisable(true);
		sequenceSelect.setDisable(true);
	}
	
	
	static LoadDialog loadDialog;
	private void showDirectoryChooser() {
		/*unielHome = new File("/Users/franciscopareja/Downloads/UNIST/USRDIR/");
		try {
			start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		if(loadDialog == null) {
			loadDialog = new LoadDialog();
		}
		Optional<ButtonType> result = loadDialog.showAndWait();
		if(result.get() == ButtonType.OK) {
			unielHome = loadDialog.getFolder();
			setAppMode(loadDialog.getMode());
			try {
				start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//static Thread looper;
	static AnimationTimer looper;
	public void start() throws IOException, InterruptedException {
		view.running = false;
		if(looper != null)
			looper.stop();
		//	looper.join();
		loadCharacter(characters.get(0));
		view.setSpriteSource(spriteSource);
		
		looper = new AnimationTimer() {
			long lastFrameNanos = 0;
			int framecount = 0;
			int framesSkipped = 0;
			final int FPS = 60;
			final long frameDurationNanos = (long) (1000000000.0/FPS);
			boolean skipFrame = false;
			@Override
			public void handle(long now) {
				framecount++;
				if(animating) {
					sequenceTime++;
					currentFrame = AnimHelper.getFrameForTime(sequenceData, sequenceTime);
				}
				
				view.setFrame(sequenceData,currentFrame);
				
				long currentFrameNanos = now-lastFrameNanos;
				if(currentFrameNanos > frameDurationNanos) { //We're on time.
					//System.out.println(frameDurationNanos+"       "+currentFrameNanos+"   "+lastFrameNanos);
					skipFrame=true; //We gotta skip a frame.
					//System.out.println("Skipped frames -"+framesSkipped);
				}
				
				if(!skipFrame) {
					//System.out.println("Draw");
					view.render();
				} else {
					framesSkipped++;
					skipFrame = false;
				}
				
				lastFrameNanos = now;
			}};
		looper.start();	
	}
	
	static ViewerWindow view;
	public static void main(String[] args)throws IOException, InterruptedException
	{	
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		view = new ViewerWindow();
		primaryStage.setOnCloseRequest(view.getWindowCloseHandler());
		primaryStage.setTitle("Under Night Framedisplay");
		
	    Scene theScene = new Scene( new VBox(), 800, 600 );
		
	    theScene.addEventFilter(KeyEvent.KEY_PRESSED,
                event -> keyListener.handle(event));
	    
		createMenu(view);
		setAppMode(AppMode.UNIST_PS3);
		
		menu.prefWidthProperty().bind(primaryStage.widthProperty());
		
		BorderPane border = new BorderPane();
		HBox topMenu = new HBox();
		
		topMenu.getChildren().addAll(characterSelect,sequenceSelect);
		// setup our canvas size and put it into the content of the frame
		border.setTop(topMenu);
		
		Pane pane = new Pane();
		border.setCenter(pane);
		
		pane.getChildren().add(view);
		
		view.widthProperty().bind(primaryStage.widthProperty());
	    view.heightProperty().bind(primaryStage.heightProperty().subtract(topMenu.heightProperty()));
		
	    theScene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> view.onClick(event));
	    theScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> view.onDrag(event));
	    theScene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> view.onRelease(event));
	    
		((VBox)theScene.getRoot()).getChildren().addAll(menu, border);
		
		view.prepareForRendering();
		
		/*if(SteamHelper.getUNIELDirectory() != null) {
			setAppMode(AppMode.UNIEL_STEAM);
			unielHome = SteamHelper.getUNIELDirectory();
			start();
		}*/
		
		primaryStage.setScene( theScene );
		primaryStage.show();
	}
}
