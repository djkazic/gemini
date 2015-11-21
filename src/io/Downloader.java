package io;

import atrium.NetHandler;
import atrium.Utilities;

public class Downloader implements Runnable {

	private BlockedFile blockedFile;
	
	public Downloader(BlockedFile bf) {
		blockedFile = bf;
		Utilities.log(this, "Downloader initialized for BlockedFile " + bf.getPointer().getName());
	}
	
	public void run() {
		try {
			String currentBlock;
			while((currentBlock = blockedFile.getNextBlock()) != null) {
				NetHandler.requestBlock(currentBlock);
				Thread.sleep(1);
			}
		} catch (Exception ex) {
			Utilities.log(this, "Downloader exception:");
			ex.printStackTrace();
		}
	}	
}