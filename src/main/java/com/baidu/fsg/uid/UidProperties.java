package com.baidu.fsg.uid;

import java.util.concurrent.TimeUnit;

import com.baidu.fsg.uid.buffer.RingBuffer;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author wangjianqiang24
 * @date 2020/10/23
 */
@ConfigurationProperties(prefix = "uid")
public class UidProperties {

	private static final int DEFAULT_BOOST_POWER = 3;

	private int timeBits = 28;
	private int workerBits = 22;
	private int seqBits = 13;

	/** Customer epoch, unit as second. For example 2016-05-20 (ms: 1463673600000)*/
	private String epochStr = "2016-05-20";protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1463673600000L);

	private long workerId;

	/** Volatile fields caused by nextId() */
	private long sequence = 0L;
	private long lastSecond = -1L;

	//-------------------cache属性----------
	/** Spring properties */
	/**
	 *  RingBuffer size扩容参数, 可提高UID生成的吞吐量.
	 * 		 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
	 */
	private int boostPower = DEFAULT_BOOST_POWER;

	/**
	 * 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
	 * 		 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512.
	 * 		 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
	 */
	private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;

	/**
	 *  另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充
	 * 		 默认:不配置此项, 即不实用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
	 */
	private Long scheduleInterval;



	public int getTimeBits() {
		return timeBits;
	}

	public void setTimeBits(int timeBits) {
		this.timeBits = timeBits;
	}

	public int getWorkerBits() {
		return workerBits;
	}

	public void setWorkerBits(int workerBits) {
		this.workerBits = workerBits;
	}

	public int getSeqBits() {
		return seqBits;
	}

	public void setSeqBits(int seqBits) {
		this.seqBits = seqBits;
	}

	public String getEpochStr() {
		return epochStr;
	}

	public void setEpochStr(String epochStr) {
		this.epochStr = epochStr;
	}

	public long getEpochSeconds() {
		return epochSeconds;
	}

	public void setEpochSeconds(long epochSeconds) {
		this.epochSeconds = epochSeconds;
	}

	public long getWorkerId() {
		return workerId;
	}

	public void setWorkerId(long workerId) {
		this.workerId = workerId;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public long getLastSecond() {
		return lastSecond;
	}

	public void setLastSecond(long lastSecond) {
		this.lastSecond = lastSecond;
	}

	public int getBoostPower() {
		return boostPower;
	}

	public void setBoostPower(int boostPower) {
		this.boostPower = boostPower;
	}

	public int getPaddingFactor() {
		return paddingFactor;
	}

	public void setPaddingFactor(int paddingFactor) {
		this.paddingFactor = paddingFactor;
	}

	public Long getScheduleInterval() {
		return scheduleInterval;
	}

	public void setScheduleInterval(Long scheduleInterval) {
		this.scheduleInterval = scheduleInterval;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"timeBits\":")
				.append(timeBits);
		sb.append(",\"workerBits\":")
				.append(workerBits);
		sb.append(",\"seqBits\":")
				.append(seqBits);
		sb.append(",\"epochStr\":\"")
				.append(epochStr).append('\"');
		sb.append(",\"epochSeconds\":")
				.append(epochSeconds);
		sb.append(",\"workerId\":")
				.append(workerId);
		sb.append(",\"sequence\":")
				.append(sequence);
		sb.append(",\"lastSecond\":")
				.append(lastSecond);
		sb.append(",\"boostPower\":")
				.append(boostPower);
		sb.append(",\"paddingFactor\":")
				.append(paddingFactor);
		sb.append(",\"scheduleInterval\":")
				.append(scheduleInterval);
		sb.append("}");
		return sb.toString();
	}
}
