package atrium;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.UIManager;
import com.esotericsoftware.minlog.Log;
import crypto.AES;
import crypto.RSA;
import gui.MainWindow;
import io.BlockedFile;

public class Core {
	
	public static RSA rsa;
	public static AES aes;
	public static String pubKey;
	public static NetHandler netHandler;
	public static MainWindow mainWindow;
	public static ArrayList<BlockedFile> blockDex;
	public static HashMap<String, ArrayList<String>> index;
	
	public static int blockSize = 192000;
	public static int tcp = 35500;
	public static int udp = 35501;
	public static String mutex;
	public static boolean headless = false;
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {	
		//TODO: remove for production
		//Set logging
		Log.set(Log.LEVEL_INFO);
		
		//GUI inits
		Utilities.log("atrium.Core", "Setting graphical preferences");
		try {
			UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		Utilities.log("atrium.Core", "Initializing front-end");
		mainWindow = new MainWindow();
		
		//Set mutex
		Utilities.log("atrium.Core", "Calculating mutex");
		mainWindow.out("Calculating mutex");
		mutex = Utilities.getMutex();
		
		//Initialize crypto routines
		Utilities.log("atrium.Core", "Initializing RSA / AES workers");
		mainWindow.out("Initializing RSA / AES workers");
		rsa = new RSA();
		aes = new AES(mutex);
		
		//File inits
		Utilities.log("atrium.Core", "Checking for file structures");
		mainWindow.out("Checking for file structures");
		FileUtils.initDirs();
		
		//Var initialization
		Utilities.log("atrium.Core", "Generating block index");
		mainWindow.out("Generating block index");
		blockDex = new ArrayList<BlockedFile> ();
		FileUtils.genBlockIndex();
		index = new HashMap<String, ArrayList<String>> ();
		
		//Start NetHandling
		mainWindow.out("Ready");
		netHandler = new NetHandler();
	}
}
