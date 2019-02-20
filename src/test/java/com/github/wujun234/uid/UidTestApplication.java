package com.github.wujun234.uid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * UID 测试主类
 *
 * @author wujun
 * @date 2019.02.20 11:01
 */
@SpringBootApplication
public class UidTestApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(UidTestApplication.class, args);
	}
	
//    @Bean
//    @ConditionalOnMissingBean
//    WorkerIdAssigner workerIdAssigner() {
//        return new DisposableWorkerIdAssigner();
//    }
}
