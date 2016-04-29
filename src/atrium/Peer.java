package atrium;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.DatatypeConverter;

import com.esotericsoftware.kryonet.Connection;

import crypto.AES;
import crypto.RSA;
import net.poll.CachePoller;
import packets.data.Data;
import packets.data.DataTypes;
import packets.requests.Request;
import packets.requests.RequestTypes;

/**
 * Representation of a incoming or outgoing peer. Handles data storage and abstract data exchange for P2P communication.
 * 
 * @author Kevin Cai
 */
public class Peer {

	private static Thread cacheThread;
	private CountDownLatch deferredRequesting;
	private CountDownLatch pubkeyDone;
	private CountDownLatch cryptoDone;
	private Connection connection;
	private boolean extVisible;
	private PublicKey pubkey;
	private AES aes;
	private String mutex;
	private String hostPort;
	private int inOut;

	/**
	 * Peer constructor for a Connection and inOut flag
	 * 
	 * @param connection
	 *            the connection this peer is identified for
	 * @param inOut
	 *            the status of incoming/outgoing; 0 = out, 1 = in
	 */
	public Peer(Connection connection, int inOut) {
		// Store instance vars
		this.connection = connection;
		this.inOut = inOut;

		// Add ourselves to peers with instance data
		Core.peers.add(this);

		// Set CountDownLatches
		deferredRequesting = new CountDownLatch(1);
		pubkeyDone = new CountDownLatch(1);
		cryptoDone = new CountDownLatch(1);

		// Begin to bootstrap data from this peer
		bootstrapRequests();
	}

	/**
	 * New thread to begin requesting data from this peer
	 */
	private void bootstrapRequests() {
		Thread bootstrapThread = (new Thread(new Runnable() {
			public void run() {
				try {
					if (inOut == 1) {
						// Pro-active request approach to this (in)peer
						Utilities.log(this, "Sending our pubkey first", true);
						connection.sendTCP(new Data(DataTypes.PUBKEY, RSA.pubKey));
						Utilities.log(this, "Requesting peer's pubkey", true);
						connection.sendTCP(new Request(RequestTypes.PUBKEY, null));
						Utilities.log(this, "Awaiting peer's pubkey", true);
						pubkeyDone.await();
						Utilities.log(this, "Requesting peer's mutex", true);
						connection.sendTCP(new Request(RequestTypes.MUTEX, null));
						cryptoDone.await();
						aes = new AES(mutex);
						Utilities.log(this, "Requesting peer's peerlist", true);
						connection.sendTCP(new Request(RequestTypes.PEERLIST, null));
						Utilities.log(this, "Requesting extVisible data", true);
						connection.sendTCP(new Request(RequestTypes.EXTVIS, null));
						Utilities.log(this, "Requesting peer's hostPort", true);
						connection.sendTCP(new Request(RequestTypes.HOSTPORT, null));
					} else {
						// When we've received both a pubkey and sent our
						// peerlist out,
						// Start sending requests to this (out)peer
						pubkeyDone.await();
						deferredRequesting.await();
						Utilities.log(this, "Requesting peer's mutex", true);
						connection.sendTCP(new Request(RequestTypes.MUTEX, null));
						cryptoDone.await();
						aes = new AES(mutex);
						Utilities.log(this, "Requesting peer's peerlist", true);
						connection.sendTCP(new Request(RequestTypes.PEERLIST, null));
						Utilities.log(this, "Requesting extVisible data", true);
						connection.sendTCP(new Request(RequestTypes.EXTVIS, null));
						Utilities.log(this, "Requesting peer's hostPort", true);
						connection.sendTCP(new Request(RequestTypes.HOSTPORT, null));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}));
		bootstrapThread.setName("Peer " + connection.getID() + " Bootstrap");
		bootstrapThread.start();

		// If we are a cacher, send cache requests every 90s
		if (Core.config.cacheEnabled && cacheThread == null) {
			Utilities.log(this, "Starting polling thread for peer", true);
			cacheThread = new Thread(new CachePoller());
			cacheThread.setName("Cache Poller");
			cacheThread.start();
		}
	}

	/**
	 * Log a disconnect for this peer, with some debug data
	 */
	public void disconnect() {
		Core.peers.remove(this);
		int connNumber = connection.getID();
		connection.close();
		if (mutex != null) {
			Utilities.log(this, "Peer " + mutex + " disconnected", false);
		} else {
			Utilities.log(this, "Peer disconnected (mutex null, #" + connNumber + ")", false);
		}
	}

	/**
	 * Returns this peer's connection object
	 * 
	 * @return this peer's connection object
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Returns this peer's public key as a PublicKey
	 * 
	 * @return PublicKey object for this peer
	 */
	public PublicKey getPubkey() {
		return pubkey;
	}

	/**
	 * Returns this peer's mutex as a String
	 * 
	 * @return string mutex of this peer
	 */
	public String getMutex() {
		return mutex.substring(0, 5);
	}

	/**
	 * Returns whether this peer is incoming or outgoing
	 * 
	 * @return integer inOut instance variable
	 */
	public int getInOut() {
		return inOut;
	}

	/**
	 * Returns this peer's cryptographic AES object
	 * 
	 * @return AES object of this peer
	 */
	public AES getAES() {
		return aes;
	}

	/**
	 * Returns external visibility of this peer
	 * 
	 * @return boolean external visibility of this peer
	 */
	public boolean getExtVis() {
		return extVisible;
	}

	/**
	 * Sets this peer's external visibility
	 * 
	 * @param in
	 *            value provided
	 */
	public void setExtVis(boolean in) {
		extVisible = in;
	}

	/**
	 * Returns this peer's deferredLatch CountDown object
	 * 
	 * @return CountDown deferredLatch object of this peer
	 */
	public CountDownLatch getDeferredLatch() {
		return deferredRequesting;
	}

	/**
	 * Returns this peer's pubkeyLatch CountDown object
	 * 
	 * @return CountDown pubkeyLatch object of this peer
	 */
	public CountDownLatch getPubkeyLatch() {
		return pubkeyDone;
	}

	/**
	 * Returns this peer's cryptoLatch CountDown object
	 * 
	 * @return CountDown cryptoLatch object of this peer
	 */
	public CountDownLatch getCryptoLatch() {
		return cryptoDone;
	}

	/**
	 * Sets this peer's pubkey object
	 * 
	 * @param pubkey
	 *            value provided
	 * @return success value
	 */
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

	/**
	 * Sets this peer's mutex
	 * 
	 * @param mutex
	 *            value provided
	 */
	public void setMutex(String mutex) {
		this.mutex = mutex;
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPortData) {
		hostPort = hostPortData;
	}

	/**
	 * Checks if a peer's mutex should warrant a disconnect
	 * 
	 * @param mutexData
	 *            peer mutex
	 * @return success value
	 */
	public boolean mutexCheck(String mutexData) {
		if (mutexData.equals(Core.mutex)) {
			Utilities.log(this, "Duplicate mutex against core: " + mutexData, false);
			disconnect();
			return false;
		} else {
			boolean passed = true;
			for (Peer peer : Core.peers) {
				if (peer != this && peer.getMutex() != null && peer.getMutex().equals(mutexData)) {
					Utilities.log(this, "Duplicate mutex: " + mutexData, false);
					passed = false;
					break;
				}
			}
			if (passed) {
				setMutex(mutexData);
				return true;
			} else {
				disconnect();
				return false;
			}
		}
	}

	/**
	 * Returns a peer, if there is one, that has this connection
	 * 
	 * @param connection
	 *            value provided
	 * @return peer value
	 */
	public static Peer findPeer(Connection connection) {
		for (Peer peer : Core.peers) {
			if (peer.getConnection().equals(connection)) {
				return peer;
			}
		}
		return null;
	}
}