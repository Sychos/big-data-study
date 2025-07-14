package com.bigdata.config;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Hadoop配置管理器
 * 负责从配置文件中读取Hadoop相关配置并应用到Configuration对象中
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HadoopConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HadoopConfigManager.class);
    
    private static final String CONFIG_FILE = "hadoop.properties";
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    /**
     * 加载配置文件
     */
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream inputStream = HadoopConfigManager.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            
            if (inputStream == null) {
                logger.error("配置文件 {} 未找到", CONFIG_FILE);
                throw new RuntimeException("配置文件未找到: " + CONFIG_FILE);
            }
            
            properties.load(inputStream);
            logger.info("成功加载配置文件: {}", CONFIG_FILE);
            
        } catch (IOException e) {
            logger.error("加载配置文件失败: {}", CONFIG_FILE, e);
            throw new RuntimeException("加载配置文件失败", e);
        }
    }
    
    /**
     * 获取配置属性值
     * 
     * @param key 配置键
     * @return 配置值
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取配置属性值，如果不存在则返回默认值
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数类型的配置值
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 整数配置值
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("配置项 {} 的值 {} 不是有效的整数，使用默认值 {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取长整型类型的配置值
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 长整型配置值
     */
    public static long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("配置项 {} 的值 {} 不是有效的长整数，使用默认值 {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * 配置Hadoop Configuration对象
     * 
     * @param conf Hadoop配置对象
     */
    public static void configureHadoop(Configuration conf) {
        logger.info("开始配置Hadoop环境...");
        
        // 设置Hadoop用户身份
        String hadoopUser = getProperty("hadoop.user.name", "hadoop");
        System.setProperty("HADOOP_USER_NAME", hadoopUser);
        logger.info("设置Hadoop用户: {}", hadoopUser);
        
        // 设置HDFS配置
        String fsDefaultFS = getProperty("fs.defaultFS");
        if (fsDefaultFS != null) {
            conf.set("fs.defaultFS", fsDefaultFS);
            logger.info("设置HDFS NameNode地址: {}", fsDefaultFS);
        }
        
        // 设置YARN ResourceManager配置
        String rmHostname = getProperty("yarn.resourcemanager.hostname");
        if (rmHostname != null) {
            conf.set("yarn.resourcemanager.hostname", rmHostname);
            logger.info("设置ResourceManager主机名: {}", rmHostname);
        }
        
        String rmAddress = getProperty("yarn.resourcemanager.address");
        if (rmAddress != null) {
            conf.set("yarn.resourcemanager.address", rmAddress);
            logger.info("设置ResourceManager地址: {}", rmAddress);
        }
        
        // 设置JobHistoryServer配置
        String jhsAddress = getProperty("mapreduce.jobhistory.address");
        if (jhsAddress != null) {
            conf.set("mapreduce.jobhistory.address", jhsAddress);
            logger.info("设置JobHistoryServer地址: {}", jhsAddress);
        }
        
        String jhsWebAddress = getProperty("mapreduce.jobhistory.webapp.address");
        if (jhsWebAddress != null) {
            conf.set("mapreduce.jobhistory.webapp.address", jhsWebAddress);
            logger.info("设置JobHistoryServer Web地址: {}", jhsWebAddress);
        }
        
        // 设置MapReduce框架
        String mrFramework = getProperty("mapreduce.framework.name", "yarn");
        conf.set("mapreduce.framework.name", mrFramework);
        logger.info("设置MapReduce框架: {}", mrFramework);
        
        // 设置作业监控配置
        long pollInterval = getLongProperty("mapreduce.client.progressmonitor.pollinterval", 10000);
        conf.setLong("mapreduce.client.progressmonitor.pollinterval", pollInterval);
        
        int completionInterval = getIntProperty("mapreduce.client.completion.pollinterval", 5000);
        conf.setInt("mapreduce.client.completion.pollinterval", completionInterval);
        
        // 设置应用程序类型
        String appType = getProperty("yarn.application.type", "MAPREDUCE");
        conf.set("yarn.application.type", appType);
        
        logger.info("Hadoop配置完成");
    }
    
    /**
     * 获取当前环境配置
     * 
     * @return 环境名称 (dev/test/prod)
     */
    public static String getEnvironment() {
        return getProperty("hadoop.environment", "dev");
    }
    
    /**
     * 检查是否为开发环境
     * 
     * @return 是否为开发环境
     */
    public static boolean isDevelopmentEnvironment() {
        return "dev".equals(getEnvironment());
    }
    
    /**
     * 检查是否为生产环境
     * 
     * @return 是否为生产环境
     */
    public static boolean isProductionEnvironment() {
        return "prod".equals(getEnvironment());
    }
    
    /**
     * 打印所有配置信息（仅在开发环境下）
     */
    public static void printConfiguration() {
        if (isDevelopmentEnvironment()) {
            logger.info("=== Hadoop配置信息 ===");
            properties.forEach((key, value) -> 
                logger.info("{} = {}", key, value)
            );
            logger.info("=== 配置信息结束 ===");
        }
    }
}