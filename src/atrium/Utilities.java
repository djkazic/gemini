package atrium;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

import com.esotericsoftware.minlog.Log;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * General utility methods class
 * @author Kevin Cai
 */
public class Utilities {

	/**
	 * Primary logging method
	 * @param someClass origin class of debug
	 * @param msg debug message
	 */
	public static void log(Object someClass, String msg, boolean debug) {
		String output = "[" + someClass.getClass().getName() + "]: " + msg;
		if(debug) {
			Log.debug(output);
		} else {
			Log.info(output);
		}
	}
	
	/**
	 * Secondary logging method (if not object class)
	 * @param someClass pseudo-class name of debug
	 * @param msg debug message
	 */
	public static void log(String someClass, String msg, boolean debug) {
		String output = "[" + someClass + "]: " + msg;
		if(debug) {
			Log.debug(output);
		} else {
			Log.info(output);
		}
	}
	
	/**
	 * Primary dynamic switching method for debug; if headless, no GUI out
	 * @param someClass origin class of debug
	 * @param msg debug message
	 * @param debug 
	 */
	public static void switchGui(Object someClass, String msg, boolean debug) {
		if(Core.mainWindow != null) {
			Core.mainWindow.out(msg);
		}
		log(someClass, msg, debug);
	}
	
	/**
	 * Secondary dynamic switching method for debug; if headless, no GUI out
	 * @param someClass origin class of debug
	 * @param msg debug message
	 */
	public static void switchGui(String someClass, String msg, boolean debug) {
		if(Core.mainWindow != null) {
			Core.mainWindow.out(msg);
		}
		log(someClass, msg, debug);
	}
	
	/**
	 * Calculates a mutex for duplicate connection avoidance
	 * @return the mutex data generated
	 */
	public static String getMutex() {
		try {
			String firstInterfaceFound = null;        
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			StringBuilder sb = new StringBuilder();
			while(networkInterfaces.hasMoreElements()){
				NetworkInterface network = networkInterfaces.nextElement();
				byte[] bmac = network.getHardwareAddress();
				
				if(bmac != null && bmac[0] != 0) {
					for(int i=0; i < bmac.length; i++) {
						sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));        
					}
				}
			}
			if(!sb.toString().isEmpty() && firstInterfaceFound == null) {
				return base64(sb.toString());
			} else {
				log("atrium.Utilities", "Interfaces are null, falling back to config mutex", false);
				if(Core.config.generatedMAC == null) {
					Core.config.generatedMAC = randomMACAddress();
				}
				return base64(Core.config.generatedMAC);
			}			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String base64(String input) {
		return Base64.encode(input.getBytes());
	}

	public static String debase64(String base64) {
		String output = "";
		try {
			output = new String(Base64.decode(base64.getBytes()), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}
	
	public static boolean isMac() {
		return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
	}
	
	public static String randomMACAddress(){
	    Random rand = new Random();
	    byte[] macAddr = new byte[6];
	    rand.nextBytes(macAddr);
	    macAddr[0] = (byte)(macAddr[0] & (byte)254);
	    StringBuilder sb = new StringBuilder(18);
	    for(byte b : macAddr){
	        if(sb.length() > 0) {
	            sb.append(":");
	        }
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
}
