import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class ViewerWindow extends Canvas implements WindowListener {
	SpriteSource source;
	Hantei6DataFile.Sequence sequence;
	int currentFrame = 0;
	BufferStrategy strategy;
	int currentX = 400;
	int currentY = 450;
	
	public ViewerWindow() {
		
	}
	
	public void setSpriteSource(SpriteSource source) {
		this.source = source;
	}
	
	volatile boolean running = false;
	Color background = Color.black;
	long lastLoopTime;
	public void prepareForRendering() {
		setIgnoreRepaint(true);
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		running = true;
	}
	
	public void render() {
		// Get hold of a graphics context for the accelerated 
		// surface and blank it out
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		g.setColor(background);
		g.fillRect(0,0,800,600);

		renderFrame((Graphics2D) g.create());
		renderData((Graphics2D) g.create());
		// finally, we've completed drawing so clear up the graphics
		// and flip the buffer over
		g.dispose();
		strategy.show();
	}
	
	private void renderData(Graphics2D g) {
		g.setColor(new Color(0xFFFFFFFF));
		g.translate(16, 16);
		Hantei6DataFile.Frame frame = sequence.frames[currentFrame];
		if(frame == null)
			return;
		g.drawString(String.format("Sequence %d/%d", currentFrame+1, sequence.frames.length), 0, 0);
		g.drawString(String.format("Frame %d    Duration %d", AnimHelper.getTimeForFrame(sequence, currentFrame), frame.AF.mDuration), 0, 16);
		
		if(frame.AT != null)
		if(frame.AT.mActive) {
			g.drawString(String.format("Damage %d", frame.AT.mDamage), 0, 48);
			//g.drawString(String.format("RedDamage %d", frame.AT.mRedDamage), 0, 64);
			//g.drawString(String.format("Proration %d", frame.AT.mProration), 0, 80);
			g.drawString(String.format("Circuit Gain %f", frame.AT.mCircuitGain/100.0), 0, 96);
		}
		
		g.dispose();
	}
	
	BufferedImage[] layers = new BufferedImage[3];
	private void renderFrame(Graphics2D g) {
		if(source == null)
			return;
		if(sequence == null)
			return;
		Hantei6DataFile.Frame frame = sequence.frames[currentFrame];
		
		if(!frame.AF.mActive)
			return;
		synchronized(sequence) {
		g.translate(currentX, currentY);
		for(int n = 0; n < 3;n++){
			Graphics2D canvas = (Graphics2D)g.create();
			Hantei6DataFile.Gx gx = frame.AF.subSprites[n];
			canvas.translate(gx.mOffsetX, gx.mOffsetY);
            if(gx.mHasZoom) {
                canvas.scale(gx.mZoomX,gx.mZoomY);
            }
            
            switch(gx.unk) {
                case 0:
	                if (gx.sprNo >= 0 && gx.sprNo < source.getNumSprites()) {
	                    layers[n] = source.getSprite(gx.sprNo);
	                    int[] correction = source.getAxisCorrection(gx.sprNo);
	                    canvas.drawImage(layers[n], correction[0]-128,correction[1]-224, null);
	                }
                    break;
                case 1:
                    if(gx.sprNo < 0) {
                        System.err.println("Invalid group "+gx.sprNo);
                        break;
                    }
                    /*FuraPanData.EffectGroup group = data.data.getEffectGroup(gx.sprNo);
                    int index = 0;
                    for(byte p: group.effectPieces) {
                        int value = p&0xFF;
                        if(value == 255)
                            break;
                        int effSpriteIndex = data.data.mEffectPieces[value].effSpriteIndex;
                        if (data.data.effectIsValid(effSpriteIndex)) {
                            Bitmap effect = data.data.getEffect(effSpriteIndex);
                            if(effect != null) {
                                drawEffect(canvas, effect, data.data.mEffectPieces[value]);
                            }
                            effect.recycle();
                        }
                    }*/

                    break;
                default:
            }

			canvas.dispose();
		}
		}
		g.setColor(new Color(0x600000FF));
		for(int n = 0;n < 25;n++){
			Rectangle box = frame.mHitboxes[n];
			if(box != null)
				g.drawRect(box.x, box.y, box.width, box.height);
		}
		g.setColor(new Color(0x60FF0000));
		for(int n = 25;n < 33;n++){
			Rectangle box = frame.mHitboxes[n];
			if(box != null)
				g.drawRect(box.x, box.y, box.width, box.height);
		}
		g.dispose();
	}

	
	public void setFrame(Hantei6DataFile.Sequence sequence, int currentFrame) {
		this.sequence = sequence;
		this.currentFrame = currentFrame;
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		running = false;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		e.getWindow().dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}
