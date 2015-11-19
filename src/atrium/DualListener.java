package atrium;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import data.Data;
import data.DataTypes;
import requests.Request;
import requests.RequestTypes;

public class DualListener extends Listener {
	
	private NetHandler net;
	
	public DualListener(NetHandler nh) {
		super();
		net = nh;
	}

	//New incoming connection
	public void connected(Connection connection) {
		try {
			Peer newPeer = new Peer(connection, 1);
			newPeer.getConnection().sendTCP(new Request(RequestTypes.PUBKEY, null));
			newPeer.getCryptoLatch().await();
			newPeer.getConnection().sendTCP(new Request(RequestTypes.MUTEX, null));
			newPeer.getConnection().sendTCP(new Request(RequestTypes.PEERLIST, null));
			//Wait for all fields done for two way
			net.addPeer(newPeer);
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
			case RequestTypes.MUTEX:
				Utilities.log(this, "Received request for mutex");
				connection.sendTCP(new Data(DataTypes.MUTEX, Core.mutex));
				break;

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

			switch(type) {
			case DataTypes.MUTEX:
				Utilities.log(this, "Received mutex data");
				Peer.findPeer(connection).setMutex((String) data.getPayload());
				break;

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
