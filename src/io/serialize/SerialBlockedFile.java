package io.serialize;

import java.io.File;
import java.util.ArrayList;

import io.block.BlockedFile;

public class SerialBlockedFile {
	
	private String pointerPath;
	private String checksum;
	private ArrayList<String> blockList;
	private ArrayList<String> blackList;
	private boolean complete;
	private String progress;
	private float blockRate;
	private long lastChecked;
	private boolean cache;
	private String signature;
	private String albumArt;
	
	public SerialBlockedFile() { }
	
	public SerialBlockedFile(String absolutePath, String checksum, ArrayList<String> blockList,
			               ArrayList<String> blacklist, boolean complete, String progress, 
			               float blockRate, long lastChecked, boolean cache, String signature, String albumArt) {
		this.pointerPath = absolutePath;
		this.checksum = checksum;
		this.blockList = blockList;
		this.blackList = blacklist;
		this.complete = complete;
		this.progress = progress;
		this.blockRate = blockRate;
		this.lastChecked = lastChecked;
		this.cache = cache;
		this.signature = signature;
		this.albumArt = albumArt;
	}

	public BlockedFile toBlockedFile() {
		return new BlockedFile(new File(pointerPath), 
							   checksum, 
							   blockList, 
							   blackList, 
							   complete, 
							   progress, 
							   blockRate, 
							   lastChecked,
							   cache,
							   signature,
							   albumArt);
	}
}
