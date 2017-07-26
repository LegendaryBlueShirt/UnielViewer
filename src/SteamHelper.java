import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class SteamHelper {
	public static File getUNIELDirectory() {
		String steamDirectory = getSteamDirectory();
		if(steamDirectory != null) {
			File steamDirFile = new File(getSteamDirectory());
			return new File(steamDirFile,"SteamApps/common/UNDER NIGHT IN-BIRTH Exe Late");
		} else {
			return null;
		}
	}
	
	public static String getSteamDirectory() {
		try {
		      Process process = Runtime.getRuntime().exec(STEAM_FOLDER_CMD);
		      StreamReader reader = new StreamReader(process.getInputStream());

		      reader.start();
		      process.waitFor();
		      reader.join();

		      String result = reader.getResult();
		      int p = result.indexOf(REGSTR_TOKEN);

		      if (p == -1)
		         return null;

		      return result.substring(p + REGSTR_TOKEN.length()).trim();
		    }
		    catch (Exception e) {
		      return null;
		    }
	}
	
	private static final String REGQUERY_UTIL = "reg query ";
	  private static final String REGSTR_TOKEN = "REG_SZ";

	  private static final String STEAM_FOLDER_CMD = REGQUERY_UTIL +
			    "\"HKCU\\Software\\Valve\\Steam\" /v SteamPath";

	  static class StreamReader extends Thread {
	    private InputStream is;
	    private StringWriter sw;

	    StreamReader(InputStream is) {
	      this.is = is;
	      sw = new StringWriter();
	    }

	    public void run() {
	      try {
	        int c;
	        while ((c = is.read()) != -1)
	          sw.write(c);
	        }
	        catch (IOException e) { ; }
	      }

	    String getResult() {
	      return sw.toString();
	    }
	  }

	  public static void main(String s[]) {
		  System.out.println("Steam directory : "
			       + getSteamDirectory());
	  }
}
