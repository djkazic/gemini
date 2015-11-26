package io;

import java.util.ArrayList;

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
			
			int repeatRounds = 1;
			ArrayList<String> blockList = blockedFile.getBlockList();
			while(true) {
				for(int i=0; i < blockList.size(); i++) {
					if(!blockedFile.getBlacklist().contains(blockList.get(i))) {
						Utilities.log(this, "Requesting block " + blockList.get(i));
						NetHandler.requestBlock(blockedFile.getPointer().getName(), blockList.get(i));
						Thread.sleep(70);
					}
				}
				if(blockedFile.getProgressNum() == 100) {
					blockedFile.setComplete(true);
					break;
				}
				repeatRounds++;
				Utilities.log(this, "DL round " + repeatRounds);
				Thread.sleep(1000);
			}
			Utilities.log(this, "Assembling BlockedFile " + blockedFile.getPointer().getName());
			FileUtils.unifyBlocks(blockedFile);
		} catch (Exception ex) {
			Utilities.log(this, "Downloader exception:");
			ex.printStackTrace();
		}
	}	
}