import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.HashMap;

public class UkArc {
	private static final String magic = "UKArc";
	private static final int HEADER_SIZE = 16;
	HashMap<String, Integer> names;
	HashMap<Integer, String> index;
	int[] offsets;
	int[] sizes;
	ByteBuffer data;
	
	public UkArc(ByteBuffer data) {
		this.data = data;
		data.order(ByteOrder.BIG_ENDIAN);
		data.position(0);
		byte[] header = new byte[8];
		data.get(header);
		if(!new String(header).contains(magic)) {
			System.err.println("Unknown format!");
			System.err.println(new String(header));
			return;
		}
		
		data.position(12);
		
		int nFiles = data.getInt();
		names = new HashMap<String, Integer>();
		index = new HashMap<Integer, String>();
		offsets = new int[nFiles];
		sizes = new int[nFiles];
		byte[] stringBuffer = new byte[64];
		for(int n = 0;n < nFiles;n++) {
			data.get(stringBuffer);
			String name = new String(stringBuffer).trim();
			names.put(name, n);
			index.put(n, name);
			sizes[n] = data.getInt();
			data.getInt();
			offsets[n] = data.getInt()+ HEADER_SIZE;
		}
	}
	
	public int getNumFiles() {
		return names.size();
	}
	
	public String getName(int position) {
		return index.get(position);
	}
	
	public byte[] getFile(String name) {
		return getFile(names.get(name));
	}
	
	public byte[] getFile(int index) {
		data.position(offsets[index]);
		byte[] output = new byte[sizes[index]];
		data.get(output);
		return output;
	}
	
	public static void main(String args[])throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String fileLoc = br.readLine();
		File file = new File(fileLoc);
		FileInputStream fis = new FileInputStream(file);
		FileChannel fc = fis.getChannel();
		MappedByteBuffer mb = fc.map(MapMode.READ_ONLY, 0, fis.available());
		
		UkArc arc = new UkArc(mb);
		for(int n = 0;n < arc.getNumFiles();n++) {
			String filename = arc.getName(n);
			byte[] data = arc.getFile(n);
			if(filename.endsWith(".gz")) {
				data = GzipHelper.inflate(data);
				filename = filename.substring(0, filename.length()-3);
			}
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(data);
			fos.close();
		}
		
		fis.close();
	}
}
