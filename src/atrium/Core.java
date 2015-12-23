package atrium;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.minlog.Log;

import crypto.AES;
import crypto.RSA;
import filter.FilterUtils;
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
	public static HashMap<ArrayList<String>, ArrayList<String>> index;
	
	public static int blockSize = 240000;
	public static Config config;
	public static String mutex;
	
	/**
	 * Entry point of application
	 * @param args command-line arguments
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
		//Load config if exists
		Utilities.log("atrium.Core", "Attempting to load configuration from file...", false);
		try {
			File configFile = new File(FileUtils.getConfigDir() + "/config.dat");
			if(configFile.exists()) {
				Utilities.log("atrium.Core", "Loaded saved configuration", false);
				Kryo kryo = new Kryo();
				Input input = new Input(new FileInputStream(configFile));
				config = kryo.readObject(input, Config.class);
			} else {
				config = new Config();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if(args.length > 0 && args[0].equals("-daemon")) {
			Core.config.hubMode = true;
			Log.set(Log.LEVEL_INFO);
			if(args.length == 2) {
				if(args[1].equals("-debug")) {
					Log.set(Log.LEVEL_DEBUG);
				}
			}
		}
		
		//GUI inits
		if(Core.config.hubMode) {
			Utilities.log("atrium.Core", "Hub/headless mode engaged", false);
		} else {
			Utilities.log("atrium.Core", "Initializing front-end", false);
			mainWindow = new MainWindow();
		}
		
		//Set mutex
		Utilities.switchGui("atrium.Core", "Calculating mutex", false);
		mutex = Utilities.getMutex();
		
		//Initialize crypto routines
		Utilities.switchGui("atrium.Core", "Initializing RSA / AES", false);
		rsa = new RSA();
		aes = new AES(mutex);
		
		//File directory checks
		Utilities.switchGui("atrium.Core", "Checking for file structures", false);
		FileUtils.initDirs();
		
		//ShutdownHook for config
		Runtime.getRuntime().addShutdownHook((new Thread(new Runnable() {
			public void run() {
				Utilities.log(this, "Writing config before shutting down", false);
				config.writeConfig();
			}
		})));
		
		//Filter loading
		FilterUtils.init();
		
		//FileWatcher initialization
		(new Thread(new FileWatcher())).start();
		
		//Vars initialization
		Utilities.switchGui("atrium.Core", "Generating block index", false);
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<ArrayList<String>, ArrayList<String>> ();
		peers = new ArrayList<Peer> ();
		
		//Generate block index
		FileUtils.genBlockIndex();
		
		//Start NetHandling
		netHandler = new NetHandler();
	}
}