package io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import atrium.Core;
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
		Core.blockDex.add(this);
	}
	
	/**
	 * Constructor for not yet existent BlockedFile
	 * @param pointer
	 * @param blockList
	 */
	public BlockedFile(String pointer, ArrayList<String> blockList) {
		this.pointer = new File(pointer);
		checksum = FileUtils.generateChecksum(this.pointer);
		this.blockList = blockList;
		blackList = new ArrayList<String> ();
		finished = false;
		progress = "";
		Core.blockDex.add(this);
	}

	public boolean matchSearch(String searchQuery) {
		if(pointer.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
			return true;
		}
		return false;
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
	
	public String getDateModified() {
		if(finished) {
			Date date = new Date (pointer.lastModified());
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			return formatter.format(date);
		} 
		return null;
	}
}