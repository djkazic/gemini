package gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class ConfigWindow extends JFrame {

	private static final long serialVersionUID = 5294562637731587218L;
	private JPanel contentPane;
	private JTextField dlDirDisplay;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigWindow frame = new ConfigWindow();
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
	public ConfigWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 380, 185);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JCheckBox checkBoxPlaceholder = new JCheckBox("Config option");
		checkBoxPlaceholder.setBounds(6, 7, 165, 23);
		contentPane.add(checkBoxPlaceholder);
		
		JCheckBox checkBoxPlaceholder2 = new JCheckBox("Config option");
		checkBoxPlaceholder2.setBounds(6, 33, 165, 23);
		contentPane.add(checkBoxPlaceholder2);
		
		JLabel lblDownloadDirectory = new JLabel("Download directory: ");
		lblDownloadDirectory.setBounds(10, 63, 120, 14);
		contentPane.add(lblDownloadDirectory);
		
		dlDirDisplay = new JTextField();
		dlDirDisplay.setBounds(10, 79, 245, 20);
		contentPane.add(dlDirDisplay);
		dlDirDisplay.setColumns(10);
		
		JButton browseBtn = new JButton("Browse");
		browseBtn.setBounds(265, 78, 89, 23);
		contentPane.add(browseBtn);
		
		JButton applyBtn = new JButton("Apply");
		applyBtn.setBounds(10, 112, 89, 23);
		contentPane.add(applyBtn);
		
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.setBounds(265, 112, 89, 23);
		contentPane.add(cancelBtn);
		
		setVisible(true);
	}
}
