package uniViewer.view;
import java.awt.Rectangle;
import java.util.Arrays;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import uniViewer.interfaces.SpriteSource;
import uniViewer.model.Hantei6DataFile;
import uniViewer.util.AnimHelper;
import static uniViewer.model.Hantei6Tags.*;

public class ViewerWindow extends Canvas {
	SpriteSource source;
	Hantei6DataFile.Sequence sequence;
	int currentFrame = 0;
	//BufferStrategy strategy;
	int currentX = 400;
	int currentY = 450;
	
	/*public ViewerWindow(int width, int height) {
		super(width, height);
	}*/
	
	public void setSpriteSource(SpriteSource source) {
		this.source = source;
	}
	
	@Override
    public boolean isResizable() {
      return true;
    }
 
    @Override
    public double prefWidth(double height) {
      return getWidth();
    }
 
    @Override
    public double prefHeight(double width) {
      return getHeight();
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
		g.fillText(String.format("Frame %d    Duration %d", AnimHelper.getTimeForFrame(sequence, currentFrame), frame.AF.flags.get(DURATION)[0]), 0, 16);
		
		if(frame.AT != null)
		if(frame.AT.mActive) {
			g.fillText(String.format("Damage %d", frame.AT.mDamage), 0, 48);
			//g.drawString(String.format("RedDamage %d", frame.AT.mRedDamage), 0, 64);
			//g.drawString(String.format("Proration %d", frame.AT.mProration), 0, 80);
			g.fillText(String.format("Circuit Gain %f", frame.AT.mCircuitGain/100.0), 0, 96);
		}
		
		if(frame.AF != null) {
			if(frame.AF.mActive) {
				int yoff = 0;
				for(String flag: frame.AF.flags.keySet()) {
					if(frame.AF.flags.get(flag) != null) {
						g.fillText(flag+"  "+Arrays.toString(frame.AF.flags.get(flag)), getWidth()-120, yoff);
					} else {
						g.fillText(flag, getWidth()-120, yoff);
					}
					yoff+=16;
				}
			}
		}
		
		g.restore();

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
		g.translate(getAxisX(), getAxisY());
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
		g.setStroke(new Color(1,1,1,.80));
		Integer boxnum = frame.mHitboxes[0];
		if(boxnum != null) {
			Rectangle box = sequence.hitboxes[boxnum];
			g.strokeRect(box.x, box.y, box.width, box.height);
		}
		g.setStroke(new Color(0,0,1,.80));
		for(int n = 1;n < 9;n++){
			boxnum = frame.mHitboxes[n];
			if(boxnum != null) {
				Rectangle box = sequence.hitboxes[boxnum];
				g.strokeRect(box.x, box.y, box.width, box.height);
			}
		}
		g.setStroke(new Color(0,1,0,.80));
		for(int n = 9;n < 25;n++){
			boxnum = frame.mHitboxes[n];
			if(boxnum != null) {
				Rectangle box = sequence.hitboxes[boxnum];
				g.strokeRect(box.x, box.y, box.width, box.height);
			}
		}
		g.setStroke(new Color(1,0,0,.80));
		for(int n = 25;n < 33;n++){
			boxnum = frame.mHitboxes[n];
			if(boxnum != null) {
				Rectangle box = sequence.hitboxes[boxnum];
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

	private int getAxisX() {
		if(dragging) {
			return (int)(displaceX - clickOriginX) + currentX;
		} else {
			return currentX;
		}
	}
	
	private int getAxisY() {
		if(dragging) {
			return (int)(displaceY - clickOriginY) + currentY;
		} else {
			return currentY;
		}
	}
	
	public void resetPosition() {
		currentX = (int) (getWidth()/2);
		currentY = (int) (this.getHeight()/2 + 150);
	}
	
	boolean dragging = false;
	double clickOriginX = 0;
	double clickOriginY = 0;
	double displaceX = 0;
	double displaceY = 0;
	public void onClick(MouseEvent event) {
		if(event.getButton() == MouseButton.PRIMARY) {
			dragging = true;
			clickOriginX = event.getScreenX();
			clickOriginY = event.getScreenY();
			displaceX = clickOriginX;
			displaceY = clickOriginY;
		}
	}
	
	public void onDrag(MouseEvent event) {
		if(dragging) {
			displaceX = event.getScreenX();
			displaceY = event.getScreenY();
		}
	}
	
	public void onRelease(MouseEvent event) {
		if(dragging) {
			if(event.getButton() == MouseButton.PRIMARY) {
				currentX = getAxisX();
				currentY = getAxisY();
				dragging = false;
			}
		}
	}
}
