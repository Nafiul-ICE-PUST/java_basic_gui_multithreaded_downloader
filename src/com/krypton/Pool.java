package com.krypton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by krypton on 3/17/2017.
 */
public class Pool implements Task.ErrorListener {
    private int threadnum;
    private List<Worker> workers;
    private LinkedBlockingQueue queue;
    private long downloaded=0;
    private UpdateListener ulistener;

    public Pool(int i) {
        workers=new ArrayList<Worker>();
        queue=new LinkedBlockingQueue();
        threadnum=i;
    }
    public int getactiveThread(){
        return workers.size();
    }
    public void submit(Task run){
        if (queue!=null) {
            queue.add(run);
        }
    }

    private void execute() {
        if(getactiveThread()<threadnum){
            int tnum=threadnum-getactiveThread();
            for (int i=0;i<tnum;i++){
                addworker();
            }
        }
    }

    public void start(){
        System.out.println("Starting pool total queue "+queue.size());
        if(!queue.isEmpty()){
            execute();
        }

    }

    private void addworker() {
        Worker w=new Worker();
        w.start();
        workers.add(w);
    }

    public void stop(){
//        Iterator<Worker> it=workers.iterator();
//        while (it.hasNext()){
//            it.next().setstop();
//            it.remove();
//        }
        for (Worker w:workers){
            w.setstop();
            w.getTask().setPause(true);
            w.setresume();
        }
        workers=null;
        queue=null;
     }

    public void pause(){
        for (Worker w:workers){
            w.setpause();
            w.getTask().setPause(true);
        }
    }
    public void resume(){
        for (Worker w:workers){
            w.setresume();
            w.getTask().setPause(false);

        }
    }

    @Override
    public synchronized void reSubmitPart(Part part) {
        submit(new Task(part));
        System.err.println("Submitting Part again");
    }

    @Override
    public synchronized void calculate(long download) {
        downloaded +=download;
        ulistener.updater();
//        System.out.println(downloaded);
    }


    class Worker extends Thread{

        private boolean stopFlag;
        private boolean pauseandresumeFlag;
        private Task t;

        public Worker() {
            stopFlag=false;
            pauseandresumeFlag=false;
        }

        @Override
        public void run() {
                while (!stopFlag) {
                    //System.out.println("Thread running "+Thread.currentThread().getName());
                    try {
                        if(t!=null){
                            if(t.isTaskfinished()){
                                t = (Task) queue.poll(15, TimeUnit.SECONDS);
                            }else {
                                System.out.println("RETRYING Task "+t.getPart().getId()+" in 5 sec");
                                Thread.sleep(5000);
                            }
                        }else {
                            t = (Task) queue.poll(15, TimeUnit.SECONDS);
                        }

                        if (t == null) {
                            System.out.println("Queue is null");
                            workers.remove(this);
                            setstop();
                        } else {
                            t.addErrorListener(Pool.this);
                            t.run();
                        }
                        // TODO work here
                        synchronized (this) {
                            while (pauseandresumeFlag) {
                                System.out.println(Thread.currentThread().getName() + " pausing");
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.err.println("Interrupted");
                    }
                }
                System.out.println(Thread.currentThread().getName() + " Stoped");
        }
        private synchronized void setstop(){
            stopFlag=true;
            if(getactiveThread()==0)
            {
                ulistener.updater(2);
                Pool.this.stop();
                System.out.println("Download finished");
            }
        }
        private synchronized void setpause(){
            pauseandresumeFlag=true;
        }
        private synchronized void setresume(){
            pauseandresumeFlag=false;
            notify();
        }

        public Task getTask() {
            return t;
        }

        public void setTask(Task t) {
            this.t = t;
        }
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setUlistener(UpdateListener ulistener) {
        this.ulistener = ulistener;
    }

    interface UpdateListener{
        void updater();
        void updater(int i);
    }
}
