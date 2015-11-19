package atrium;

import java.util.concurrent.CountDownLatch;

import com.esotericsoftware.kryonet.Connection;

public class Peer {
	
	private CountDownLatch cryptoDone;
	private Connection connection;
	private String mutex;
	private int inOut;  //= 1 for incoming
	
	public Peer(Connection connection, int inOut) {
		cryptoDone = new CountDownLatch(1);
		this.connection = connection;
		this.inOut = inOut;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public String getMutex() {
		return mutex;
	}
	
	public int getInOut() {
		return inOut;
	}
	
	public CountDownLatch getCryptoLatch() {
		return cryptoDone;
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
}
