package listeners;

import atrium.Core;

public class PeerCountListener implements Runnable {
	
	public void run() {
		while(true) {
			try {
				if(Core.peers.size() == 0) {
					if(!Core.config.hubMode) {
						if(Core.index.size() > 0) {
							//No more peers, so reset search results and data
							Core.index.clear();
							Core.mainWindow.clearSearchData();
							Core.mainWindow.out("Ready");
						}
					}
				}
				Thread.sleep(450);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}