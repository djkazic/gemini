package io;

import java.util.ArrayList;

import atrium.Core;
import atrium.NetHandler;
import atrium.Utilities;
import io.block.BlockedFile;

public class Downloader implements Runnable {

	public static ArrayList<Downloader> downloaders = new ArrayList<Downloader> ();

	private BlockedFile blockedFile;
	private boolean stream = false;
	private boolean download = true;

	public Downloader(BlockedFile blockedFile, boolean stream) {
		this.blockedFile = blockedFile;
		this.stream = stream;
	}

	public void run() {
		try {
			for(Downloader downloader : downloaders) {
				if(downloader != this && downloader.blockedFile.getChecksum().equals(blockedFile.getChecksum())) {
					return;
				}
			}
			
			while(downloaders.size() > 0) {
				Thread.sleep(1000);
				continue;
			}

			downloaders.add(this);
			Utilities.log(this, "Downloader instance created for BlockedFile " + blockedFile.getPointer().getName(), true);
		
			Utilities.log(this, "Enumerating block data blacklist", true);
			blockedFile.setBlackList(FileUtils.enumerateIncompleteBlackList(blockedFile));

			//Counter for length of blocks until we reach a quadrant of (~32MB)
			int quadrantMark = 0;

			String lastBlock = null;
			String block = null;

			Utilities.log(this, "Starting download for " + blockedFile.getPointer().getName(), false);

			while(Core.peers.size() > 0 && !blockedFile.isComplete()) {
				if(!download) {
					Utilities.log(this, "Idling in pause", true);
					//TODO: GUI update for Downloader pause
					Thread.sleep(3000);
					continue;
				}

				if(stream) {
					block = blockedFile.nextStreamBlock();
				} else {
					block = blockedFile.nextRandomBlock();
				}
				
				if(block != null && !block.equals(lastBlock)) {
					lastBlock = block;
					Utilities.log(this, "Requesting block " + block, true);
					if(Core.peers.size() == 0) {
						quadrantMark += Core.blockSize;
					}
					NetHandler.requestBlock(blockedFile.getChecksum(), block);
					Thread.sleep(90 / Core.peers.size());
				} else if(block != null && block.equals(lastBlock)) {
					Utilities.log(this, "Bad randomness, continue loop", true);
					Thread.sleep(25);
					continue;
				} else if(block == null) {
					Utilities.log(this, "BlockedFile " + blockedFile.getPointer().getName() + " is complete", false);
					blockedFile.setComplete(true);
					blockedFile.updateProgress();
					BlockedFile.serializeAll();
				}

				if(Core.peers.size() >= 3) {
					//If bytes counter is greater or equal to 32MB
					if(quadrantMark >= (32000 * 1000)) {
						Utilities.log(this, "Sleep for 32nd quadrant initiated", true);
						quadrantMark = 0;
						long defaultWait = 750;
						defaultWait *= (0.45D + (blockedFile.getProgressNum() / 100D));
						Utilities.log(this, "Quadrant sleep: " + defaultWait, true);
						Thread.sleep(defaultWait);
					}
				}
				
				if(stream) {
					FileUtils.unifyBlocksStream(blockedFile);
				}
			}

			download = false;
			if(Core.config.hubMode || blockedFile.getCacheStatus()) {
				Utilities.log(this, "Successful BlockedFile cache: " + blockedFile.getPointer().getName(), true);
			} else if(!stream) {
				Utilities.log(this, "Assembling BlockedFile: " + blockedFile.getPointer().getName(), true);
				FileUtils.unifyBlocks(blockedFile);
			}
			
			downloaders.remove(this);
		} catch (Exception ex) {
			Utilities.log(this, "Downloader exception: ", false);
			ex.printStackTrace();
			downloaders.remove(this);
		}
	}

	public static void pauseDownloader(String bfPointerStr) {
		for(Downloader dl : downloaders) {
			if(dl.blockedFile.getChecksum().equals(bfPointerStr)) {
				dl.download = false;
			}
		}
	}

	public static void resumeDownloader(String bfPointerStr) {
		for(Downloader dl : downloaders) {
			if(dl.blockedFile.getChecksum().equals(bfPointerStr)) {
				if(!dl.download) {
					//TODO: updateTime GUI fix
					/**
					if(!Core.config.hubMode) {
						Core.mainWindow.updateTime(dl.blockedFile.getChecksum(), " ... ");
					}
					 */
					dl.download = true;
				}
			}
		}
	}

	public static void removeDownloader(String bfPointerStr) {
		for(Downloader dl : downloaders) {
			if(dl.blockedFile.getChecksum().equals(bfPointerStr)) {
				if(!dl.download) {
					dl.download = false;
					downloaders.remove(dl);
				}
			}
		}
	}
}