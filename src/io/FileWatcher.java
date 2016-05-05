package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import atrium.Core;
import atrium.Utilities;
import filter.FilterUtils;
import io.block.BlockedFile;

/**
 * Watches file in working directory, and adds/removes them as BlockedFiles live
 * 
 * @author Kevin Cai
 */
public class FileWatcher implements Runnable {

	private WatchKey watchKey;

	public FileWatcher() {
		Path regDir = Paths.get(FileUtils.getWorkspaceDir());
		try {
			WatchService fileWatcher = regDir.getFileSystem().newWatchService();
			watchKey = regDir.register(fileWatcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Utilities.log(this, "FileWatcher registered to normal directory", false);
	}

	/**
	 * Main FileWatcher loop; examines the workspace directory for changes
	 */
	public void run() {
		while (true) {
			List<WatchEvent<?>> events = watchKey.pollEvents();
			for (final WatchEvent<?> we : events) {
				if (we.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					try {
						(new Thread(new Runnable() {
							public void run() {
								try {
									createHook(we.context().toString(), null);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						})).start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (we.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					(new Thread(new Runnable() {
						public void run() {
							deleteHook(we.context().toString());
						}
					})).start();
				}
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			boolean valid = watchKey.reset();
			if (!valid) {
				Utilities.log(this, "Failure to reset FileWatcher key!", false);
				break;
			}
		}
	}

	private void createHook(String we, File preDef) throws InterruptedException, IOException {
		Utilities.log(this, "Creation detected in workspace: " + we, false);
		final String relevantFileName = we;
		File preFinal = null;
		if (preDef != null) {
			preFinal = preDef;
		} else {
			preFinal = new File(FileUtils.getWorkspaceDir() + "/" + relevantFileName);
		}
		final File bfs = preFinal;

		if (bfs.isDirectory()) {
			FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes atts) throws IOException {
					Utilities.log(this, "Visiting file " + file.getFileName(), false);
					try {
						createHook(file.toFile().getName(), file.toFile());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			};

			// Wait for folder lock
			long initialFileBytesCount = -1;
			while (initialFileBytesCount != fileBytesCount(bfs)) {
				long fileBytesCountNow = fileBytesCount(bfs);
				Utilities.log(this, initialFileBytesCount + " vs. " + fileBytesCountNow, true);
				initialFileBytesCount = fileBytesCountNow;
				Thread.sleep(5000);
				continue;
			}

			try {
				Files.walkFileTree(bfs.toPath(), fv);
			} catch (IOException e) {
				// Second to last exception
			}
		} else {
			if (FilterUtils.mandatoryFilter(relevantFileName)) {
				while (true) {
					try {
						// TODO: Extension and name filtering done here
						while (!bfs.renameTo(bfs)) {
							Thread.sleep(200);
							continue;
						}
						if (FileUtils.getBlockedFile(FileUtils.generateChecksum(bfs)) == null) {
							Utilities.log(this, "Created BlockedFile: " + relevantFileName, true);
							new BlockedFile(bfs, true);
						}

						break;
					} catch (Exception ex) {
						Utilities.log(this, "File lock not yet released", true);
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				// TODO: updateLibrary call
				/**
				 * if(!Core.config.hubMode) { Core.mainWindow.updateLibrary(); }
				 */
			} else {
				if (!bfs.getName().endsWith(".filepart")) {
					Utilities.log(this, "Rejected file by filter: [" + relevantFileName + "]", false);
					FileUtils.deleteRecursive(bfs);
					FileUtils.removeFileAndParentsIfEmpty(bfs.toPath());
				} else {
					Utilities.log(this, "Detected filepart upload: [" + relevantFileName + "]", false);
				}
			}
		}
	}

	private void deleteHook(String we) {
		final String relevantFileName = we;
		final File bfs = new File(FileUtils.getWorkspaceDir() + "/" + relevantFileName);

		if (bfs.isFile() || !bfs.exists()) {
			if (!Core.config.hubMode) {
				Utilities.log(this, "Deletion detected in workspace: " + we, false);
			}
			BlockedFile bf = null;
			for (BlockedFile ibf : Core.blockDex) {
				if (ibf.getPointer().getName().equals(we)) {
					bf = ibf;
				}
			}
			if (!Core.config.hubMode && bf != null) {
				Utilities.log(this, "Reset: " + bf.getPointer().getName(), false);
				bf.reset();
				BlockedFile.serializeAll();
				// TODO: deleteHook
				/**
				 * if(!Core.config.hubMode) { Core.mainWindow.removeDownload(bf); Core.mainWindow.updateLibrary(); }
				 */
			}
		} else {
			for (BlockedFile bf : Core.blockDex) {
				if (Core.config.hubMode) {
					if (!(new File(bf.getBlocksFolder())).exists()) {
						Utilities.log(this, "Reset: " + bf.getPointer().getName(), false);
						bf.reset();
					}
				} else {
					if (!bf.getPointer().exists()) {
						Utilities.log(this, "Reset: " + bf.getPointer().getName(), false);
						bf.reset();
					}
				}
				BlockedFile.serializeAll();
				// TODO: deleteHook
				/**
				 * if(!Core.config.hubMode) { Core.mainWindow.removeDownload(bf); Core.mainWindow.updateLibrary(); }
				 */
			}
		}
	}

	private int fileBytesCount(File start) {
		int output = 0;
		if (start.isDirectory()) {
			File[] files = start.listFiles();
			if (files != null) {
				for (File file : files) {
					output += fileBytesCount(file);
				}
			}
		} else {
			output = (int) start.length();
		}
		return output;
	}
}