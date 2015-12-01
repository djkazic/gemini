package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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
import gui.render.ProgressCellRenderer;
import gui.render.TableModelDL;
import gui.render.TableModelSpec;
import io.BlockedFile;
import io.Downloader;
import io.FileUtils;

import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -5729024992996472278L;
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
	private JPopupMenu downloadPopupMenu;
	private boolean searchMode;
	private JMenuItem downloadPopupMenuRemoveFromList;
	private JTabbedPane tabbedPane;
	private JScrollPane libraryScrollPane;
	private JTable libraryTable;
	private JPanel searchPanel;
	private JTextField searchInput;
	private JLabel lblSearchResults;
	private JPanel libPanel;
	private JButton btnSearch;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
					frame.out("Test");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {

		Utilities.log("atrium.Core", "Setting graphical preferences");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		setResizable(false);
		searchMode = false;
		setTitle("Radiator Beta");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 590);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
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

		libraryModel = new TableModelSpec();
		libraryModel.addColumn("Filename");
		libraryModel.addColumn("Size");
		libraryModel.addColumn("Date");

		resLatch = new CountDownLatch(1);
		contentPane.setLayout(null);

		try {
			//ImageIcon imageIcon = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/res/imgres/glasses.png")));
			//mntmAbout.setIcon(imageIcon);
		} catch(Exception e) {
			e.printStackTrace();
		}
		betterRenderer = new DefaultTableCellRenderer();
		betterRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		downloadPopupMenu = new JPopupMenu();
		downloadPopupMenuRemoveFromList = new JMenuItem("Remove from list");
		downloadPopupMenu.add(downloadPopupMenuRemoveFromList);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 11, 614, 519);
		contentPane.add(tabbedPane);

		searchPanel = new JPanel();
		tabbedPane.addTab("Search", null, searchPanel, null);
		searchPanel.setLayout(null);

		searchInput = new JTextField();
		searchInput.setBounds(10, 10, 490, 23);

		searchInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {
					//clear any previous res
					clearTable(searchModel);
					//clear core index
					Core.index.clear();

					if(NetHandler.peers.size() == 0) {
						out("No peers connected. Query is not possible.");
					} else {
						String input = searchInput.getText();
						if(input.equals("")) {
							out("You cannot search for a blank query.");
						} else if(input.length() < 3) {
							out("You cannot search for a query shorter than 3 characters.");
						} else {
							if(!searchMode) {
								removeColumnAndData(searchRes, 0);
								searchModel.addColumn("Filename");
								searchModel.addColumn("Size");
								searchMode = true;
							}
							NetHandler.doSearch(input);
						}
					}
					searchInput.setText("");
				}
			}
		});
		searchPanel.add(searchInput);
		searchInput.setColumns(10);
		searchInput.setEnabled(false);

		btnSearch = new JButton("Search");
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//clear any previous res
				clearTable(searchModel);
				//clear core index
				Core.index.clear();

				if(NetHandler.peers.size() == 0) {
					out("No peers connected. Query is not possible.");
				} else {
					String input = searchInput.getText();
					if(input.equals("")) {
						out("You cannot search for a blank query.");
					} else if(input.length() < 3) {
						out("You cannot search for a query shorter than 3 characters.");
					} else {
						if(!searchMode) {
							removeColumnAndData(searchRes, 0);
							searchModel.addColumn("Filename");
							searchModel.addColumn("Size");
							searchMode = true;
						}
						NetHandler.doSearch(input);
					}
				}
				searchInput.setText("");
			}
		});
		btnSearch.setBounds(510, 11, 89, 23);
		searchPanel.add(btnSearch);
		btnSearch.setEnabled(false);

		lblSearchResults = new JLabel(" Search Results");
		lblSearchResults.setBounds(10, 42, 80, 14);
		searchPanel.add(lblSearchResults);

		searchResScrollPane = new JScrollPane();
		searchResScrollPane.setBounds(10, 57, 589, 197);
		searchPanel.add(searchResScrollPane);

		searchRes = new JTable(searchModel);
		searchRes.setDefaultRenderer(Object.class, betterRenderer);
		//.getColumn(0).setCellRenderer(betterRenderer);
		searchRes.getTableHeader().setReorderingAllowed(false);
		searchRes.getTableHeader().setResizingAllowed(false);
		searchResScrollPane.setViewportView(searchRes);
		searchRes.setCellSelectionEnabled(true);
		searchRes.setColumnSelectionAllowed(true);

		JLabel lblDownloads = new JLabel(" Downloads");
		lblDownloads.setBounds(10, 265, 80, 14);
		searchPanel.add(lblDownloads);

		downloadScrollPane = new JScrollPane();
		downloadScrollPane.setBounds(10, 280, 589, 198);
		searchPanel.add(downloadScrollPane);

		downloadList = new JTable(downloadModel);
		downloadList.getColumnModel().getColumn(0).setCellRenderer(betterRenderer);
		downloadList.getColumnModel().getColumn(1).setCellRenderer(betterRenderer);
		downloadList.getTableHeader().setReorderingAllowed(false);
		downloadList.getTableHeader().setResizingAllowed(false);
		downloadScrollPane.setViewportView(downloadList);

		libPanel = new JPanel();
		tabbedPane.addTab("Library", null, libPanel, null);
		libPanel.setLayout(null);
		
		tabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) {
	            if(tabbedPane.getSelectedIndex() == 1) {
	            	updateLibrary();
	            }
	        }
	    });

		libraryScrollPane = new JScrollPane();
		libraryScrollPane.setBounds(10, 11, 589, 469);
		libPanel.add(libraryScrollPane);

		libraryTable = new JTable(libraryModel);
		libraryTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		libraryTable.getTableHeader().setReorderingAllowed(false);
		libraryTable.getTableHeader().setResizingAllowed(false);
		libraryScrollPane.setViewportView(libraryTable);

		lblPeers = new JLabel("");
		lblPeers.setBounds(606, 535, 24, 24);
		contentPane.add(lblPeers);
		lblPeers.setToolTipText("[0|0]");
		lblPeers.setOpaque(false);
		lblPeers.setIcon(new ImageIcon(MainWindow.class.getResource("/res/imgres/0bars.png")));
		lblPeers.setFont(new Font("Tahoma", Font.PLAIN, 11));

		registerListeners();

	}

	public void registerListeners() {
		downloadPopupMenuRemoveFromList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedRows = downloadList.getSelectedRows();
				for(Integer i : selectedRows) {
					downloadModel.removeRow(i);
				}
			}
		});

		downloadList.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.isPopupTrigger()) {
					Point clickPoint = arg0.getPoint();
					int tableRow = downloadList.rowAtPoint(clickPoint);
					if(!downloadList.isRowSelected(tableRow)) {
						downloadList.changeSelection(tableRow, 0, false, false);
					}
					downloadPopupMenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
				}
			}
		});

		libraryTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				Point clickPoint = arg0.getPoint();
				int tableRow = libraryTable.rowAtPoint(clickPoint);
				if(arg0.getClickCount() == 2) {
					String fileName = (String) libraryModel.getValueAt(tableRow, 0);
					BlockedFile bf = FileUtils.getBlockedFile(fileName.substring(1));
					FileUtils.openBlockedFile(bf);
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
						@SuppressWarnings("rawtypes")
						Iterator it = Core.index.entrySet().iterator();
						//Iterate through HashMap until a match by blockListStr is found
						while(it.hasNext()) {
							@SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry) it.next();
							String tableFileName = (String) pairs.getKey();
							ArrayList<String> blockList = (ArrayList<String>) pairs.getValue();
							//Check to see if the HashMap's matching is accurate
							if(tableFileName.equals(fileName)) {
								BlockedFile bf;
								//Check if this BlockedFile exists in index
								if(FileUtils.getBlockedFile(blockList) != null) {
									bf = FileUtils.getBlockedFile(blockList);
								} else {
									//If not, create a new BlockedFile instance
									bf = new BlockedFile(fileName, blockList);
								}
								int numRows = downloadModel.getRowCount();
								boolean alreadyDoneInPane = false;
								for(int i = 0; i < numRows; i++) {
									if(downloadModel.getValueAt(i, 0).equals(bf.getPointer().getName())) {
										if(downloadModel.getValueAt(i, 1).equals("100%")) {
											alreadyDoneInPane = true;
											break;
										}
									}
								}
								if(!alreadyDoneInPane && !bf.isComplete()) {
									downloadModel.addRow(new String[]{bf.getPointer().getName(), "0%", "?"});
									downloadList.getColumnModel().getColumn(1).setCellRenderer(new ProgressCellRenderer());
									(new Thread(new Downloader(bf))).start();
								} else if(bf.isComplete()) {
									String bfFileName = bf.getPointer().getName();
									Utilities.log(this, "This file is already downloaded.");
									JOptionPane.showMessageDialog(null, bfFileName + 
												" has already been downloaded.", "",
		                                    	JOptionPane.INFORMATION_MESSAGE);
									resetTable();
								}
								//resetTable();
							}
							it.remove();
						}
					}
				}
			}
		});
		resLatch.countDown();
		setVisible(true);
	}

	private void clearTable(DefaultTableModel tableModel) {
		int rowCount = tableModel.getRowCount();
		if(rowCount > 0) {
			for(int i=0; i < tableModel.getRowCount(); i++) {
				tableModel.removeRow(i);
				i--;
			}
		}
	}

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

	public void out(String str) {

		try {
			resLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(searchMode) {
			removeColumnAndData(searchRes, 1);
			removeColumnAndData(searchRes, 0);
			searchModel.addColumn("Status");
			searchMode = false;
		}
		clearTable(searchModel);
		searchModel.addRow(new String[]{str});

	}

	public void updatePeerCount() {
		String peers = peersCountIcon();
		lblPeers.setIcon(new ImageIcon(MainWindow.class.getResource("/res/imgres/" + peers + ".png")));
		lblPeers.setToolTipText(peerToolTip());
	}

	public void updateTime(String forFile, String time) {
		int rowCount = downloadModel.getRowCount();
		for(int i=0; i < rowCount; i++) {
			if(downloadModel.getValueAt(i, 0).equals(forFile)) {
				downloadModel.setValueAt(" " + time, i, 2);
			}
		}
	}
	
	public void updateProgress(String forFile, String progress) {
		int rowCount = downloadModel.getRowCount();
		for(int i=0; i < rowCount; i++) {
			if(downloadModel.getValueAt(i, 0).equals(forFile)) {
				downloadModel.setValueAt(progress, i, 1);
			}
		}
	}

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

	public void setSearchFocusable() {

		searchInput.setFocusable(true);

	}

	public void setSearchEditable() {

		searchInput.setEditable(true);

	}

	public void resetTable() {
		out("Enter your search query and press Enter.");
	}

	public void addRowToSearchModel(String[] info) {
		searchModel.addRow(info);
	}
	
	public void removeRowFromSearchModel(String pointerName) {
		for(int i=0; i < searchModel.getRowCount(); i++) {
			if(searchModel.getValueAt(i, 0).equals(pointerName)) {
				searchModel.removeRow(i);
				return;
			}
		}
	}

	public String peersCountIcon() {
		int size = NetHandler.peers.size();
		if(size == 0) {
			//0 peers
			return "0bars";
		} else if(size <= 2) {
			//1 or 2 peers
			return "1bars";
		} else if(size <= 4) {
			//3 or 4 peers
			return "2bars";
		} else if(size > 4) {
			return "3bars";
		}
		return null;
	}

	public String peerToolTip() {
		int inCount = 0;
		int outCount = 0;
		for(Peer peer : NetHandler.peers) {
			if(peer.getInOut() == 1) {
				inCount++;
			} else {
				outCount++;
			}
		}
		return "[" + inCount + "|" + outCount + "]";
	}
	
	public void ready() {
		searchInput.setEnabled(true);
		btnSearch.setEnabled(true);
		out("Ready");
	}
}