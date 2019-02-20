/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.wujun234.uid.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.wujun234.uid.BitsAllocator;
import com.github.wujun234.uid.UidGenerator;
import com.github.wujun234.uid.exception.UidGenerateException;
import com.github.wujun234.uid.impl.UidProperties;
import com.github.wujun234.uid.utils.DateUtils;
import com.github.wujun234.uid.worker.WorkerIdAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Represents an implementation of {@link UidGenerator}
 * <p>
 * The unique id has 64bits (long), default allocated as blow:<br>
 * <li>sign: The highest bit is 0
 * <li>delta seconds: The next 28 bits, represents delta seconds since a customer epoch(2016-05-20 00:00:00.000).
 * Supports about 8.7 years until to 2024-11-20 21:24:16
 * <li>worker id: The next 22 bits, represents the worker's id which assigns based on database, max id is about 420W
 * <li>sequence: The next 13 bits, represents a sequence within the same second, max for 8192/s<br><br>
 * <p>
 * The {@link DefaultUidGenerator#parseUID(long)} is a tool method to parse the bits
 * <p>
 * <pre>{@code
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 *   1bit          28bits              22bits         13bits
 * }</pre>
 * <p>
 * You can also specified the bits by Spring property setting.
 * <li>timeBits: default as 28
 * <li>workerBits: default as 22
 * <li>seqBits: default as 13
 * <li>epochStr: Epoch date string format 'yyyy-MM-dd'. Default as '2016-05-20'<p>
 * <p>
 * <b>Note that:</b> The total bits must be 64 -1
 *
 * @author yutianbao
 * @author wujun
 */
public class DefaultUidGenerator implements UidGenerator, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUidGenerator.class);

    protected UidProperties uidProperties;

    /**
     * Bit分配器,Stable fields after spring bean initializing
     */
    protected BitsAllocator bitsAllocator;
    protected long workerId;

    /**
     * Volatile fields caused by nextId()
     */
    protected long sequence = 0L;
    protected long lastSecond = -1L;

    /**
     * Spring property
     */
    @Autowired
    protected WorkerIdAssigner workerIdAssigner;

    public DefaultUidGenerator(UidProperties uidProperties) {
        this.uidProperties = uidProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // initialize bits allocator
        bitsAllocator = new BitsAllocator(uidProperties.getTimeBits(), uidProperties.getWorkerBits(), uidProperties.getSeqBits());

        // initialize worker id
        workerId = workerIdAssigner.assignWorkerId();
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            LOGGER.error("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
            workerId = workerId % bitsAllocator.getMaxWorkerId();
            LOGGER.info("new Worker id = " + workerId);
        }

        LOGGER.info("Initialized bits(1, {}, {}, {}) for workerID:{}", uidProperties.getTimeBits(), uidProperties.getWorkerBits(), uidProperties.getSeqBits(), workerId);
    }

    @Override
    public long getUID() throws UidGenerateException {
        try {
            return nextId();
        } catch (Exception e) {
            LOGGER.error("Generate unique id exception. ", e);
            throw new UidGenerateException(e);
        }
    }

    @Override
    public String parseUID(long uid) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long workerIdBits = bitsAllocator.getWorkerIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();

        // parse UID
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long workerId = (uid << (totalBits - workerIdBits - sequenceBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

        Date thatTime = new Date(TimeUnit.SECONDS.toMillis(uidProperties.getEpochSeconds() + deltaSeconds));
        String thatTimeStr = DateUtils.formatByDateTimePattern(thatTime);

        // format as string
        return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                uid, thatTimeStr, workerId, sequence);
    }

    /**
     * Get UID
     *
     * @return UID
     * @throws UidGenerateException in the case: Clock moved backwards; Exceeds the max timestamp
     */
    protected synchronized long nextId() {
        long currentSecond = getCurrentSecond();

        // Clock moved backwards, refuse to generate uid
        if (currentSecond < lastSecond) {
            long refusedSeconds = lastSecond - currentSecond;
            if (uidProperties.isEnableBackward()) {
                if (refusedSeconds <= uidProperties.getMaxBackwardSeconds()) {
                    LOGGER.error("Clock moved backwards. wait for %d seconds", refusedSeconds);
                    while (currentSecond < lastSecond) {
                        currentSecond = getCurrentSecond();
                    }
                } else {
                    workerId = workerIdAssigner.assignFakeWorkerId();
                    LOGGER.error("Clock moved backwards. Assign New WorkerId %d", workerId);
                    if (workerId > bitsAllocator.getMaxWorkerId()) {
                        LOGGER.error("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
                        workerId = workerId % bitsAllocator.getMaxWorkerId();
                        LOGGER.info("new Worker id = " + workerId);
                    }
                }
            } else {
                throw new UidGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
            }
        }

        // At the same second, increase sequence
        if (currentSecond == lastSecond) {
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // Exceed the max sequence, we wait the next second to generate uid
            if (sequence == 0) {
                currentSecond = getNextSecond(lastSecond);
            }

            // At the different second, sequence restart from zero
        } else {
            sequence = 0L;
        }

        lastSecond = currentSecond;

        // Allocate bits for UID
        return bitsAllocator.allocate(currentSecond - uidProperties.getEpochSeconds(), workerId, sequence);
    }

    /**
     * Get next second
     */
    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }

        return timestamp;
    }

    /**
     * Get current second
     */
    private long getCurrentSecond() {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - uidProperties.getEpochSeconds() > bitsAllocator.getMaxDeltaSeconds()) {
            throw new UidGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }

        return currentSecond;
    }

    /**
     * Setters for spring property
     */
    public void setWorkerIdAssigner(WorkerIdAssigner workerIdAssigner) {
        this.workerIdAssigner = workerIdAssigner;
    }
}
