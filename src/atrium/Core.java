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
import gui.LoadWindow;
import gui.MainWindow;
import io.FileUtils;
import io.FileWatcher;
import io.block.BlockedFile;
import io.block.Metadata;

/**
 * Holds centralized data (variables and instances)
 * @author Kevin Cai
 */
public class Core {
	
	public static RSA rsa;
	public static AES aes;
	public static ArrayList<Peer> peers;
	public static NetHandler netHandler;
	public static LoadWindow loadWindow;
	public static MainWindow mainWindow;
	public static ArrayList<BlockedFile> blockDex;
	public static ArrayList<Metadata> metaDex;
	public static HashMap<ArrayList<String>, ArrayList<String>> index;
	
	public static int blockSize = 256000;
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
			loadWindow = new LoadWindow();
			//mainWindow = new MainWindow();
		}
		
		//Set mutex
		Utilities.switchGui("atrium.Core", "Calculating mutex", false);
		if(loadWindow != null) {
			loadWindow.setProgress(5);
		}
		mutex = Utilities.getMutex();
		
		//Initialize crypto routines
		Utilities.switchGui("atrium.Core", "Initializing RSA / AES", false);
		if(loadWindow != null) {
			loadWindow.setProgress(10);
		}
		if(Core.config.rsaPub != null && Core.config.rsaPriv != null) {
			rsa = new RSA(Core.config.rsaPub, Core.config.rsaPriv);
		} else {
			rsa = new RSA();
		}
		aes = new AES(mutex);
		if(loadWindow != null) {
			loadWindow.setProgress(15);
		}
		
		//File directory checks
		Utilities.switchGui("atrium.Core", "Checking for file structures", false);
		if(loadWindow != null) {
			loadWindow.setProgress(30);
		}
		FileUtils.initDirs();
		
		//ShutdownHook for config
		if(loadWindow != null) {
			loadWindow.setProgress(35);
		}
		Thread shutHook = (new Thread(new Runnable() {
			public void run() {
				Utilities.log(this, "Writing config before shutting down", false);
				config.writeConfig();
			}
		}));
		shutHook.setName("Shutdown Hook");
		Runtime.getRuntime().addShutdownHook(shutHook);
		
		//Filter loading
		if(loadWindow != null) {
			loadWindow.setProgress(40);
		}
		FilterUtils.init();
		
		//FileWatcher initialization
		Utilities.switchGui("atrium.Core", "Registering file watcher", false);
		if(loadWindow != null) {
			loadWindow.setProgress(50);
		}
		(new Thread(new FileWatcher())).start();
		
		//Vars initialization
		if(loadWindow != null) {
			loadWindow.setProgress(60);
		}
		metaDex = new ArrayList<Metadata> ();
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<ArrayList<String>, ArrayList<String>> ();
		peers = new ArrayList<Peer> ();
		
		//Load meta index
		Utilities.switchGui("atrium.Core", "Loading metadata index", false);
		if(loadWindow != null) {
			loadWindow.setProgress(70);
		}
		FileUtils.loadMetaIndex();
		
		//Generate block index
		Utilities.switchGui("atrium.Core", "Generating block index", false);
		if(loadWindow != null) {
			loadWindow.setProgress(75);
		}
		FileUtils.genBlockIndex();
		
		//Start NetHandling
		Utilities.switchGui("atrium.Core", "Initializing networking", false);
		if(loadWindow != null) {
			loadWindow.setProgress(85);
		}
		netHandler = new NetHandler();
	}
}