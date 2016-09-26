package net.listeners;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import atrium.Core;
import atrium.NetHandler;
import atrium.Peer;
import atrium.Utilities;
import crypto.RSA;
import filter.FilterUtils;
import io.Downloader;
import io.FileUtils;
import io.block.BlockedFile;
import io.serialize.StreamedBlock;
import io.serialize.StreamedBlockedFile;
import packets.data.Data;
import packets.data.DataTypes;

public class DualListener extends Listener {

	private static ExecutorService replyPool;
	private int inOut;

	public DualListener(int inOut) {
		super();
		this.inOut = inOut;
		if (replyPool == null) {
			replyPool = Executors.newCachedThreadPool();
		}
	}

	// New connection, either incoming or outgoing
	public void connected(Connection connection) {
		if (inOut == 1) {
			Utilities.log(this, "New incoming peer: " + connection.getRemoteAddressTCP().getHostString(), false);
		} else {
			Utilities.log(this, "New outgoing peer: " + connection.getRemoteAddressTCP().getHostString(), false);
		}
		connection.setIdleThreshold(0.3f);
		try {
			if (inOut == 1) {
				new Peer(connection, inOut);
			} else {
				new Peer(connection, inOut);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void disconnected(Connection connection) {
		Peer foundPeer = Peer.findPeer(connection);
		if (foundPeer != null) {
			foundPeer.disconnect();
		}
	}

	// New incoming packet post-connection
	public void received(final Connection connection, Object object) {
		final Peer foundPeer = Peer.findPeer(connection);

		if (object instanceof Data) {
			final Data data = (Data) object;
			byte type = data.getType();

			switch (type) {
				// Requests below are non-encrypted
				case DataTypes.PUBKEY_REQS:
					Utilities.log(this, "Received request for pubkey", false);
					replyPool.execute(new Runnable() {
						public void run() {
							connection.sendTCP(new Data(DataTypes.PUBKEY_DATA, RSA.pubKey));
							Utilities.log(this, "\tSent pubkey back", false);
						}
					});
					break;

				case DataTypes.MUTEX_REQS:
					Utilities.log(this, "Received request for mutex", false);
					replyPool.execute(new Runnable() {
						public void run() {
							connection.sendTCP(new Data(DataTypes.MUTEX_DATA, Core.rsa.encrypt(Core.mutex, foundPeer.getPubkey())));
							Utilities.log(this, "\tSent mutex back", false);
						}
					});
					break;

				// Requests below are symmetrically encrypted
				case DataTypes.PEERLIST_REQS:
					Utilities.log(this, "Received request for peerlist", false);
					// TODO: more refined peerList filtering
					replyPool.execute(new Runnable() {
						public void run() {
							ArrayList<String> refinedPeerList = new ArrayList<String>();
							for (Peer peer : Core.peers) {
								if (peer != foundPeer && peer.getExtVis()) {
									String peerData = peer.getConnection().getRemoteAddressTCP().getHostString() + ":"
											+ peer.getHostPort();
									refinedPeerList.add(Core.aes.encrypt(peerData));
								}
							}
							connection.sendTCP(new Data(DataTypes.PEERLIST_DATA, refinedPeerList));
							Utilities.log(this, "\tSent peerlist back, len " + refinedPeerList.size(), false);
							if (foundPeer.getInOut() == 0) {
								foundPeer.getDeferredLatch().countDown();
							}
						}
					});
					break;

				case DataTypes.SEARCH_REQS:
					Utilities.log(this, "Received request for search", false);
					replyPool.execute(new Runnable() {
						public void run() {
							String encryptedQuery = (String) data.getPayload();
							String decrypted = foundPeer.getAES().decrypt(encryptedQuery);
							// For all known BlockedFiles, check if relevant
							ArrayList<StreamedBlockedFile> streams = new ArrayList<StreamedBlockedFile>();
							for (BlockedFile bf : Core.blockDex) {
								if (bf.matchSearch(decrypted)) {
									boolean add = false;
									if (!bf.isComplete() || Core.config.hubMode) {
										File blocksFolder = new File(bf.getBlocksFolder());
										if (blocksFolder != null) {
											File[] files = null;
											if ((files = blocksFolder.listFiles()) != null && files.length > 0) {
												add = true;
											}
										}
									} else if (bf.isComplete()) {
										add = true;
									}
									if (add) {
										String searchSploitFileName = bf.getPointer().getName();
										if (FilterUtils.mandatoryFilter(searchSploitFileName)) {
											streams.add(bf.toStreamedBlockedFile());
										} else if (searchSploitFileName.startsWith(".")) {
											Utilities.log(this,
													"Search result rejected by period filter: [" + searchSploitFileName + "]",
													false);
										} else {
											Utilities.log(this, "Search result rejected by filter: [" + searchSploitFileName + "]",
													false);
										}
									}
								}
							}
							connection.sendTCP(new Data(DataTypes.SEARCH_DATA, streams));
							Utilities.log(this, "\tSent search results back", false);
						}
					});
					// Search results are ArrayList<StreamedBlockedFile> which
					// have encrypted name + onboard encrypted blockList
					break;

				case DataTypes.EXTVIS_REQS:
					Utilities.log(this, "Received request for external visibility", false);
					replyPool.execute(new Runnable() {
						public void run() {
							connection.sendTCP(new Data(DataTypes.EXTVIS_DATA, Core.config.cacheEnabled));
							Utilities.log(this, "\tSent external visibility data back", false);
						}
					});
					break;

				case DataTypes.CACHE_REQS:
					Utilities.log(this, "Received request for cache feed", false);
					replyPool.execute(new Runnable() {
						public void run() {
							ArrayList<String> cacheStreams = new ArrayList<String>();
							for (BlockedFile bf : Core.blockDex) {
								boolean add = false;
								if (!bf.isComplete() || Core.config.hubMode) {
									File blocksFolder = new File(bf.getBlocksFolder());
									if (blocksFolder != null) {
										File[] files = null;
										if ((files = blocksFolder.listFiles()) != null && files.length > 0) {
											add = true;
										}
									}
								} else if (bf.isComplete()) {
									add = true;
								}
								if (add) {
									String searchSploitFileName = bf.getPointer().getName();
									if (FilterUtils.mandatoryFilter(searchSploitFileName)) {
										cacheStreams.add(Core.aes.encrypt(bf.getChecksum()));
									} else if (searchSploitFileName.startsWith(".")) {
										Utilities.log(this,
												"Search result rejected by period filter: [" + searchSploitFileName + "]", false);
									} else {
										Utilities.log(this, "Search result rejected by filter: [" + searchSploitFileName + "]",
												false);
									}
								}
							}
							connection.sendTCP(new Data(DataTypes.CACHE_DATA, cacheStreams));
							Utilities.log(this, "\tSent cache search results back", false);
						}
					});
					break;

				case DataTypes.CACHEPULL_REQS:
					Utilities.log(this, "Received request for cache pull", false);
					replyPool.execute(new Runnable() {
						public void run() {
							Object oCachePull = data.getPayload();
							ArrayList<String> cacheNeeded = new ArrayList<String>();
							if (oCachePull instanceof ArrayList<?>) {
								ArrayList<?> potentialCacheToSend = (ArrayList<?>) oCachePull;
								for (int i = 0; i < potentialCacheToSend.size(); i++) {
									Object o = potentialCacheToSend.get(i);
									if (o instanceof String) {
										cacheNeeded.add(foundPeer.getAES().decrypt((String) o));
									}
								}

								if (cacheNeeded.size() > 0) {
									ArrayList<StreamedBlockedFile> streamsToSend = new ArrayList<StreamedBlockedFile>();
									for (BlockedFile bf : Core.blockDex) {
										String checksum = bf.getChecksum();
										if (cacheNeeded.contains(checksum)) {
											streamsToSend.add(bf.toStreamedBlockedFile());
										}
									}
									Utilities.log(this,
											"Sending back data for cache pull, package size " + streamsToSend.size(),
											false);
									foundPeer.getConnection().sendTCP(new Data(DataTypes.CACHEPULL_DATA, streamsToSend));
								} else {
									Utilities.log(this, "No cache available to send", false);
								}
							}
						}
					});
					break;

				case DataTypes.HOSTPORT_REQS:
					Utilities.log(this, "Received request for hosting port", false);
					replyPool.execute(new Runnable() {
						public void run() {
							Utilities.log(this, "\tSent back hostport", false);
							foundPeer.getConnection().sendTCP(new Data(DataTypes.HOSTPORT_DATA, Core.config.tcpPort));
						}
					});
					break;

				// Data
				case DataTypes.PUBKEY_DATA:
					Utilities.log(this, "Received pubkey data: ", false);
					replyPool.execute(new Runnable() {
						public void run() {
							String pubkeyData = (String) data.getPayload();
							if (foundPeer.setPubkey(pubkeyData)) {
								Utilities.log(this, "Set pubkey " + pubkeyData + " for " + foundPeer, false);
								foundPeer.getPubkeyLatch().countDown();
							} else {
								Utilities.log(this, "Could not set pubkey " + pubkeyData + " for " + foundPeer, false);
							}
						}
					});
					break;

				case DataTypes.MUTEX_DATA:
					Utilities.log(this, "Received mutex data", false);
					replyPool.execute(new Runnable() {
						public void run() {
							String encryptedMutex = (String) data.getPayload();
							try {
								String mutexData = Core.rsa.decrypt(encryptedMutex);
								// Update foundPeer
								Peer bridgeFoundPeer = foundPeer;
								int attemptsToFindPeer = 0;
								while (bridgeFoundPeer == null && attemptsToFindPeer <= 5) {
									bridgeFoundPeer = Peer.findPeer(connection);
									Thread.sleep(100);
									attemptsToFindPeer++;
								}
								if (bridgeFoundPeer != null) {
									if (bridgeFoundPeer.mutexCheck(mutexData)) {
										bridgeFoundPeer.getCryptoLatch().countDown();
									}
								}
							} catch (Exception ex) {
								Utilities.log(this, "Failed to set mutex", false);
								ex.printStackTrace();
							}
						}
					});
					break;

				// All data past this point is encrypted via symmetric
				// encryption
				// TODO: symmetric encryption for peerlist and on

				case DataTypes.PEERLIST_DATA:
					Utilities.log(this, "Received peerlist data", false);
					replyPool.execute(new Runnable() {
						public void run() {
							Object payload = data.getPayload();
							if (payload instanceof ArrayList<?>) {
								ArrayList<String> finishedList = new ArrayList<String>();
								ArrayList<?> potentialList = (ArrayList<?>) payload;
								for (int i = 0; i < potentialList.size(); i++) {
									Object o = potentialList.get(i);
									if (o instanceof String) {
										String encrypted = (String) o;
										String decrypted = foundPeer.getAES().decrypt(encrypted);
										finishedList.add(decrypted);
									}
								}
								if (finishedList.size() == 0) {
									Utilities.log(this, "No viable peers were received from " + foundPeer.getMutex(),
											false);
								} else {
									for (int i = 0; i < finishedList.size(); i++) {
										try {
											Thread.sleep(1000);
										} catch (Exception ex) {
										}
										// Attempt to split the entry
										try {
											String[] split = finishedList.get(i).split(":");
											String host = split[0];
											int port = Integer.parseInt(split[1]);
											Client tempClient = Core.netHandler.getClient();
											tempClient.connect(8000, host, port);
										} catch (Exception ex) {
											try {
												Utilities.log(this, InetAddress
																.getByName(finishedList.get(i).split(":")[0]).toString(),
														false);
											} catch (UnknownHostException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											Utilities.log(this, "Peerlist data corrupted", false);
											ex.printStackTrace();
										}
									}
								}
							}
						}
					});
					break;

				case DataTypes.SEARCH_DATA:
					Utilities.log(this, "Received search reply data", false);
					replyPool.execute(new Runnable() {
						public void run() {
							Object searchPayload = data.getPayload();
							if (searchPayload instanceof ArrayList<?>) {
								ArrayList<?> potentialStreams = (ArrayList<?>) searchPayload;
								for (int i = 0; i < potentialStreams.size(); i++) {
									Object o = potentialStreams.get(i);
									if (o instanceof StreamedBlockedFile) {
										StreamedBlockedFile sbl = (StreamedBlockedFile) o;
										BlockedFile intermediate = sbl.toBlockedFile(foundPeer.getAES());

										// Store title, artist, time, and
										// checksum + blockList in index in
										// preparation for Download thread
										// fetching from GUI
										String name = intermediate.getPointer().getName();
										if (FilterUtils.mandatoryFilter(name)) {
											String checksum = intermediate.getChecksum();

											// TODO: URGENT [haveSearchAlready
											// container]
											// if(!Core.mainWindow.haveSearchAlready(checksum))
											// {
											ArrayList<String> blockList = intermediate.getBlockList();
											ArrayList<String> multiStore = new ArrayList<String>();
											multiStore.add(name);
											multiStore.add(checksum);
											Core.index.put(multiStore, blockList);

											NetHandler.searchResults.add(new String[] { name, checksum });
										}
									}
								}
							}
						}
					});
					break;

				case DataTypes.BLOCK_DATA:
					Utilities.log(this, "Received block data", true);
					StreamedBlock streamedBlock = (StreamedBlock) data.getPayload();
					streamedBlock.insertSelf(foundPeer.getAES());
					break;

				case DataTypes.EXTVIS_DATA:
					Utilities.log(this, "Received external visibility data", false);
					replyPool.execute(new Runnable() {
						public void run() {
							boolean vis = (boolean) data.getPayload();
							foundPeer.setExtVis(vis);
						}
					});
					break;

				case DataTypes.CACHE_DATA:
					Utilities.log(this, "Received cache pre-sync data", true);
					if (!Core.config.cacheEnabled) {
						Utilities.log(this, "Garbage cache data received, discarded", false);
						break;
					} else {
						final Object oPayload = data.getPayload();
						replyPool.execute(new Runnable() {
							public void run() {
								ArrayList<String> cacheDataRes = new ArrayList<String>();
								if (oPayload instanceof ArrayList<?>) {
									ArrayList<?> potentialCache = (ArrayList<?>) oPayload;
									for (int i = 0; i < potentialCache.size(); i++) {
										Object o = potentialCache.get(i);
										String thisCache = (String) o;
										cacheDataRes.add(foundPeer.getAES().decrypt(thisCache));
									}
								}

								// Filter out response for any BlockedFiles that
								// we have
								for (BlockedFile bf : Core.blockDex) {
									String curChecksum = bf.getChecksum();
									if (cacheDataRes.contains(curChecksum)) {
										cacheDataRes.remove(curChecksum);
									}
								}

								// Re-encrypt all entries for the cache data
								for (int i = 0; i < cacheDataRes.size(); i++) {
									cacheDataRes.set(i, Core.aes.encrypt(cacheDataRes.get(i)));
								}

								if (cacheDataRes.size() > 0) {
									Utilities.log(this, "Requesting " + cacheDataRes.size() + " BlockedFiles from peer "
											+ foundPeer.getMutex(), false);
									foundPeer.getConnection()
											.sendTCP(new Data(DataTypes.CACHEPULL_REQS, cacheDataRes));
								} else {
									Utilities.log(this, "No new data found from peer " + foundPeer.getMutex(), false);
								}
							}
						});
					}
					break;

				case DataTypes.CACHEPULL_DATA:
					Utilities.log(this, "Received cache pull data", false);
					final Object cachePullPayload = data.getPayload();
					replyPool.execute(new Runnable() {
						public void run() {
							if (cachePullPayload instanceof ArrayList<?>) {
								ArrayList<?> potentialStreams = (ArrayList<?>) cachePullPayload;
								for (int i = 0; i < potentialStreams.size(); i++) {
									Object o = potentialStreams.get(i);
									if (o instanceof StreamedBlockedFile) {
										StreamedBlockedFile sbl = (StreamedBlockedFile) o;
										BlockedFile intermediate = sbl.toBlockedFile(foundPeer.getAES());
										intermediate.setCacheStatus(true);

										if (FilterUtils.mandatoryFilter(intermediate.getPointer().getName())) {
											BlockedFile testBf = FileUtils.getBlockedFile(intermediate.getChecksum());
											if (!testBf.isComplete()) {
												// Silently download this
												// BlockedFile (complete)
												fetchCache(intermediate, true);
											}
										}
									}
								}
							}
						}
					});
					break;

				case DataTypes.HOSTPORT_DATA:
					Utilities.log(this, "Received hostport data", false);
					replyPool.execute(new Runnable() {
						public void run() {
							int hostPortData = (Integer) data.getPayload();
							foundPeer.setHostPort("" + hostPortData);
						}
					});
					break;
			}
		}
	}

	private void fetchCache(BlockedFile intermediate, boolean complete) {
		if (FileUtils.cacheReady(intermediate)) {
			if (!Core.blockDex.contains(intermediate)) {
				Core.blockDex.add(intermediate);
			}
			if (complete) {
				Utilities.log(this, "Beginning request for cache sync [C] on BlockedFile " + intermediate.getChecksum(),
						true);
				(new Thread(new Downloader(intermediate, false))).start();
			} else {
				Utilities.log(this,
						"Beginning request for cache sync [IC] on BlockedFile " + intermediate.getChecksum(), true);
				(new Thread(new Downloader(intermediate, false))).start();
			}
		} else {
			Utilities.log(this, "Non-compliance with cache readiness, deleting cachepull data", false);
		}
	}
}