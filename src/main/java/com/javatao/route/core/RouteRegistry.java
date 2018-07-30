package com.javatao.route.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.javatao.route.annotations.RouteService;
import com.javatao.route.support.SpringUtils;

/**
 * 路由代理类注册
 * 
 * @author tao
 */
public class RouteRegistry implements BeanDefinitionRegistryPostProcessor {
    private String basePackage = "";

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtils.factory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
            for (String pkg : basePackage.split(",")) {
                scanner.registryBeans(pkg);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册扫描类
     */
    private class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {
        private BeanDefinitionRegistry registry;
        private String basePackage;
        private Set<String> allInterfaces = new HashSet<>();

        public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
            this.registry = registry;
            addIncludeFilter(new AnnotationTypeFilter(RouteService.class));
        }

        public void registryBeans(String basePackage) throws ClassNotFoundException {
            this.basePackage = basePackage;
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            if (candidates.isEmpty()) {
                logger.warn("No  RouteFactoryBean was found in '" + basePackage + "' package. Please check your configuration.");
            }
            for (BeanDefinition beanDefinition : candidates) {
                registryBean(beanDefinition);
            }
            registryIface();
        }

        private void registryBean(BeanDefinition holder) throws ClassNotFoundException {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder;
            String beanClassName = definition.getBeanClassName();
            Class<?> classz = Class.forName(beanClassName);
            // 获取所有接口
            Class<?>[] interfaces = getAllInterfaces(classz);
            RouteService annotation = classz.getAnnotation(RouteService.class);
            String value = annotation.value();
            String beanName = annotation.name();
            for (int i = 0; i < interfaces.length; i++) {
                Class<?> iface = interfaces[i];
                String ifcName = iface.getName();
                // 排除其他接口
                if (!ifcName.contains(basePackage)) {
                    continue;
                }
                definition.setBeanClass(classz);
                String ifaceName = iface.getSimpleName();
                beanName = SpringUtils.changeBeanName(ifaceName, beanName, value);
                if (StringUtils.isNotBlank(value)) {
                    allInterfaces.add(iface.getName());
                    definition.setLazyInit(false);
                    definition.setAutowireCandidate(false);
                }
                if (i > 0) {
                    beanName = beanName + "_" + i;
                    logger.warn(iface.getName() + " name " + beanName);
                }
                // 判断是不是存在的名字
                boolean hasBean = registry.containsBeanDefinition(beanName);
                if (hasBean) {
                    BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                    String exitName = beanDefinition.getBeanClassName();
                    String className = classz.getName();
                    // 如果已注册的bean报名长度大于现在的长度跳过(子包长度小于父包)
                    if (exitName.length() > className.length()) {
                        logger.warn("skip register [" + className + "] exists [" + exitName + "] ");
                        continue;
                    } else {
                        logger.warn("register [" + className + "] Override [" + exitName + "] ");
                    }
                }
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                logger.info("Creating RouteFactoryBean  name " + beanName + " with " + classz.getName());
                registry.registerBeanDefinition(beanName, definition);
            }
        }

        // 注册接口
        private void registryIface() throws ClassNotFoundException {
            for (String classzName : allInterfaces) {
                Class<?> classz = Class.forName(classzName);
                GenericBeanDefinition definition = new GenericBeanDefinition();
                definition.setBeanClass(RouteFactoryBean.class);
                definition.setScope("singleton");
                definition.setLazyInit(false);
                definition.setAutowireCandidate(true);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                String name = classz.getSimpleName();
                definition.getPropertyValues().add("interfaces", classz);
                // IVMService >> vmService
                name = SpringUtils.changeBeanName(name, "");
                definition.getPropertyValues().add("instanceName", name);
                logger.info("Creating RouteFactoryBeanIface  name " + name + " with " + classz.getName());
                registry.registerBeanDefinition(name, definition);
            }
        }

        private Class<?>[] getAllInterfaces(Class<?> classz) {
            Class<?>[] interfaces = classz.getInterfaces();
            String name = classz.getSimpleName();
            Class<?> superclass = classz.getSuperclass();
            String superName = superclass.getSimpleName();
            if ("Object".equals(name) || "Object".equals(superName)) {
                return interfaces;
            }
            List<Class<?>> list = new ArrayList<>();
            Class<?>[] ifcx = getAllInterfaces(superclass);
            List<Class<?>> nifaces = Arrays.asList(ifcx);
            list.addAll(nifaces);
            return list.toArray(new Class<?>[] {});
        }
    }
}
