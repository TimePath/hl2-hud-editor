/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.timepath.tf2.hudedit.swing;

import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author andrew
 */
public class EditorPropertiesTable extends JTable {
    private static final long serialVersionUID = 1L;
    EditorPropertiesTablePane outer;

    public EditorPropertiesTable(EditorPropertiesTablePane outer) {
        this.outer = outer;
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Key");
        model.addColumn("Value");
        model.addColumn("Info");
        model.insertRow(0, new String[]{"", "", ""});
        this.setModel(model);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(true);
        this.getTableHeader().setReorderingAllowed(false);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0; // deny editing of key
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return super.getCellRenderer(row, column);
    }
    private static final Logger LOG = Logger.getLogger(EditorPropertiesTable.class.getName());
    
}
