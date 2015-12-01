package io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import atrium.Core;
import atrium.Utilities;
import io.serialize.BlockdexSerializer;
import io.serialize.KryoBlockedFile;
import io.serialize.StreamedBlockedFile;

public class BlockedFile {
	
	public static void main(String[] args) {
		Core.blockDex = new ArrayList<BlockedFile> ();
		BlockedFile bf = new BlockedFile(new File("libGLESv2.dll"), true);
		System.out.println(bf.getBlockList());
		System.out.println(FileUtils.findBlockFromComplete(bf, 12));
	}

	private File pointer;
	private String checksum;
	private ArrayList<String> blockList;
	private ArrayList<String> blackList;
	private boolean complete;
	private String progress;
	private float blockRate;
	private long lastChecked;
	
	/**
	 * Constructor for brand new BlockedFiles in the work directory or empty pointers
	 * @param pointer
	 */
	public BlockedFile(File pointer, boolean finished) {
		this.pointer = pointer;
		if(finished) {
			checksum = FileUtils.generateChecksum(pointer);
			blockList = FileUtils.enumerateBlocks(pointer);
			blackList = blockList;
		} else {
			blockList = new ArrayList<String> ();
			blackList = new ArrayList<String> ();
		}
		this.complete = finished;
		progress = "";
		Core.blockDex.add(this);
		BlockdexSerializer.run();
	}
	
	/**
	 * Constructor for not yet existent BlockedFile
	 * @param pointer
	 * @param blockList
	 */
	public BlockedFile(String pointer, ArrayList<String> blockList) {
		this.pointer = new File(FileUtils.getWorkspaceDir() + "/" + pointer);
		this.blockList = blockList;
		blackList = new ArrayList<String> ();
		complete = false;
		progress = "";
		Core.blockDex.add(this);
		BlockdexSerializer.run();
	}

	/**
	 * Kryo conversion constructor
	 * @param file
	 * @param checksum
	 * @param blockList
	 * @param blackList
	 * @param complete
	 * @param progress
	 * @param blockRate
	 * @param lastChecked
	 */
	public BlockedFile(File file, String checksum, ArrayList<String> blockList, ArrayList<String> blackList,
					   boolean complete, String progress, float blockRate, long lastChecked) {
		this.pointer = file;
		this.checksum = checksum;
		this.blockList = blockList;
		this.blackList = blackList;
		this.complete = complete;
		this.progress = progress;
		this.blockRate = blockRate;
		this.lastChecked = lastChecked;
		Core.blockDex.add(this);
		BlockdexSerializer.run();
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
		for(int i=0; i < blockList.size(); i++) {
			String thisBlock = blockList.get(i);
			if(!blackList.contains(thisBlock)) {
				return thisBlock;
			}
		}
		return null;
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
	
	public void setBlacklist(ArrayList<String> in) {
		blackList = in;
	}
	
	public void logBlock(String str) {
		if(!blackList.contains(str)) {
			blackList.add(str);
		}
		if(blackList.size() % 8 == 0) {
			if(lastChecked == 0) {
				lastChecked = System.currentTimeMillis();
			}
			blockRate = (8 / ((System.currentTimeMillis() - lastChecked) / 1000f));
			int blocksLeft = blockList.size() - blackList.size();
			float res = (blocksLeft / blockRate);
			String units = " sec";
			if(res > 60 && res < 360) {
				res /= 60;
				units = " min";
			}
			if(res > 60) {
				res /= 60;
				units = " hr";
			}
			int ires = (int) res;
			updateTime(ires + units);
			lastChecked = System.currentTimeMillis();
		}
		updateProgress();
	}

	private void updateTime(String time) {
		if(!Core.headless) {
			Core.mainWindow.updateTime(pointer.getName(), time);
		}
	}
	
	private void updateProgress() {
		if(complete) {
			progress = "100%";
			updateTime("0 sec");
		} else {
			double dProgress = ((double) blackList.size()) / blockList.size();
			dProgress *= 100;
			progress = Math.round(dProgress) + "%";
		}
		if(!Core.headless) {
			Core.mainWindow.updateProgress(pointer.getName(), progress);
		}
	}
	
	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean bool) {
		complete = bool;
	}

	public double getProgressNum() {
		double dProgress = ((double) blackList.size()) / blockList.size();
		dProgress *= 100;
		return dProgress;
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
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			return formatter.format(date);
		} 
		return null;
	}
	
	public StreamedBlockedFile toStreamedBlockedFile() {
		ArrayList<String> encryptedList = new ArrayList<String> ();
		for(int i=0; i < blockList.size(); i++) {
			encryptedList.add(Core.aes.encrypt(blockList.get(i)));
		}
		return new StreamedBlockedFile(Core.aes.encrypt(pointer.getName()), encryptedList);
	}

	public KryoBlockedFile toKryoBlockedFile() {
		return new KryoBlockedFile(pointer.getAbsolutePath(), checksum, blockList, blackList, 
								   complete, progress, blockRate, lastChecked);
	}
	
	public String toString() {
		return pointer.getName() + " | " + 
			   blockList + " " + blackList + " | " + complete + " | " + progress 
			   + " | " + blockRate + " | " + lastChecked;
	}
}