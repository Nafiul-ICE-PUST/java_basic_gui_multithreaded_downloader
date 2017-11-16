package com.krypton;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by krypton on 3/18/2017.
 */
public class DownloadManager extends JFrame implements Observer {
    private JTextField addTextField;
    private DownloadTableModel tableModel;
    private JTable table;
    private JButton pauseButton,resumeButton,cancelButton,clearButton;
    //TODO change Main
    private Download selectedDownload;
    private boolean clearing;
    JTextField saveTextField;

    public DownloadManager(String title) {
        super(title);
        setSize(640,480);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });
        JMenuBar menuBar=new JMenuBar();
        JMenu fileMenu=new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuIteam=new JMenuItem("Exit",KeyEvent.VK_X);
        fileExitMenuIteam.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuIteam);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);


        JPanel topPanel=new JPanel();
        topPanel.setLayout(new BorderLayout());
        JPanel addPanel=new JPanel();
        JTextArea urlText=new JTextArea("Paste URL");
        urlText.setEditable(false);
        addPanel.add(urlText);
        addTextField=new JTextField(30);
        addPanel.add(addTextField);
        JButton addDownlaodButton=new JButton("Add Download");
        addDownlaodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionAdd();
            }
        });
        addPanel.add(addDownlaodButton);


        JPanel savePanel=new JPanel();

        JTextArea saveText=new JTextArea("Save DIR");
        urlText.setEditable(false);

        saveTextField=new JTextField(30);

        JButton browseButton=new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jc=new JFileChooser();
                jc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jc.setAcceptAllFileFilterUsed(false);
                int returnVal=jc.showDialog(DownloadManager.this,"save");
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " + jc.getSelectedFile().getPath());
                    saveTextField.setText(jc.getSelectedFile().getPath());
                }
               // selectedDownload.savedir=file.getPath();
            }
        });

        savePanel.add(saveText);
        savePanel.add(saveTextField);
        savePanel.add(browseButton);

        topPanel.add(addPanel,BorderLayout.NORTH);
        topPanel.add(savePanel,BorderLayout.SOUTH);


        tableModel=new DownloadTableModel();
        table=new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                tableSelectionChanged();
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ProgressRenderer renderer=new ProgressRenderer(0,100);
        renderer.setStringPainted(true);
        table.setDefaultRenderer(JProgressBar.class,renderer);
        table.setRowHeight((int) renderer.getPreferredSize().getHeight());

        JPanel downloasdsPanel=new JPanel();
        downloasdsPanel.setBorder(BorderFactory.createTitledBorder("Downlaods"));
        downloasdsPanel.setLayout(new BorderLayout());
        downloasdsPanel.add(new JScrollPane(table),BorderLayout.CENTER);

        JPanel buttonsPanel=new JPanel();
        pauseButton=new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionPause();
            }
        });
        pauseButton.setEnabled(false);
        buttonsPanel.add(pauseButton);
        resumeButton =new JButton("Resume");
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionResume();
            }
        });
        resumeButton.setEnabled(false);
        buttonsPanel.add(resumeButton);
        cancelButton =new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });
        cancelButton.setEnabled(false);
        buttonsPanel.add(cancelButton);
        clearButton=new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionClear();
            }
        });
        clearButton.setEnabled(false);
        buttonsPanel.add(clearButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel,BorderLayout.NORTH);
        getContentPane().add(downloasdsPanel,BorderLayout.CENTER);
        getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
    }

    private void actionAdd() {
        //TODO Add Download
        if(saveTextField.getText().equals("")){
            JOptionPane.showMessageDialog(this,"Plz select a download DIR","ERROR",JOptionPane.ERROR_MESSAGE);
            return;
        }else {
            tableModel.addDownload(new Download(addTextField.getText(),saveTextField.getText()));
            addTextField.setText("");
        }

    }

    private void actionExit() {
        System.exit(0);
    }

    private void actionClear() {
        clearing=true;
        tableModel.clearDownload(table.getSelectedRow());
        clearing=false;
        selectedDownload=null;
        updateButtons();
    }

    private void actionCancel() {
        selectedDownload.cancel();
        updateButtons();
    }

    private void actionResume() {
        selectedDownload.resume();
        updateButtons();
    }

    private void actionPause() {
        selectedDownload.pause();
        updateButtons();
    }

    private void tableSelectionChanged() {
        if(selectedDownload !=null) {
            selectedDownload.deleteObserver(DownloadManager.this);
        }
            if(!clearing && table.getSelectedRow() > -1){
                selectedDownload=tableModel.getDownload(table.getSelectedRow());
                selectedDownload.addObserver(DownloadManager.this);
                updateButtons();
            }

    }

    private void updateButtons() {
        if(selectedDownload !=null){
            int status=selectedDownload.getStatus();
            switch (status){
                case Download.DOWNLOADING:
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                    case Download.PAUSED:
                        pauseButton.setEnabled(false);
                        resumeButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        clearButton.setEnabled(false);
                        break;
                        case Download.ERROR:
                            pauseButton.setEnabled(false);
                            resumeButton.setEnabled(true);
                            cancelButton.setEnabled(false);
                            clearButton.setEnabled(true);
                            break;
                            default:
                                pauseButton.setEnabled(false);
                                resumeButton.setEnabled(false);
                                cancelButton.setEnabled(false);
                                clearButton.setEnabled(true);
            }
        }else {
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            clearButton.setEnabled(false);
        }
    }



    @Override
    public void update(Observable o, Object arg) {
        if(selectedDownload!=null && selectedDownload.equals(o)){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateButtons();
                }
            });
        }
    }
}
