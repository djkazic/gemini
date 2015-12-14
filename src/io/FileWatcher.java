package io;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import atrium.Core;
import atrium.Utilities;
import filter.FilterUtils;
import io.serialize.BlockdexSerializer;

/**
 * Watches file in working directory, and adds/removes them as BlockedFiles live
 * @author Kevin Cai
 */
public class FileWatcher implements Runnable {
	
	private WatchKey watchKey;
	
	public FileWatcher() {
		Path regDir = Paths.get(FileUtils.getWorkspaceDir());
		try {
			WatchService fileWatcher = regDir.getFileSystem().newWatchService();
			watchKey = regDir.register(fileWatcher, 
									   StandardWatchEventKinds.ENTRY_CREATE, 
									   StandardWatchEventKinds.ENTRY_DELETE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Utilities.log(this, "FileWatcher registered to normal directory", false);
	}
	
	/**
	 * Main FileWatcher loop; examines the workspace directory for changes
	 */
	public void run() {
		while(true) {
			List<WatchEvent<?>> events = watchKey.pollEvents();
			for(final WatchEvent<?> we : events) {
				if(we.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					Utilities.log(this, "Creation detected in workspace: " + we.context().toString(), false);
					final String relevantFileName = we.context().toString();
					if(FilterUtils.mandatoryFilter(relevantFileName)) {
						(new Thread(new Runnable() {
							public void run() {
								while(true) {
									try {
										//TODO: Extension and name filtering done here
										File bfs = new File(FileUtils.getWorkspaceDir() + "/" + relevantFileName);
										while(!bfs.renameTo(bfs)) {
											Thread.sleep(200);
											continue;
										}
										if(FileUtils.getBlockedFile(FileUtils.generateChecksum(bfs)) == null) {
											Utilities.log(this, "Created BlockedFile: " + relevantFileName, true);
											new BlockedFile(bfs, true);
										}
										
										break;
									} catch (Exception ex) { 
										Utilities.log(this, "File lock not yet released", true);
									}
									try {
										Thread.sleep(300);
									} catch(InterruptedException ex) {
										ex.printStackTrace();
									}	
								}
								if(!Core.config.hubMode) {
									Core.mainWindow.updateLibrary();
								}
							}
						})).start();
					} else {
						Utilities.log(this, "Rejected file by filter: [" + relevantFileName + "]", false);
					}
				}
				if(we.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					if(!Core.config.hubMode) {
						Utilities.log(this, "Deletion detected in workspace: " + we.context().toString(), false);
					}
					BlockedFile bf = null;
					for(BlockedFile ibf : Core.blockDex) {
						if(ibf.getPointer().getName().equals(we.context().toString())) {
							bf = ibf;
						}
					}
					if(!Core.config.hubMode && bf != null) {
						Utilities.log(this, "Reset: " + bf.getPointer().getName(), true);
						bf.reset();
						BlockdexSerializer.run();
						if(!Core.config.hubMode) {
							Core.mainWindow.removeDownload(bf);
							Core.mainWindow.updateLibrary();
						}
					}
				}
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
}