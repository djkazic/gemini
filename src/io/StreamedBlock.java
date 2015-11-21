package io;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import atrium.Core;
import atrium.FileUtils;
import atrium.Utilities;
import crypto.AES;

public class StreamedBlock {

	private String origin;
	private String blockName;
	private byte[] fileBytes;
	
	public StreamedBlock() {
		origin = null;
		blockName = null;
		fileBytes = null;
	}
	
	public StreamedBlock(String origin, String blockName, File file) {
		this.origin = Core.aes.encrypt(origin);
		this.blockName = Core.aes.encrypt(blockName);
		try {
			fileBytes = Core.aes.encrypt(Files.readAllBytes(file.toPath()));
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
	
	public void insertSelf(AES aes) {
		byte[] decrypted = aes.decrypt(fileBytes);
		Utilities.log(this, "Decrypted bytes size: " + decrypted.length);
		BlockedFile bf = FileUtils.getBlockedFile(aes.decrypt(origin));
		File folder = new File(bf.getBlocksFolder());
		String blockDest = aes.decrypt(blockName);
		File dest = new File(bf.getBlocksFolder() + "/" + blockDest);
		bf.getBlacklist().add(blockDest);
		try {
			if(!folder.exists()) {
				folder.mkdirs();
			}
			if(!dest.exists()) {
				//TODO: remove debugging
				Utilities.log(this, "Writing block to " + dest);
				FileOutputStream fos = new FileOutputStream(dest, true);
				fos.write(decrypted);
				fos.close();
			} else {
				//TODO: remove debugging
				Utilities.log(this, "Duplication error: already have this block");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}