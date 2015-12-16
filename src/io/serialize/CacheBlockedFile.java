package io.serialize;

public class CacheBlockedFile extends StreamedBlockedFile {
	
	public CacheBlockedFile(String encryptedPointerName, String encryptedChecksum) {
		super(encryptedPointerName, encryptedChecksum, null);
	}
	
}