package uniViewer.model;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uniViewer.interfaces.UnielCharacter;

public class UnielCharacterImpl implements UnielCharacter {
	public static final String[] names = new String[] {
            "Aka", "Bya", "Car", "Cha", "Elt", "Gor", "Hil", "Hyd", "Lin", "Mer", "Nan", "Ori", "Set", "Vat", "Wal", "Yuz"
    };
	public static final String[] fullnames = new String[] {
            "Akatsuki", "Byakuya", "Carmine", "Chaos", "Eltnum", "Gordeau", "Hilda", "Hyde", "Linne", "Merkava", "Nanase", "Orie", "Seth", "Vatista", "Waldstein", "Yuzuriha"
    };
	
	public static final String[] stnames = new String[] {
            "Enk", "Mik", "Pho", "Wag"
    };
	public static final String[] stfullnames = new String[] {
            "Enkidu", "Mika", "Phonon", "Wagner"
    };
	
	public static List<UnielCharacter> getCharacters() {
		ArrayList<UnielCharacter> characters = new ArrayList<UnielCharacter>();
		
		for(int n = 0;n < names.length;n++) {
			characters.add(new UnielCharacterImpl(names[n],fullnames[n]));
		}
		
		return characters;
	}
	
	public static List<UnielCharacter> getStCharacters() {
		List<UnielCharacter> characters = getCharacters();
		
		for(int n = 0;n < stnames.length;n++) {
			characters.add(new UnielCharacterImpl(stnames[n],stfullnames[n]));
		}
		
		return characters;
	}
	
	private HashMap<String, File> files;
	private final String fullname;
	
	public UnielCharacterImpl(String tag, String fullname) {
		File baseDir = new File(String.format("data/%s_0", tag));
		File packDir = new File("PackFile/data");
		this.fullname = fullname;
		
		files = new HashMap<String, File>();
		files.put(PACKFILE, new File(baseDir, String.format("%s.pac", tag)));
		files.put(PACKFILE_COMPRESSED, new File(packDir, String.format("%s.pac.gz", tag)));
		files.put(FILE_DATA, new File(baseDir, String.format("%s.HA6", tag)));
		files.put(FILE_SPRITES, new File(baseDir, String.format("%s.cg", tag)));
		files.put(FILE_SPRITES_CG, new File(baseDir, "cgarc.uka"));
		files.put(FILE_PALETTE, new File(baseDir, String.format("%s.pal", tag)));
		files.put(ANIM_NAME_OVERRIDE, new File(String.format("%s.json", tag)));
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
