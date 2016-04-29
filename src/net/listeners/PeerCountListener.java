package net.listeners;

import atrium.Core;

public class PeerCountListener implements Runnable {

	private boolean triggeredReady = false;

	public void run() {
		while (true) {
			try {
				if (Core.peers.size() == 0) {
					if (!Core.config.hubMode) {
						if (Core.index.size() > 0) {
							// No more peers, so reset search results and data
							Core.index.clear();
							// TODO: container clear
							// Core.mainWindow.clearSearchData();
							// Core.mainWindow.out("Ready");
						}
					} else {
						return;
					}
				} else {
					if (!triggeredReady && !Core.config.hubMode) {
						triggeredReady = true;
						// TODO: mainWindow ready call
						// Core.mainWindow.ready();
					}
				}
				Thread.sleep(450);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}