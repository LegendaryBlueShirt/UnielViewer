import java.io.File;

public interface UnielCharacter {
	public static final String
			FILE_PALETTE = "palette",
			FILE_DATA = "data",
			FILE_SPRITES = "sprites",
			FILE_EFFECT = "effects";
	
	public File getFile(String type);
}
