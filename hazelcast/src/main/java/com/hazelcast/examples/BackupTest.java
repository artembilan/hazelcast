package com.hazelcast.examples;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

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
        final HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);

        if (hz.getCluster().getMembers().iterator().next().localMember()) {
            final int nThreads = 100;
            ExecutorService ex = Executors.newFixedThreadPool(nThreads);
            final IMap<Object,Object> map = hz.getMap("test");
            final int entries = 10000000;
            final CountDownLatch latch = new CountDownLatch(nThreads);
            for (int i = 0; i < nThreads; i++) {
                final int finalI = i;
                ex.execute(new Runnable() {
                    public void run() {
                        for (int j = 0; j < entries / nThreads; j++) {
                            map.put(finalI + "k-" + j, new byte[SIZE]);
                        }
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }
    }
}
