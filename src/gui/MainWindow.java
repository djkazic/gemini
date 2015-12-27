package gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import atrium.Core;
import atrium.NetHandler;
import atrium.Peer;
import atrium.Utilities;
import filter.FilterUtils;
import gui.render.ProgressCellRenderer;
import gui.render.TableModelDL;
import gui.render.TableModelSpec;
import io.Downloader;
import io.FileUtils;
import io.block.BlockedFile;
import net.iharder.dnd.FileDrop;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -4060708900516820183L;
	
	private JPanel contentPane;
	private JTable searchRes;
	private JTable downloadList;
	private DefaultTableModel searchModel;
	private DefaultTableModel libraryModel;
	private DefaultTableModel downloadModel;
	private DefaultTableCellRenderer betterRenderer;
	private CountDownLatch resLatch;
	private JScrollPane searchResScrollPane;
	private JScrollPane downloadScrollPane;
	private JLabel lblPeers;
	private JLabel lblDownloads;
	private JPopupMenu downloadPopupMenu;
	private JMenuItem downloadPopupMenuPause;
	private JMenuItem downloadPopupMenuResume;
	private JMenuItem downloadPopupMenuRemoveFromList;
	private boolean searchMode;
	private JTabbedPane tabbedPane;
	private JScrollPane libraryScrollPane;
	private JTable libraryTable;
	private JPanel searchPanel;
	private JTextField searchInput;
	private JLabel lblSearchResults;
	private JPanel libPanel;
	private JButton btnSearch;
	private JLabel spacerLabel;
	private JLabel spacerLabel_1;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		Core.loadWindow.setProgress(100);
		
		searchMode = false;
		setTitle("Radiator Beta");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 645, 535);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 0, 5));
		setContentPane(contentPane);
		
		//Set title icon
		try {
			//Image iconImage = ImageIO.read(getClass().getResourceAsStream("/res/imgres/titleicon.png"));
			//setIconImage(iconImage);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		searchModel = new TableModelSpec();
		searchModel.addColumn("Status");

		downloadModel = new TableModelDL();
		downloadModel.addColumn("Filename");
		downloadModel.addColumn("Progress");
		downloadModel.addColumn("Time Remaining");
		downloadModel.addColumn("Checksum");

		libraryModel = new TableModelSpec();
		libraryModel.addColumn("Filename");
		libraryModel.addColumn("Size");
		libraryModel.addColumn("Date");

		resLatch = new CountDownLatch(1);
		
		try {
			//ImageIcon imageIcon = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/res/imgres/glasses.png")));
			//mntmAbout.setIcon(imageIcon);
		} catch(Exception e) {
			e.printStackTrace();
		}
		betterRenderer = new DefaultTableCellRenderer();
		betterRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		downloadPopupMenu = new JPopupMenu();
		downloadPopupMenuPause = new JMenuItem("Pause download");
		downloadPopupMenuPause.setEnabled(false);
		downloadPopupMenuResume = new JMenuItem("Resume download");
		downloadPopupMenuResume.setEnabled(false);
		downloadPopupMenuRemoveFromList = new JMenuItem("Remove from downloads");
		
		downloadPopupMenu.add(downloadPopupMenuPause);
		downloadPopupMenu.add(downloadPopupMenuResume);
		downloadPopupMenu.add(downloadPopupMenuRemoveFromList);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{299, 0};
		gbl_contentPane.rowHeights = new int[]{460, 26, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPane.add(tabbedPane, gbc_tabbedPane);
		
		searchPanel = new JPanel();
		searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		tabbedPane.addTab("Search", null, searchPanel, null);
		GridBagLayout gbl_searchPanel = new GridBagLayout();
		gbl_searchPanel.columnWidths = new int[]{89, 389, 59, 25, 0};
		gbl_searchPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_searchPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_searchPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		searchPanel.setLayout(gbl_searchPanel);
		
		searchInput = new JTextField();
		searchInput.setEnabled(false);
		GridBagConstraints gbc_searchInput = new GridBagConstraints();
		gbc_searchInput.gridwidth = 2;
		gbc_searchInput.fill = GridBagConstraints.BOTH;
		gbc_searchInput.insets = new Insets(0, 0, 5, 5);
		gbc_searchInput.gridx = 0;
		gbc_searchInput.gridy = 0;
		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					clearTable(searchModel);
					//clear core index
					Core.index.clear();

					if(Core.peers.size() == 0) {
						out("No peers connected. Query is not possible.");
					} else {
						String input = searchInput.getText();
						if(input.equals("")) {
							out("You cannot search for a blank query.");
						} else if(input.startsWith(".") 
							      || FilterUtils.extensionOnly(input) 
								  || FilterUtils.extensionOnly(input.substring(1))) {
							out("You cannot search for a query that starts with an extension.");
						} else if(input.length() < 3) {
							out("You cannot search for a query shorter than 3 characters.");
						} else {
							if(!searchMode) {
								removeColumnAndData(searchRes, 0);
								searchModel.addColumn("Filename");
								searchModel.addColumn("Size");
								searchModel.addColumn("Checksum");
								searchModel.addColumn("Rating");
								searchRes.getColumnModel().getColumn(2).setMinWidth(0);
								searchRes.getColumnModel().getColumn(2).setMaxWidth(0);
								searchRes.getColumnModel().getColumn(2).setWidth(0);
								searchMode = true;
							}
							NetHandler.doSearch(input);
						}
					}
					searchInput.setText("");
				}
			}
		});
		searchPanel.add(searchInput, gbc_searchInput);
		searchInput.setColumns(45);
		
		btnSearch = new JButton("Search");
		btnSearch.setEnabled(false);
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.gridwidth = 2;
		gbc_btnSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnSearch.fill = GridBagConstraints.BOTH;
		gbc_btnSearch.gridx = 2;
		gbc_btnSearch.gridy = 0;
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//clear any previous res
				clearTable(searchModel);
				//clear core index
				Core.index.clear();

				if(Core.peers.size() == 0) {
					out("No peers connected. Query is not possible.");
				} else {
					String input = searchInput.getText();
					if(input.equals("")) {
						out("You cannot search for a blank query.");
					} else if(input.startsWith(".")) {
						out("You cannot search for a query that starts with an extension.");
					} else if(input.length() < 3) {
						out("You cannot search for a query shorter than 3 characters.");
					} else {
						if(!searchMode) {
							removeColumnAndData(searchRes, 0);
							searchModel.addColumn("Filename");
							searchModel.addColumn("Size");
							searchModel.addColumn("Checksum");
							searchModel.addColumn("Rating");
							searchRes.getColumnModel().getColumn(2).setMinWidth(0);
							searchRes.getColumnModel().getColumn(2).setMaxWidth(0);
							searchRes.getColumnModel().getColumn(2).setWidth(0);
							searchMode = true;
						}
						NetHandler.doSearch(input);
					}
				}
				searchInput.setText("");
			}
		});
		searchPanel.add(btnSearch, gbc_btnSearch);
		
		spacerLabel = new JLabel("");
		GridBagConstraints gbc_spacerLabel = new GridBagConstraints();
		gbc_spacerLabel.insets = new Insets(5, 0, 5, 5);
		gbc_spacerLabel.gridx = 1;
		gbc_spacerLabel.gridy = 1;
		searchPanel.add(spacerLabel, gbc_spacerLabel);
		
		lblSearchResults = new JLabel("Search Results");
		GridBagConstraints gbc_lblSearchResults = new GridBagConstraints();
		gbc_lblSearchResults.anchor = GridBagConstraints.WEST;
		gbc_lblSearchResults.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchResults.gridx = 0;
		gbc_lblSearchResults.gridy = 2;
		searchPanel.add(lblSearchResults, gbc_lblSearchResults);
		
		searchResScrollPane = new JScrollPane();
		GridBagConstraints gbc_searchResScrollPane = new GridBagConstraints();
		gbc_searchResScrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_searchResScrollPane.gridwidth = 4;
		gbc_searchResScrollPane.fill = GridBagConstraints.BOTH;
		gbc_searchResScrollPane.gridx = 0;
		gbc_searchResScrollPane.gridy = 3;
		searchPanel.add(searchResScrollPane, gbc_searchResScrollPane);
		
		searchRes = new JTable(searchModel);
		searchRes.setDefaultRenderer(Object.class, betterRenderer);
		searchRes.getTableHeader().setReorderingAllowed(false);
		searchRes.getTableHeader().setResizingAllowed(false);
		searchRes.setRowSelectionAllowed(true);
		searchResScrollPane.setViewportView(searchRes);
		
		spacerLabel_1 = new JLabel("");
		GridBagConstraints gbc_spacerLabel_1 = new GridBagConstraints();
		gbc_spacerLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_spacerLabel_1.gridx = 1;
		gbc_spacerLabel_1.gridy = 4;
		searchPanel.add(spacerLabel_1, gbc_spacerLabel_1);
		
		lblDownloads = new JLabel("Downloads");
		GridBagConstraints gbc_lblDownloads = new GridBagConstraints();
		gbc_lblDownloads.anchor = GridBagConstraints.WEST;
		gbc_lblDownloads.insets = new Insets(0, 0, 5, 5);
		gbc_lblDownloads.gridx = 0;
		gbc_lblDownloads.gridy = 5;
		searchPanel.add(lblDownloads, gbc_lblDownloads);
		
		downloadScrollPane = new JScrollPane();
		GridBagConstraints gbc_downloadScrollPane = new GridBagConstraints();
		gbc_downloadScrollPane.gridwidth = 4;
		gbc_downloadScrollPane.fill = GridBagConstraints.BOTH;
		gbc_downloadScrollPane.gridx = 0;
		gbc_downloadScrollPane.gridy = 6;
		searchPanel.add(downloadScrollPane, gbc_downloadScrollPane);
		
		downloadList = new JTable(downloadModel);
		downloadList.getColumnModel().getColumn(3).setMinWidth(0);
		downloadList.getColumnModel().getColumn(3).setMaxWidth(0);
		downloadList.getColumnModel().getColumn(3).setWidth(0);
		downloadList.getColumnModel().getColumn(0).setCellRenderer(betterRenderer);
		downloadList.getColumnModel().getColumn(1).setCellRenderer(betterRenderer);
		downloadList.getTableHeader().setReorderingAllowed(false);
		downloadList.getTableHeader().setResizingAllowed(false);
		downloadScrollPane.setViewportView(downloadList);
		
		libPanel = new JPanel();
		new FileDrop(libPanel, new FileDrop.Listener() {
			@Override
			public void filesDropped(final File[] files) {
				Utilities.log(this, "Drag-drop latched", true);
				for(int i=0; i < files.length; i++) {
					try {
						String fileName = files[i].getName();
						if(FilterUtils.mandatoryFilter(fileName)) {
							Files.copy(files[i].toPath(), Paths.get(FileUtils.getWorkspaceDir() + "/" + fileName));
						}
						//fos.close();
					} catch (Exception ex) {
						Utilities.log(this, "Drag-drop listener exception: ", false);
						ex.printStackTrace();
					}
				}
			}
		});
		libPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		tabbedPane.addTab("Library", null, libPanel, null);
		
		GridBagLayout gbl_libPanel = new GridBagLayout();
		gbl_libPanel.columnWidths = new int[]{0, 0, 0};
		gbl_libPanel.rowHeights = new int[]{0, 0, 0};
		gbl_libPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_libPanel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		libPanel.setLayout(gbl_libPanel);
		
		libraryScrollPane = new JScrollPane();
		GridBagConstraints gbc_libraryScrollPane = new GridBagConstraints();
		gbc_libraryScrollPane.gridwidth = 2;
		gbc_libraryScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_libraryScrollPane.fill = GridBagConstraints.BOTH;
		gbc_libraryScrollPane.gridx = 0;
		gbc_libraryScrollPane.gridy = 0;
		libPanel.add(libraryScrollPane, gbc_libraryScrollPane);
		
		libraryTable = new JTable(libraryModel);
		libraryTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		libraryTable.getTableHeader().setReorderingAllowed(false);
		libraryTable.getTableHeader().setResizingAllowed(false);
		libraryScrollPane.setViewportView(libraryTable);
		
		lblPeers = new JLabel("");
		lblPeers.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPeers = new GridBagConstraints();
		gbc_lblPeers.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPeers.gridx = 0;
		gbc_lblPeers.gridy = 1;
		contentPane.add(lblPeers, gbc_lblPeers);
		lblPeers.setToolTipText("[0|0]");
		lblPeers.setOpaque(false);
		lblPeers.setIcon(new ImageIcon(MainWindow.class.getResource("/res/imgres/0bars.png")));
		registerListeners();
	}
	
	/**
	 * Registers GUI listeners to the instance variables
	 */
	public void registerListeners() {
		tabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent ce) {
	            if(tabbedPane.getSelectedIndex() == 1) {
	            	updateLibrary();
	            }
	        }
	    });
		
		downloadPopupMenuPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int[] selectedRows = downloadList.getSelectedRows();
				for(Integer i : selectedRows) {
					Object firstColumn = downloadModel.getValueAt(i, 3);
					if(firstColumn instanceof String) {
						Downloader.pauseDownloader((String) firstColumn);
					}
				}
			}
		});
		
		downloadPopupMenuResume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int[] selectedRows = downloadList.getSelectedRows();
				for(Integer i : selectedRows) {
					Object firstColumn = downloadModel.getValueAt(i, 3);
					if(firstColumn instanceof String) {
						Downloader.resumeDownloader((String) firstColumn);
					}
				}
			}
		});
		
		downloadPopupMenuRemoveFromList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int[] selectedRows = downloadList.getSelectedRows();
				for(Integer i : selectedRows) {
					Object firstColumn = downloadModel.getValueAt(i, 3);
					if(firstColumn instanceof String) {
						Downloader.removeDownloader((String) firstColumn);
					}
					downloadModel.removeRow(i);
				}
			}
		});

		downloadList.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent ae) {
				if(ae.isPopupTrigger()) {
					Point clickPoint = ae.getPoint();
					int tableRow = downloadList.rowAtPoint(clickPoint);
					if(!downloadList.isRowSelected(tableRow)) {
						downloadList.changeSelection(tableRow, 0, false, false);
					}
					downloadPopupMenu.show(ae.getComponent(), ae.getX(), ae.getY());
				}
			}
		});

		libraryTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				Point clickPoint = arg0.getPoint();
				int tableRow = libraryTable.rowAtPoint(clickPoint);
				if(arg0.getClickCount() == 2) {
					String fileName = (String) libraryModel.getValueAt(tableRow, 0);
					BlockedFile bf = null;
					for(BlockedFile ibf : Core.blockDex) {
						if(ibf.getPointer().getName().equals(fileName.substring(1))) {
							bf = ibf;
						}
					}
					if(bf != null) {
						FileUtils.openBlockedFile(bf);
					}
				}
			}
		});

		searchRes.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(searchMode) {
					Point clickPoint = arg0.getPoint();
					int tableRow = searchRes.rowAtPoint(clickPoint);
					if(arg0.getClickCount() == 2) {
						String fileName = (String) searchModel.getValueAt(tableRow, 0);
						String tableChecksum = (String) searchModel.getValueAt(tableRow, 2);
						@SuppressWarnings("rawtypes")
						Iterator it = Core.index.entrySet().iterator();
						
						//Iterate through HashMap until a match by filename is found
						while(it.hasNext()) {
							@SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry) it.next();
							//String tableFileName = (String) pairs.getKey();
							ArrayList<String> indexNameAndChecksum = (ArrayList<String>) pairs.getKey();
							String indexName = null;
							String checksum = null;
							try {
								if(indexNameAndChecksum.size() == 2) {
									indexName = indexNameAndChecksum.get(0);
									checksum = indexNameAndChecksum.get(1);
								} else {
									throw new Exception("Corrupt indexNameAndChecksum data for " + fileName);
								}
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							ArrayList<String> blockList = (ArrayList<String>) pairs.getValue();
							//Check to see if the HashMap's matching is accurate
							if((indexName != null && checksum != null) && tableChecksum.equals(checksum)) {
								BlockedFile bf;
								//Check if this BlockedFile exists in index by checksum
								if(FileUtils.getBlockedFile(checksum) != null) {
									bf = FileUtils.getBlockedFile(checksum);
									bf.setBlockList(blockList);
								} else {
									//If not, create a new BlockedFile instance
									bf = new BlockedFile(fileName, checksum, blockList);
								}
								boolean alreadyDoneInPane = false;
								for(int i = 0; i < downloadModel.getRowCount(); i++) {
									if(downloadModel.getValueAt(i, 3).equals(bf.getChecksum())) {
										if(downloadModel.getValueAt(i, 1).equals("100%")) {
											alreadyDoneInPane = true;
											break;
										}
									}
								}
								if(!alreadyDoneInPane && !bf.isComplete()) {
									downloadModel.addRow(new String[]{bf.getPointer().getName(), "0%", " ... ", bf.getChecksum()});
									downloadList.getColumnModel().getColumn(1).setCellRenderer(new ProgressCellRenderer());
									downloadPopupMenuPause.setEnabled(true);
									downloadPopupMenuResume.setEnabled(true);
									(new Thread(new Downloader(bf))).start();
									return;
								}
								if(bf.isComplete()) {
									String bfFileName = bf.getPointer().getName();
									Utilities.log(this, "File is already downloaded: [" + bfFileName + "]", false);
									JOptionPane.showMessageDialog(null, bfFileName + " has already been downloaded.", "",
																  JOptionPane.INFORMATION_MESSAGE);
								}
							}
							//it.remove();
						}
					}
				}
			}
		});
		resLatch.countDown();
		Core.loadWindow.setVisible(false);
		Core.loadWindow.dispose();
		setVisible(true);
	}

	public void clearSearchData() {
		clearTable(searchModel);
	}
	
	/**
	 * Clears a given table of its entries
	 * @param tableModel tableModel of table to clear
	 */
	private void clearTable(DefaultTableModel tableModel) {
		int rowCount = tableModel.getRowCount();
		if(rowCount > 0) {
			for(int i=0; i < tableModel.getRowCount(); i++) {
				tableModel.removeRow(i);
				i--;
			}
		}
	}

	/**
	 * Removes the column of a table
	 * @param table table to remove column from
	 * @param vColIndex index of column to remove
	 */
	private void removeColumnAndData(JTable table, int vColIndex) {
		TableModelSpec model = (TableModelSpec)table.getModel();
		TableColumn col = table.getColumnModel().getColumn(vColIndex);
		int columnModelIndex = col.getModelIndex();
		Vector<?> data = model.getDataVector();
		Vector<?> colIds = model.getColumnIdentifiers();
		table.removeColumn(col);
		colIds.removeElementAt(columnModelIndex);
		for (int r=0; r<data.size(); r++) {
			Vector<?> row = (Vector<?>)data.get(r);
			row.removeElementAt(columnModelIndex);
		}
		model.setDataVector(data, colIds);
		Enumeration<?> enumer = table.getColumnModel().getColumns();
		for(;enumer.hasMoreElements();) {
			TableColumn c = (TableColumn)enumer.nextElement();
			if (c.getModelIndex() >= columnModelIndex) {
				c.setModelIndex(c.getModelIndex()-1);
			}
		}
		model.fireTableStructureChanged();
	}

	/**
	 * Outputs a string into the main GUI
	 * @param str output data
	 */
	public void out(String str) {
		try {
			resLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(searchMode) {
			removeColumnAndData(searchRes, 3);
			removeColumnAndData(searchRes, 2);
			removeColumnAndData(searchRes, 1);
			removeColumnAndData(searchRes, 0);
			searchModel.addColumn("Status");
			searchMode = false;
		}
		clearTable(searchModel);
		searchModel.addRow(new String[]{str});
	}

	/**
	 * Updates the icon for peers and its tooltip
	 */
	public void updatePeerCount() {
		String peers = peersCountIcon();
		lblPeers.setIcon(new ImageIcon(MainWindow.class.getResource("/res/imgres/" + peers + ".png")));
		lblPeers.setToolTipText(peerToolTip());
	}

	/**
	 * Updates the time given for a BlockedFile in download
	 * @param checksum the BlockedFile's checksum
	 * @param time the updated amount of time left
	 */
	public void updateTime(String checksum, String time) {
		int rowCount = downloadModel.getRowCount();
		for(int i=0; i < rowCount; i++) {
			if(downloadModel.getValueAt(i, 3).equals(checksum)) {
				downloadModel.setValueAt(" " + time, i, 2);
			}
		}
	}
	
	/**
	 * Updates the progress percentage for a BlockedFile in download
	 * @param checksum the BlockedFile's checksum
	 * @param progress the updated level of progress
	 */
	public void updateProgress(String checksum, String progress) {
		int rowCount = downloadModel.getRowCount();
		for(int i=0; i < rowCount; i++) {
			if(downloadModel.getValueAt(i, 3).equals(checksum)) {
				downloadModel.setValueAt(progress, i, 1);
			}
		}
	}

	/**
	 * Removes specific BlockedFile from Downloads pane
	 * @param bf
	 */
	public void removeDownload(BlockedFile bf) {
		for(int i = 0; i < downloadModel.getRowCount(); i++) {
			if(downloadModel.getValueAt(i, 3).equals(bf.getChecksum())) {
				downloadModel.removeRow(i);
			}
		}
	}
	
	/**
	 * Updates the contents of the library tab
	 */
	public void updateLibrary() {
		clearTable(libraryModel);
		for(BlockedFile bf : Core.blockDex) {
			if(bf.isComplete()) {
				String fileEstimateStr = "";
				double fileEstimateKb = bf.getPointer().length() / 1000D;
				if(fileEstimateKb > 1000) {
					double fileEstimateMb = (fileEstimateKb / 1000D);
					fileEstimateStr += String.format("%.2f", fileEstimateMb) + " MB";
				} else {
					fileEstimateStr += String.format("%.2f", fileEstimateKb) + " KB";
				}
				libraryModel.addRow(new String[]{" " + bf.getPointer().getName(), 
												 " " + fileEstimateStr, 
												 " " + bf.getDateModified()});
			}
		}
	}

	/**
	 * Sets the GUI focus to the search bar
	 */
	public void setSearchFocusable() {
		searchInput.setFocusable(true);
	}

	/**
	 * Sets the search bar to editable
	 */
	public void setSearchEditable() {
		searchInput.setEditable(true);
	}

	/**
	 * Adds a specific row to the search results table
	 * @param info string array of data [Filename, Est. Size]
	 */
	public void addRowToSearchModel(String[] info) {
		searchModel.addRow(info);
	}
	
	public boolean haveSearchAlready(String checksum) { 
		for(int i = 0; i < searchRes.getRowCount(); i++) {
			if(searchRes.getValueAt(i, 2).equals(checksum)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value of the correct peer count icon
	 * @return string value of the correct PNG file for this number of peers
	 */
	public String peersCountIcon() {
		int size = Core.peers.size();
		if(size == 0) {
			//0 peers
			return "0bars";
		} else if(size > 0 && size <= 2) {
			//1 or 2 peers
			return "1bars";
		} else if(size > 2 && size <= 4) {
			//3 or 4 peers
			return "2bars";
		} else if(size > 4) {
			//5+ peers
			return "3bars";
		}
		return null;
	}

	/**
	 * Returns value of tooltip for a given peer count
	 * @return value of tooltip as a string
	 */
	public String peerToolTip() {
		int inCount = 0;
		int outCount = 0;
		for(Peer peer : Core.peers) {
			if(peer.getInOut() == 1) {
				inCount++;
			} else {
				outCount++;
			}
		}
		return "[" + inCount + "|" + outCount + "]";
	}
	
	/**
	 * Sets the GUI to a ready state, with search enabled
	 */
	public void ready() {
		searchInput.setEnabled(true);
		btnSearch.setEnabled(true);
		setSearchFocusable();
		out("Ready");
	}
}
