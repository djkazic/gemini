package gui;

import java.awt.EventQueue;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;
import atrium.Utilities;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LoadWindow extends JFrame {

	private static final long serialVersionUID = 6150895061626361531L;
	private JPanel contentPane;
	private JProgressBar progressBar;
	private JLabel status;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoadWindow frame = new LoadWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoadWindow() {

		Utilities.log("Core", "Setting graphical preferences", true);
		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 328, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 366, 292, 24);
		contentPane.add(progressBar);

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(0, 40, 312, 315);
		try {
			ImageIcon imageIcon = new ImageIcon(this.getClass().getClassLoader().getResource("res/imgres/boot.gif"));
			lblNewLabel.setIcon(imageIcon);
			imageIcon.setImageObserver(lblNewLabel);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		contentPane.add(lblNewLabel);

		status = new JLabel("Starting Gemini");
		status.setHorizontalAlignment(SwingConstants.CENTER);
		status.setBounds(10, 11, 292, 18);
		contentPane.add(status);

		setVisible(true);
	}

	public void out(String str) {
		status.setText(str);
	}

	public void setProgress(int progress) {
		progressBar.setValue(progress);
	}
}
