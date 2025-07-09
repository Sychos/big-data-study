package com.bigdata.hdfs.project;

import com.bigdata.hdfs.util.HDFSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志文件分析器
 * 分析HDFS中的Web服务器日志文件
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class LogAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(LogAnalyzer.class);
    
    // Apache日志格式正则表达式
    private static final String LOG_PATTERN = 
        "^(\\S+) \\S+ \\S+ \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)";
    
    private static final Pattern pattern = Pattern.compile(LOG_PATTERN);
    
    public static void main(String[] args) {
        LogAnalyzer analyzer = new LogAnalyzer();
        
        try {
            // 1. 创建测试日志文件
            analyzer.createTestLogFile();
            
            // 2. 分析日志文件
            analyzer.analyzeLogFile("/user/bigdata/access.log");
            
        } catch (Exception e) {
            logger.error("日志分析异常", e);
        }
    }
    
    /**
     * 创建测试日志文件
     */
    private void createTestLogFile() {
        try {
            HDFSUtil hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");
            
            // 创建模拟的Apache访问日志
            StringBuilder logContent = new StringBuilder();
            String[] ips = {"192.168.1.100", "192.168.1.101", "192.168.1.102", "10.0.0.1", "10.0.0.2"};
            String[] methods = {"GET", "POST", "PUT", "DELETE"};
            String[] urls = {"/index.html", "/api/users", "/api/orders", "/images/logo.png", "/css/style.css"};
            int[] statusCodes = {200, 404, 500, 301, 403};
            
            for (int i = 0; i < 1000; i++) {
                String ip = ips[i % ips.length];
                String method = methods[i % methods.length];
                String url = urls[i % urls.length];
                int statusCode = statusCodes[i % statusCodes.length];
                int responseSize = (int) (Math.random() * 10000) + 100;
                
                String logLine = String.format(
                    "%s - - [25/Dec/2023:10:%02d:%02d +0800] \"%s %s HTTP/1.1\" %d %d\n",
                    ip, i / 60, i % 60, method, url, statusCode, responseSize
                );
                
                logContent.append(logLine);
            }
            
            // 上传到HDFS
            hdfsUtil.writeFile("/user/bigdata/access.log", logContent.toString());
            logger.info("测试日志文件创建完成");
            
            hdfsUtil.close();
            
        } catch (Exception e) {
            logger.error("创建测试日志文件异常", e);
        }
    }
    
    /**
     * 分析日志文件
     * 
     * @param logFilePath 日志文件路径
     */
    private void analyzeLogFile(String logFilePath) {
        try {
            HDFSUtil hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");
            
            // 读取日志文件内容
            String logContent = hdfsUtil.readFile(logFilePath);
            if (logContent == null) {
                logger.error("无法读取日志文件: {}", logFilePath);
                return;
            }
            
            // 分析统计
            Map<String, Integer> ipCount = new HashMap<>();
            Map<String, Integer> statusCount = new HashMap<>();
            Map<String, Integer> methodCount = new HashMap<>();
            long totalBytes = 0;
            int totalRequests = 0;
            
            BufferedReader reader = new BufferedReader(new StringReader(logContent));
            String line;
            
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                
                if (matcher.matches()) {
                    String ip = matcher.group(1);
                    String method = matcher.group(3);
                    String statusCode = matcher.group(6);
                    String responseSize = matcher.group(7);
                    
                    // 统计IP访问次数
                    ipCount.put(ip, ipCount.getOrDefault(ip, 0) + 1);
                    
                    // 统计状态码
                    statusCount.put(statusCode, statusCount.getOrDefault(statusCode, 0) + 1);
                    
                    // 统计请求方法
                    methodCount.put(method, methodCount.getOrDefault(method, 0) + 1);
                    
                    // 累计响应大小
                    totalBytes += Long.parseLong(responseSize);
                    totalRequests++;
                }
            }
            
            // 输出分析结果
            logger.info("=== 日志分析结果 ===");
            logger.info("总请求数: {}", totalRequests);
            logger.info("总响应字节数: {} ({} MB)", totalBytes, totalBytes / 1024 / 1024);
            logger.info("平均响应大小: {} 字节", totalBytes / totalRequests);
            
            logger.info("\n=== IP访问统计 ===");
            ipCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.info("{}: {} 次", entry.getKey(), entry.getValue()));
            
            logger.info("\n=== 状态码统计 ===");
            statusCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.info("{}: {} 次", entry.getKey(), entry.getValue()));
            
            logger.info("\n=== 请求方法统计 ===");
            methodCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.info("{}: {} 次", entry.getKey(), entry.getValue()));
            
            // 保存分析结果到HDFS
            saveAnalysisResult(hdfsUtil, ipCount, statusCount, methodCount, totalRequests, totalBytes);
            
            hdfsUtil.close();
            
        } catch (Exception e) {
            logger.error("分析日志文件异常", e);
        }
    }
    
    /**
     * 保存分析结果到HDFS
     */
    private void saveAnalysisResult(HDFSUtil hdfsUtil, Map<String, Integer> ipCount, 
                                   Map<String, Integer> statusCount, Map<String, Integer> methodCount,
                                   int totalRequests, long totalBytes) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== 日志分析报告 ===\n");
            result.append("分析时间: ").append(new java.util.Date()).append("\n\n");
            
            result.append("总体统计:\n");
            result.append("总请求数: ").append(totalRequests).append("\n");
            result.append("总响应字节数: ").append(totalBytes).append("\n");
            result.append("平均响应大小: ").append(totalBytes / totalRequests).append(" 字节\n\n");
            
            result.append("IP访问排行:\n");
            ipCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> result.append(entry.getKey()).append(": ").append(entry.getValue()).append(" 次\n"));
            
            result.append("\n状态码统计:\n");
            statusCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> result.append(entry.getKey()).append(": ").append(entry.getValue()).append(" 次\n"));
            
            // 保存到HDFS
            hdfsUtil.writeFile("/user/bigdata/log_analysis_report.txt", result.toString());
            logger.info("分析结果已保存到: /user/bigdata/log_analysis_report.txt");
            
        } catch (Exception e) {
            logger.error("保存分析结果异常", e);
        }
    }
}