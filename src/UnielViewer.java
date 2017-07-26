import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UnielViewer extends Application {
	static AppMode currentAppMode = AppMode.UNIST_PS3;
	static SpriteSource spriteSource;
	static Hantei6DataFile dataFile;
	static File unielHome;
	static UnPac.PacFile pacFile;
	static ComboBox<UnielCharacter> characterSelect;
	
	private static void setAppMode(AppMode appMode) {
		currentAppMode = appMode;
		switch(currentAppMode) {
		case UNIEL_STEAM:
			characters = UnielCharacterImpl.getCharacters();
			break;
		case UNIST_PS3:
			characters = UnielCharacterImpl.getStCharacters();
			break;
		}
		
		sequenceSelect.getItems().clear();
		characterSelect.getItems().clear();
		characterSelect.getItems().addAll(characters);
		characterSelect.getSelectionModel().select(0);
		characterSelect.setDisable(true);
		sequenceSelect.setDisable(true);
	}
	
	private static void loadCharacter(UnielCharacter character) {
		if(unielHome == null)
			return;
		try {
			switch(currentAppMode) {
			case UNIEL_STEAM:
				loadCharacterUniel(character);
				break;
			case UNIST_PS3:
				loadCharacterUnist(character);
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
		sequenceSelect.getSelectionModel().select(0);
		
		characterSelect.setDisable(false);
		sequenceSelect.setDisable(false);
	}
	
	private static void loadCharacterUnist(UnielCharacter character)throws IOException {
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
	
	public static void loadCharacterUniel(UnielCharacter character)throws IOException {
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
	
	static void createMenu(final ViewerWindow view) {
		menu = new MenuBar();
		
		Menu fileMenu = new Menu("File");
		MenuItem loadDirectory = new MenuItem("Load Directory");
		loadDirectory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showDirectoryChooser();
			}});
		fileMenu.getItems().add(loadDirectory);
		menu.getMenus().add(fileMenu);
		
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
	
	private static void showDirectoryChooser() {
		unielHome = new File("/Users/franciscopareja/Downloads/UNIST/USRDIR/");
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
	
	//static Thread looper;
	static AnimationTimer looper;
	public static void start() throws IOException, InterruptedException {
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
		view = new ViewerWindow(800, 600);
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
		border.setCenter(view);
		
		((VBox)theScene.getRoot()).getChildren().addAll(menu, border);
		
		view.prepareForRendering();
		
		if(SteamHelper.getUNIELDirectory() != null) {
			setAppMode(AppMode.UNIEL_STEAM);
			unielHome = SteamHelper.getUNIELDirectory();
			start();
		}
		
		primaryStage.setScene( theScene );
		primaryStage.show();
	}
}
