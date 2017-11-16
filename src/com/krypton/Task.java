package com.krypton;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by krypton on 3/17/2017.
 */
public class Task {

    private static final int BUFFER_SIZE =1024 ;
    private Part part;
    private ErrorListener listener;
    private boolean pause;
    private boolean taskfinished;

    public Task(Part p) {
        this.part=p;
    }
    public void addErrorListener(Pool worker){
        listener=worker;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName()+" taking "+part.getId());
        if(part.getStatus().equals(Part.states.QUEUED)){
            downlaodPart(part);
        }

    }
        private void downlaodPart(Part part) {
//          part.checkForDownloadedPart();
            RandomAccessFile ras = null;
            BufferedInputStream bis = null;
            HttpURLConnection conn = null;
            try {
                long start = part.getStartbyte() + part.getDownloaded();
                long end = part.getEndbyte();

                if (end - start + 1 == 0) return;

                conn = (HttpURLConnection) new URL(part.getUrl()).openConnection();
                conn.setReadTimeout(3000);
                conn.setConnectTimeout(3000);
                File f = new File(part.getActualFileName());
                ras = new RandomAccessFile(f, "rw");
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                conn.connect();
                ras.seek(start);
                byte[] buffer = new byte[BUFFER_SIZE];
                int len = 0;
                bis = new BufferedInputStream(conn.getInputStream());
                boolean threadFlag = false;

                while (!isPause() && (len = bis.read(buffer)) > 0)  {

                    long partEnd = (part.getEndbyte() - part.getStartbyte() + 1) - part.getDownloaded();
                    if (len > partEnd) {
                        len = (int) partEnd;
                        threadFlag = true;
                    }
                    ras.write(buffer, 0, len);
                    part.setDownloaded(part.getDownloaded() + len);
                    listener.calculate(len);
                    if (threadFlag) return;

                }
                if(!isPause()){
                    part.setStatus(Part.states.DONE);
                    System.out.println("Part download finish part id: "+part.getId());
                    setTaskfinished(true);
                }

            } catch (IOException e) {
                e.printStackTrace();
//                part.setStatus(Part.states.QUEUED);
//                listener.reSubmitPart(part);
            } finally{
                try {
                    if(bis!=null) {
                        bis.close();
                    }
                    if (ras != null) {
                        ras.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    public boolean isPause() {
//        if(pause==true){
//            part.setStatus(Part.states.QUEUED);
//           //listener.reSubmitPart(part);
//        }
        return pause;
    }

    public void setPause(boolean pause) {

        this.pause = pause;
    }

    public boolean isTaskfinished() {
        return taskfinished;
    }

    public void setTaskfinished(boolean taskfinished) {
        this.taskfinished = taskfinished;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public interface ErrorListener{
        void reSubmitPart(Part part);
        void calculate(long downloaded);
        }
    }

