package org.remote.invocation.starter.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * 配置初始化事件
 *
 * @author liucheng
 * @create 2018-06-12 14:37
 **/
@Data
public class InvocationEvent extends ApplicationEvent {

    public InvocationEvent(Object source) {
        super(source);
    }

}
