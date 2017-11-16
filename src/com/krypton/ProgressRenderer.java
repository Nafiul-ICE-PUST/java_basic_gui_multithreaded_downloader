package com.krypton;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by krypton on 3/18/2017.
 */
public class ProgressRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressRenderer(int min, int max) {
        super(min, max);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setValue((int)((Float) value).floatValue());
        return this;
    }
}
