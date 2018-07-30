package com.javatao.route.support;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * SpringUtils
 * 
 * @author tao
 */
public abstract class SpringUtils {
    private final static Logger logger = LoggerFactory.getLogger(SpringUtils.class);
    public static ConfigurableListableBeanFactory factory = null;
    public static Map<String, String> mapInst = new LinkedCaseInsensitiveMap<>();

    public static Object getBean(String prop) {
        if (mapInst.containsKey(prop)) {
            prop = mapInst.get(prop);
        }
        try {
            Object obj = factory.getBean(prop);
            logger.debug("property=[{}],object=[{}]", prop, obj);
            return obj;
        } catch (Exception e) {
            throw new RouteException(e);
        }
    }

    public static <T> T getBean(Class<T> classz) {
        T o = factory.getBean(classz);
        logger.debug("property=[{}],object=[{}]", classz, o);
        return o;
    }

    /**
     * 转换beanName IDemoService >> demoService_type
     */
    public static String changeBeanName(String ifcName, String defaultName, String type) {
        if (StringUtils.isNotBlank(type)) {
            type = "_" + type;
        }
        if (ifcName.startsWith("I")) {
            ifcName = ifcName.substring(1);
        }
        String beanName = firstToMix(ifcName) + type;
        ifcName = beanName;
        if (StringUtils.isNotBlank(defaultName)) {
            beanName = defaultName + type;
        }
        if (!mapInst.containsKey(ifcName)) {
            mapInst.put(ifcName, beanName);
        }
        return beanName;
    }

    /**
     * 转换beanName IDemoService >> demoService_type
     */
    public static String changeBeanName(String ifcName, String type) {
        return changeBeanName(ifcName, null, type);
    }

    public static <T> Map<String, T> getAllBean(Class<T> classz) {
        Map<String, T> beansOfType = factory.getBeansOfType(classz);
        logger.debug("property=[{}],[{}]", classz, beansOfType);
        Map<String, T> result = new LinkedCaseInsensitiveMap<>();
        for (Entry<String, T> element : beansOfType.entrySet()) {
            String key = element.getKey();
            T value = element.getValue();
            if (key.startsWith("&")) {
                key = key.substring(1);
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * 首字母小写
     * 
     * @param column
     *            字符
     * @return 结果
     */
    public static String firstToMix(String column) {
        if (StringUtils.isBlank(column)) {
            return "";
        } else {
            return column.substring(0, 1).toLowerCase() + column.substring(1);
        }
    }
}
