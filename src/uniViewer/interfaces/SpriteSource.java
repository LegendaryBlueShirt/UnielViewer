package uniViewer.interfaces;
import javafx.scene.image.Image;

public interface SpriteSource {
	public Image getSprite(int index);
	public int getNumSprites();
	public int[] getAxisCorrection(int index);
}
