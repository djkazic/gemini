package io.serialize;

import java.io.File;
import java.io.FileOutputStream;

import atrium.Core;
import atrium.Utilities;
import crypto.AES;
import io.BlockedFile;
import io.FileUtils;

public class StreamedBlock {

	private String origin;
	private String blockName;
	private byte[] fileBytes;
	
	public StreamedBlock() {
		origin = null;
		blockName = null;
		fileBytes = null;
	}
	
	public StreamedBlock(String origin, String blockName, byte[] searchRes) {
		this.origin = Core.aes.encrypt(origin);
		this.blockName = Core.aes.encrypt(blockName);
		try {
			fileBytes = Core.aes.encrypt(searchRes);
		} catch (Exception ex) {
			Utilities.log(this, "Could not get file bytes for StreamedBlock");
		}
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public byte[] getFileBytes() {
		return fileBytes;
	}
	
	public void insertSelf(final AES aes) {
		(new Thread(new Runnable() {
			public void run() {
				String blockDest = aes.decrypt(blockName);
				byte[] decrypted = aes.decrypt(fileBytes);
				//Utilities.log(this, "Decrypted bytes size: " + decrypted.length);
				BlockedFile bf = FileUtils.getBlockedFile(aes.decrypt(origin));
				if(bf.isComplete()) {
					Utilities.log(this, "Discarding block, BlockedFile is done");
				} else {
					File folder = new File(bf.getBlocksFolder());
					File dest = new File(bf.getBlocksFolder() + "/" + blockDest);

					try {
						if(!folder.exists()) {
							Utilities.log(this, "Creating directory: " + folder);
							folder.mkdirs();
						}
						if(!dest.exists()) {
							//Utilities.log(this, "Writing block to " + dest);
							Utilities.log(this, "Logging block into blacklist");
							bf.logBlock(blockDest);
							FileOutputStream fos = new FileOutputStream(dest);
							fos.write(decrypted);
							fos.close();
						} else {
							//TODO: remove debugging
							Utilities.log(this, "Race condition: already have this block");
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		})).start();
	}
}