package io;

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
			blockedFile.setBlackList(FileUtils.enumerateIncompleteBlackList(blockedFile));

			//Counter for length of blocks until we reach a quadrant of (~32MB)
			int quadrantMark = 0;

			String lastBlock = null;
			String block = null;
			while(Core.peers.size() > 0 || !blockedFile.isComplete()) {
				block = blockedFile.nextBlockNeeded();
				
				if(block != null && !block.equals(lastBlock)) {
					lastBlock = block;
					Utilities.log(this, "Requesting block " + block);
					quadrantMark += Core.blockSize;
					NetHandler.requestBlock(blockedFile.getPointer().getName(), block);
					//long defaultWait = 100;
					//defaultWait *= (1 + ((blockedFile.getProgressNum() / 100D) * 1.5));
					//Utilities.log(this, "blockSleep: " + defaultWait);
					Thread.sleep(100);
				} else if(block != null && block.equals(lastBlock)) {
					Utilities.log(this, "Bad randomness, continue loop");
					Thread.sleep(5);
					continue;
				} else {
					if(blockedFile.getProgressNum() == 100) {
						Utilities.log(this, "BlockedFile " + blockedFile.getPointer().getName() + " is complete");
						blockedFile.setComplete(true);
						break;
					}
				}

				//If bytes counter is greater or equal to 32MB
				if(quadrantMark >= (32000 * 1000)) {
					Utilities.log(this, "Sleep for 32nd quadrant initiated");
					quadrantMark = 0;
					long defaultWait = 1000;
					defaultWait *= (0.75D + (blockedFile.getProgressNum() / 100D));
					Utilities.log(this, "blockSleep: " + defaultWait);
					Thread.sleep(defaultWait);
				}
			}
			Utilities.log(this, "Assembling BlockedFile " + blockedFile.getPointer().getName());
			FileUtils.unifyBlocks(blockedFile);
		} catch (Exception ex) {
			Utilities.log(this, "Downloader exception:");
			ex.printStackTrace();
		}
	}	
}