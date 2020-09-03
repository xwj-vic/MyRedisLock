package com.xwj.lock;

public class ThreadUtil {

    public static Thread thread;

    public static Thread newThread(Runnable runnable){
        if (thread==null){
            synchronized (ThreadUtil.class){
                if(thread==null){
                    thread = new Thread(runnable);
                    thread.setDaemon(true);
                }
                return thread;
            }
        }
        return thread;
    }

}
