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
	
	public static int blockSize = 384000;
	public static int tcp = 35500;
	public static int udp = 35501;
	public static String mutex;
	public static boolean headless = false;
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {	
		//TODO: remove for production
		//Set logging
		Log.set(Log.LEVEL_INFO);
		
		//Set mutex
		mutex = Utilities.getMutex();
		
		//Initialize crypto routines
		rsa = new RSA();
		aes = new AES(mutex);
		
		//Var initialization
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<String, ArrayList<String>> ();
		
		//File inits
		FileUtils.initDirs();
		
		//GUI inits
		try {
			UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		mainWindow = new MainWindow();
		
		//Start NetHandling
		netHandler = new NetHandler();
	}
}
