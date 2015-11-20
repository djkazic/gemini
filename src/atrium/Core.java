package atrium;

import java.security.NoSuchAlgorithmException;
import com.esotericsoftware.minlog.Log;
import crypto.RSA;

public class Core {
	
	public static RSA rsa;
	public static String pubKey;
	public static NetHandler netHandler;
	
	public static int tcp = 35500;
	public static int udp = 35501;
	public static String mutex;
	
	public static void main(String[] args) throws NoSuchAlgorithmException {	
		//TODO: remove for production
		//Set logging
		Log.set(Log.LEVEL_INFO);
		
		//Set mutex
		mutex = Utilities.getMutex();
		
		//Initialize crypto routines
		rsa = new RSA();
		
		//Start NetHandling
		netHandler = new NetHandler();
	}
}
