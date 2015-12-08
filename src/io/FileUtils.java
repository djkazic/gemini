package io;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import atrium.Core;
import atrium.Utilities;
import io.serialize.BlockdexSerializer;
import io.serialize.SerialBlockedFile;

public class FileUtils {

	public static void initDirs() {
		Utilities.log("atrium.FileUtils", "Initializing file worker");
		File findir = new File(getWorkspaceDir());
		if(!findir.exists()) {
			Utilities.log("atrium.FileUtils", "Could not find directory, creating");
			boolean attempt = false;
			try {
				findir.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if(attempt) {
				Utilities.log("atrium.FileUtils", "Successfully created directory");
			}
		}
		File configDir = new File(getConfigDir());
		if(!configDir.exists()) {
			Utilities.log("atrium.FileUtils", "Could not find config directory, creating");
			boolean attempt = false;
			try {
				configDir.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if(attempt) {
				Utilities.log("atrium.FileUtils", "Successfully created config directory");
			}
		}
		File appDataGen = new File(getAppDataDir());
		if(!appDataGen.exists()) {
			Utilities.log("atrium.FileUtils", "Could not find appData directory, creating");
			boolean attempt = false;
			try {
				appDataGen.mkdir();
				attempt = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(attempt) {
				Utilities.log("atrium.FileUtils", "Successfully created appData directory");
			}
		}
	}

	public static String getWorkspaceDir() {
		String directory;
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		directory = fw.getDefaultDirectory().toString();
		if(Utilities.isMac()) { 
			directory += "/Documents/Radiator";
		} else {
			directory += "/Radiator";
		}
		return directory;
	}

	public static String getAppDataDir() {
		String scratchDirectory;
		if(Utilities.isWindows()) {
			scratchDirectory = System.getenv("AppData") + "/Radiator";
		} else {
			scratchDirectory = getWorkspaceDir() + "/.cache";
		}
		return scratchDirectory;
	}
	
	public static String getConfigDir() {
		return getWorkspaceDir() + "/.config";
	}

	public static File findBlockAppData(String origin, String block) {
		String base64Name = Utilities.base64(origin);
		File directory = new File(FileUtils.getAppDataDir() + "/" + base64Name);
		if(!directory.exists()) {
			return null;
		}
		File[] listOfFiles = directory.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			try {
				if(generateChecksum(listOfFiles[i]).equals(block)) {
					return listOfFiles[i];
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static byte[] findBlockFromComplete(BlockedFile bf, int blockIndex) {
		try {
			InputStream fis = new FileInputStream(bf.getPointer());
			byte[] buffer = new byte[Core.blockSize];

			int blockNum = -1;
			int numRead;
			do {
				numRead = fis.read(buffer);
				if(numRead > 0) {
					blockNum++;
				} else {
					break;
				}
				if(blockNum == blockIndex) {
					byte[] res = new byte[numRead];
					for(int i=0; i < res.length; i++) {
						res[i] = buffer[i];
					}
					fis.close();
					return res;
				}
			} while(numRead != -1);
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void genBlockIndex() {
		File encCacheFile = new File(getConfigDir() + "/eblockdex.dat");
		File cacheFile = new File(getConfigDir() + "/blockdex.dat");
		if(encCacheFile.exists()) {
			Utilities.log("atrium.FileUtils", "Attempting to read blockdex cache");
			try {
				byte[] encFileBytes = Files.readAllBytes(encCacheFile.toPath());
				byte[] decryptedBytes = Core.aes.decrypt(encFileBytes);
				FileOutputStream fos = new FileOutputStream(cacheFile);
				fos.write(decryptedBytes);
				fos.close();
				encCacheFile.delete();
				Kryo kryo = new Kryo();
				Input input = new Input(new FileInputStream(cacheFile));

				try {
					ArrayList<?> uKbf = kryo.readObject(input, ArrayList.class);
					Utilities.log("atrium.FileUtils", "Read " + uKbf.size() + " entries from cache");
					for(int i=0; i < uKbf.size(); i++) {
						((SerialBlockedFile) uKbf.get(i)).toBlockedFile();
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				input.close();
				cacheFile.delete();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		File baseFolder = new File(getWorkspaceDir());
		int physicalBfCount = 0;
		File[] list = baseFolder.listFiles();
		for(int i=0; i < list.length; i++) {
			if(list[i].isFile() && !list[i].getName().startsWith(".")) {
				physicalBfCount++;
			}
		}
		while(Core.blockDex.size() != physicalBfCount && Core.blockDex.size() < physicalBfCount) {
			Utilities.log("atrium.FileUtils", "Validity check FAIL, cached " + Core.blockDex.size() 
					      + " but detected " + physicalBfCount);
			for(int i=0; i < list.length; i++) {
				if(list[i].isFile() && !list[i].getName().startsWith(".") && !haveInBlockDex(list[i])) {
					new BlockedFile(list[i], true);
				}
			}
			BlockdexSerializer.run();
		}
		
		while(Core.blockDex.size() != physicalBfCount && Core.blockDex.size() > physicalBfCount) {
			Utilities.log("atrium.FileUtils", "Validity check FAIL, cached " + Core.blockDex.size() 
		      			  + " but detected " + physicalBfCount);
			for(int i=0; i < Core.blockDex.size(); i++) {
				BlockedFile curBf = Core.blockDex.get(i);
				File curPointer = curBf.getPointer();
				boolean found = false;
				for(int j=0; j < list.length; j++) {
					if(curPointer.equals(list[j])) {
						found = true;
						break;
					}
				}
				if(!found) {
					Core.blockDex.remove(curBf);
				}
			}
			BlockdexSerializer.run();
		}
		String checkPassFail = (Core.blockDex.size() == physicalBfCount) ? "PASS" : "FAIL";
		Utilities.log("atrium.FileUtils", "Final validity check: " + checkPassFail + "; cached " + Core.blockDex.size() 
	                + " and detected " + physicalBfCount);
	}
	
	public static boolean haveInBlockDex(File file) {
		for(BlockedFile bf : Core.blockDex) {
			if(bf.getPointer().equals(file)) {
				return true;
			}
		}
		return false;
	}
	
	public static String generateChecksum(File file) {
		try {
			InputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[8096];
			MessageDigest complete = MessageDigest.getInstance("SHA1");
			int numRead;
			do {
				numRead = fis.read(buffer);
				if(numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while(numRead != -1);
			fis.close();
			String result = "";
			byte[] digest = complete.digest();
			for(int i=0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
			}
			return result;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String> enumerateBlocks(File file) {
		try {
			ArrayList<String> blockList = new ArrayList<String> ();
			InputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[Core.blockSize];

			MessageDigest complete = MessageDigest.getInstance("SHA1");
			int numRead;
			do {
				numRead = fis.read(buffer);
				if(numRead > 0) {
					complete.update(buffer, 0, numRead);
				} else {
					break;
				}
				byte[] digest = complete.digest();
				String result = "";
				for(int i=0; i < digest.length; i++) {
					result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
				}
				blockList.add(result);
			} while(numRead != -1);
			fis.close();
			return blockList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<String> enumerateIncompleteBlacklist(BlockedFile bf) {
		try {
			ArrayList<String> output = new ArrayList<String> ();
			File bfDir = new File(bf.getBlocksFolder());
			if(bfDir.exists()) {
				File[] files = bfDir.listFiles();
				for(File file : files) {
					if(FileUtils.generateChecksum(file).equals(file.getName())) {
						output.add(file.getName());
					}
				}
			}
			return output;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void unifyBlocks(BlockedFile bf) throws Exception {
		int numberParts = bf.getBlockList().size();
		String outputPath = bf.getPath();
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath));
		File[] blocks = new File(bf.getBlocksFolder()).listFiles();
		if(blocks.length != numberParts) {
			Utilities.log("atrium.FileUtils", "Number of blocks present (" + blocks.length + ") != number of parts (" + numberParts + ")");
			out.close();
			return;
		}
		for(String block : bf.getBlockList()) {
			File thisBlockFile = new File(bf.getBlocksFolder() + "/" + block);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(thisBlockFile));
			int pointer;
			while((pointer = in.read()) != -1) {
				out.write(pointer);
			}
			in.close();
		}
		out.close();
		//Clear haveList, so progressBar doesn't show 200%
		bf.getBlacklist().clear();
		//Reset progress
		bf.setProgress("0%");
		//Delete contents then the block directory
		File blocksDir = new File(bf.getBlocksFolder());
		File[] blocksDirBlocks = blocksDir.listFiles();
		for(File file : blocksDirBlocks) {
			file.delete();
		}
		blocksDir.delete();
		if(blocksDir.exists()) {
			Utilities.log("atrium.FileUtils", "Unable to clear data for " + bf.getPointer().getName());
		}
		//Set complete flag
		bf.setComplete(true);
	}
	
	public static void openBlockedFile(BlockedFile bf) {
		if(bf.isComplete()) {
			Desktop thisDesktop = Desktop.getDesktop();
			try {
				thisDesktop.open(bf.getPointer());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static BlockedFile getBlockedFile(String filename) {
		for(BlockedFile block : Core.blockDex) {
			if(block.getPointer().getName().equals(filename)) {
				return block;
			}
		}
		return null;
	}
	
	public static BlockedFile getBlockedFile(ArrayList<String> blockList) {
		for(BlockedFile block : Core.blockDex) {
			if(block.getBlockList().containsAll(blockList) && blockList.containsAll(block.getBlockList())) {
				return block;
			}
		}
		return null;
	}
	
	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		int i;
		byte[] b = new byte[1024];
		while((i=is.read(b))!=-1) {
			os.write(b, 0, i);
		}
	}
}
