package uniViewer.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

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
}
