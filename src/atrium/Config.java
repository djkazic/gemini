package atrium;

import java.io.File;
import java.io.FileOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import io.FileUtils;

public class Config {

	//Instance variables, serialized data
	public int tcpPort = 35500;
	public int discoverPort = 35501;
	public boolean cacheEnabled = false;
	public boolean notifiedPortForwarding = false;
	public boolean hubMode = false;
	public String generatedMAC = null;
	
	/**
	 * Writes configuration to disk (config.dat)
	 */
	public void writeConfig() {
		//Try saving config data
		try {
			File configFile = new File(FileUtils.getConfigDir() + "/config.dat");
			if(configFile.exists()) {
				configFile.delete();
			}
			configFile.createNewFile();
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