package jp.ne.paypay.uid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jp.ne.paypay.uid.impl.DefaultUidGenerator;

/**
 * Test for {@link DefaultUidGenerator}
 *
 * @author yutianbao
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "classpath:uid/default-uid-spring.xml" })
public class DefaultUidGeneratorTest {
    private static final int SIZE = 100000; // 10w
    private static final boolean VERBOSE = true;
    private static final int THREADS = Runtime.getRuntime().availableProcessors() << 1;

    @Resource
    private UidGenerator uidGenerator;

    /**
     * Test for serially generate
     */
    @Test
    public void testSerialGenerate() {
        // Generate UID serially
        Set<Long> uidSet = new HashSet<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            doGenerate(uidSet, i);
        }

        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    /**
     * Test for parallel generate
     *
     * @throws InterruptedException
     */
    @Test
    public void testParallelGenerate() throws InterruptedException {
        AtomicInteger control = new AtomicInteger(-1);
        Set<Long> uidSet = new ConcurrentSkipListSet<>();

        // Initialize threads
        List<Thread> threadList = new ArrayList<>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(() -> workerRun(uidSet, control));
            thread.setName("UID-generator-" + i);

            threadList.add(thread);
            thread.start();
        }

        // Wait for worker done
        for (Thread thread : threadList) {
            thread.join();
        }

        // Check generate 10w times
        Assertions.assertEquals(SIZE, control.get());

        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    /**
     * Worker run
     */
    private void workerRun(Set<Long> uidSet, AtomicInteger control) {
        for (;;) {
            int myPosition = control.updateAndGet(old -> old == SIZE ? SIZE : old + 1);
            if (myPosition == SIZE) {
                return;
            }

            doGenerate(uidSet, myPosition);
        }
    }

    /**
     * Do generating
     */
    private void doGenerate(Set<Long> uidSet, int index) {
        long uid = uidGenerator.getUID();
        String parsedInfo = uidGenerator.parseUID(uid);
        uidSet.add(uid);

        // Check UID is positive, and can be parsed
        Assertions.assertTrue(uid > 0L);
        Assertions.assertTrue(StringUtils.isNotBlank(parsedInfo));

        if (VERBOSE) {
//            System.out.println(Thread.currentThread().getName() + " No." + index + " >>> " + parsedInfo);
        }
    }

    /**
     * Check UIDs are all unique
     */
    private void checkUniqueID(Set<Long> uidSet) {
        System.out.println(uidSet.size());
        Assertions.assertEquals(SIZE, uidSet.size());
    }

}
