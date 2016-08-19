import java.awt.Point;
import java.awt.image.BufferedImage;

public interface SpriteSource {
	public BufferedImage getSprite(int index);
	public int getNumSprites();
	public Point getAxisCorrection(int index);
}
