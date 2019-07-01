package org.remote.invocation.starter.event;

import org.remote.invocation.starter.config.InvocationConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author liucheng
 * @create 2018-06-12 14:40
 **/
@Component
public class InvocationNotifier implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof InvocationEvent) {
            InvocationEvent event = (InvocationEvent) applicationEvent;
            ApplicationContext applicationContext = (ApplicationContext) event.getSource();
            applicationContext.getBean(InvocationConfig.class).init();
        }
    }
}
