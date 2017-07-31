package uniViewer;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import uniViewer.interfaces.UnielCharacter;
import uniViewer.model.UnielCharacterImpl;
import uniViewer.util.GzipHelper;
import uniViewer.util.UnPac;

public class SpriteDumper {
	public static void main(String[] args)throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String unielHome = br.readLine();
		List<UnielCharacter> characters = UnielCharacterImpl.getStCharacters();
			
		for(int n = 0;n < characters.size();n++) {
			UnielCharacter character = characters.get(n);
			File outputFolder = new File("Sprites/"+character.toString());
			outputFolder.mkdirs();
			outputFolder.mkdir();
			File packFile = new File(unielHome, character.getFile(UnielCharacter.PACKFILE).getPath());
			if(!packFile.exists()) {
				GzipHelper.inflate(new File(unielHome, character.getFile(UnielCharacter.PACKFILE_COMPRESSED).getPath()), packFile);
			}
			UnPac.PacFile pacFile = new UnPac.PacFile(packFile);
			UnielSpriteLoader spriteLoader = new UnielSpriteLoader(ByteBuffer.wrap(pacFile.getFile(character.getFile(UnielCharacter.FILE_SPRITES).getPath())));
			if(spriteLoader.needsCgarc) {
				spriteLoader.loadSheets(ByteBuffer.wrap(pacFile.getFile(character.getFile(UnielCharacter.FILE_SPRITES_CG).getPath())));
			}
			
			spriteLoader.loadPalettes(ByteBuffer.wrap(pacFile.getFile(character.getFile(UnielCharacter.FILE_PALETTE).getPath())));
			
			for(int m = 0;m < spriteLoader.getNumSprites();m++) {
				spriteLoader.selectedPalette = 0;
				BufferedImage image = spriteLoader.getIndexedSprite(m);
				if(image == null)
					continue;
				File file = new File(outputFolder, String.format("%03d.png", m));
				ImageIO.write(image, "png", file);
			}
			for(int m = 0;m < 70;m++) {
				spriteLoader.selectedPalette = m;
				BufferedImage image = spriteLoader.getIndexedSprite(0);
				if(image == null)
					continue;
				File file = new File(outputFolder, String.format("Pal_%02d.png", m));
				ImageIO.write(image, "png", file);
			}
		}
	}
}
