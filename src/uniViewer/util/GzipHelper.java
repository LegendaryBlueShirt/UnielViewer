package uniViewer.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javafx.scene.chart.PieChart.Data;
import uniViewer.interfaces.DDSFile;

public class GzipHelper {
	private GzipHelper() {}
	
	public static void inflate(File compressed, File decompressed)throws IOException {
		FileInputStream fis = new FileInputStream(compressed);
		GZIPInputStream gis = new GZIPInputStream(fis);
		decompressed.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(decompressed);
		
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = gis.read(buffer)) > 0) {
			fos.write(buffer, 0, bytesRead);
		}
		
		gis.close();
		fos.close();
	}
	
	public static byte[] inflate(byte[] compressed) {
		GZIPInputStream gis;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
			gis = new GZIPInputStream(bais);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = gis.read(buffer)) > 0) {
				baos.write(buffer, 0, bytesRead);
			}
			gis.close();
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static DDSFile inflateDDS(byte[] compressed) {
		GZIPInputStream gis;
		DDSHelper helper = new DDSHelper();
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
			gis = new GZIPInputStream(bais);
			byte[] header = new byte[128];
			int bytesRead;
			int totalBytes = 0;
			while ((bytesRead = gis.read(header, totalBytes, 128-totalBytes)) > 0) {
				totalBytes += bytesRead;
			}
			
			helper.setHeight( (header[12]&0xFF) | ((header[13]&0xFF)<<8) | ((header[14]&0xFF)<<16) | ((header[15]&0xFF)<<24));
			helper.setWidth( (header[16]&0xFF) | ((header[17]&0xFF)<<8) | ((header[18]&0xFF)<<16) | ((header[19]&0xFF)<<24));
			byte[] data = new byte[helper.getHeight()*helper.getWidth()];
			totalBytes = 0;
			while ((bytesRead = gis.read(data, totalBytes, data.length-totalBytes)) > 0) {
				totalBytes += bytesRead;
			}
			helper.setData(data);
			gis.close();
			return helper;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
