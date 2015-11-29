package org.cortex.Radiator.gui.render;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressCellRenderer extends JProgressBar
implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public ProgressCellRenderer(){
		super(0, 100);
		setValue(0);
		setString("0%");
		setStringPainted(true);
	}

	public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {

		final String sValue = value.toString();
		int index = sValue.indexOf('%');
		if(index != -1) {
			int p = 0;
			try{
				p = Integer.parseInt(sValue.substring(0, index));
			}
			catch(NumberFormatException e){
			}
			setValue(p);
			setString(sValue);
		}
		return this;
	}
}