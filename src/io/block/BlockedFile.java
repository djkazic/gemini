package io.block;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import atrium.Core;
import atrium.Utilities;
import io.FileUtils;
import io.serialize.SerialBlockedFile;
import io.serialize.StreamedBlockedFile;

public class BlockedFile {
	
	private static final File blockDexCache = new File(FileUtils.getConfigDir() + "/eblockdex.dat");
	private static final File protoBlockDexCache = new File(FileUtils.getConfigDir() + "/blockdex.dat");
	private static Kryo kryo = new Kryo();
	
	private File pointer;
	private String checksum;
	private ArrayList<String> blockList;
	private ArrayList<String> blackList;
	private boolean complete;
	private String progress;
	private float blockRate;
	private long lastChecked;
	private boolean cacheStatus;
	private long length;
	private String signature = "";
	
	/**
	 * Constructor for brand new BlockedFiles in the work directory or empty pointers
	 * @param pointer
	 */
	public BlockedFile(File pointer, boolean finished) {
		this.pointer = pointer;
		if(finished) {
			checksum = FileUtils.generateChecksum(pointer);
			blockList = FileUtils.enumerateBlocks(this, Core.config.hubMode);
			blackList = blockList;
		} else {
			blockList = new ArrayList<String> ();
			blackList = new ArrayList<String> ();
		}
		this.complete = finished;
		progress = "";
		if(!FileUtils.haveInBlockDex(pointer)) {
			Core.blockDex.add(this);
		}
		BlockedFile.serializeAll();
	}
	
	/**
	 * Constructor for StreamedBlockedFile conversion back
	 * @param pointer string pointer for File
	 * @param blockList ArrayList<String> of block names
	 */
	public BlockedFile(String pointer, String checksum, ArrayList<String> blockList, String signature, boolean searchConstructed) {
		this.pointer = new File(FileUtils.getWorkspaceDir() + "/" + pointer);
		this.checksum = checksum;
		this.blockList = blockList;
		blackList = new ArrayList<String> ();
		complete = false;
		progress = "";
		if(searchConstructed) {
			if(!FileUtils.haveInBlockDex(this.pointer)) {
				Core.blockDex.add(this);
			}
			BlockedFile.serializeAll();
		}
	}

	/**
	 * Kryo conversion constructor
	 * @param file direct file pointer
	 * @param checksum string checksum
	 * @param blockList ArrayList<String> of blocks
	 * @param blackList ArrayList<String> of blocks already had
	 * @param complete boolean value of completion
	 * @param progress string representation of progress downloaded
	 * @param blockRate rate of block downloads
	 * @param lastChecked last millisecond value checked for blockRate
	 * @param cache 
	 */
	public BlockedFile(File file, String checksum, ArrayList<String> blockList, ArrayList<String> blackList,
					   boolean complete, String progress, float blockRate, long lastChecked, boolean cache, long length, String signature) {
		this.pointer = file;
		this.checksum = checksum;
		this.blockList = blockList;
		this.blackList = blackList;
		this.complete = complete;
		this.progress = progress;
		this.blockRate = blockRate;
		this.lastChecked = lastChecked;
		this.cacheStatus = cache;
		this.signature = signature;
		if(!FileUtils.haveInBlockDex(pointer)) {
			Core.blockDex.add(this);
		}
		BlockedFile.serializeAll();
	}

	/**
	 * Returns whether this BlockedFile's name matches a query
	 * @param searchQuery string query for search
	 * @return boolean on whether there is a match
	 */
	public boolean matchSearch(String searchQuery) {
		if(pointer.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
			return true;
		}
		return false;
	}

	/**
	 * Returns file pointer
	 * @return file pointer
	 */
	public File getPointer() {
		return pointer;
	}

	/**
	 * Returns path
	 * @return string path
	 */
	public String getPath() {
		return FileUtils.getWorkspaceDir() + "/" + pointer.getName();
	}

	/**
	 * Returns AppData/cache directory for this BlockedFile
	 * @return string AppData/cache directory for this BlockedFile
	 */
	public String getBlocksFolder() {
		return FileUtils.getAppDataDir() + "/" + checksum;
	}

	/**
	 * Returns pre-calculated checksum
	 * @return string pre-calculated checksum
	 */
	public String getChecksum() {
		return checksum;
	}
	
	public void setChecksum(String in) {
		checksum = in;
	}

	/**
	 * Returns ArrayList<String> list of blocks
	 * @return ArrayList<String> list of blocks
	 */
	public ArrayList<String> getBlockList() {
		return blockList;
	}

	/**
	 * Returns ArrayList<String> list of blocks already had
	 * @return ArrayList<String> list of blocks already had
	 */
	public ArrayList<String> getBlacklist() {
		return blackList;
	}
	
	/**
	 * Sets the instance variable for the blockList
	 * @param in replacement variable provided
	 */
	public void setBlockList(ArrayList<String> in) {
		blockList = in;
	}
	
	/**
	 * Sets the instance variable for the blacklist
	 * @param in replacement variable provided
	 */
	public void setBlackList(ArrayList<String> in) {
		blackList = in;
	}
	
	/**
	 * Returns a randomly selected needed block
	 * @return randomly selected needed block
	 */
	public String nextBlockNeeded() {
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
			int ind = new SecureRandom().nextInt(qualified.size());
			return qualified.get(ind);
		}
	}
	
	/**
	 * Logs a block downloaded into the blackList ArrayList
	 * @param str block name
	 */
	public void logBlock(String str) {
		if(!blackList.contains(str)) {
			blackList.add(str);
		}
		if(!Core.config.hubMode) {
			if(blackList.size() % 8 == 0) {
				if(lastChecked == 0) {
					lastChecked = System.currentTimeMillis();
				}
				blockRate = (8 / ((System.currentTimeMillis() - lastChecked) / 1000f));
				int blocksLeft = blockList.size() - blackList.size();
				float res = (blocksLeft / blockRate);
				float finalRes = 0f + res;
				String units = " sec";
				if(res > 60 && res < 3600) {
					finalRes = res / 60f;
					units = " min";
				} else if(res >= 3600 && res < 86400) {
					finalRes = res / 3600f;
					units = " hr";
				} else if(res >= 86400) {
					finalRes = res / 84600f;
					units = " days";
				}
				int ires = (int) finalRes;
				updateTime(ires + units);
				lastChecked = System.currentTimeMillis();
			}
		}
		updateProgress();
	}

	/**
	 * Updates the estimated time remaining
	 * @param time value provided
	 */
	private void updateTime(String time) {
		//TODO: GUI replace Core.mainWindow.updateTime(checksum, time);
	}
	
	/**
	 * Updates the progress value via calculating sizes of blackList/blockList
	 */
	public void updateProgress() {	
		float dProgress = ((float) blackList.size()) / blockList.size();
		dProgress *= 100;
		progress = Math.round(dProgress) + "%";
		if(progress.equals("100%")) {
			if(!Core.config.hubMode) {
				updateTime("Done");
			}
		}

		if(!Core.config.hubMode) {
			//TODO: GUI call Core.mainWindow.updateProgress(checksum, progress);
		}
	}
	
	/**
	 * Returns boolean value for completeness
	 * @return boolean value for completeness
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Sets boolean value for completeness
	 * @param bool value provided
	 */
	public void setComplete(boolean bool) {
		complete = bool;
	}

	/**
	 * Returns numerical representation of progress
	 * @return numerical representation of progress
	 */
	public float getProgressNum() {
		float dProgress = ((float) blackList.size()) / blockList.size();
		dProgress *= 100;
		return dProgress;
	}
	
	/**
	 * Returns string representation of progress
	 * @return string representation of progress
	 */
	public String getProgress() {
		return progress;
	}

	/**
	 * Sets string representation of progress
	 * @param str value provided
	 */
	public void setProgress(String str) {
		progress = str;
	}
	
	public boolean getCacheStatus() {
		return cacheStatus;
	}
	
	public void setCacheStatus(boolean in) {
		cacheStatus = in;
	}
	
	/**
	 * Calculates and returns date modified for this BlockedFile
	 * @return string date modified for this BlockedFile
	 */
	public String getDateModified() {
		if(complete) {
			Date date = new Date (pointer.lastModified());
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			return formatter.format(date);
		} 
		return null;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public void setSignature(String in) {
		signature = in;
	}

	/**
	 * Converts this BlockedFile to SerialBlockedFile for serialization
	 * @return SerialBlockedFile conversion
	 */
	public SerialBlockedFile toSerialBlockedFile() {
		return new SerialBlockedFile(pointer.getAbsolutePath(), checksum, blockList, blackList, 
								     complete, progress, blockRate, lastChecked, cacheStatus, length, signature);
	}
	
	/**
	 * Converts this BlockedFile to StreamedBlockedFile for network transmission
	 * @return StreamedBlockedFile conversion
	 */
	public StreamedBlockedFile toStreamedBlockedFile() {
		ArrayList<String> encryptedList = new ArrayList<String> ();
		for(int i=0; i < blockList.size(); i++) {
			encryptedList.add(Core.aes.encrypt(blockList.get(i)));
		}
		return new StreamedBlockedFile(Core.aes.encrypt(pointer.getName()), 
									   Core.aes.encrypt(checksum), 
									   encryptedList,
									   Core.aes.encrypt(signature));
	}
	
	/**
	 * Debug method for verifying equality of BlockedFiles
	 */
	public String toString() {
		return pointer.getName() + " | " + 
			   blockList + " " + blackList + " | " + complete + " | " + progress 
			   + " | " + blockRate + " | " + lastChecked;
	}
	
	public long length() {
		return length;
	}
	
	public void reset() {
		blackList.clear();
		progress = "";
		blockRate = 0;
		lastChecked = 0;
		complete = false;
	}

	/**
	 * Serializes all BlockedFiles in the blockDex to an encrypted file (blockdex.dat)
	 */
	public static void serializeAll() {
		try {
			if(blockDexCache.exists()) {
				blockDexCache.delete();
			}
			if(protoBlockDexCache.exists()) {
				protoBlockDexCache.delete();
			}
			if(Core.blockDex.size() > 0) {
				protoBlockDexCache.createNewFile();
				FileOutputStream fos = new FileOutputStream(protoBlockDexCache);
				Output out = new Output(fos);
				ArrayList<SerialBlockedFile> kbf = new ArrayList<SerialBlockedFile> ();
				for(int i=0; i < Core.blockDex.size(); i++) {
					kbf.add(Core.blockDex.get(i).toSerialBlockedFile());
				}
				kryo.writeObject(out, kbf);
				out.close();
				
				byte[] encFileBytes = Core.aes.encrypt(Files.readAllBytes(protoBlockDexCache.toPath()));
				FileOutputStream fosEnc = new FileOutputStream(blockDexCache);
				fosEnc.write(encFileBytes);
				fosEnc.close();
				
				protoBlockDexCache.delete();
			}
		} catch(Exception ex) { 
			Utilities.log("io.block.BlockedFile", "Attempted write to blockdex, lock detected", false); 
		}
	}
}