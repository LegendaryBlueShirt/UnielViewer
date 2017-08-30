package uniViewer.util;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import uniViewer.interfaces.DDSFile;

public class DDSHelper implements DDSFile {
	private static final byte[] magic = new byte[] {0x44, 0x44, 0x53, 0x20};
	private byte[] data;
	private int width;
	private int height;
	
	public static DDSFile parse(byte[] data) {
		DDSHelper helper = new DDSHelper();
		helper.data = new byte[data.length-128];
		helper.height = (data[12]&0xFF) | ((data[13]&0xFF)<<8) | ((data[14]&0xFF)<<16) | ((data[15]&0xFF)<<24);
		helper.width = (data[16]&0xFF) | ((data[17]&0xFF)<<8) | ((data[18]&0xFF)<<16) | ((data[19]&0xFF)<<24);
		System.arraycopy(data, 128, helper.data, 0, helper.data.length);
		return helper;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public int getWidth() {
		return width;
	}


	@Override
	public int getHeight() {
		return height;
	}
	
	public static void main(String args[])throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String path = br.readLine();
		File inputFile = new File(path);
		FileInputStream fis = new FileInputStream(inputFile);
		byte[] data = new byte[fis.available()];
		fis.read(data);
		fis.close();
		
		File outputFile = new File(inputFile.getParentFile(), "output.png");
		
		DDSFile helper = DDSHelper.parse(data);
		byte[] rawData = helper.getData();
		int[] pixelData = new int[helper.getWidth() * helper.getHeight()];
		for(int m = 0;m < (rawData.length/4);m++) {
	        	if(rawData[m*4+3] != 0)
	        		rawData[m*4+3] = (byte) 0xFF;
	        	pixelData[m] = ((rawData[m*4+3]&0xFF) << 24) | ((rawData[m*4]&0xFF) << 16) | ((rawData[m*4+1]&0xFF) << 8) | (rawData[m*4+2]&0xFF);
	    }
		DataBuffer db = new DataBufferInt(pixelData, helper.getWidth()*helper.getHeight());
		WritableRaster wraster = Raster.createWritableRaster(ColorModel.getRGBdefault().createCompatibleSampleModel(helper.getWidth(), helper.getHeight()), db, null);
		BufferedImage myImage = new BufferedImage(ColorModel.getRGBdefault(), wraster, false, null);
		ImageIO.write(myImage, "png", outputFile);
	}
}
