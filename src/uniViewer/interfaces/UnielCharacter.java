package uniViewer.interfaces;
import java.io.File;

public interface UnielCharacter {
	public static final String
			PACKFILE = "packfile",
			PACKFILE_COMPRESSED = "packfilegz",
			FILE_PALETTE = "palette",
			FILE_DATA = "data",
			FILE_SPRITES = "sprites",
			FILE_SPRITES_CG = "cgarc",
			FILE_EFFECT = "effects",
			ANIM_NAME_OVERRIDE = "override";
	
	public File getFile(String type);
}
