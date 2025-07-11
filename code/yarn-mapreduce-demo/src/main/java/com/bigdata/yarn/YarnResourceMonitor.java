package com.bigdata.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * YARN资源监控工具类
 * 用于监控YARN集群资源使用情况和应用程序状态
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class YarnResourceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(YarnResourceMonitor.class);
    
    private YarnClient yarnClient;
    private Configuration conf;
    private SimpleDateFormat dateFormat;
    
    /**
     * 构造函数
     */
    public YarnResourceMonitor() {
        this.conf = new Configuration();
        this.yarnClient = YarnClient.createYarnClient();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * 构造函数（带配置）
     * 
     * @param conf Hadoop配置
     */
    public YarnResourceMonitor(Configuration conf) {
        this.conf = conf;
        this.yarnClient = YarnClient.createYarnClient();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * 初始化YARN客户端
     * 
     * @throws IOException IO异常
     */
    public void init() throws IOException {
        yarnClient.init(conf);
        yarnClient.start();
        logger.info("YARN Resource Monitor initialized successfully");
    }
    
    /**
     * 关闭YARN客户端
     */
    public void close() {
        if (yarnClient != null) {
            yarnClient.stop();
            logger.info("YARN Resource Monitor closed");
        }
    }
    
    /**
     * 获取集群资源信息
     * 
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public void printClusterMetrics() throws YarnException, IOException {
        YarnClusterMetrics clusterMetrics = yarnClient.getYarnClusterMetrics();
        
        logger.info("=== YARN Cluster Metrics ===");
        logger.info("Total Nodes: {}", clusterMetrics.getNumNodeManagers());
        logger.info("Active Nodes: {}", clusterMetrics.getNumActiveNodeManagers());
        logger.info("Decommissioned Nodes: {}", clusterMetrics.getNumDecommissionedNodeManagers());
        logger.info("Lost Nodes: {}", clusterMetrics.getNumLostNodeManagers());
        logger.info("Unhealthy Nodes: {}", clusterMetrics.getNumUnhealthyNodeManagers());
        logger.info("Rebooted Nodes: {}", clusterMetrics.getNumRebootedNodeManagers());
        logger.info("=== End of Cluster Metrics ===");
    }
    
    /**
     * 获取节点资源信息
     * 
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public void printNodeReports() throws YarnException, IOException {
        List<NodeReport> nodeReports = yarnClient.getNodeReports(NodeState.RUNNING);
        
        logger.info("=== Node Reports ===");
        for (NodeReport nodeReport : nodeReports) {
            Resource capability = nodeReport.getCapability();
            Resource used = nodeReport.getUsed();
            
            logger.info("Node: {}", nodeReport.getNodeId().getHost());
            logger.info("  State: {}", nodeReport.getNodeState());
            logger.info("  Total Memory: {} MB", capability.getMemorySize());
            logger.info("  Used Memory: {} MB", used.getMemorySize());
            logger.info("  Available Memory: {} MB", capability.getMemorySize() - used.getMemorySize());
            logger.info("  Total vCores: {}", capability.getVirtualCores());
            logger.info("  Used vCores: {}", used.getVirtualCores());
            logger.info("  Available vCores: {}", capability.getVirtualCores() - used.getVirtualCores());
            logger.info("  Health Report: {}", nodeReport.getHealthReport());
            logger.info("  Last Health Update: {}", 
                       dateFormat.format(new Date(nodeReport.getLastHealthReportTime())));
            logger.info("  ---");
        }
        logger.info("=== End of Node Reports ===");
    }
    
    /**
     * 获取应用程序列表
     * 
     * @param states 应用程序状态过滤器
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public void printApplications(EnumSet<YarnApplicationState> states) 
            throws YarnException, IOException {
        
        List<ApplicationReport> applications = yarnClient.getApplications(states);
        
        logger.info("=== Applications (States: {}) ===", states);
        logger.info("Total Applications: {}", applications.size());
        
        for (ApplicationReport app : applications) {
            logger.info("Application ID: {}", app.getApplicationId());
            logger.info("  Name: {}", app.getName());
            logger.info("  Type: {}", app.getApplicationType());
            logger.info("  State: {}", app.getYarnApplicationState());
            logger.info("  Final Status: {}", app.getFinalApplicationStatus());
            logger.info("  Progress: {}%", String.format("%.2f", app.getProgress() * 100));
            logger.info("  Queue: {}", app.getQueue());
            logger.info("  User: {}", app.getUser());
            logger.info("  Start Time: {}", dateFormat.format(new Date(app.getStartTime())));
            if (app.getFinishTime() > 0) {
                logger.info("  Finish Time: {}", dateFormat.format(new Date(app.getFinishTime())));
                logger.info("  Elapsed Time: {} ms", app.getFinishTime() - app.getStartTime());
            }
            logger.info("  Tracking URL: {}", app.getTrackingUrl());
            
            // 资源使用情况
            ApplicationResourceUsageReport resourceUsage = app.getApplicationResourceUsageReport();
            if (resourceUsage != null) {
                Resource usedResources = resourceUsage.getUsedResources();
                if (usedResources != null) {
                    logger.info("  Used Memory: {} MB", usedResources.getMemorySize());
                    logger.info("  Used vCores: {}", usedResources.getVirtualCores());
                }
                Resource reservedResources = resourceUsage.getReservedResources();
                if (reservedResources != null) {
                    logger.info("  Reserved Memory: {} MB", reservedResources.getMemorySize());
                    logger.info("  Reserved vCores: {}", reservedResources.getVirtualCores());
                }
            }
            
            logger.info("  ---");
        }
        logger.info("=== End of Applications ===");
    }
    
    /**
     * 获取队列信息
     * 
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public void printQueueInfo() throws YarnException, IOException {
        List<QueueInfo> queueInfos = yarnClient.getAllQueues();
        
        logger.info("=== Queue Information ===");
        for (QueueInfo queueInfo : queueInfos) {
            logger.info("Queue: {}", queueInfo.getQueueName());
            logger.info("  State: {}", queueInfo.getQueueState());
            logger.info("  Capacity: {}%", queueInfo.getCapacity() * 100);
            logger.info("  Current Capacity: {}%", queueInfo.getCurrentCapacity() * 100);
            logger.info("  Maximum Capacity: {}%", queueInfo.getMaximumCapacity() * 100);
            logger.info("  Child Queues: {}", queueInfo.getChildQueues());
            logger.info("  ---");
        }
        logger.info("=== End of Queue Information ===");
    }
    
    /**
     * 监控特定应用程序
     * 
     * @param applicationId 应用程序ID
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public void monitorApplication(ApplicationId applicationId) 
            throws YarnException, IOException {
        
        ApplicationReport appReport = yarnClient.getApplicationReport(applicationId);
        
        logger.info("=== Application Monitoring ===");
        logger.info("Application ID: {}", appReport.getApplicationId());
        logger.info("Name: {}", appReport.getName());
        logger.info("Current State: {}", appReport.getYarnApplicationState());
        logger.info("Progress: {}%", String.format("%.2f", appReport.getProgress() * 100));
        
        // 获取应用程序尝试信息
        List<ApplicationAttemptReport> attempts = 
            yarnClient.getApplicationAttempts(applicationId);
        
        logger.info("Application Attempts: {}", attempts.size());
        for (ApplicationAttemptReport attempt : attempts) {
            logger.info("  Attempt ID: {}", attempt.getApplicationAttemptId());
            logger.info("  State: {}", attempt.getYarnApplicationAttemptState());
            logger.info("  AM Container ID: {}", attempt.getAMContainerId());
            logger.info("  Tracking URL: {}", attempt.getTrackingUrl());
            logger.info("  Diagnostics: {}", attempt.getDiagnostics());
        }
        
        logger.info("=== End of Application Monitoring ===");
    }
    
    /**
     * 获取集群资源使用率统计
     * 
     * @return 资源使用率信息
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public ResourceUtilization getClusterResourceUtilization() 
            throws YarnException, IOException {
        
        List<NodeReport> nodeReports = yarnClient.getNodeReports(NodeState.RUNNING);
        
        long totalMemory = 0;
        long usedMemory = 0;
        int totalCores = 0;
        int usedCores = 0;
        
        for (NodeReport nodeReport : nodeReports) {
            Resource capability = nodeReport.getCapability();
            Resource used = nodeReport.getUsed();
            
            totalMemory += capability.getMemorySize();
            usedMemory += used.getMemorySize();
            totalCores += capability.getVirtualCores();
            usedCores += used.getVirtualCores();
        }
        
        return new ResourceUtilization(totalMemory, usedMemory, totalCores, usedCores);
    }
    
    /**
     * 资源使用率信息类
     */
    public static class ResourceUtilization {
        private final long totalMemory;
        private final long usedMemory;
        private final int totalCores;
        private final int usedCores;
        
        public ResourceUtilization(long totalMemory, long usedMemory, 
                                 int totalCores, int usedCores) {
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
            this.totalCores = totalCores;
            this.usedCores = usedCores;
        }
        
        public double getMemoryUtilization() {
            return totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0;
        }
        
        public double getCoreUtilization() {
            return totalCores > 0 ? (double) usedCores / totalCores * 100 : 0;
        }
        
        public long getTotalMemory() { return totalMemory; }
        public long getUsedMemory() { return usedMemory; }
        public long getAvailableMemory() { return totalMemory - usedMemory; }
        public int getTotalCores() { return totalCores; }
        public int getUsedCores() { return usedCores; }
        public int getAvailableCores() { return totalCores - usedCores; }
        
        @Override
        public String toString() {
            return String.format(
                "Memory: %d/%d MB (%.2f%%), Cores: %d/%d (%.2f%%)",
                usedMemory, totalMemory, getMemoryUtilization(),
                usedCores, totalCores, getCoreUtilization()
            );
        }
    }
    
    /**
     * 主方法：演示YARN资源监控功能
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        YarnResourceMonitor monitor = new YarnResourceMonitor();
        
        try {
            monitor.init();
            
            // 打印集群指标
            monitor.printClusterMetrics();
            
            // 打印节点报告
            monitor.printNodeReports();
            
            // 打印运行中的应用程序
            monitor.printApplications(EnumSet.of(YarnApplicationState.RUNNING));
            
            // 打印队列信息
            monitor.printQueueInfo();
            
            // 打印资源使用率
            ResourceUtilization utilization = monitor.getClusterResourceUtilization();
            logger.info("Cluster Resource Utilization: {}", utilization);
            
        } catch (Exception e) {
            logger.error("Error monitoring YARN resources", e);
        } finally {
            monitor.close();
        }
    }
}