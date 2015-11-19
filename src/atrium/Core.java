package atrium;

import com.esotericsoftware.minlog.Log;

public class Core {
	
	public static NetHandler netHandler;
	public static int tcp = 35500;
	public static int udp = 35501;
	public static String mutex;
	
	public static void main(String[] args) {	
		//TODO: remove for production
		//Set logging
		Log.set(Log.LEVEL_NONE);
		
		//Set mutex
		mutex = Utilities.getMutex();
		
		//Start NetHandling
		netHandler = new NetHandler();
	}
}
