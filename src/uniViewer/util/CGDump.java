package uniViewer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CGDump {
	private static final String magic = "BMP Cutter3";
	private static final String filetype = ".cg";
	
	private static class MetaData {
		int unknownVal1;
		int unknownVal2;
		
		List<TiledSprite> spriteMetaData = new ArrayList<TiledSprite>();
	}
	
	private static class TiledSprite {
        public final String name;
        public final int[] data;
        public final SpriteTile[] tiles;

        public TiledSprite(String name, int[] data, int nTiles) {
            this.name = name;
            this.data = data;
            this.tiles = new SpriteTile[nTiles];
        }
    }
	
	private static class SpriteTile {
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
	
	private void dump(String path)throws IOException {
		MetaData myData = new MetaData();
		
		File inputFile = new File(path);
		String prefix = inputFile.getName().split("\\.")[0];
		File outFolder = new File(inputFile.getParentFile(), prefix);
		outFolder.mkdir();
		FileInputStream fis = new FileInputStream(inputFile);
		FileChannel fc = fis.getChannel();
		MappedByteBuffer data = fc.map(MapMode.READ_ONLY, 0, fc.size());
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.position(0);
		byte[] header = new byte[16];
		data.get(header);
		if(!new String(header).contains(magic)) {
			System.out.println("Unknown format!");
			return;
		}
		data.position(20);
		
		byte[] paldata = new byte[1024];
        for(int n = 0;n < 8;n++) {
            data.get(paldata);
            writePal(paldata, outFolder, n);
        }
		
		data.position(8224);
		
		int nSprites = data.getInt();
		data.position(8260);
		int[] offsets = new int[nSprites];
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

            data.position(TTOffset + 24 * chunkOffset);
            for(int m = 0;m < nChunks;m++) {
                newSprite.tiles[m] = new SpriteTile(
                		data.getInt(),data.getInt(),data.getInt(),data.getInt(),
                        data.getShort(),data.getShort(),data.getShort(),data.getShort());
            }
            
            data.position(offsets[n]+72);
            myData.spriteMetaData.add(newSprite);
        }
	}
	
	void writePal(byte[] data, File outFolder, int palNum) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(outFolder, String.format("pal%d.pal", palNum)));
		fos.write(data);
		fos.close();
	}
	
	public static void main(String[] args) throws IOException {
		
	}
}
