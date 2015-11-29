package org.cortex.Radiator.gui.render;

import javax.swing.table.DefaultTableModel;

public class TableModelDL extends DefaultTableModel {

	private static final long serialVersionUID = 6313819348170740098L;

	public boolean isCellEditable(int rowIndex,
            int columnIndex) {
		return false;
	}
}