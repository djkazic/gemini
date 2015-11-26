package atrium;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.CountDownLatch;
import javax.xml.bind.DatatypeConverter;
import com.esotericsoftware.kryonet.Connection;
import crypto.AES;
import data.Data;
import data.DataTypes;
import requests.Request;
import requests.RequestTypes;

public class Peer {

	private CountDownLatch deferredRequesting;
	private CountDownLatch pubkeyDone;
	private CountDownLatch cryptoDone;
	private Connection connection;
	private PublicKey pubkey;
	private AES aes;
	private String mutex;
	private int inOut;  //= 1 for incoming

	public Peer(Connection connection, int inOut) {
		//Add ourselves to peers without data
		NetHandler.peers.add(this);
		if(!Core.headless) {
			Core.mainWindow.updatePeerCount();
		}
				
		deferredRequesting = new CountDownLatch(1);
		pubkeyDone = new CountDownLatch(1);
		cryptoDone = new CountDownLatch(1);
		this.connection = connection;
		this.inOut = inOut;

		bootstrapRequests();
	}
	
	private void bootstrapRequests() {
		(new Thread(new Runnable() {
			public void run() {
				try {
					if(inOut == 1) {
						//Pro-active request approach as a psuedo-server
						Utilities.log(this, "Sending our pubkey first");
						connection.sendTCP(new Data(DataTypes.PUBKEY, Core.pubKey));
						Utilities.log(this, "Requesting peer's pubkey");
						connection.sendTCP(new Request(RequestTypes.PUBKEY, null));
						Utilities.log(this, "Awaiting peer's pubkey");
						pubkeyDone.await();
						Utilities.log(this, "Requesting peer's mutex");
						connection.sendTCP(new Request(RequestTypes.MUTEX, null));
						cryptoDone.await();
						aes = new AES(mutex);
						Utilities.log(this, "Requesting peer's peerlist");
						connection.sendTCP(new Request(RequestTypes.PEERLIST, null));
					} else {
						//When we send back a peerList, it's time to start sending requests
						//Keeping pubkeyDone as a sequential check
						pubkeyDone.await();
						deferredRequesting.await();
						Utilities.log(this, "Requesting peer's mutex");
						connection.sendTCP(new Request(RequestTypes.MUTEX, null));
						cryptoDone.await();
						aes = new AES(mutex);
						Utilities.log(this, "Requesting peer's peerlist");
						connection.sendTCP(new Request(RequestTypes.PEERLIST, null));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		})).start();
	}
	
	public void disconnect() {
		if(mutex != null) {
			Utilities.log(this, "Peer " + mutex + " disconnected");
		} else {
			Utilities.log(this, "Peer disconnected (mutex was null on disconnect");
		}
		NetHandler.peers.remove(this);
		if(!Core.headless) {
			Core.mainWindow.updatePeerCount();
		}
		connection.close();
	}

	public Connection getConnection() {
		return connection;
	}

	public PublicKey getPubkey() {
		return pubkey;
	}

	public String getMutex() {
		return mutex;
	}

	public int getInOut() {
		return inOut;
	}
	
	public AES getAES() {
		return aes;
	}

	public CountDownLatch getDeferredLatch() {
		return deferredRequesting;
	}
	
	public CountDownLatch getPubkeyLatch() {
		return pubkeyDone;
	}
	
	public CountDownLatch getCryptoLatch() {
		return cryptoDone;
	}

	public boolean setPubkey(String pubkey) {
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(pubkey));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(keySpec);
			this.pubkey = pk;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public void setMutex(String mutex) {
		this.mutex = mutex;
	}

	public static Peer findPeer(Connection connection) {
		for(Peer peer : NetHandler.peers) {
			if(peer.getConnection().equals(connection)) {
				return peer;
			}
		}
		return null;
	}
	
	public boolean mutexCheck(String mutexData) {
		if(mutexData.equals(Core.mutex)) {
			disconnect();
			return false;
		} else {
			boolean passed = true;
			for(Peer peer : NetHandler.peers) {
				if(peer != this && peer.getMutex().equals(mutexData)) {
					passed = false;
					break;
				}
			}
			if(passed) {
				setMutex(mutexData);
				return true;
			} else {
				disconnect();
				return false;
			}	
		}
	}
}