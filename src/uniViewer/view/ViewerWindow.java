package uniViewer.view;
import java.awt.Rectangle;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import uniViewer.interfaces.SpriteSource;
import uniViewer.model.Hantei6DataFile;
import uniViewer.util.AnimHelper;

public class ViewerWindow extends Canvas {
	SpriteSource source;
	Hantei6DataFile.Sequence sequence;
	int currentFrame = 0;
	//BufferStrategy strategy;
	int currentX = 400;
	int currentY = 450;
	
	public ViewerWindow(int width, int height) {
		super(width, height);
	}
	
	public void setSpriteSource(SpriteSource source) {
		this.source = source;
	}
	
	public volatile boolean running = false;
	Color background = Color.BLACK;
	long lastLoopTime;
	public void prepareForRendering() {
		//setIgnoreRepaint(true);
		//createBufferStrategy(2);
		//strategy = getBufferStrategy();
		running = true;
	}
	
	public void render() {
		// Get hold of a graphics context for the accelerated 
		// surface and blank it out
		//Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		GraphicsContext g = getGraphicsContext2D();
		g.setFill(background);
		g.fillRect(0,0,getWidth(),getHeight());

		//renderFrame((Graphics2D) g.create());
		//renderData((Graphics2D) g.create());
		renderFrame(g);
		renderData(g);
		// finally, we've completed drawing so clear up the graphics
		// and flip the buffer over
		//g.dispose();
		//strategy.show();
	}
	
	private void renderData(GraphicsContext g) {
		g.save();
		if(sequence == null)
			return;
		g.setFill(new Color(1,1,1,1));
		g.translate(16, 16);
		Hantei6DataFile.Frame frame = sequence.frames[currentFrame];
		if(frame == null)
			return;
		g.fillText(String.format("Sequence %d/%d", currentFrame+1, sequence.frames.length), 0, 0);
		g.fillText(String.format("Frame %d    Duration %d", AnimHelper.getTimeForFrame(sequence, currentFrame), frame.AF.mDuration), 0, 16);
		
		if(frame.AT != null)
		if(frame.AT.mActive) {
			g.fillText(String.format("Damage %d", frame.AT.mDamage), 0, 48);
			//g.drawString(String.format("RedDamage %d", frame.AT.mRedDamage), 0, 64);
			//g.drawString(String.format("Proration %d", frame.AT.mProration), 0, 80);
			g.fillText(String.format("Circuit Gain %f", frame.AT.mCircuitGain/100.0), 0, 96);
		}
		g.restore();
		//g.dispose();
	}
	
	Image[] layers = new Image[3];
	private void renderFrame(GraphicsContext g) {
		g.save();
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
			g.save();
			Hantei6DataFile.Gx gx = frame.AF.subSprites[n];
			g.translate(gx.mOffsetX, gx.mOffsetY);
            if(gx.mHasZoom) {
                g.scale(gx.mZoomX,gx.mZoomY);
            }
            
            switch(gx.unk) {
                case 0:
	                if (gx.sprNo >= 0 && gx.sprNo < source.getNumSprites()) {
	                    layers[n] = source.getSprite(gx.sprNo);
	                    int[] correction = source.getAxisCorrection(gx.sprNo);
	                    g.drawImage(layers[n], correction[0]-128,correction[1]-224);
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

			g.restore();
		}
		}
		g.setStroke(new Color(0,0,1,.38));
		for(int n = 0;n < 25;n++){
			Rectangle box = frame.mHitboxes[n];
			if(box != null) {
				g.strokeRect(box.x, box.y, box.width, box.height);
			}
		}
		g.setStroke(new Color(1,0,0,.38));
		for(int n = 25;n < 33;n++){
			Rectangle box = frame.mHitboxes[n];
			if(box != null) {
				g.strokeRect(box.x, box.y, box.width, box.height);
			}
		}
		g.restore();
	}

	
	public void setFrame(Hantei6DataFile.Sequence sequence, int currentFrame) {
		this.sequence = sequence;
		this.currentFrame = currentFrame;
	}
	
	public EventHandler<WindowEvent> getWindowCloseHandler() {
		return new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				running = false;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//e.getWindow().dispose();
			}
			};
	}
}
