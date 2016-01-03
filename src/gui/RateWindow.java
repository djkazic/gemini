package gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import atrium.Core;
import atrium.Utilities;
import io.block.BlockedFile;
import io.block.Metadata;

public class RateWindow extends JFrame {

	private static final long serialVersionUID = 8132415706423152570L;
	private JPanel contentPane;
	private JLabel lblNewLabel;
	
	private ActionListener btnListener;

	private JToggleButton btnUp;
	private JToggleButton btnDown;
	
	private Color defColor;
	
	private BlockedFile blockedFile;
	private Metadata metadata;
	private JLabel lblScore;

	/**
	 * Create the frame.
	 */
	public RateWindow(BlockedFile bf) {
		blockedFile = bf;
		metadata = Metadata.findMetaByChecksum(bf.getChecksum());
		Utilities.log(this, "Match found for: " + bf.getChecksum() + "\t" + Core.metaDex, false);
		
		if(metadata == null) {
			Utilities.log(this, "No match found for: " + bf.getChecksum() + "\t" + Core.metaDex, false);
			metadata = new Metadata(bf.getChecksum());
			Metadata.serializeAll();
		}
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 285, 128);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(9, 10, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{350, 0, 39, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		lblNewLabel = new JLabel(blockedFile.getPointer().getName());
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		btnListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(metadata != null) {
					JToggleButton thisBtn = (JToggleButton) arg0.getSource();
					String thisBtnState = "";
					if(thisBtn.equals(btnUp)) {
						thisBtnState = "U";
					} else if(thisBtn.equals(btnDown)) {
						thisBtnState = "D";
					}
					if(thisBtn.isSelected() && !thisBtnState.equals(metadata.getLastState())) {
						JToggleButton btnDisable = null;
						
						//Mutually exclusive selection
						if(thisBtnState.equals("U")) {
							thisBtn.setBackground(Color.GREEN);
							metadata.vote(1);
							metadata.setLastState(thisBtnState);
							btnDisable = btnDown;
						} else if(thisBtnState.equals("D")) {
							thisBtn.setBackground(Color.RED);
							metadata.vote(-1);
							metadata.setLastState(thisBtnState);
							btnDisable = btnUp;
						}
						btnDisable.setSelected(false);
						btnDisable.setBackground(defColor);
						
						Metadata.serializeAll();
					}
					updateScore();
				} else {
					//Already voted
					btnUp.setSelected(false);
					btnUp.setBackground(defColor);
					btnDown.setSelected(false);
					btnDown.setBackground(defColor);
				}
			}
		};

		btnUp = new JToggleButton("");
		defColor = btnUp.getBackground();
		btnUp.addActionListener(btnListener);
		btnUp.setIcon(new ImageIcon(RateWindow.class.getResource("/res/imgres/upvote.png")));
		GridBagConstraints gbc_btnUp = new GridBagConstraints();
		gbc_btnUp.insets = new Insets(0, 0, 5, 0);
		gbc_btnUp.gridx = 2;
		gbc_btnUp.gridy = 0;
		contentPane.add(btnUp, gbc_btnUp);
		
		lblScore = new JLabel("" + metadata.getScore());
		GridBagConstraints gbc_lblScore = new GridBagConstraints();
		gbc_lblScore.insets = new Insets(0, 0, 5, 0);
		gbc_lblScore.gridx = 2;
		gbc_lblScore.gridy = 1;
		contentPane.add(lblScore, gbc_lblScore);
		
		btnDown = new JToggleButton("");
		btnDown.addActionListener(btnListener);
		btnDown.setIcon(new ImageIcon(RateWindow.class.getResource("/res/imgres/downvote.png")));
		GridBagConstraints gbc_btnDown = new GridBagConstraints();
		gbc_btnDown.gridx = 2;
		gbc_btnDown.gridy = 2;
		contentPane.add(btnDown, gbc_btnDown);
		
		String metaButton = metadata.getLastState();
		JToggleButton btnDisable = null;
		
		if(metaButton != null) {
			//Load colors of buttons
			if(metaButton.equals("U")) {
				btnUp.setBackground(Color.GREEN);
				btnDisable = btnDown;
			} else if(metaButton.equals("D")) {
				btnDown.setBackground(Color.RED);
				btnDisable = btnUp;
			}
			btnDisable.setSelected(false);
			btnDisable.setBackground(defColor);
		}
		
		setVisible(true);
	}

	private void updateScore() {
		if(metadata != null) {
			lblScore.setText("" + metadata.getScore());
		}
	}
}
