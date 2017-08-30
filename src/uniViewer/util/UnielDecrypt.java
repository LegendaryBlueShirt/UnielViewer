package uniViewer.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * HUGE THANKS to @dantarion for figuring this all out.
 * All I've done is port his code to Java for my purposes.
 * https://github.com/dantarion/UNIEL_decrypt
 * 
 * @author LegendaryBlueShirt
 */

public class UnielDecrypt {
	private static final byte[] magic = new byte[] { -83, -60, -41, -20, 56, -102, -99, -31, 116, 82, 12, 108, -21, -104, 82, -30, 59, 20, -106, 116 };
	
	public static byte[] decrypt(File file) {
		try{
			FileInputStream fis = new FileInputStream(file);
			byte[] dataBytes = new byte[fis.available()];
			fis.read(dataBytes);
			fis.close();
			return decrypt(dataBytes);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static byte[] hash;
	static {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			digest.update(magic);
			hash = digest.digest();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static byte[] decrypt(byte[] data) {
		try{
			Cipher rc4 = Cipher.getInstance("RC4");
			SecretKeySpec key = new SecretKeySpec(hash, 0, 16, "RC4");
			rc4.init(Cipher.DECRYPT_MODE, key);
			return rc4.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("File?");
			args = new String[]{br.readLine()};
		}
		
		File file = new File(args[0]);
		FileInputStream fis = new FileInputStream(file);
		byte[] dataBytes = new byte[fis.available()];
		fis.read(dataBytes);
		fis.close();
		
		
		dataBytes = UnielDecrypt.decrypt(dataBytes);
		if(dataBytes != null) {
			FileOutputStream fos = new FileOutputStream(args[0]+".jdecrypted");
			fos.write(dataBytes);
			fos.close();
		}
		
		System.exit(0);
	}
}
