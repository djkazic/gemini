package atrium;

import java.security.PublicKey;
import javax.crypto.SealedObject;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import data.Data;
import data.DataTypes;
import requests.Request;
import requests.RequestTypes;

public class DualListener extends Listener {
	
	private NetHandler netHandler;
	private int inOut;
	
	public DualListener(NetHandler netHandler, int inOut) {
		super();
		this.netHandler = netHandler;
		this.inOut = inOut;
	}

	//New connection, either incoming or outgoing
	public void connected(Connection connection) {
		try {
			Peer newPeer = null;
			if(inOut == 1) {
				newPeer = new Peer(connection, inOut);
				newPeer.getConnection().sendTCP(new Request(RequestTypes.PUBKEY, null));
				newPeer.getCryptoLatch().await();
				newPeer.getConnection().sendTCP(new Request(RequestTypes.MUTEX, null));
				newPeer.getConnection().sendTCP(new Request(RequestTypes.PEERLIST, null));
			} else {
				newPeer = new Peer(connection, inOut);
				//Wait until their requests are done
				//TODO: replace static wait with latch
				Thread.sleep(1000);
				newPeer.getConnection().sendTCP(new Request(RequestTypes.PUBKEY, null));
				newPeer.getCryptoLatch().await();
				newPeer.getConnection().sendTCP(new Request(RequestTypes.MUTEX, null));
				newPeer.getConnection().sendTCP(new Request(RequestTypes.PEERLIST, null));
			}
			netHandler.addPeer(newPeer);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//New incoming packet post-connection
	public void received(Connection connection, Object object) {
		if(object instanceof Request) {
			Request request = (Request) object;
			String type = request.getType();

			switch(type) {
				//Requests below are non-encrypted
			
				case RequestTypes.PUBKEY:
					Utilities.log(this, "Received request for pubkey");
					connection.sendTCP(new Data(DataTypes.PUBKEY, Core.pubKey));
					break;
			
				case RequestTypes.MUTEX:
					Utilities.log(this, "Received request for mutex");
					connection.sendTCP(new Data(DataTypes.MUTEX, Core.mutex));
					break;
					
				//Requests below are symmetrically encrypted
	
				case RequestTypes.PEERLIST:
					Utilities.log(this, "Received request for peerlist");
					//TODO: more refined peerList filtering
					connection.sendTCP(new Data(DataTypes.PEERLIST, NetHandler.peers));
					break;

				default: 
					Utilities.log(this, "Got request type " + type + " with payload of " + request.getPayload().toString());
					break;
			}
		}

		if(object instanceof Data) {
			Data data = (Data) object;
			String type = data.getType();
			Peer foundPeer = Peer.findPeer(connection);

			switch(type) {
				//Data below are encryption keys, mutex is encrypted via RSA
			
				case DataTypes.PUBKEY:
					Utilities.log(this,  "Received pubkey data");
					PublicKey pubkeyData = (PublicKey) data.getPayload();
					foundPeer.setPubkey(pubkeyData);
					foundPeer.getCryptoLatch().countDown();
					break;
			
				case DataTypes.MUTEX:
					Utilities.log(this, "Received mutex data");
					SealedObject sealedMutex = (SealedObject) data.getPayload();
					try {
						String mutexData = Core.rsa.decrypt(sealedMutex, foundPeer.getPubkey());
						foundPeer.setMutex(mutexData);
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
					Utilities.log(this, "Peerlist: " + (String) data.getPayload());
					break;
	
				default: 
					Utilities.log(this, "Got data type " + type + " with payload of " + data.getPayload().toString());
					break;
			}
		}
	}

}
