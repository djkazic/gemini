package io;

import java.io.File;
import java.util.ArrayList;

public class KryoBlockedFile {
	
	private String pointerPath;
	private String checksum;
	private ArrayList<String> blockList;
	private ArrayList<String> blackList;
	private boolean complete;
	private String progress;
	private float blockRate;
	private long lastChecked;
	
	public KryoBlockedFile() {
		pointerPath = "";
		checksum = "";
		blockList = null;
		blackList = null;
		complete = false;
		progress = "";
		blockRate = 0;
		lastChecked = 0;
	}
	
	public KryoBlockedFile(String absolutePath, String checksum, ArrayList<String> blockList,
			               ArrayList<String> blacklist, boolean complete, String progress, 
			               float blockRate, long lastChecked) {
		this.pointerPath = absolutePath;
		this.checksum = checksum;
		this.blockList = blockList;
		this.blackList = blacklist;
		this.complete = complete;
		this.progress = progress;
		this.blockRate = blockRate;
		this.lastChecked = lastChecked;
	}

	public BlockedFile toBlockedFile() {
		return new BlockedFile(new File(pointerPath), 
							   checksum, 
							   blockList, 
							   blackList, 
							   complete, 
							   progress, 
							   blockRate, 
							   lastChecked);
	}
}
