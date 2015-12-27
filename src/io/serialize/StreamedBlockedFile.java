package io.serialize;

import java.util.ArrayList;

import atrium.Core;
import crypto.AES;
import io.block.BlockedFile;
import io.block.Metadata;

/**
 * Representation of a BlockedFile for transmission
 * Currently is done by default; this may change in an update
 *
 */
public class StreamedBlockedFile {

	private String pointerName;
	private String checksum;
	private ArrayList<String> blockList;
	private Metadata metadata;
	
	public StreamedBlockedFile() {
		pointerName = null;
		checksum = null;
		blockList = null;
		metadata = null;
	}
	
	public StreamedBlockedFile(String encryptedPointerName, String encryptedChecksum, 
							   ArrayList<String> encryptedBlockList, Metadata metadata) {
		pointerName = encryptedPointerName;
		checksum = encryptedChecksum;
		blockList = encryptedBlockList;
		this.metadata = metadata;
	}
	
	public BlockedFile toBlockedFile(AES aes) {
		ArrayList<String> decrypted = new ArrayList<String> ();
		for(int i=0; i < blockList.size(); i++) {
			decrypted.add(aes.decrypt(blockList.get(i)));
		}
		if(hasMetadata()) {
			if(!Core.metaDex.contains(metadata)) {
				Core.metaDex.add(metadata);
			}
		}
		return new BlockedFile(aes.decrypt(pointerName), aes.decrypt(checksum), decrypted);
	}
	
	public boolean hasMetadata() {
		return metadata != null;
	}
}
