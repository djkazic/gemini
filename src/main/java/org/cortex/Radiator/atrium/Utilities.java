package org.cortex.Radiator.atrium;

import java.net.NetworkInterface;
import java.util.Enumeration;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Utilities {

	public static void log(Object someClass, String msg) {
		System.out.println("<LOG> [" + someClass.getClass().getName() + "]: " + msg);
	}
	
	public static void log(String someClass, String msg) {
		System.out.println("<LOG> [" + someClass + "]: " + msg);
	}
	
	public static void switchGui(Object someClass, String msg) {
		if(Core.mainWindow != null) {
			Core.mainWindow.out(msg);
		}
		log(someClass, msg);
	}
	
	public static void switchGui(String someClass, String msg) {
		if(Core.mainWindow != null) {
			Core.mainWindow.out(msg);
		}
		log(someClass, msg);
	}
	
	public static String getMutex() {
		try {
			String firstInterfaceFound = null;        
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while(networkInterfaces.hasMoreElements()){
				NetworkInterface network = networkInterfaces.nextElement();
				byte[] bmac = network.getHardwareAddress();
				if(bmac != null){
					StringBuilder sb = new StringBuilder();
					for(int i=0; i < bmac.length; i++) {
						sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));        
					}
					if(!sb.toString().isEmpty() && firstInterfaceFound == null) {
						return base64(sb.toString());
					}
				}
			}
			log("atrium.Utilities", "Interfaces are null, falling back to supernode mutex");
			return base64("0C-64-32-64-SN-3B");
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
}
