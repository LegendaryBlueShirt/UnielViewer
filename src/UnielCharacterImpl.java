import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UnielCharacterImpl implements UnielCharacter {
	public static final String[] names = new String[] {
            "Aka", "Bya", "Car", "Cha", "Elt", "Gor", "Hil", "Hyd", "Lin", "Mer", "Nan", "Ori", "Set", "Vat", "Wal", "Yuz"
    };
	public static final String[] fullnames = new String[] {
            "Akatsuki", "Byakuya", "Carmine", "Chaos", "Eltnum", "Gordeau", "Hilda", "Hyde", "Linne", "Merkava", "Nanase", "Orie", "Seth", "Vatista", "Waldstein", "Yuzuriha"
    };
	
	public static List<UnielCharacter> getCharacters() {
		ArrayList<UnielCharacter> characters = new ArrayList<UnielCharacter>();
		
		for(int n = 0;n < names.length;n++) {
			characters.add(new UnielCharacterImpl(names[n],fullnames[n]));
		}
		
		return characters;
	}
	
	private HashMap<String, File> files;
	private final String fullname;
	
	public UnielCharacterImpl(String tag, String fullname) {
		File baseDir = new File(SteamHelper.getUNIELDirectory(), String.format("data/%s_0", tag));
		this.fullname = fullname;
		
		files = new HashMap<String, File>();
		files.put(FILE_DATA, new File(baseDir, String.format("%s.HA6", tag)));
		files.put(FILE_SPRITES, new File(baseDir, String.format("%s.cg", tag)));
		files.put(FILE_PALETTE, new File(baseDir, String.format("%s.pal", tag)));
		if(tag.equals("Gor"))
			files.put(FILE_EFFECT, new File(baseDir, String.format("%s_ef.pat", tag)));
		else
			files.put(FILE_EFFECT, new File(baseDir, String.format("%s_ef00.pat", tag)));
	}
	
	@Override
	public File getFile(String type) {
		return files.get(type);
	}

	@Override
	public String toString() {
		return fullname;
	}
}
