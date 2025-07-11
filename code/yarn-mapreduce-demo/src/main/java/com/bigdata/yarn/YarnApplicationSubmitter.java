package com.bigdata.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * YARN应用程序提交工具类
 * 用于演示如何通过编程方式向YARN集群提交应用程序
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class YarnApplicationSubmitter {
    
    private static final Logger logger = LoggerFactory.getLogger(YarnApplicationSubmitter.class);
    
    private Configuration conf;
    private YarnClient yarnClient;
    private FileSystem fs;
    
    /**
     * 构造函数
     */
    public YarnApplicationSubmitter() {
        this.conf = new YarnConfiguration();
    }
    
    /**
     * 构造函数（带配置）
     * 
     * @param conf Hadoop配置
     */
    public YarnApplicationSubmitter(Configuration conf) {
        this.conf = conf;
    }
    
    /**
     * 初始化YARN客户端和文件系统
     * 
     * @throws IOException IO异常
     */
    public void init() throws IOException {
        yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);
        yarnClient.start();
        
        fs = FileSystem.get(conf);
        
        logger.info("YARN Application Submitter initialized successfully");
    }
    
    /**
     * 关闭客户端
     */
    public void close() {
        if (yarnClient != null) {
            yarnClient.stop();
        }
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                logger.warn("Error closing filesystem", e);
            }
        }
        logger.info("YARN Application Submitter closed");
    }
    
    /**
     * 提交MapReduce应用程序
     * 
     * @param appName 应用程序名称
     * @param jarPath JAR文件路径
     * @param mainClass 主类名
     * @param args 应用程序参数
     * @param queue 队列名称
     * @return 应用程序ID
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public ApplicationId submitMapReduceApplication(
            String appName, 
            String jarPath, 
            String mainClass, 
            String[] args, 
            String queue) throws YarnException, IOException {
        
        logger.info("Submitting MapReduce application: {}", appName);
        
        // 创建应用程序
        YarnClientApplication app = yarnClient.createApplication();
        GetNewApplicationResponse appResponse = app.getNewApplicationResponse();
        ApplicationId appId = appResponse.getApplicationId();
        
        logger.info("Application ID: {}", appId);
        
        // 设置应用程序提交上下文
        ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
        appContext.setApplicationName(appName);
        appContext.setApplicationType("MAPREDUCE");
        appContext.setQueue(queue);
        
        // 设置应用程序资源需求
        Resource capability = Records.newRecord(Resource.class);
        capability.setMemorySize(1024); // 1GB
        capability.setVirtualCores(1);
        appContext.setResource(capability);
        
        // 设置应用程序优先级
        Priority priority = Records.newRecord(Priority.class);
        priority.setPriority(0);
        appContext.setPriority(priority);
        
        // 设置Container启动上下文
        ContainerLaunchContext amContainer = createAMContainerSpec(
            jarPath, mainClass, args);
        appContext.setAMContainerSpec(amContainer);
        
        // 提交应用程序
        yarnClient.submitApplication(appContext);
        
        logger.info("MapReduce application submitted successfully with ID: {}", appId);
        return appId;
    }
    
    /**
     * 创建ApplicationMaster容器规范
     * 
     * @param jarPath JAR文件路径
     * @param mainClass 主类名
     * @param args 应用程序参数
     * @return Container启动上下文
     * @throws IOException IO异常
     */
    private ContainerLaunchContext createAMContainerSpec(
            String jarPath, String mainClass, String[] args) throws IOException {
        
        ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
        
        // 设置本地资源
        Map<String, LocalResource> localResources = new HashMap<>();
        
        // 添加应用程序JAR文件
        Path jarHdfsPath = new Path(jarPath);
        FileStatus jarStatus = fs.getFileStatus(jarHdfsPath);
        LocalResource jarResource = Records.newRecord(LocalResource.class);
        jarResource.setResource(ConverterUtils.getYarnUrlFromPath(jarHdfsPath));
        jarResource.setSize(jarStatus.getLen());
        jarResource.setTimestamp(jarStatus.getModificationTime());
        jarResource.setType(LocalResourceType.FILE);
        jarResource.setVisibility(LocalResourceVisibility.APPLICATION);
        localResources.put("app.jar", jarResource);
        
        amContainer.setLocalResources(localResources);
        
        // 设置环境变量
        Map<String, String> env = new HashMap<>();
        
        // 设置CLASSPATH
        StringBuilder classPathEnv = new StringBuilder();
        classPathEnv.append(ApplicationConstants.Environment.CLASSPATH.$$())
                   .append(ApplicationConstants.CLASS_PATH_SEPARATOR)
                   .append("./app.jar");
        
        // 添加Hadoop相关的CLASSPATH
        for (String c : conf.getStrings(
                YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)) {
            classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
            classPathEnv.append(c.trim());
        }
        
        env.put("CLASSPATH", classPathEnv.toString());
        
        // 设置其他环境变量
        env.put("HADOOP_CONF_DIR", System.getenv("HADOOP_CONF_DIR"));
        env.put("HADOOP_HOME", System.getenv("HADOOP_HOME"));
        
        amContainer.setEnvironment(env);
        
        // 设置启动命令
        List<String> commands = new ArrayList<>();
        StringBuilder command = new StringBuilder();
        
        command.append(ApplicationConstants.Environment.JAVA_HOME.$$())
               .append("/bin/java")
               .append(" -Xmx512m")
               .append(" ").append(mainClass);
        
        // 添加应用程序参数
        if (args != null) {
            for (String arg : args) {
                command.append(" ").append(arg);
            }
        }
        
        command.append(" 1>").append(ApplicationConstants.LOG_DIR_EXPANSION_VAR)
               .append("/stdout")
               .append(" 2>").append(ApplicationConstants.LOG_DIR_EXPANSION_VAR)
               .append("/stderr");
        
        commands.add(command.toString());
        amContainer.setCommands(commands);
        
        logger.debug("AM Container command: {}", command.toString());
        
        return amContainer;
    }
    
    /**
     * 监控应用程序执行状态
     * 
     * @param appId 应用程序ID
     * @param timeoutMs 超时时间（毫秒）
     * @return 应用程序最终状态
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    public FinalApplicationStatus monitorApplication(
            ApplicationId appId, long timeoutMs) 
            throws YarnException, IOException, InterruptedException {
        
        logger.info("Monitoring application: {}", appId);
        
        long startTime = System.currentTimeMillis();
        
        while (true) {
            // 检查超时
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Application monitoring timeout after {} ms", timeoutMs);
                break;
            }
            
            ApplicationReport appReport = yarnClient.getApplicationReport(appId);
            YarnApplicationState appState = appReport.getYarnApplicationState();
            FinalApplicationStatus finalStatus = appReport.getFinalApplicationStatus();
            
            logger.info("Application State: {}, Progress: {}%", 
                       appState, String.format("%.2f", appReport.getProgress() * 100));
            
            // 检查应用程序是否完成
            if (appState == YarnApplicationState.FINISHED ||
                appState == YarnApplicationState.KILLED ||
                appState == YarnApplicationState.FAILED) {
                
                logger.info("Application {} finished with state: {} and final status: {}", 
                           appId, appState, finalStatus);
                
                if (appState == YarnApplicationState.FAILED) {
                    logger.error("Application failed with diagnostics: {}", 
                               appReport.getDiagnostics());
                }
                
                return finalStatus;
            }
            
            // 等待一段时间后再次检查
            Thread.sleep(5000); // 5秒
        }
        
        return FinalApplicationStatus.UNDEFINED;
    }
    
    /**
     * 杀死应用程序
     * 
     * @param appId 应用程序ID
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public void killApplication(ApplicationId appId) 
            throws YarnException, IOException {
        
        logger.info("Killing application: {}", appId);
        yarnClient.killApplication(appId);
        logger.info("Application {} killed successfully", appId);
    }
    
    /**
     * 获取应用程序日志
     * 
     * @param appId 应用程序ID
     * @return 应用程序日志内容
     * @throws YarnException YARN异常
     * @throws IOException IO异常
     */
    public String getApplicationLogs(ApplicationId appId) 
            throws YarnException, IOException {
        
        // 注意：这里只是示例，实际获取日志需要更复杂的实现
        ApplicationReport appReport = yarnClient.getApplicationReport(appId);
        
        StringBuilder logs = new StringBuilder();
        logs.append("Application ID: ").append(appId).append("\n");
        logs.append("Application Name: ").append(appReport.getName()).append("\n");
        logs.append("Application State: ").append(appReport.getYarnApplicationState()).append("\n");
        logs.append("Final Status: ").append(appReport.getFinalApplicationStatus()).append("\n");
        logs.append("Diagnostics: ").append(appReport.getDiagnostics()).append("\n");
        logs.append("Tracking URL: ").append(appReport.getTrackingUrl()).append("\n");
        
        return logs.toString();
    }
    
    /**
     * 主方法：演示YARN应用程序提交功能
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: YarnApplicationSubmitter <app-name> <jar-path> <main-class> <queue> [app-args...]");
            System.err.println("Example: YarnApplicationSubmitter WordCount /apps/wordcount.jar com.bigdata.mapreduce.wordcount.WordCountDriver default /input /output");
            System.exit(1);
        }
        
        String appName = args[0];
        String jarPath = args[1];
        String mainClass = args[2];
        String queue = args[3];
        String[] appArgs = Arrays.copyOfRange(args, 4, args.length);
        
        YarnApplicationSubmitter submitter = new YarnApplicationSubmitter();
        
        try {
            submitter.init();
            
            // 提交应用程序
            ApplicationId appId = submitter.submitMapReduceApplication(
                appName, jarPath, mainClass, appArgs, queue);
            
            // 监控应用程序（最多等待10分钟）
            FinalApplicationStatus finalStatus = submitter.monitorApplication(
                appId, 10 * 60 * 1000);
            
            logger.info("Application final status: {}", finalStatus);
            
            // 获取应用程序日志
            String logs = submitter.getApplicationLogs(appId);
            logger.info("Application logs:\n{}", logs);
            
        } catch (Exception e) {
            logger.error("Error submitting YARN application", e);
            System.exit(1);
        } finally {
            submitter.close();
        }
    }
}