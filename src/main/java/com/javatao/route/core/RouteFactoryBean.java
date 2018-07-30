package com.javatao.route.core;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

/**
 * 路由代理工厂类
 * 
 * @author tao
 */
public class RouteFactoryBean<T> implements FactoryBean<T> {
    private Class<?> interfaces;
    private Class<?> instanceClass;
    private String instanceName;

    @Override
    public T getObject() throws Exception {
        return newInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return interfaces;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setInterfaces(Class<?> interfaces) {
        this.interfaces = interfaces;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setInstanceClass(Class<?> instanceClass) {
        this.instanceClass = instanceClass;
    }

    public Class<?> getInterfaces() {
        return interfaces;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Class<?> getInstanceClass() {
        return instanceClass;
    }

    @SuppressWarnings("unchecked")
    private T newInstance() {
        RouteProxy routeProxy = new RouteProxy(interfaces, instanceName, instanceClass);
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { interfaces }, routeProxy);
    }

    @Override
    public String toString() {
        String print = instanceName;
        if (instanceClass != null) {
            print = instanceClass.getName();
        } else if (interfaces != null) {
            print = interfaces.getName();
        }
        return print;
    }

    @Override
    public boolean equals(Object obj) {
        String name = this.getInstanceName();
        String name2 = ((RouteFactoryBean<?>) obj).getInstanceName();
        return name.equals(name2);
    }
}
