package atrium;

import java.util.ArrayList;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import data.Data;
import data.DataTypes;
import requests.Request;
import requests.RequestTypes;

public class DualListener extends Listener {
	
	private int inOut;
	
	public DualListener(int inOut) {
		super();
		this.inOut = inOut;
	}

	//New connection, either incoming or outgoing
	public void connected(Connection connection) {
		if(inOut == 1) {
			Utilities.log(this, "New incoming peer");
		} else {
			Utilities.log(this, "New outgoing peer");
		}
		try {
			if(inOut == 1) {
				new Peer(connection, inOut);
			} else {
				new Peer(connection, inOut);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//New incoming packet post-connection
	public void received(Connection connection, Object object) {
		Peer foundPeer = Peer.findPeer(connection);
		
		if(object instanceof Request) {
			Request request = (Request) object;
			String type = request.getType();

			switch(type) {
				//Requests below are non-encrypted
			
				case RequestTypes.PUBKEY:
					Utilities.log(this, "Received request for pubkey");
					connection.sendTCP(new Data(DataTypes.PUBKEY, Core.pubKey));
					Utilities.log(this, "\tSent pubkey back");
					break;
			
				case RequestTypes.MUTEX:
					Utilities.log(this, "Received request for mutex");
					connection.sendTCP(new Data(DataTypes.MUTEX, Core.rsa.encrypt(Core.mutex, foundPeer.getPubkey())));
					Utilities.log(this, "\tSent mutex back");
					break;
					
				//TODO: Requests below are symmetrically encrypted
	
				case RequestTypes.PEERLIST:
					Utilities.log(this, "Received request for peerlist");
					//TODO: more refined peerList filtering
					ArrayList<String> refinedPeerList = new ArrayList<String> ();
					for(Peer peer : NetHandler.peers) {
						//if(peer.externallyVisible())
						String peerData = peer.getConnection().getRemoteAddressTCP().getHostString() + ":"
										+ peer.getConnection().getRemoteAddressTCP().getPort();
						refinedPeerList.add(Core.aes.encrypt(peerData));
					}
					connection.sendTCP(new Data(DataTypes.PEERLIST, refinedPeerList));
					Utilities.log(this, "\tSent peerlist back");
					if(foundPeer.getInOut() == 0) {
						foundPeer.getDeferredLatch().countDown();
					}
					break;
			}
		} else if(object instanceof Data) {
			Data data = (Data) object;
			String type = data.getType();

			switch(type) {
				//Data below are encryption keys, mutex is encrypted via RSA
			
				case DataTypes.PUBKEY:
					Utilities.log(this, "Received pubkey data: ");
					String pubkeyData = (String) data.getPayload();
					Utilities.log(this, "\t" + pubkeyData);
					foundPeer.setPubkey(pubkeyData);
					foundPeer.getCryptoLatch().countDown();
					break;
			
				case DataTypes.MUTEX:
					Utilities.log(this, "Received mutex data");
					String encryptedMutex = (String) data.getPayload();
					try {
						String mutexData = Core.rsa.decrypt(encryptedMutex);
						foundPeer.mutexCheck(mutexData);
					} catch (Exception ex) {
						Utilities.log(this, "Failed to set mutex");
						ex.printStackTrace();
					}
					break;
	
				//All data past this point is encrypted via symmetric encryption
				//TODO: symmetric encryption for peerlist and on
					
				case DataTypes.PEERLIST:
					Utilities.log(this,  "Received peerlist data");
					//TODO: implement peerlist processing
					Object payload = data.getPayload();
					if(payload instanceof ArrayList<?>) {
						ArrayList<String> finishedList = new ArrayList<String> ();
						ArrayList<?> potentialList = (ArrayList<?>) payload;
						for(int i=0; i < potentialList.size(); i++) {
							Object o = potentialList.get(i);
							if(o instanceof String) {
								String encrypted = (String) o;
								String decrypted = foundPeer.getAES().decrypt(encrypted);
								finishedList.add(decrypted);
							}
						}
						Utilities.log(this, "\tPeerlist: " + finishedList);
					}
					break;
			}
		} else {
			Utilities.log(this, "Unknown object recevied:");
			Utilities.log(this, object.toString());
		}
	}

}
