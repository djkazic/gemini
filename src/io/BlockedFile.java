package io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import atrium.Core;
import atrium.FileUtils;
import atrium.Utilities;
import crypto.AES;

public class BlockedFile {

	private File pointer;
	private String checksum;
	private ArrayList<String> blockList;
	private ArrayList<String> blackList;
	private boolean complete;
	private String progress;

	/**
	 * Constructor for brand new BlockedFiles in the work directory or empty pointers
	 * @param pointer
	 */
	public BlockedFile(File pointer, boolean finished) {
		this.pointer = pointer;
		if(finished) {
			checksum = FileUtils.generateChecksum(pointer);
			blockList = FileUtils.enumerateBlocks(pointer);
		} else {
			blockList = new ArrayList<String> ();
		}
		blackList = new ArrayList<String> ();
		this.complete = finished;
		progress = "";
		Core.blockDex.add(this);
	}
	
	/**
	 * Constructor for not yet existent BlockedFile
	 * @param pointer
	 * @param blockList
	 */
	public BlockedFile(String pointer, ArrayList<String> blockList) {
		this.pointer = new File(FileUtils.getWorkspaceDir() + "/" + pointer);
		checksum = FileUtils.generateChecksum(this.pointer);
		this.blockList = blockList;
		blackList = new ArrayList<String> ();
		complete = false;
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

	public String getPath() {
		return FileUtils.getWorkspaceDir() + "/" + pointer.getName();
	}

	public String getBlocksFolder() {
		return FileUtils.getAppDataDir() + "/" + Utilities.base64(pointer.getName());
	}
	
	public String getNextBlock() {
		ArrayList<String> qualified = new ArrayList<String> ();
		for(int i=0; i < blockList.size(); i++) {
			String thisBlock = blockList.get(i);
			if(!blackList.contains(thisBlock)) {
				qualified.add(thisBlock);
			}
		}
		if(qualified.size() == 0) {
			return null;
		} else {
			return qualified.get((int) (Math.random() * qualified.size()));
		}
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

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean bool) {
		complete = bool;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String str) {
		progress = str;
	}
	
	public String getDateModified() {
		if(complete) {
			Date date = new Date (pointer.lastModified());
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			return formatter.format(date);
		} 
		return null;
	}
	
	public StreamedBlockedFile toStreamedBlockedFile() {
		ArrayList<String> encryptedList = new ArrayList<String> ();
		for(int i=0; i < blockList.size(); i++) {
			encryptedList.set(i, Core.aes.encrypt(blockList.get(i)));
		}
		return new StreamedBlockedFile(Core.aes.encrypt(pointer.getName()), encryptedList);
	}
}