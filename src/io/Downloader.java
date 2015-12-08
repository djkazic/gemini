package io;

import java.util.ArrayList;

import atrium.Core;
import atrium.NetHandler;
import atrium.Utilities;

public class Downloader implements Runnable {

	private BlockedFile blockedFile;
	
	public Downloader(BlockedFile bf) {
		blockedFile = bf;
		Utilities.log(this, "Downloader instance created for BlockedFile " + bf.getPointer().getName());
	}
	
	public void run() {
		try {
			blockedFile.setBlacklist(FileUtils.enumerateIncompleteBlacklist(blockedFile));
			
			int repeatRounds = 1;
			ArrayList<String> blockList = blockedFile.getBlockList();
			
			//Counter for length of blocks until we reach a quadrant of (~32MB)
			int quadrantMark = 0;
			
			while(Core.peers.size() > 0) {
				for(int i=0; i < blockList.size(); i++) {
					String block = null;
					if((block = blockedFile.getNextBlock()) != null) {
						Utilities.log(this, "Requesting block " + block);
						quadrantMark += Core.blockSize;
						NetHandler.requestBlock(blockedFile.getPointer().getName(), block);
						//long defaultWait = 100;
						//defaultWait *= (1 + ((blockedFile.getProgressNum() / 100D) * 1.5));
						//Utilities.log(this, "blockSleep: " + defaultWait);
						Thread.sleep(100);
					}
					
					//If bytes counter is greater or equal to 32MB
					if(quadrantMark >= (32000 * 1000)) {
						Utilities.log(this, "Sleep for 32nd quadrant initiated");
						quadrantMark = 0;
						long defaultWait = 1000;
						defaultWait *= (0.65D + (blockedFile.getProgressNum() / 100D));
						Utilities.log(this, "blockSleep: " + defaultWait);
						Thread.sleep(defaultWait);
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