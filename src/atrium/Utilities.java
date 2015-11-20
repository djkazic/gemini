package atrium;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Utilities {

	public static void log(Object someClass, String msg) {
		System.out.println("<LOG> [" + someClass.getClass().getName() + "]: " + msg);
	}
	
	public static String getMutex() {
		try {
			String firstInterfaceFound = null;        
			Map<String,String> addrByNet = new HashMap<> ();
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while(networkInterfaces.hasMoreElements()){
				NetworkInterface network = networkInterfaces.nextElement();
				byte[] bmac = network.getHardwareAddress();
				if(bmac != null){
					StringBuilder sb = new StringBuilder();
					for(int i=0; i < bmac.length; i++) {
						sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));        
					}
					if(!sb.toString().isEmpty()){
						addrByNet.put(network.getName(), sb.toString());
					}
					if(!sb.toString().isEmpty() && firstInterfaceFound == null){
						firstInterfaceFound = network.getName();
					}
				}
			}
			if(firstInterfaceFound != null){
				return base64(addrByNet.get(firstInterfaceFound));
			}
		} catch (Exception ex) {}
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
}
