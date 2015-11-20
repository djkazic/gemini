package atrium;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import com.esotericsoftware.kryonet.Connection;
import crypto.AES;
import data.Data;
import data.DataTypes;
import requests.Request;
import requests.RequestTypes;

public class Peer {

	private CountDownLatch deferredRequesting;
	private CountDownLatch cryptoDone;
	private Connection connection;
	private PublicKey pubkey;
	private AES aes;
	private String mutex;
	private int inOut;  //= 1 for incoming

	public Peer(Connection connection, int inOut) {
		deferredRequesting = new CountDownLatch(1);
		cryptoDone = new CountDownLatch(1);
		this.connection = connection;
		this.inOut = inOut;

		//Add ourselves to peers without data
		NetHandler.peers.add(this);
		
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
						cryptoDone.await();
						aes = new AES(mutex);
						Utilities.log(this, "Requesting peer's mutex");
						connection.sendTCP(new Request(RequestTypes.MUTEX, null));
						Utilities.log(this, "Requesting peer's peerlist");
						connection.sendTCP(new Request(RequestTypes.PEERLIST, null));
					} else {
						//When we send back a peerList, it's time to start sending requests
						//Keeping cryptoDone as a sequential check
						cryptoDone.await();
						aes = new AES(mutex);
						deferredRequesting.await();
						Utilities.log(this, "Requesting peer's mutex");
						connection.sendTCP(new Request(RequestTypes.MUTEX, null));
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
	
	public CountDownLatch getCryptoLatch() {
		return cryptoDone;
	}

	public void setPubkey(String pubkey) {
		try {
			byte[] pubKeyBytes = pubkey.getBytes();
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyBytes));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(keySpec);
			this.pubkey = pk;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
	
	public void mutexCheck(String mutexData) {
		if(mutexData.equals(Core.mutex)) {
			disconnect();
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
			} else {
				disconnect();
			}	
		}
	}
}