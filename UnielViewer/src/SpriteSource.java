import java.awt.image.BufferedImage;

public interface SpriteSource {
	public BufferedImage getSprite(int index);
	public int getNumSprites();
}
