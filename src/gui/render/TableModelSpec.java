package gui.render;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class TableModelSpec extends DefaultTableModel {

	private static final long serialVersionUID = 691110491809914387L;

	public boolean isCellEditable(int rowIndex,
            int columnIndex) {
		return false;
	}
	
	public Vector<?> getColumnIdentifiers() {
        return columnIdentifiers;
    }
}