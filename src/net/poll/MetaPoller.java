package net.poll;

import atrium.Core;
import atrium.Peer;
import atrium.Utilities;
import packets.requests.Request;
import packets.requests.RequestTypes;

public class MetaPoller implements Runnable {

	public void run() {
		while(true) {
			try {
				for(int i=0; i < Core.peers.size(); i++) {
					Peer peer = Core.peers.get(i);
					Thread.sleep(2500);
					if(peer.getConnection().isConnected()) {
						if(peer.getAES() == null) {
							continue;
						}
						Utilities.log(this, "Polling peer " + peer.getMutex() + " for metadata", false);
						peer.getConnection().sendTCP(new Request(RequestTypes.METADATA, null));
					}
				}
				Thread.sleep(45000);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
