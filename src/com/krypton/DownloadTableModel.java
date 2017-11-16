package com.krypton;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by krypton on 3/18/2017.
 */
public class DownloadTableModel extends AbstractTableModel implements Observer {

    private static final String[] columnNames={"URL","Size","Downloaded","Progress","Status"};
    private static final Class[] columnClass={String.class,String.class,String.class, JProgressBar.class,String.class};
    private ArrayList<Download> downloadList=new ArrayList<Download>();
    public void addDownload(Download download){
        //TODO download.addObserver(this)
        download.addObserver(this);
        downloadList.add(download);
        fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
    }
    public Download getDownload(int row){
        return downloadList.get(row);
    }
    public void clearDownload(int row){
        downloadList.remove(row);
        fireTableRowsDeleted(row,row);
    }
    @Override
    public int getRowCount() {
        return downloadList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    public String getcolumnName(int col){
        return columnNames[col];
    }

    public Class<?> getColumnClass(int col) {
        return columnClass[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        //TODO Change Main
        Download download=downloadList.get(rowIndex);
        switch (columnIndex){
            case 0:
                return download.getUrl();
            case 1:
                return download.getLength();
            case 2:
                return download.getDownloaded();
            case 3:
                return download.getProgress();
            case 4:
                return Download.STATUUSES[download.getStatus()];
        }
        return "";
    }

    @Override
    public void update(Observable o, Object arg) {
        int index=downloadList.indexOf(o);
        fireTableRowsUpdated(index,index);
    }
}
