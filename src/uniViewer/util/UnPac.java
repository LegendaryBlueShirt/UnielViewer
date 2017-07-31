package uniViewer.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class UnPac {
	public static class PacFile {
		private static final int HEADER_SIZE = 16;
		private static final byte[] MAGIC = new byte[] {0x55, 0x4B, 0x41, 0x72, 0x63, 0x00, 0x00, 0x00};
		private RandomAccessFile raf;
		private HashMap<String, Integer> names;
		private HashMap<Integer, String> index;
		private long[] offsets;
		private int[] sizes;
		private boolean valid = false;
		
		public PacFile(File file) throws FileNotFoundException {
			this(new RandomAccessFile(file, "r"));
		}
		
		public PacFile(RandomAccessFile raf) {
			try {
				this.raf = raf;
				byte[] magic = new byte[8];
				raf.seek(0);
				raf.read(magic);
				
				if(!new String(MAGIC).equals(new String(magic))) {
					System.err.println("Invalid file.");
				}
				valid = true;
				
				int unk = raf.readInt();
				int nFiles = raf.readInt();
				
				names = new HashMap<String, Integer>();
				index = new HashMap<Integer, String>();
				offsets = new long[nFiles];
				sizes = new int[nFiles];
				
				byte[] nameBuffer = new byte[64];
				for(int n = 0;n < nFiles;n++) {
					raf.read(nameBuffer);
					String name = new String(nameBuffer).trim();
					names.put(name, n);
					index.put(n, name);
					sizes[n] = raf.readInt();
					int unk2 = raf.readInt();
					offsets[n] = raf.readInt() + HEADER_SIZE;
				}
			} catch(IOException e) {
				valid = false;
				e.printStackTrace();
			}
		}
		
		public byte[] getFile(int position) throws IOException {
			if(!valid)
				return null;
			raf.seek(offsets[position]);
			byte[] output = new byte[sizes[position]];
			int bytesRead = 0;
			while(bytesRead < output.length) {
				bytesRead += raf.read(output, bytesRead, output.length - bytesRead);
			}
			return output;
		}
		
		public int getNumFiles() {
			return names.size();
		}
		
		public String getFilename(int position) {
			return index.get(position);
		}
		
		public byte[] getFile(String name) throws IOException {
			if(!valid)
				return null;
			return getFile(names.get(name));
		}
		
		public boolean isValid() {
			return valid;
		}
	}
	
	public static void main(String[] args)throws IOException {
		File target;
		if(args.length > 0) {
			target = new File(args[0]);
		} else {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			target = new File(br.readLine());
		}
		RandomAccessFile raf = new RandomAccessFile(target, "r");
		PacFile pacFile = new PacFile(raf);
		
		for(int n = 0;n < pacFile.getNumFiles();n++) {
			System.out.println(pacFile.getFilename(n));
			File outputFile = new File(pacFile.getFilename(n));
			outputFile.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(pacFile.getFile(n));
			fos.close();
		}
	}
}
