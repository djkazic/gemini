package net.poll;

import atrium.Peer;
import atrium.Utilities;
import packets.requests.Request;
import packets.requests.RequestTypes;

public class CachePoller implements Runnable {
	
	private Peer peer;
	
	public CachePoller(Peer peer) {
		this.peer = peer;
	}

	public void run() {
		try {
			while(peer.getConnection().isConnected()) {
				if(peer.getAES() == null) {
					Thread.sleep(1000);
					continue;
				}
				Utilities.log(this, "Polling peer " + peer.getMutex() + " for cache", false);
				peer.getConnection().sendTCP(new Request(RequestTypes.CACHE, null));
				Thread.sleep(60000);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
