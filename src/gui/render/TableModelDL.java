package gui.render;

import javax.swing.table.DefaultTableModel;

/**
 * Custom table model to prevent editing
 * @author Kevin Cai
 */
public class TableModelDL extends DefaultTableModel {

	private static final long serialVersionUID = 6313819348170740098L;

	public boolean isCellEditable(int rowIndex,
            int columnIndex) {
		return false;
	}
}