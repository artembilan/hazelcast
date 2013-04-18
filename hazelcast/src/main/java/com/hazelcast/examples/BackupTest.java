package com.hazelcast.examples;

import com.hazelcast.config.Config;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.monitor.LocalMapStats;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupTest {

    static final int SIZE;

    static {
        int s = 2048;
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
                    final LocalMapStats st = map.getLocalMapStats();
                    System.out.println(localMember + " -> SIZE: " + map.size()
                            + ", Owned: " + st.getOwnedEntryCount() + ", Backup: " + st.getBackupEntryCount());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();

        final int nThreads = 100;
        final String uuid = localMember.getUuid();
        ExecutorService ex = Executors.newFixedThreadPool(nThreads);
        final int entries = 10000000;
        final CountDownLatch latch = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            final int finalI = i;
            ex.execute(new Runnable() {
                public void run() {
                    final int total = entries / nThreads / 20;
                    for (int j = 0; j < total; j++) {
                        map.put(uuid + finalI + "-" + j, new byte[SIZE]);
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
