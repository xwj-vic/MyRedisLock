package com.xwj.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;


@Component
public class RedisLock implements Lock {


    @Autowired
    private JedisPool jedisPool;

    private static final String key="lock";

    private ThreadLocal<String> threadLocal=new ThreadLocal<>();

    private static AtomicBoolean isHappened = new AtomicBoolean(true);

    //加锁
    @Override
    public void lock() {
        boolean b = tryLock();  //尝试加锁
        if(b){
            //拿到了锁
            return;
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    //尝试加锁
    @Override
    public boolean tryLock() {
        SetParams setParams=new SetParams();
        setParams.ex(2);  //2s 设置过期时间
        setParams.nx();
        String s = UUID.randomUUID().toString();
        Jedis resource = jedisPool.getResource();
        String lock = resource.set(key, s, setParams);
        resource.close();
        if("OK".equals(lock)){
            //拿到了锁
            threadLocal.set(s); //记录当前值
            if(isHappened.get()){ //开启守护线程给锁续期
                ThreadUtil.newThread(new MyRUnble(jedisPool)).start();
                isHappened.set(false);
            }
            return true;
        }
        return false;
    }



    static class MyRUnble implements Runnable{

        private JedisPool jedisPool;
        public MyRUnble(JedisPool jedisPool){
            this.jedisPool=jedisPool;
        }
        @Override
        public void run() {
            Jedis jedis = jedisPool.getResource();
            while (true){
                Long ttl = jedis.ttl(key);
                if(ttl!=null && ttl>0){
                    jedis.expire(key, (int) (ttl+1));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }


    /**
     * 解锁
     * 第一步判断设置时候的value 和 此时redis的value是否相同
     * 执行lua脚本
     * @throws Exception
     */

    @Override
    public void unlock() throws Exception{
        String script="if redis.call(\"get\",KEYS[1])==ARGV[1] then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";

        Jedis resource = jedisPool.getResource();
        Object eval = resource.eval(script, Arrays.asList(key), Arrays.asList(threadLocal.get()));
        if(Integer.parseInt(eval.toString())==0){
            resource.close();
            throw new Exception("解锁失败");
        }
        resource.close();
    }

    @Override
    public Condition newCondition() {
        return null;
    }

}
