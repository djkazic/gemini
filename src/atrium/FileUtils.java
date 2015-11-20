package atrium;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import io.BlockedFile;

public class FileUtils {

	public static void initDirs() {
		File findir = new File(getWorkspaceDir());
		if(!findir.exists()) {
			System.out.println("Could not find directory, creating");
			boolean attempt = false;
			try {
				findir.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if(attempt) {
				System.out.println("Successfully created directory");
			}
		}
		File configDir = new File(getWorkspaceDir() + "/" + ".config");
		if(!configDir.exists()) {
			System.out.println("Could not find config directory, creating");
			boolean attempt = false;
			try {
				configDir.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if(attempt) {
				System.out.println("Successfully created config directory");
			}
		}
		File appDataGen = new File(getAppDataDir());
		if(!appDataGen.exists()) {
			System.out.println("Could not find appData directory, creating");
			boolean attempt = false;
			try {
				appDataGen.mkdir();
				attempt = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(attempt) {
				System.out.println("Successfully created appData directory");
			}
		}
	}

	public static String getWorkspaceDir() {
		String directory;
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		directory = fw.getDefaultDirectory().toString();
		if(Utilities.isWindows()) {
			directory += "/XNet";
		} else { 
			directory += "/Documents/XNet";
		}
		return directory;
	}

	public static String getAppDataDir() {
		String scratchDirectory;
		if(Utilities.isWindows()) {
			scratchDirectory = System.getenv("AppData") + "/XNet";
		} else {
			scratchDirectory = getWorkspaceDir() + "/.cache";
		}
		return scratchDirectory;
	}

	public static File findBlock(String baseForFile, String block) {
		File directory = new File(FileUtils.getAppDataDir() + "/" + baseForFile);
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

	public void unifyBlocks(BlockedFile bf) throws Exception {
		int numberParts = bf.getBlockList().size();
		String outputPath = bf.getFolder();
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath));
		File[] blocks = new File(bf.getBlocksFolder()).listFiles();
		if(blocks.length != numberParts) {
			Utilities.log(this, "Number of blocks present (" + blocks.length + ") != number of parts (" + numberParts + ")");
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
			Utilities.log(this, "Unable to clear data for " + bf.getPointer().getName());
		}
		//Set complete flag
		bf.setFinished(true);
	}
}
