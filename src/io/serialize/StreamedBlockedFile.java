package io.serialize;

import java.util.ArrayList;

import crypto.AES;
import io.BlockedFile;

/**
 * Representation of a BlockedFile for transmission
 * Currently is done by default; this may change in an update
 *
 */
public class StreamedBlockedFile {

	private String pointerName;
	private String checksum;
	private ArrayList<String> blockList;
	
	public StreamedBlockedFile() {
		pointerName = null;
		blockList = null;
	}
	
	public StreamedBlockedFile(String encryptedPointerName, String encryptedChecksum, ArrayList<String> encryptedBlockList) {
		pointerName = encryptedPointerName;
		checksum = encryptedChecksum;
		blockList = encryptedBlockList;
	}
	
	public BlockedFile toBlockedFile(AES aes) {
		ArrayList<String> decrypted = new ArrayList<String> ();
		for(int i=0; i < blockList.size(); i++) {
			decrypted.add(aes.decrypt(blockList.get(i)));
		}
		return new BlockedFile(aes.decrypt(pointerName), aes.decrypt(checksum), decrypted);
	}
}
