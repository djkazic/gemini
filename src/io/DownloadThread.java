package io;

import atrium.NetHandler;

public class DownloadThread implements Runnable {

	private BlockedFile blockedFile;
	
	public DownloadThread(BlockedFile bf) {
		blockedFile = bf;
	}
	
	public void run() {
		String currentBlock;
		while((currentBlock = blockedFile.getNextBlock()) != null) {
			NetHandler.requestBlock(currentBlock);
		}
	}	
}