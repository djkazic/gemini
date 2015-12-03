package atrium;

import java.io.File;
import java.io.FileOutputStream;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import io.FileUtils;

//TODO: write a ShutdownHook for saving a serialized Config
public class Config {

	public int tcpPort = 35500;
	public int udpPort = 35501;
	public boolean notifiedPortForwarding = false;
	
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}