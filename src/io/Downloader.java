package io;

import java.io.File;
import java.util.ArrayList;

import atrium.Core;
import atrium.NetHandler;
import atrium.Utilities;
import io.block.BlockedFile;

public class Downloader implements Runnable {

	public static ArrayList<Downloader> downloaders = new ArrayList<Downloader>();

	private BlockedFile blockedFile;
	private boolean download = true;

	public Downloader(BlockedFile blockedFile) {
		this.blockedFile = blockedFile;
	}

	public void run() {
		try {
			for (Downloader downloader : downloaders) {
				if (downloader != this && downloader.blockedFile.getChecksum().equals(blockedFile.getChecksum())) {
					return;
				}
			}

			while (downloaders.size() > 0) {
				Thread.sleep(1000);
				continue;
			}

			downloaders.add(this);
			Utilities.log(this, "Downloader instance created for BlockedFile " + blockedFile.getPointer().getName(),
					true);

			Utilities.log(this, "Enumerating block data blacklist", true);
			blockedFile.setBlackList(FileUtils.enumerateIncompleteBlackList(blockedFile));

			String lastBlock = null;
			String block = null;

			Utilities.log(this, "Starting download for " + blockedFile.getPointer().getName(), false);

			while (Core.peers.size() > 0 && !blockedFile.isComplete()) {
				if (!download) {
					Utilities.log(this, "Idling in pause", true);
					Thread.sleep(3000);
					continue;
				}

				block = blockedFile.nextRandomBlock();

				if (block != null && !block.equals(lastBlock)) {
					lastBlock = block;
					Utilities.log(this, "Requesting block " + block, true);
					NetHandler.requestBlock(blockedFile.getChecksum(), block);
					Thread.sleep(120 / Core.peers.size());
				} else if (block != null && block.equals(lastBlock)) {
					Utilities.log(this, "Bad randomness, continue loop", true);
					Thread.sleep(25);
					continue;
				} else if (block == null) {
					Utilities.log(this, "BlockedFile " + blockedFile.getPointer().getName() + " is complete", false);
					blockedFile.setComplete(true);
					blockedFile.getBufferFileChannel().close();
					blockedFile.updateProgress();
					BlockedFile.serializeAll();
				}
			}

			// Finish condition
			try {
				File bufferFile = blockedFile.getBufferFile();
				if (bufferFile.exists()) {
					File newFile = new File(FileUtils.getWorkspaceDir() + "/" + blockedFile.getPointer().getName());
					boolean rename = false;
					while (!rename) {
						rename = bufferFile.renameTo(newFile);
						Utilities.log(this, "Finalizing buffer file: " + rename, false);
						Thread.sleep(25);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			download = false;
			if (Core.config.hubMode || blockedFile.getCacheStatus()) {
				Utilities.log(this, "Successful BlockedFile cache: " + blockedFile.getPointer().getName(), true);
			}

			downloaders.remove(this);
		} catch (Exception ex) {
			Utilities.log(this, "Downloader exception: ", false);
			ex.printStackTrace();
			downloaders.remove(this);
		}
	}

	public static void pauseDownloader(String bfPointerStr) {
		for (Downloader dl : downloaders) {
			if (dl.blockedFile.getChecksum().equals(bfPointerStr)) {
				dl.download = false;
			}
		}
	}

	public static void resumeDownloader(String bfPointerStr) {
		for (Downloader dl : downloaders) {
			if (dl.blockedFile.getChecksum().equals(bfPointerStr)) {
				if (!dl.download) {
					// TODO: updateTime GUI fix
					/**
					 * if(!Core.config.hubMode) { Core.mainWindow.updateTime(dl.blockedFile.getChecksum(), " ... "); }
					 */
					dl.download = true;
				}
			}
		}
	}

	public static void removeDownloader(String bfPointerStr) {
		for (Downloader dl : downloaders) {
			if (dl.blockedFile.getChecksum().equals(bfPointerStr)) {
				if (!dl.download) {
					dl.download = false;
					downloaders.remove(dl);
				}
			}
		}
	}
}