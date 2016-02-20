package atrium;

import java.io.File;
import java.io.FileOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import io.FileUtils;

public class Config {
	
	//Networking
	public int tcpPort = 35500;
	public int discoverPort = 35501;
	public int webPort = 17380;
	public boolean cacheEnabled = false;
	public String generatedMAC = null;
	
	//GUI
	public boolean hubMode = false;
	public boolean clearBarAfterSearch = true;
	public boolean notifiedPortForwarding = false;

	//Crypto
	public byte[] rsaPub;
	public byte[] rsaPriv;
	
	/**
	 * Writes configuration to disk (config.dat)
	 */
	public void writeConfig() {
		try {
			File configFile = new File(FileUtils.getConfigDir() + "/config.dat");
			if(configFile.exists()) {
				configFile.delete();
			}
			configFile.createNewFile();
			rsaPub = Core.rsa.publicKeyBytes();
			rsaPriv = Core.rsa.privateKeyBytes();
			Kryo kryo = new Kryo();
			FileOutputStream fos = new FileOutputStream(configFile);
			Output out = new Output(fos);
			kryo.writeObject(out, this);
			out.close();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}