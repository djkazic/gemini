package io.serialize;

import java.io.File;
import java.io.FileOutputStream;

import atrium.Core;
import atrium.Utilities;
import crypto.AES;
import io.BlockedFile;
import io.FileUtils;

public class StreamedBlock {

	private String originChecksum;
	private String blockName;
	private byte[] fileBytes;
	
	public StreamedBlock() {
		originChecksum = null;
		blockName = null;
		fileBytes = null;
	}
	
	public StreamedBlock(String originChecksum, String blockName, byte[] searchRes) {
		this.originChecksum = Core.aes.encrypt(originChecksum);
		this.blockName = Core.aes.encrypt(blockName);
		try {
			if(Core.config.hubMode) {
				//Already encrypted format
				fileBytes = searchRes;
			} else {
				//Encrypt for transmission
				fileBytes = Core.aes.encrypt(searchRes);
			}	
		} catch (Exception ex) {
			Utilities.log(this, "Could not get file bytes for StreamedBlock", true);
		}
	}
	
	public String getOrigin() {
		return originChecksum;
	}
	
	public byte[] getFileBytes() {
		return fileBytes;
	}
	
	public void insertSelf(final AES aes) {
		(new Thread(new Runnable() {
			public void run() {
				String blockDest = aes.decrypt(blockName);
				byte[] decrypted = aes.decrypt(fileBytes);
				
				//Match BlockedFile from blockDex by checksum
				BlockedFile bf = FileUtils.getBlockedFile(aes.decrypt(originChecksum));
				if(bf.isComplete()) {
					Utilities.log(this, "Discarding block, BlockedFile is done", true);
				} else {
					File folder = new File(bf.getBlocksFolder());
					File dest = new File(bf.getBlocksFolder() + "/" + blockDest);

					try {
						if(!folder.exists()) {
							Utilities.log(this, "Creating directory: " + folder, true);
							folder.mkdirs();
						}
						if(!dest.exists()) {
							//Utilities.log(this, "Writing block to " + dest);
							FileOutputStream fos = new FileOutputStream(dest);
							fos.write(decrypted);
							fos.close();
							if(FileUtils.generateChecksum(dest).equals(blockDest)) {
								Utilities.log(this, "Logging block into blacklist", true);
								bf.logBlock(blockDest);
								if(Core.config.hubMode) {
									//TODO: test hubmode block acceptance
									Utilities.log(this, "Hub mode: encrypting received block", true);
									dest.delete();
									FileOutputStream sfos = new FileOutputStream(dest);
									sfos.write(Core.aes.encrypt(decrypted));
									sfos.close();
								}
							} else {
								Utilities.log(this, "Checksum error for block " + blockDest, false);
								dest.delete();
							}
						} else {
							//TODO: remove debugging
							Utilities.log(this, "Race condition: already have this block", true);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		})).start();
	}
}