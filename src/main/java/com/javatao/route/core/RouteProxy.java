package com.javatao.route.core;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.javatao.route.annotations.RouteService;
import com.javatao.route.support.IRouteType;
import com.javatao.route.support.RouteException;
import com.javatao.route.support.SpringUtils;

/**
 * 路由代理类
 * 
 * @author tao
 */
public class RouteProxy implements InvocationHandler, Serializable, MethodInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(RouteProxy.class);
    private static final long serialVersionUID = 1L;
    private Class<?> interfaces;
    private String instance;

    public RouteProxy(Class<?> interfaces, String instance, Class<?> instanceClass) {
        this.interfaces = interfaces;
        this.instance = instance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if("toString".equals(name)){
            return method.invoke(this, args);
        }
        logger.info("Invoke [{}][{}][{}]", new Object[] { interfaces, instance, method.getName() });
        Map<String, ?> allBean = SpringUtils.getAllBean(interfaces);
        if (allBean == null) {
            return method.invoke(this, args);
        }
        logger.info("Container Bean [{}]", allBean.keySet());
        Object instance = allBean.values().iterator().next();
        RouteService route = instance.getClass().getAnnotation(RouteService.class);
        if (route == null) {
            if (instance.getClass().isInterface()) {
                logger.warn("invoke iface [{}] not instance", interfaces);
                return null;
            }
            return method.invoke(instance, args);
        }
        String plat = route.value();
        if (StringUtils.isBlank(plat)) {
            logger.info("DEFAULT [{}]", instance);
            return method.invoke(instance, args);
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof IRouteType) {
                    String type = ((IRouteType) arg)._getType();
                    if (type == null) {
                        throw new RouteException("RouteType is not set");
                    }
                    String ifaceName = interfaces.getSimpleName();
                    // 接口名字
                    ifaceName = SpringUtils.changeBeanName(ifaceName, type);
                    // 接口实例Bean名字
                    // String ins = ifaceName + "-" + type;
                    Object o = allBean.get(ifaceName);
                    if (o == null) {
                        // 重新从容器获取
                        o = SpringUtils.getBean(ifaceName);
                    }
                    if (o != null) {
                        instance = o;
                    } else {
                        throw new RouteException("RouteType type :" + type + " service not fond");
                    }
                }
            }
        }
        logger.info("return [{}]", instance);
        return method.invoke(instance, args);
    }

    @Override
    public String toString() {
        String print = instance;
        if (interfaces != null) {
            print = interfaces.getName();
        }
        return print;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RouteProxy) {
            String name = this.instance;
            String name2 = ((RouteProxy) obj).instance;
            return name.equals(name2);
        } else {
            return false;
        }
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] arg, MethodProxy argx) throws Throwable {
        return this.invoke(proxy, method, arg);
    }
}
