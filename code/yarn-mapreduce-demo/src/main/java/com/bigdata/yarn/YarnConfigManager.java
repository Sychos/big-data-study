package com.bigdata.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * YARN配置管理工具类
 * 用于管理和优化YARN集群配置
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class YarnConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(YarnConfigManager.class);
    
    private Configuration conf;
    
    /**
     * 构造函数
     */
    public YarnConfigManager() {
        this.conf = new YarnConfiguration();
    }
    
    /**
     * 构造函数（带配置）
     * 
     * @param conf Hadoop配置
     */
    public YarnConfigManager(Configuration conf) {
        this.conf = conf;
    }
    
    /**
     * 设置ResourceManager配置
     * 
     * @param rmHostname ResourceManager主机名
     * @param rmPort ResourceManager端口
     * @param rmWebPort ResourceManager Web端口
     */
    public void configureResourceManager(String rmHostname, int rmPort, int rmWebPort) {
        conf.set(YarnConfiguration.RM_HOSTNAME, rmHostname);
        conf.set(YarnConfiguration.RM_ADDRESS, rmHostname + ":" + rmPort);
        conf.set(YarnConfiguration.RM_WEBAPP_ADDRESS, rmHostname + ":" + rmWebPort);
        conf.set(YarnConfiguration.RM_SCHEDULER_ADDRESS, rmHostname + ":8030");
        conf.set(YarnConfiguration.RM_RESOURCE_TRACKER_ADDRESS, rmHostname + ":8031");
        conf.set(YarnConfiguration.RM_ADMIN_ADDRESS, rmHostname + ":8033");
        
        logger.info("ResourceManager configured: {}:{}", rmHostname, rmPort);
    }
    
    /**
     * 配置ResourceManager高可用
     * 
     * @param clusterId 集群ID
     * @param rm1Hostname RM1主机名
     * @param rm2Hostname RM2主机名
     * @param zkQuorum ZooKeeper集群地址
     */
    public void configureRMHighAvailability(String clusterId, 
                                           String rm1Hostname, 
                                           String rm2Hostname, 
                                           String zkQuorum) {
        // 启用RM高可用
        conf.setBoolean(YarnConfiguration.RM_HA_ENABLED, true);
        conf.set(YarnConfiguration.RM_CLUSTER_ID, clusterId);
        conf.set(YarnConfiguration.RM_HA_IDS, "rm1,rm2");
        
        // 配置RM地址
        conf.set(YarnConfiguration.RM_HOSTNAME + ".rm1", rm1Hostname);
        conf.set(YarnConfiguration.RM_HOSTNAME + ".rm2", rm2Hostname);
        
        // 配置ZooKeeper
        conf.set(YarnConfiguration.RM_ZK_ADDRESS, zkQuorum);
        conf.set("yarn.resourcemanager.ha.automatic-failover.enabled", "true");
        
        logger.info("ResourceManager HA configured with cluster ID: {}", clusterId);
    }
    
    /**
     * 配置NodeManager资源
     * 
     * @param memoryMb 内存大小（MB）
     * @param vcores 虚拟核心数
     * @param diskSpaceGb 磁盘空间（GB）
     */
    public void configureNodeManagerResources(int memoryMb, int vcores, int diskSpaceGb) {
        conf.setInt("yarn.nodemanager.resource.memory-mb", memoryMb);
        conf.setInt("yarn.nodemanager.resource.cpu-vcores", vcores);
        conf.setFloat("yarn.nodemanager.disk-health-checker.max-disk-utilization-per-disk-percentage", 90.0f);
        
        // 设置本地目录
        conf.set(YarnConfiguration.NM_LOCAL_DIRS, "/tmp/yarn/local");
        conf.set(YarnConfiguration.NM_LOG_DIRS, "/tmp/yarn/logs");
        
        logger.info("NodeManager resources configured: {} MB memory, {} vcores", 
                   memoryMb, vcores);
    }
    
    /**
     * 配置容器执行器
     * 
     * @param executorClass 执行器类名
     */
    public void configureContainerExecutor(String executorClass) {
        conf.set(YarnConfiguration.NM_CONTAINER_EXECUTOR, executorClass);
        
        // 如果使用LinuxContainerExecutor，设置相关配置
        if ("org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor".equals(executorClass)) {
            conf.set("yarn.nodemanager.linux-container-executor.group", "yarn");
            conf.set("yarn.nodemanager.linux-container-executor.path", 
                    "/usr/local/hadoop/bin/container-executor");
        }
        
        logger.info("Container executor configured: {}", executorClass);
    }
    
    /**
     * 配置调度器
     * 
     * @param schedulerClass 调度器类名
     */
    public void configureScheduler(String schedulerClass) {
        conf.set(YarnConfiguration.RM_SCHEDULER, schedulerClass);
        
        // 根据调度器类型设置特定配置
        if (schedulerClass.contains("CapacityScheduler")) {
            configureCapacityScheduler();
        } else if (schedulerClass.contains("FairScheduler")) {
            configureFairScheduler();
        }
        
        logger.info("Scheduler configured: {}", schedulerClass);
    }
    
    /**
     * 配置容量调度器
     */
    private void configureCapacityScheduler() {
        // 设置默认队列配置
        conf.set("yarn.scheduler.capacity.root.queues", "default,production,development");
        
        // 默认队列配置
        conf.set("yarn.scheduler.capacity.root.default.capacity", "40");
        conf.set("yarn.scheduler.capacity.root.default.maximum-capacity", "60");
        conf.set("yarn.scheduler.capacity.root.default.state", "RUNNING");
        
        // 生产队列配置
        conf.set("yarn.scheduler.capacity.root.production.capacity", "40");
        conf.set("yarn.scheduler.capacity.root.production.maximum-capacity", "80");
        conf.set("yarn.scheduler.capacity.root.production.state", "RUNNING");
        
        // 开发队列配置
        conf.set("yarn.scheduler.capacity.root.development.capacity", "20");
        conf.set("yarn.scheduler.capacity.root.development.maximum-capacity", "40");
        conf.set("yarn.scheduler.capacity.root.development.state", "RUNNING");
        
        logger.info("Capacity Scheduler configured with multiple queues");
    }
    
    /**
     * 配置公平调度器
     */
    private void configureFairScheduler() {
        // 设置公平调度器配置文件
        conf.set("yarn.scheduler.fair.allocation.file", "fair-scheduler.xml");
        conf.setBoolean("yarn.scheduler.fair.preemption", true);
        conf.setInt("yarn.scheduler.fair.preemption.cluster-utilization-threshold", 80);
        
        logger.info("Fair Scheduler configured with preemption enabled");
    }
    
    /**
     * 配置应用程序资源限制
     * 
     * @param maxMemoryMb 最大内存（MB）
     * @param maxVcores 最大虚拟核心数
     */
    public void configureApplicationLimits(int maxMemoryMb, int maxVcores) {
        conf.setInt(YarnConfiguration.RM_SCHEDULER_MAXIMUM_ALLOCATION_MB, maxMemoryMb);
        conf.setInt(YarnConfiguration.RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES, maxVcores);
        
        // 设置最小分配
        conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 512);
        conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_VCORES, 1);
        
        logger.info("Application limits configured: max {} MB memory, {} vcores", 
                   maxMemoryMb, maxVcores);
    }
    
    /**
     * 配置日志聚合
     * 
     * @param enabled 是否启用日志聚合
     * @param retentionSeconds 日志保留时间（秒）
     */
    public void configureLogAggregation(boolean enabled, long retentionSeconds) {
        conf.setBoolean(YarnConfiguration.LOG_AGGREGATION_ENABLED, enabled);
        
        if (enabled) {
            conf.setLong(YarnConfiguration.LOG_AGGREGATION_RETAIN_SECONDS, retentionSeconds);
            conf.set(YarnConfiguration.NM_REMOTE_APP_LOG_DIR, "/tmp/yarn/logs");
            conf.set(YarnConfiguration.NM_REMOTE_APP_LOG_DIR_SUFFIX, "logs");
        }
        
        logger.info("Log aggregation {}, retention: {} seconds", 
                   enabled ? "enabled" : "disabled", retentionSeconds);
    }
    
    /**
     * 配置安全认证
     * 
     * @param kerberosEnabled 是否启用Kerberos
     * @param principal Kerberos主体
     * @param keytab Keytab文件路径
     */
    public void configureSecurity(boolean kerberosEnabled, String principal, String keytab) {
        if (kerberosEnabled) {
            conf.set("hadoop.security.authentication", "kerberos");
            conf.set("hadoop.security.authorization", "true");
            
            // ResourceManager Kerberos配置
            conf.set(YarnConfiguration.RM_PRINCIPAL, principal);
            conf.set(YarnConfiguration.RM_KEYTAB, keytab);
            
            // NodeManager Kerberos配置
            conf.set(YarnConfiguration.NM_PRINCIPAL, principal);
            conf.set(YarnConfiguration.NM_KEYTAB, keytab);
            
            logger.info("Kerberos security enabled with principal: {}", principal);
        } else {
            conf.set("hadoop.security.authentication", "simple");
            logger.info("Simple authentication configured");
        }
    }
    
    /**
     * 获取推荐的性能优化配置
     * 
     * @return 优化配置映射
     */
    public Map<String, String> getPerformanceOptimizations() {
        Map<String, String> optimizations = new HashMap<>();
        
        // 内存优化
        optimizations.put("yarn.nodemanager.vmem-check-enabled", "false");
        optimizations.put("yarn.nodemanager.pmem-check-enabled", "true");
        
        // 调度优化
        optimizations.put("yarn.scheduler.capacity.node-locality-delay", "40");
        optimizations.put("yarn.scheduler.capacity.rack-locality-additional-delay", "-1");
        
        // 容器优化
        optimizations.put("yarn.nodemanager.container-executor.class", 
                         "org.apache.hadoop.yarn.server.nodemanager.DefaultContainerExecutor");
        optimizations.put("yarn.nodemanager.delete.debug-delay-sec", "600");
        
        // 网络优化
        optimizations.put("yarn.ipc.client.connect.max.retries", "50");
        optimizations.put("yarn.ipc.client.connect.retry.interval", "1000");
        
        // ApplicationMaster优化
        optimizations.put("yarn.am.liveness-monitor.expiry-interval-ms", "600000");
        optimizations.put("yarn.am.liveness-monitor.expiry-interval-ms", "600000");
        
        return optimizations;
    }
    
    /**
     * 应用性能优化配置
     */
    public void applyPerformanceOptimizations() {
        Map<String, String> optimizations = getPerformanceOptimizations();
        
        for (Map.Entry<String, String> entry : optimizations.entrySet()) {
            conf.set(entry.getKey(), entry.getValue());
        }
        
        logger.info("Applied {} performance optimizations", optimizations.size());
    }
    
    /**
     * 验证配置
     * 
     * @return 验证结果
     */
    public boolean validateConfiguration() {
        boolean isValid = true;
        
        // 检查必要的配置项
        String[] requiredConfigs = {
            YarnConfiguration.RM_HOSTNAME,
            YarnConfiguration.RM_ADDRESS,
            "yarn.nodemanager.resource.memory-mb",
            "yarn.nodemanager.resource.cpu-vcores"
        };
        
        for (String config : requiredConfigs) {
            if (conf.get(config) == null) {
                logger.error("Missing required configuration: {}", config);
                isValid = false;
            }
        }
        
        // 检查资源配置的合理性
        int nmMemory = conf.getInt("yarn.nodemanager.resource.memory-mb", 0);
        int nmCores = conf.getInt("yarn.nodemanager.resource.cpu-vcores", 0);
        
        if (nmMemory < 1024) {
            logger.warn("NodeManager memory is too low: {} MB", nmMemory);
        }
        
        if (nmCores < 1) {
            logger.warn("NodeManager vcores is too low: {}", nmCores);
        }
        
        logger.info("Configuration validation {}", isValid ? "passed" : "failed");
        return isValid;
    }
    
    /**
     * 导出配置到文件
     * 
     * @param filePath 文件路径
     * @throws IOException IO异常
     */
    public void exportConfiguration(String filePath) throws IOException {
        Properties props = new Properties();
        
        // 将Configuration转换为Properties
        for (Map.Entry<String, String> entry : conf) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.store(fos, "YARN Configuration Export");
        }
        
        logger.info("Configuration exported to: {}", filePath);
    }
    
    /**
     * 打印当前配置摘要
     */
    public void printConfigurationSummary() {
        logger.info("=== YARN Configuration Summary ===");
        logger.info("ResourceManager: {}", conf.get(YarnConfiguration.RM_HOSTNAME, "Not configured"));
        logger.info("Scheduler: {}", conf.get(YarnConfiguration.RM_SCHEDULER, "Default"));
        logger.info("NodeManager Memory: {} MB", 
                   conf.getInt("yarn.nodemanager.resource.memory-mb", 0));
        logger.info("NodeManager vCores: {}", 
                   conf.getInt("yarn.nodemanager.resource.cpu-vcores", 0));
        logger.info("Log Aggregation: {}", 
                   conf.getBoolean(YarnConfiguration.LOG_AGGREGATION_ENABLED, false) ? "Enabled" : "Disabled");
        logger.info("RM HA: {}", 
                   conf.getBoolean(YarnConfiguration.RM_HA_ENABLED, false) ? "Enabled" : "Disabled");
        logger.info("=== End of Configuration Summary ===");
    }
    
    /**
     * 获取配置对象
     * 
     * @return Configuration对象
     */
    public Configuration getConfiguration() {
        return conf;
    }
    
    /**
     * 主方法：演示YARN配置管理功能
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        YarnConfigManager configManager = new YarnConfigManager();
        
        try {
            // 配置ResourceManager
            configManager.configureResourceManager("localhost", 8032, 8088);
            
            // 配置NodeManager资源
            configManager.configureNodeManagerResources(8192, 4, 100);
            
            // 配置调度器
            configManager.configureScheduler(
                "org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler");
            
            // 配置应用程序限制
            configManager.configureApplicationLimits(4096, 2);
            
            // 配置日志聚合
            configManager.configureLogAggregation(true, 86400); // 1天
            
            // 应用性能优化
            configManager.applyPerformanceOptimizations();
            
            // 验证配置
            boolean isValid = configManager.validateConfiguration();
            
            // 打印配置摘要
            configManager.printConfigurationSummary();
            
            // 导出配置
            if (isValid) {
                configManager.exportConfiguration("yarn-config-export.properties");
                logger.info("YARN configuration completed successfully");
            } else {
                logger.error("YARN configuration validation failed");
            }
            
        } catch (Exception e) {
            logger.error("Error managing YARN configuration", e);
        }
    }
}