package com.hazelcast.examples;

import com.hazelcast.config.Config;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupTest {

    static final int SIZE;

    static {
        int s = 1024;
        try {
             s = Integer.parseInt(System.getProperty("entry.size"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        SIZE = s;
    }

    public static void main(String[] args) throws Exception {
        final Config config = new XmlConfigBuilder().build();
        config.getPartitionGroupConfig().setEnabled(true).setGroupType(PartitionGroupConfig.MemberGroupType.HOST_AWARE);

        final HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        final IMap<Object,Object> map = hz.getMap("test");
        final Member localMember = hz.getCluster().getLocalMember();

        new Thread() {
            public void run() {
                while (true) {
                    System.out.println(localMember + " -> SIZE: " + map.size());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();

        final int nThreads = 100;
        ExecutorService ex = Executors.newFixedThreadPool(nThreads);
        final int entries = 10000000;
        final CountDownLatch latch = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            final int finalI = i;
            ex.execute(new Runnable() {
                public void run() {
                    final int total = entries / nThreads;
                    for (int j = 0; j < total; j++) {
                        map.put(finalI + "-" + j, new byte[SIZE]);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        ex.shutdown();
        System.err.println(localMember + " -> DONE");
    }
}
