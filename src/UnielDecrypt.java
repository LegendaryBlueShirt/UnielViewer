import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * HUGE THANKS to @dantarion for figuring this all out.
 * All I've done is port his code to Java for my purposes.
 * https://github.com/dantarion/UNIEL_decrypt
 * 
 * removeCryptographyRestrictions() is copy/pasted from stackoverflow.
 * http://stackoverflow.com/a/22492582
 * 
 * @author LegendaryBlueShirt
 */

public class UnielDecrypt {
	private static final byte[] magic = new byte[] { -83, -60, -41, -20, 56, -102, -99, -31, 116, 82, 12, 108, -21, -104, 82, -30, 59, 20, -106, 116 };
	public static boolean restricted = true;
	
	private static void removeCryptographyRestrictions() {
		if(!restricted) {
			return;
		}
		if (!isRestrictedCryptography()) {
			System.out.println("Cryptography restrictions removal not needed");
			restricted = false;
			return;
		}
		try {
			/*
			 * Do the following, but with reflection to bypass access checks:
			 *
			 * JceSecurity.isRestricted = false;
			 * JceSecurity.defaultPolicy.perms.clear();
			 * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
			 */
			final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
			final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
			final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

			final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
			isRestrictedField.setAccessible(true);
			isRestrictedField.set(null, false);

			final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
			defaultPolicyField.setAccessible(true);
			final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

			final Field perms = cryptoPermissions.getDeclaredField("perms");
			perms.setAccessible(true);
			((Map<?, ?>) perms.get(defaultPolicy)).clear();

			final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
			instance.setAccessible(true);
			defaultPolicy.add((Permission) instance.get(null));

			System.out.println("Successfully removed cryptography restrictions");
			restricted = false;
		} catch (final Exception e) {
			System.err.println("Failed to remove cryptography restrictions");
			e.printStackTrace();
		}
	}

	private static boolean isRestrictedCryptography() {
		// This simply matches the Oracle JRE, but not OpenJDK.
		return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}
	
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
		removeCryptographyRestrictions();
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
