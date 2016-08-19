import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

public class UnielViewer {
	static SpriteSource spriteSource;
	static Hantei6DataFile dataFile;
	
	public static void loadCharacter(UnielCharacter character)throws IOException {
		byte[] data = UnielDecrypt.decrypt(character.getFile(UnielCharacter.FILE_SPRITES));
		UnielSpriteLoader spriteLoader = new UnielSpriteLoader(ByteBuffer.wrap(data));
		
		data = UnielDecrypt.decrypt(character.getFile(UnielCharacter.FILE_PALETTE));
		spriteLoader.loadPalettes(ByteBuffer.wrap(data));
		
		FileInputStream fis = new FileInputStream(character.getFile(UnielCharacter.FILE_DATA));
		byte[] dataBytes = new byte[fis.available()];
		fis.read(dataBytes);
		fis.close();
		dataFile = new Hantei6DataFile(ByteBuffer.wrap(dataBytes));
		spriteSource = spriteLoader;
		
		sequenceSelect.removeAllItems();
		for(Entry<Integer, Hantei6DataFile.Sequence> sequence: dataFile.mSequences.entrySet()) {
			sequenceSelect.addItem(sequence.getValue());
		}
		sequenceSelect.setSelectedIndex(0);
	}
	
	static KeyListener keyListener = new KeyListener(){
	
		@Override
		public void keyPressed(KeyEvent arg0) {
		    int currentIndex = sequenceSelect.getSelectedIndex();
		    switch(arg0.getKeyCode()) {
			    case KeyEvent.VK_UP:
			    	try{
			    		sequenceSelect.setSelectedIndex(currentIndex-1);
			    	} catch(Exception e) {
			    		sequenceSelect.setSelectedIndex(currentIndex);
			    	}
			    	
			    	break;
			    case KeyEvent.VK_DOWN:
			    	try{
			    		sequenceSelect.setSelectedIndex(currentIndex+1);
			    	} catch(Exception e) {
			    		sequenceSelect.setSelectedIndex(currentIndex);
			    	}
			    	
			    	break;
			    case KeyEvent.VK_LEFT:
			    	if(currentFrame > 0)
			    		currentFrame--;
			    	animating = false;
			    	break;
			    case KeyEvent.VK_RIGHT:
			    	if((currentFrame+1) < sequenceData.frames.length) {
			    		currentFrame++;
			    	}
			    	animating = false;
			    	break;
			    case KeyEvent.VK_SPACE:
			    	animating = !animating;
			    	break;
			    default:
		    }
		}
	
		@Override
		public void keyReleased(KeyEvent arg0) {
		    // TODO Auto-generated method stub
	
		}
	
		@Override
		public void keyTyped(KeyEvent arg0) {
		    // TODO Auto-generated method stub
	
		}
	};
	
	static int currentFrame = 0;
	static boolean animating = false;
	static int sequenceTime = 0;
	//static int currentSequence = 0;
	static volatile Hantei6DataFile.Sequence sequenceData;
	static JMenuBar menu;
	static List<UnielCharacter> characters;
	static JComboBox<Hantei6DataFile.Sequence> sequenceSelect;
	
	static void createMenu(final ViewerWindow view) {
		menu = new JMenuBar();
		
		JComboBox<UnielCharacter> characterSelect = new JComboBox<UnielCharacter>(characters.toArray(new UnielCharacter[0]));
		characterSelect.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				try {
					loadCharacter((UnielCharacter) arg0.getItem());
					currentFrame = 0;
					//currentSequence = 0;
					sequenceData = dataFile.mSequences.get(0);
					view.setSpriteSource(spriteSource);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}});
		menu.add(characterSelect);
		
		sequenceSelect = new JComboBox<Hantei6DataFile.Sequence>();
		sequenceSelect.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(arg0.getItem() == null)
					return;
				sequenceData = (Hantei6DataFile.Sequence) arg0.getItem();
				currentFrame = 0;
				sequenceTime = 0;
			}});
		menu.add(sequenceSelect);
	}
	
	public static void main(String[] args)throws IOException
	{
		characters = UnielCharacterImpl.getCharacters();
		
		JFrame window = new JFrame();
		ViewerWindow view = new ViewerWindow();
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(view);
		view.addKeyListener(keyListener);
		view.setFocusable(true);
		
		JPanel panel = (JPanel) window.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(null);
				
		// setup our canvas size and put it into the content of the frame
		view.setBounds(0,0,800,600);
		panel.add(view);
		
		createMenu(view);
		
		window.setJMenuBar(menu);
		window.setResizable(true);
		window.pack();
		window.setVisible(true);
		
		view.prepareForRendering();
		
		loadCharacter(characters.get(0));
		
		view.setSpriteSource(spriteSource);
		
		int FPS = 60;
		long lastFrameNanos, currentFrameNanos;
		long frameDurationNanos = (long) (1000000000.0/FPS);
		int framecount = 0;
		int framesSkipped = 0;
		boolean skipFrame = false;
		Hantei6DataFile.Frame frame;
		while(view.running) {
			framecount++;
			if(animating) {
				sequenceTime++;
				currentFrame = AnimHelper.getFrameForTime(sequenceData, sequenceTime);
			}
			
			frame = sequenceData.frames[currentFrame];
			lastFrameNanos = System.nanoTime();
			
			view.setFrame(frame);
			
			if(!skipFrame) {
				view.render();
			} else {
				framesSkipped++;
				skipFrame = false;
			}
			
			currentFrameNanos = System.nanoTime()-lastFrameNanos;
			if(currentFrameNanos <= frameDurationNanos) { //We're on time.
				try {
					long sleepTimeNanos = frameDurationNanos-currentFrameNanos;
					long sleepTimeMillis = sleepTimeNanos/1000000;
					sleepTimeNanos = sleepTimeNanos%1000000;
					Thread.sleep(sleepTimeMillis, (int) sleepTimeNanos);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(frameDurationNanos+"       "+currentFrameNanos+"   "+lastFrameNanos);
				skipFrame=true; //We gotta skip a frame.
				System.out.println("Skipped frames -"+framesSkipped);
			}
		}
	}
}
