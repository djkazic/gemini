package org.cortex.Radiator.atrium;

import com.jfastnet.IServerHooks;

public class ServerHook implements IServerHooks {

	//TODO: add in outgoing functionality - new client each time
	@Override
	public void onRegister(int clientId) {
		Utilities.log(this, "New incoming peer, ID " + clientId);
		new Peer(clientId, 1);
	}
	
	@Override
	public void onUnregister(int clientId) {
		Peer foundPeer = Peer.findPeer(clientId);
		if(foundPeer != null) {
			foundPeer.disconnect();
		}
	}
}