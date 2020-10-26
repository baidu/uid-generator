package com.baidu.fsg.uid;

import java.util.Objects;

import com.baidu.fsg.uid.buffer.RejectedPutBufferHandler;
import com.baidu.fsg.uid.buffer.RejectedTakeBufferHandler;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * uid autoConfiguration,if you want to use uid with springboot should set the property uid.enable = tre,you will enable default uid.
 * @author wangjianqiang24
 * @date 2020/10/22
 */
@Configuration
@ConditionalOnProperty(name = "uid.enable",havingValue = "true")
@EnableConfigurationProperties(UidProperties.class)
public class UidAutoConfiguraition implements ApplicationContextAware {

	@Bean
	public DisposableWorkerIdAssigner disposableWorkerIdAssigner(){
		return new DisposableWorkerIdAssigner();
	}


	@Bean
	@ConditionalOnProperty(name = "uid.cached.enable",havingValue = "true")
	@ConfigurationProperties(prefix = "uid.cached")
	public UidProperties cachedUidProperties(){
		return new UidProperties();
	}


	@Bean
	@ConditionalOnProperty(name = "uid.cached.enable",havingValue = "true")
	public UidGenerator cachedUidGenerator(UidProperties cachedUidProperties, ObjectProvider<RejectedTakeBufferHandler> rejectedTakeBufferHandlers,ObjectProvider<RejectedPutBufferHandler> rejectedPutBufferHandlers){
		CachedUidGenerator uidGenerator = new CachedUidGenerator();
		uidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner());
		uidGenerator.setEpochStr(cachedUidProperties.getEpochStr());
		uidGenerator.setSeqBits(cachedUidProperties.getSeqBits());
		uidGenerator.setTimeBits(cachedUidProperties.getTimeBits());
		uidGenerator.setWorkerBits(cachedUidProperties.getWorkerBits());
		uidGenerator.setBoostPower(cachedUidProperties.getBoostPower());
		if (rejectedTakeBufferHandlers.getIfAvailable() != null)
			uidGenerator.setRejectedTakeBufferHandler(rejectedTakeBufferHandlers.getIfAvailable());
		if (rejectedPutBufferHandlers.getIfAvailable() != null)
			uidGenerator.setRejectedPutBufferHandler(rejectedPutBufferHandlers.getIfAvailable());
		if (Objects.nonNull(cachedUidProperties.getScheduleInterval()))
			uidGenerator.setScheduleInterval(cachedUidProperties.getScheduleInterval());
		return uidGenerator;
	}


	@Bean
	public UidProperties uidProperties(){
		return new UidProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public UidGenerator defaultUidGenerator(UidProperties uidProperties){
		DefaultUidGenerator uidGenerator = new DefaultUidGenerator();
		uidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner());
		uidGenerator.setEpochStr(uidProperties.getEpochStr());
		uidGenerator.setSeqBits(uidProperties.getSeqBits());
		uidGenerator.setTimeBits(uidProperties.getTimeBits());
		uidGenerator.setWorkerBits(uidProperties.getWorkerBits());
		return uidGenerator;
	}



	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
