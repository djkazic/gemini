package atrium;

import filter.FilterUtils;
import gui.TrayHandler;
import io.FileUtils;
import io.FileWatcher;
import io.block.BlockedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.api.APIRouter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.minlog.Log;

import crypto.AES;
import crypto.RSA;

/**
 * Holds centralized data (variables and instances)
 * 
 * @author Kevin Cai
 */
public class Core {

	public static RSA rsa;
	public static AES aes;
	public static ArrayList<Peer> peers;
	public static NetHandler netHandler;
	public static ArrayList<BlockedFile> blockDex;
	public static HashMap<ArrayList<String>, ArrayList<String>> index;

	public static int blockSize = 256000;
	public static Config config;
	public static String mutex;

	/**
	 * Entry point of application
	 * 
	 * @param args
	 *            command-line arguments
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		// Load config if exists
		Utilities.log("Core", "Attempting to load configuration from file...", false);
		try {
			File configFile = new File(FileUtils.getConfigDir() + "/config.dat");
			if (configFile.exists()) {
				Utilities.log("Core", "Loaded saved configuration", false);
				Kryo kryo = new Kryo();
				Input input = new Input(new FileInputStream(configFile));
				config = kryo.readObject(input, Config.class);
			} else {
				config = new Config();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Log.set(Log.LEVEL_INFO);
		boolean printPubKey = false;
		for (String str : args) {
			switch (str) {
				case "-daemon":
					Core.config.hubMode = true;
					break;

				case "-debug":
					Log.set(Log.LEVEL_DEBUG);
					break;

				case "-getpubkey":
					printPubKey = true;
					break;
			}
		}

		// GUI inits
		Utilities.log("Core", "Attempting automatic hubmode detection...", false);
		try {
			Utilities.log("Core", "Initializing front-end", false);
			TrayHandler.init();
		} catch (Exception ex) {
			Core.config.hubMode = true;
		}

		if (Core.config.hubMode) {
			Utilities.log("Core", "Hub/headless mode engaged", false);
		}

		// Set mutex
		Utilities.log("Core", "Calculating mutex", false);
		mutex = Utilities.getMutex();

		// Initialize crypto RSA
		Utilities.log("Core", "Initializing RSA", false);
		if (Core.config.rsaPub != null && Core.config.rsaPriv != null)
			rsa = new RSA(Core.config.rsaPub, Core.config.rsaPriv);
		else
			rsa = new RSA();
		if (printPubKey) {
			Utilities.log("Core", "Pubkey dump: " + RSA.pubKey, false);
		}

		// Initialize crypto AES
		Utilities.log("Core", "Initializing AES", false);
		aes = new AES(mutex);

		// File directory checks
		Utilities.log("Core", "Checking for file structures", false);
		FileUtils.initDirs();

		// ShutdownHook for config
		Thread shutHook = (new Thread(new Runnable() {
			public void run() {
				Utilities.log(this, "Writing config before shutting down", false);
				config.writeConfig();
			}
		}));
		shutHook.setName("Shutdown Hook");
		Runtime.getRuntime().addShutdownHook(shutHook);

		// Filter loading
		FilterUtils.init();

		// FileWatcher initialization
		Utilities.log("Core", "Registering file watcher", false);
		(new Thread(new FileWatcher())).start();

		// Vars initialization
		blockDex = new ArrayList<BlockedFile>();
		index = new HashMap<ArrayList<String>, ArrayList<String>>();
		peers = new ArrayList<Peer>();

		// Generate block index
		Utilities.log("Core", "Generating block index", false);
		FileUtils.genBlockIndex();

		// Initialize NetHandler object
		Utilities.log("Core", "Initializing networking", false);
		netHandler = new NetHandler();

		// Start APIRouter
		if (!Core.config.hubMode) {
			Utilities.log("Core", "Initializing API router", false);
			APIRouter.init();
		}

		Utilities.log("Core", "Done being initialized", false);

		// Open browser window (if this is not headless)
		try {
			// Debug
			File index = new File(FileUtils.getConfigDir() + "/web/index.html");
			if (!Core.config.hubMode) {
				if (!index.exists()) {
					JarFile file = new JarFile(
							new File(Core.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
					File webDir = new File(FileUtils.getConfigDir() + "/web");
					if (!webDir.exists()) {
						webDir.mkdir();
					}
					for (Enumeration<JarEntry> enume = file.entries(); enume.hasMoreElements();) {
						JarEntry entry = enume.nextElement();
						if (entry.getName().startsWith("web") && !entry.getName().equals("web/")) {
							try {
								File extLoc = new File(FileUtils.getConfigDir() + "/" + entry.getName());
								if (!extLoc.exists()) {
									if (entry.getName().endsWith("/")) {
										extLoc.mkdirs();
									} else {
										extLoc.getParentFile().mkdirs();
									}
								}
								InputStream is = file.getInputStream(entry);
								FileOutputStream fos = new FileOutputStream(extLoc);
								while (is.available() > 0) {
									fos.write(is.read());
								}
								fos.close();
							} catch (Exception ex) {
							}
						}
					}
					file.close();
				}
				Utilities.openWebpage(new URL("file:///" + index.getAbsolutePath()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Do peer discovery
		netHandler.peerDiscovery();
	}
}