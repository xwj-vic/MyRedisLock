package com.xwj;

import com.xwj.lock.Lock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=SpringConfig.class)
public class MainDemo {

    private int count=10;

    @Autowired
    @Qualifier("redisLock")
    private Lock lock;


    /**
     * 测试一
     */
    @Test
    public void testRedis(){
        Jedis jedis=new Jedis("192.168.204.188",6379);
        SetParams setParams=new SetParams();
        setParams.ex(6);  //setex     设置值的同时设置过期时间
        setParams.nx();  //
        String s = UUID.randomUUID().toString();
        String lock = jedis.set("lock", s,setParams);
//        Long setnx = jedis.setnx("lock", "value2");
//        if(setnx==1){
//            jedis.expire("lock",10);
//        }

        System.out.println(lock);
    }


    /**
     * 测试二
     * @throws InterruptedException
     */
    @Test
    public void Test() throws InterruptedException {
        TicketsRunBle ticketsRunBle=new TicketsRunBle();
        Thread thread1=new Thread(ticketsRunBle,"窗口1");
        Thread thread2=new Thread(ticketsRunBle,"窗口2");
        Thread thread3=new Thread(ticketsRunBle,"窗口3");
        Thread thread4=new Thread(ticketsRunBle,"窗口4");
        Thread thread5=new Thread(ticketsRunBle,"窗口5");
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        Thread.currentThread().join();
    }


    public class TicketsRunBle implements Runnable{

        @Override
        public void run() {
            while (count>0){
                lock.lock();
                try {
                    if(count>0){
//                        Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 3000));
                        System.out.println(Thread.currentThread().getName()+"售出第"+count--+"张票");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    try {
                        lock.unlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
