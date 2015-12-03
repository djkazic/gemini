package io.serialize;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import atrium.Core;
import io.FileUtils;

public class BlockdexSerializer {

	public static void run() {
		//Save blockdex.dat
		try {
			File blockDexCache = new File(FileUtils.getConfigDir() + "/eblockdex.dat");
			File protoBlockDexCache = new File(FileUtils.getConfigDir() + "/blockdex.dat");
			if(blockDexCache.exists()) {
				blockDexCache.delete();
			}
			if(protoBlockDexCache.exists()) {
				protoBlockDexCache.delete();
			}
			if(Core.blockDex.size() > 0) {
				protoBlockDexCache.createNewFile();
				Kryo kryo = new Kryo();
				FileOutputStream fos = new FileOutputStream(protoBlockDexCache);
				Output out = new Output(fos);
				ArrayList<SerialBlockedFile> kbf = new ArrayList<SerialBlockedFile> ();
				for(int i=0; i < Core.blockDex.size(); i++) {
					kbf.add(Core.blockDex.get(i).toSerialBlockedFile());
				}
				kryo.writeObject(out, kbf);
				out.close();
				
				byte[] fileBytes = Files.readAllBytes(protoBlockDexCache.toPath());
				byte[] encFileBytes = Core.aes.encrypt(fileBytes);
				FileOutputStream fosEnc = new FileOutputStream(blockDexCache);
				fosEnc.write(encFileBytes);
				fosEnc.close();
				
				protoBlockDexCache.delete();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
