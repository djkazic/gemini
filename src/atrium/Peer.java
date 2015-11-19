package atrium;

import java.util.concurrent.CountDownLatch;

import com.esotericsoftware.kryonet.Connection;

public class Peer {
	
	private CountDownLatch allFieldsDone;
	private Connection connection;
	private String mutex;
	private int inOut;  //= 1 for incoming
	
	public Peer(Connection connection, int inOut) {
		allFieldsDone = new CountDownLatch(1);
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
	
	public CountDownLatch getLatch() {
		return allFieldsDone;
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
