package atrium;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import com.esotericsoftware.minlog.Log;
import crypto.AES;
import crypto.RSA;
import gui.MainWindow;
import io.BlockedFile;
import io.FileUtils;
import io.FileWatcher;

/**
 * Holds centralized data (variables and instances)
 * @author Kevin Cai
 */
public class Core {
	
	public static RSA rsa;
	public static AES aes;
	public static ArrayList<Peer> peers;
	public static NetHandler netHandler;
	public static MainWindow mainWindow;
	public static ArrayList<BlockedFile> blockDex;
	public static HashMap<String, ArrayList<String>> index;
	
	public static int blockSize = 240000;
	public static String mutex;
	public static boolean headless = false;
	public static String debugHost;
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if(args.length > 0 && args[0].equals("-daemon")) {
			headless = true;
		}
		
		//TODO: remove for production
		//Set logging
		Log.set(Log.LEVEL_INFO);
		
		//GUI inits
		try {
			Utilities.log("atrium.Core", "Initializing front-end");
			mainWindow = new MainWindow();
		} catch (Exception ex) {
			Utilities.log("atrium.Core", "Headless mode engaged");
		}
		
		//Set mutex
		Utilities.switchGui("atrium.Core", "Calculating mutex");
		mutex = Utilities.getMutex();
		
		//Initialize crypto routines
		Utilities.switchGui("atrium.Core", "Initializing RSA / AES");
		rsa = new RSA();
		aes = new AES(mutex);
		
		//File directory checks
		Utilities.switchGui("atrium.Core", "Checking for file structures");
		FileUtils.initDirs();
		
		//FileWatcher initialization
		(new Thread(new FileWatcher())).start();
		
		//Vars initialization
		Utilities.switchGui("atrium.Core", "Generating block index");
		blockDex = new ArrayList<BlockedFile> ();
		FileUtils.genBlockIndex();
		index = new HashMap<String, ArrayList<String>> ();
		
		//Start NetHandling
		netHandler = new NetHandler();
	}
}