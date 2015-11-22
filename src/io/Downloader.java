package io;

import atrium.FileUtils;
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
			blockedFile.setBlacklist(FileUtils.enumerateIncompleteBlacklist(blockedFile));
			String currentBlock;
			while((currentBlock = blockedFile.getNextBlock()) != null) {
				Utilities.log(this, "Requesting block " + currentBlock);
				NetHandler.requestBlock(blockedFile.getPointer().getName(), currentBlock);
				Thread.sleep(1000);
			}
			FileUtils.unifyBlocks(blockedFile);
		} catch (Exception ex) {
			Utilities.log(this, "Downloader exception:");
			ex.printStackTrace();
		}
	}	
}