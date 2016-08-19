import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

public class UnielSpriteLoader implements SpriteSource {
	String magic = "BMP Cutter3";
	
	final static int BYTES_PER_CHUNK = 24;
	
	int[] offsets;
	private List<TiledSprite> spriteMetaData;
	private List<DataBufferByte> sprites;
	IndexColorModel[] palettes;
	int selectedPalette = 0;
	
	public UnielSpriteLoader(ByteBuffer data) {
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.position(0);
		byte[] header = new byte[16];
		data.get(header);
		if(!new String(header).contains(magic)) {
			System.out.println("Unknown format!");
			return;
		}
		data.position(20);
		
		palettes = new IndexColorModel[8]; //8 Sample palettes, all of which are the same for some reason.
		byte[] paldata = new byte[1024];
        for(int n = 0;n < 8;n++) {
            data.get(paldata);
            
            for(int m = 0;m < 256;m++) {
            	if(paldata[m*4+3] != 0)
            		paldata[m*4+3] = (byte) 0xFF;
            }
            
            palettes[n] = new IndexColorModel(8,256,paldata,0,true,0);
        }
		
		data.position(8224);
		
		int nSprites = data.getInt();
		data.position(8260);
		offsets = new int[nSprites];
		for(int n = 0;n < nSprites;n++) {
			offsets[n] = data.getInt();
		}
		data.position(20260);
		int TTOffset = data.getInt();
		data.getInt();
		int fsize = data.getInt();
		
		if(data.limit() != fsize) {
			System.out.println("Filesize mismatch!");
			return;
		}
		
		byte[] buffer = new byte[32];
        int[] dataBuffer = new int[8];
        spriteMetaData = new ArrayList<TiledSprite>();
        sprites = new ArrayList<DataBufferByte>();
        for(int n = 0;n < nSprites;n++) {
            data.position(offsets[n]);
            data.get(buffer);
            String name = new String(buffer).trim();
            for(int m = 0;m < 8;m++) {
                dataBuffer[m] = data.getInt();
            }
            int chunkOffset = data.getInt();
            int nChunks = data.getShort()&0xFFFF;
            TiledSprite newSprite = new TiledSprite(name, Arrays.copyOf(dataBuffer,dataBuffer.length), nChunks);

            data.position(TTOffset + BYTES_PER_CHUNK * chunkOffset);
            for(int m = 0;m < nChunks;m++) {
                newSprite.tiles[m] = new SpriteTile(
                		data.getInt(),data.getInt(),data.getInt(),data.getInt(),
                        data.getShort(),data.getShort(),data.getShort(),data.getShort());
            }
            
            data.position(offsets[n]+72);
            spriteMetaData.add(newSprite);
            sprites.add(makeSprite(data, newSprite));
        }
	}
	
	public void loadPalettes(ByteBuffer data) {
		data.order(ByteOrder.LITTLE_ENDIAN);
		
		int nPalettes = data.getInt();
		
		palettes = new IndexColorModel[nPalettes];
		byte[] paldata = new byte[1024];
        for(int n = 0;n < nPalettes;n++) {
            data.get(paldata);
            
            for(int m = 0;m < 256;m++) {
            	if(paldata[m*4+3] != 0)
            		paldata[m*4+3] = (byte) 0xFF;
            }
            
            palettes[n] = new IndexColorModel(8,256,paldata,0,true,0);
        }
	}
	
	public int getNumSprites() {
		return sprites.size();
	}
	
	public static class TiledSprite {
        public final String name;
        public final int[] data;
        public final SpriteTile[] tiles;

        public TiledSprite(String name, int[] data, int nTiles) {
            this.name = name;
            this.data = data;
            this.tiles = new SpriteTile[nTiles];
        }
    }
	
	public static class SpriteTile {
        public final int dstX, dstY, width, height;
        public final short srcX, srcY, srcImg, unk;

        public SpriteTile(int dstX, int dstY, int width, int height, short srcX, short srcY, short srcImg, short unk) {
            this.dstX = dstX;
            this.dstY = dstY;
            this.width = width;
            this.height = height;
            this.srcX = srcX;
            this.srcY = srcY;
            this.srcImg = srcImg;
            this.unk = unk;
        }
    }
	
	byte[] tileBuffer;
	
	public DataBufferByte makeSprite(ByteBuffer data, final TiledSprite tiledSprite) {
        int destWidth = tiledSprite.data[1];
        int destHeight = tiledSprite.data[2];
        
        int xDisplace = tiledSprite.data[4];
        int yDisplace = tiledSprite.data[5];
        
        int realWidth = tiledSprite.data[6] - tiledSprite.data[4];
        int realHeight = tiledSprite.data[7] - tiledSprite.data[5];
        
        byte[] canvas = new byte[realWidth * realHeight];
        
        for(SpriteTile tile: tiledSprite.tiles) {
        	tileBuffer = new byte[32*tile.height];
        	data.get(tileBuffer);
            for(int y = 0;y < tile.height;y++) {
                if((tile.dstY - yDisplace + y) >= realHeight)
                    break;
                System.arraycopy(tileBuffer, y*32, canvas, tile.dstX - xDisplace + (tile.dstY - yDisplace + y)*realWidth, 32);
                /*for (int x = 0; x < tile.width; x++) {
                    if((tile.dstX + x) >= destWidth)
                        break;
                    canvas[tile.dstX + x + (tile.dstY + y)*destWidth] = data.get();
                }*/
            }
        }
        
		return new DataBufferByte(canvas, realWidth*realHeight); 
	}
	
	public BufferedImage getSprite(int index) {
		TiledSprite tiledSprite = spriteMetaData.get(index);
		//int destWidth = tiledSprite.data[1];
        //int destHeight = tiledSprite.data[2];
		int realWidth = tiledSprite.data[6] - tiledSprite.data[4];
        int realHeight = tiledSprite.data[7] - tiledSprite.data[5];
        
		DataBuffer db = sprites.get(index);
		WritableRaster wraster = Raster.createWritableRaster(palettes[selectedPalette].createCompatibleSampleModel(realWidth,realHeight), db, null);
		return new BufferedImage(palettes[selectedPalette], wraster, false, null);
	}
	
	public Point getAxisCorrection(int index) {
		TiledSprite tiledSprite = spriteMetaData.get(index);
		int originX = tiledSprite.data[4];
        int originY = tiledSprite.data[5];
        return new Point(originX, originY);
	}
	
	/*public void saveSprite(int index, OutputStream out) throws IOException {
        final TiledSprite tiledSprite = sprites.get(index);
        int destWidth = tiledSprite.data[1];
        int destHeight = tiledSprite.data[2];

        ImageInfo imi = new ImageInfo(destWidth, destHeight, 8, false, false, true); // 8 bits per channel, no alpha, not grayscale, has palette
        // open image for writing to a output stream
        PngWriter png = new PngWriter(out, imi);
        // add some optional metadata (chunks)
        addPalette(png);
        
        byte[] canvas = new byte[destWidth * destHeight];
        data.position(offsets[index]+72);
        for(SpriteTile tile: tiledSprite.tiles) {
            for(int y = 0;y < tile.height;y++) {
                if((tile.dstY + y) >= destHeight)
                    break;
                for (int x = 0; x < tile.width; x++) {
                    if((tile.dstX + x) >= destWidth)
                        break;
                    canvas[tile.dstX + x + (tile.dstY + y)*destWidth] = data.get();
                }
            }
        }

        ImageLineInt linew = new ImageLineInt(imi);
        int position = 0;
        for (int row = 0; row < destHeight; row++) {
            for (int j = 0; j < destWidth; j++) {
                linew.getScanline()[j] = canvas[position++]&0xff;
            }
            png.writeRow(linew, row);

        }
        png.end();
    }*/
	
	/*private void addPalette(PngWriter pngw) {
        PngChunkPLTE plte = pngw.getMetadata().createPLTEChunk();
        plte.setNentries(256);
        byte[] palette = palettes[selectedPalette];
        for (int i = 0; i < 256; i++) {
            plte.setEntry(i, palette[i*4]&0xff, palette[i*4+1]&0xff, palette[i*4+2]&0xff);
        }
        PngChunkTRNS trns = pngw.getMetadata().createTRNSChunk();
        trns.setIndexEntryAsTransparent(0);
        //pngw.getChunksList().queue(trns);
    }*/
	
	public static void main(String args[])throws IOException {
		List<UnielCharacter> characters = UnielCharacterImpl.getCharacters();
		
		byte[] data = UnielDecrypt.decrypt(characters.get(0).getFile(UnielCharacter.FILE_SPRITES));
		UnielSpriteLoader spriteLoader = new UnielSpriteLoader(ByteBuffer.wrap(data));
		
		data = UnielDecrypt.decrypt(characters.get(0).getFile(UnielCharacter.FILE_PALETTE));
		spriteLoader.loadPalettes(ByteBuffer.wrap(data));
		
		File output = new File("sprites");
		output.mkdir();
		for(int n = 0;n < spriteLoader.getNumSprites();n++) {
			File outfile = new File(output, n+".png");
			FileOutputStream fos = new FileOutputStream(outfile);
			//spriteLoader.saveSprite(n, fos);
			fos.close();
		}
		
	}
}
