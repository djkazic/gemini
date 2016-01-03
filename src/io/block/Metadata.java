package io.block;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import atrium.Core;
import crypto.AES;
import io.FileUtils;

public class Metadata {
	
	private String bfChecksum;
	private int ups;
	private int downs;
	private Map<String, Object[]> comments;
	private boolean voted;
	private String lastState;
	private long timestamp;

	public Metadata() {}
	
	public Metadata(String bfChecksum) {
		this.bfChecksum = bfChecksum;
		comments = new HashMap<String, Object[]> ();
		if(Core.metaDex.contains(this)) {
			Metadata md = Metadata.findMetaByChecksum(bfChecksum);
			if(md.getTime() < timestamp) {
				Core.metaDex.remove(md);
			}
		}
		Core.metaDex.add(this);
	}
	
	public boolean matchBf(String checksum) {
		return bfChecksum.equals(checksum);
	}
	
	public void addComment(String comment) {
		String signKey = Core.rsa.sign(comment);
		comments.put(comment, new Object[] {Core.rsa.rawPublicKey(), signKey});
		timestamp = System.currentTimeMillis();
	}
	
	public String getLastState() {
		return lastState;
	}
	
	public void setLastState(String state) {
		lastState = state;
	}
	
	public int getScore() {
		return ups - downs;
	}
	
	public String getChecksum() {
		return bfChecksum;
	}
	
	public long getTime() {
		return timestamp;
	}
	
	public ArrayList<String> getComments() {
		if(timestamp <= System.currentTimeMillis()) {
			ArrayList<String> approved = new ArrayList<String> ();
			for(Entry<String, Object[]> entry : comments.entrySet()) {
				String comment = (String) entry.getKey();
				Object[] pkAndSignature = (Object[]) entry.getValue();
				PublicKey pubKey = (PublicKey) pkAndSignature[0];
				String signature = (String) pkAndSignature[1];
				if(Core.rsa.verify(comment, pubKey, signature)) {
					approved.add(comment);
				}
			}
			return approved;
		}
		return null;
	}
	
	public void vote(int upDown) {
		if(!voted) {
			voted = true;
			if(upDown == 1) {
				ups++;
			} else if(upDown == -1) {
				downs++;
			}
		} else {
			if(upDown == 1) {
				if(downs > 0) {
					downs -= 2;
				} else {
					ups += 2;
				}
			} else if(upDown == -1) {
				if(ups > 0) {
					ups -= 2;
				} else {
					downs += 2;
				}
			}
		}
		timestamp = System.currentTimeMillis();
	}
	
	public boolean voted() {
		return voted;
	}
	
	public String toString() {
		return "Checksum: " + bfChecksum + " | Score: " + getScore() + "(" + ups + "." + downs + ")";
	}
	
	public void encrypt() {
		bfChecksum = Core.aes.encrypt(bfChecksum);
		Map<String, Object[]> finalized = new HashMap<String, Object[]> ();
		for(Entry<String, Object[]> entry : comments.entrySet()) {
			String comment = (String) entry.getKey();
			Object[] pkAndSignature = (Object[]) entry.getValue();
			PublicKey pubKey = (PublicKey) pkAndSignature[0];
			String signature = (String) pkAndSignature[1];
			finalized.put(Core.aes.encrypt(comment), new Object[] {pubKey, Core.aes.encrypt(signature)});
		}
		comments = finalized;
	}
	
	public void decrypt(AES aes) {
		bfChecksum = aes.decrypt(bfChecksum);
		Map<String, Object[]> finalized = new HashMap<String, Object[]> ();
		for(Entry<String, Object[]> entry : comments.entrySet()) {
			String comment = (String) entry.getKey();
			Object[] pkAndSignature = (Object[]) entry.getValue();
			PublicKey pubKey = (PublicKey) pkAndSignature[0];
			String signature = (String) pkAndSignature[1];
			finalized.put(aes.decrypt(comment), new Object[] {pubKey, aes.decrypt(signature)});
		}
		comments = finalized;
	}
	
	public static Metadata findMetaByChecksum(String checksum) {
		Metadata streamMeta = null;
		for(Metadata meta : Core.metaDex) {
			if(meta.matchBf(checksum)) {
				streamMeta = meta;
				break;
			}
		}
		return streamMeta;
	}
	
	public static void serializeAll() {
		try {
			File metaDexSerialized = new File(FileUtils.getConfigDir() + "/metadex.dat");
			if(metaDexSerialized.exists()) {
				metaDexSerialized.delete();
			}
			
			if(Core.metaDex.size() > 0) {
				metaDexSerialized.createNewFile();
				Kryo kryo = new Kryo();
				FileOutputStream fos = new FileOutputStream(metaDexSerialized);
				Output out = new Output(fos);
				kryo.writeObject(out, Core.metaDex);
				out.close();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
