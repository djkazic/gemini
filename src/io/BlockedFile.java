package io;

import java.io.File;
import java.util.ArrayList;
import atrium.FileUtils;
import atrium.Utilities;

public class BlockedFile {
	
	private File pointer;
	private String checksum;
	private ArrayList<String> blockList;
	private ArrayList<String> blackList;
	private boolean finished;
	private String progress;
	
	/**
	 * Constructor for brand new BlockedFile (likely first time seeing in the directory)
	 * @param pointer
	 */
	public BlockedFile(File pointer) {
		this.pointer = pointer;
		checksum = FileUtils.generateChecksum(pointer);
		blockList = FileUtils.enumerateBlocks(pointer);
		blackList = new ArrayList<String> ();
		finished = false;
		progress = "";
	}
	
	public File getPointer() {
		return pointer;
	}
	
	public String getFolder() {
		return FileUtils.getWorkspaceDir() + "/" + pointer.getName();
	}
	
	public String getBlocksFolder() {
		return FileUtils.getAppDataDir() + "/" + Utilities.base64(pointer.getName());
	}
	
	public String getChecksum() {
		return checksum;
	}
	
	public ArrayList<String> getBlockList() {
		return blockList;
	}
	
	public ArrayList<String> getBlacklist() {
		return blackList;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public void setFinished(boolean bool) {
		finished = bool;
	}
	
	public String getProgress() {
		return progress;
	}
	
	public void setProgress(String str) {
		progress = str;
	}
}