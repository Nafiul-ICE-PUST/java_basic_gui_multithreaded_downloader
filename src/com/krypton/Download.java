package com.krypton;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Observable;

/**
 * Created by krypton on 3/18/2017.
 */
public class Download extends Observable implements Pool.UpdateListener{
    public static String STATUUSES[]={"Dwonloading","Paused","Complete","Canceled","Error","Getting info.."};

    public static final int DOWNLOADING=0;
    public static final int PAUSED=1;
    public static final int COMPLTE=2;
    public static final int CANCELED=3;
    public static final int ERROR=4;
    public static final int GETTINGINFO=5;
    private long downloaded;
    private int status;
    private HttpURLConnection conn;
    private URL url;
    private String FileName;
    private long Length=1;
    private Pool threadpool;
    private String savedir;

    public Download(String text,String dir) {

            setStatus(5);
            stateChanged();
            this.savedir=dir;

            threadpool=new Pool(5);
            threadpool.setUlistener(this);
            File f=new File(savedir);
            if(!f.exists()){
                f.mkdir();
            }
            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        url=new URL(text);
                        conn = (HttpURLConnection) url.openConnection();
                        extractRange(); //Filerange extraction method required for multithreading
                        calculateFileName(); //Filename handling
                        multiPart(getLength()/10); //spilting file into smaller chuncks
                        threadpool.start();
                        setStatus(0);
                        stateChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        t.start();
        t=null;
        stateChanged();
    }

    private void extractRange() throws IOException {
        conn.setRequestProperty("Range", "bytes=" +0+"-"+0);
        String range = conn.getHeaderField("Content-Range");
        if (range == null)
            throw new RuntimeException("range not supported");
        if(conn.getResponseCode()==206){
            System.out.println(range);
            String[] pattern=range.split("/");
            setLength(Long.parseLong(pattern[1]));
        }
    }
    private void multiPart(long partsize){
        long size=getLength();
        long count=size / partsize +1;
        if(count >2){
//            parts=new ArrayList<Part>();
            long start=0;
            for (int i=0;i<count;i++){
                Part p=new Part();
                p.setUrl(url.toString());
                p.setId(i);
//                p.setTmpFileName(tmpdir+File.separator+getFileName()+"_"+i);
                p.setActualFileName(savedir+ File.separator+getFileName());
                p.setStartbyte(start);
                p.setEndbyte(start+partsize - 1);
                if(p.getEndbyte() > size -1 ){
                    p.setEndbyte(size-1);
                }
                p.setStatus(Part.states.QUEUED);
                threadpool.submit(new Task(p));
                start +=partsize;
            }
        }
    }
    private void calculateFileName(){

        //http://172.27.27.124/s1d1/today/index.php?dir=&file=Resident.Evil.The.Final.Chapter.2017.1080p.WEB-DL.6CH.ShAaNiG.mkv
        //http://mirror.dhakacom.com/ubuntu-releases/17.10/ubuntu-17.10-desktop-amd64.iso
        String u=url.toString();
        String readableURL=null;
        try {
            readableURL= URLDecoder.decode(u,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] pattern=readableURL.split("/");
        if(pattern[(pattern.length-1)].contains("file=")){
            String s=pattern[(pattern.length-1)];
            s=s.substring(s.indexOf("file=")+5);
            setFileName(s);
        }else {
            String s=pattern[(pattern.length-1)];
            setFileName(s);
        }
        setFileName(pattern[pattern.length-1]);
        System.out.println(FileName);
    }
    private void stateChanged(){
        setChanged();
        notifyObservers();

    }


    public float getProgress() {

        return ((float) threadpool.getDownloaded()/getLength())*100;

    }

    public void cancel() {
        threadpool.stop();
        setStatus(3);
        stateChanged();
    }

    public void resume() {
        threadpool.resume();
        setStatus(0);
        stateChanged();
    }

    public void pause() {
        threadpool.pause();
        setStatus(1);
        stateChanged();
    }

    public long getDownloaded() {
        return threadpool.getDownloaded();
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public long getLength() {
        return Length;
    }

    public void setLength(long length) {
        Length = length;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    @Override
    public void updater() {
        stateChanged();
    }
    public void updater(int i)
    {
        setStatus(i);
        stateChanged();
    }
}
