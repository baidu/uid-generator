package com.baidu.fsg.uid;

import com.baidu.fsg.uid.buffer.RejectedTakeBufferHandler;
import com.baidu.fsg.uid.buffer.RingBuffer;

/**
 *
 * @author wangjianqiang24
 * @date 2020/10/23
 */
public class RejectHandler implements RejectedTakeBufferHandler {
	/**
	 * Reject take buffer request
	 *
	 * @param ringBuffer
	 */
	@Override
	public void rejectTakeBuffer(RingBuffer ringBuffer) {

	}
}
