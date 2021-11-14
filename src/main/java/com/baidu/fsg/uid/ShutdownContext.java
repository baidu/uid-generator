package com.baidu.fsg.uid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * shut down the jvm instance
 *
 * @author gongxiaoyue
 */
@Component
public class ShutdownContext implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownContext.class);

    private ConfigurableApplicationContext context;

    /**
     * show down this ConfigurableApplicationContext
     */
    public void showdown() {
        if (null != context) {
            LOGGER.info("uid generator showdown!");
            context.close();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            this.context = (ConfigurableApplicationContext) applicationContext;
        }
    }
}
