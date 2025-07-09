package com.bigdata.hdfs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * HDFS配置管理类
 * 负责加载和管理HDFS连接相关的配置参数
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HDFSConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(HDFSConfig.class);
    
    private static final String CONFIG_FILE = "hdfs-config.properties";
    private static HDFSConfig instance;
    private Properties properties;
    
    // 默认配置值
    private static final String DEFAULT_HDFS_URI = "hdfs://10.132.144.24:9000";
    private static final String DEFAULT_USER_NAME = "hadoop";
    private static final String DEFAULT_BUFFER_SIZE = "131072"; // 128KB
    private static final String DEFAULT_CONNECT_TIMEOUT = "10000"; // 10秒
    private static final String DEFAULT_MAX_RETRIES = "3";
    
    private HDFSConfig() {
        loadConfig();
    }
    
    /**
     * 获取配置实例（单例模式）
     * 
     * @return HDFSConfig实例
     */
    public static synchronized HDFSConfig getInstance() {
        if (instance == null) {
            instance = new HDFSConfig();
        }
        return instance;
    }
    
    /**
     * 加载配置文件
     */
    private void loadConfig() {
        properties = new Properties();
        
        // 尝试从classpath加载配置文件
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("成功加载配置文件: {}", CONFIG_FILE);
            } else {
                logger.warn("配置文件 {} 不存在，使用默认配置", CONFIG_FILE);
                setDefaultProperties();
            }
        } catch (IOException e) {
            logger.error("加载配置文件异常，使用默认配置", e);
            setDefaultProperties();
        }
        
        // 设置系统属性
        System.setProperty("HADOOP_USER_NAME", getUserName());
        
        logger.info("HDFS配置加载完成:");
        logger.info("- HDFS URI: {}", getHdfsUri());
        logger.info("- 用户名: {}", getUserName());
        logger.info("- 缓冲区大小: {} 字节", getBufferSize());
        logger.info("- 连接超时: {} 毫秒", getConnectTimeout());
        logger.info("- 最大重试次数: {}", getMaxRetries());
    }
    
    /**
     * 设置默认配置
     */
    private void setDefaultProperties() {
        properties.setProperty("hdfs.uri", DEFAULT_HDFS_URI);
        properties.setProperty("hdfs.user.name", DEFAULT_USER_NAME);
        properties.setProperty("hdfs.buffer.size", DEFAULT_BUFFER_SIZE);
        properties.setProperty("hdfs.connect.timeout", DEFAULT_CONNECT_TIMEOUT);
        properties.setProperty("hdfs.max.retries", DEFAULT_MAX_RETRIES);
    }
    
    /**
     * 获取HDFS URI
     * 
     * @return HDFS URI
     */
    public String getHdfsUri() {
        return properties.getProperty("hdfs.uri", DEFAULT_HDFS_URI);
    }
    
    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public String getUserName() {
        return properties.getProperty("hdfs.user.name", DEFAULT_USER_NAME);
    }
    
    /**
     * 获取缓冲区大小
     * 
     * @return 缓冲区大小（字节）
     */
    public int getBufferSize() {
        String bufferSize = properties.getProperty("hdfs.buffer.size", DEFAULT_BUFFER_SIZE);
        try {
            return Integer.parseInt(bufferSize);
        } catch (NumberFormatException e) {
            logger.warn("缓冲区大小配置无效: {}, 使用默认值: {}", bufferSize, DEFAULT_BUFFER_SIZE);
            return Integer.parseInt(DEFAULT_BUFFER_SIZE);
        }
    }
    
    /**
     * 获取连接超时时间
     * 
     * @return 连接超时时间（毫秒）
     */
    public int getConnectTimeout() {
        String timeout = properties.getProperty("hdfs.connect.timeout", DEFAULT_CONNECT_TIMEOUT);
        try {
            return Integer.parseInt(timeout);
        } catch (NumberFormatException e) {
            logger.warn("连接超时配置无效: {}, 使用默认值: {}", timeout, DEFAULT_CONNECT_TIMEOUT);
            return Integer.parseInt(DEFAULT_CONNECT_TIMEOUT);
        }
    }
    
    /**
     * 获取最大重试次数
     * 
     * @return 最大重试次数
     */
    public int getMaxRetries() {
        String retries = properties.getProperty("hdfs.max.retries", DEFAULT_MAX_RETRIES);
        try {
            return Integer.parseInt(retries);
        } catch (NumberFormatException e) {
            logger.warn("最大重试次数配置无效: {}, 使用默认值: {}", retries, DEFAULT_MAX_RETRIES);
            return Integer.parseInt(DEFAULT_MAX_RETRIES);
        }
    }
    
    /**
     * 获取指定键的配置值
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 设置配置值
     * 
     * @param key 配置键
     * @param value 配置值
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        logger.debug("设置配置: {} = {}", key, value);
    }
    
    /**
     * 获取所有配置属性
     * 
     * @return Properties对象
     */
    public Properties getProperties() {
        return new Properties(properties);
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        logger.info("重新加载HDFS配置...");
        loadConfig();
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean validateConfig() {
        boolean valid = true;
        
        // 验证HDFS URI
        String uri = getHdfsUri();
        if (uri == null || uri.trim().isEmpty() || !uri.startsWith("hdfs://")) {
            logger.error("无效的HDFS URI: {}", uri);
            valid = false;
        }
        
        // 验证用户名
        String userName = getUserName();
        if (userName == null || userName.trim().isEmpty()) {
            logger.error("无效的用户名: {}", userName);
            valid = false;
        }
        
        // 验证数值配置
        if (getBufferSize() <= 0) {
            logger.error("无效的缓冲区大小: {}", getBufferSize());
            valid = false;
        }
        
        if (getConnectTimeout() <= 0) {
            logger.error("无效的连接超时时间: {}", getConnectTimeout());
            valid = false;
        }
        
        if (getMaxRetries() < 0) {
            logger.error("无效的最大重试次数: {}", getMaxRetries());
            valid = false;
        }
        
        if (valid) {
            logger.info("HDFS配置验证通过");
        } else {
            logger.error("HDFS配置验证失败");
        }
        
        return valid;
    }
    
    /**
     * 打印当前配置信息
     */
    public void printConfig() {
        logger.info("=== HDFS配置信息 ===");
        properties.forEach((key, value) -> {
            logger.info("{} = {}", key, value);
        });
        logger.info("====================");
    }
}