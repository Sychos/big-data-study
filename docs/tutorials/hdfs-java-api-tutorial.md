# HDFS Java API 操作教程

## 目录
1. [HDFS Java API 概述](#1-hdfs-java-api-概述)
2. [环境准备](#2-环境准备)
3. [Maven项目配置](#3-maven项目配置)
4. [核心API介绍](#4-核心api介绍)
5. [基础操作示例](#5-基础操作示例)
6. [高级操作示例](#6-高级操作示例)
7. [完整项目案例](#7-完整项目案例)
8. [常见问题解决](#8-常见问题解决)

## 1. HDFS Java API 概述

### 1.1 什么是HDFS Java API
HDFS Java API是Hadoop提供的用于与HDFS文件系统进行交互的Java编程接口。通过这些API，我们可以：
- 创建、删除、重命名文件和目录
- 上传和下载文件
- 读取和写入文件内容
- 获取文件和目录信息
- 设置文件权限

### 1.2 核心类介绍
- **Configuration**: 配置类，用于设置Hadoop配置参数
- **FileSystem**: 文件系统抽象类，提供文件操作方法
- **Path**: 路径类，表示HDFS中的文件或目录路径
- **FSDataInputStream**: HDFS输入流
- **FSDataOutputStream**: HDFS输出流

## 2. 环境准备

### 2.1 前置条件
- JDK 1.8+
- Maven 3.5+
- Hadoop 3.3.4（已安装并启动）
- IntelliJ IDEA 或 Eclipse

### 2.2 验证Hadoop环境
```bash
# 检查HDFS是否正常运行
hdfs dfsadmin -report

# 检查Web UI
# 访问 http://localhost:9870
```

## 3. Maven项目配置

### 3.1 创建Maven项目
```bash
# 创建项目目录
mkdir hdfs-java-demo
cd hdfs-java-demo

# 创建Maven项目结构
mkdir -p src/main/java/com/bigdata/hdfs
mkdir -p src/main/resources
mkdir -p src/test/java
```

### 3.2 pom.xml配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.bigdata</groupId>
    <artifactId>hdfs-java-demo</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <hadoop.version>3.3.4</hadoop.version>
        <junit.version>4.13.2</junit.version>
        <slf4j.version>1.7.36</slf4j.version>
    </properties>
    
    <dependencies>
        <!-- Hadoop Client -->
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
        
        <!-- Hadoop HDFS -->
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-hdfs</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
        
        <!-- Hadoop Common -->
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-common</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
        
        <!-- 日志依赖 -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.3 log4j.properties配置
在 `src/main/resources` 目录下创建 `log4j.properties`：
```properties
log4j.rootLogger=INFO, stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# 减少Hadoop日志输出
log4j.logger.org.apache.hadoop=WARN
log4j.logger.org.apache.zookeeper=WARN
```

## 4. 核心API介绍

### 4.1 Configuration类
```java
// 创建配置对象
Configuration conf = new Configuration();

// 设置HDFS地址
conf.set("fs.defaultFS", "hdfs://10.132.144.24:9000");

// 设置用户名（可选）
System.setProperty("HADOOP_USER_NAME", "hadoop");
```

### 4.2 FileSystem类
```java
// 获取文件系统实例
FileSystem fs = FileSystem.get(conf);

// 或者指定URI
URI uri = new URI("hdfs://10.132.144.24:9000");
FileSystem fs = FileSystem.get(uri, conf);
```

### 4.3 Path类
```java
// 创建路径对象
Path path = new Path("/user/data/test.txt");

// 路径操作
Path parent = path.getParent();  // 获取父目录
String name = path.getName();    // 获取文件名
```

## 5. 基础操作示例

### 5.1 HDFS工具类
```java
package com.bigdata.hdfs.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;

/**
 * HDFS操作工具类
 * 提供HDFS文件系统的基本操作功能
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HDFSUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(HDFSUtil.class);
    
    private FileSystem fileSystem;
    private Configuration configuration;
    
    /**
     * 构造函数，初始化HDFS连接
     * 
     * @param hdfsUri HDFS地址，例如：hdfs://10.132.144.24:9000
     * @throws Exception 初始化异常
     */
    public HDFSUtil(String hdfsUri) throws Exception {
        this.configuration = new Configuration();
        this.configuration.set("fs.defaultFS", hdfsUri);
        
        // 设置用户名，避免权限问题
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        
        // 获取文件系统实例
        this.fileSystem = FileSystem.get(URI.create(hdfsUri), configuration);
        
        logger.info("HDFS连接初始化成功: {}", hdfsUri);
    }
    
    /**
     * 创建目录
     * 
     * @param dirPath 目录路径
     * @return 创建成功返回true，否则返回false
     */
    public boolean createDirectory(String dirPath) {
        try {
            Path path = new Path(dirPath);
            if (fileSystem.exists(path)) {
                logger.warn("目录已存在: {}", dirPath);
                return true;
            }
            
            boolean result = fileSystem.mkdirs(path);
            if (result) {
                logger.info("目录创建成功: {}", dirPath);
            } else {
                logger.error("目录创建失败: {}", dirPath);
            }
            return result;
        } catch (Exception e) {
            logger.error("创建目录异常: {}", dirPath, e);
            return false;
        }
    }
    
    /**
     * 上传本地文件到HDFS
     * 
     * @param localFilePath 本地文件路径
     * @param hdfsFilePath HDFS文件路径
     * @return 上传成功返回true，否则返回false
     */
    public boolean uploadFile(String localFilePath, String hdfsFilePath) {
        try {
            Path localPath = new Path(localFilePath);
            Path hdfsPath = new Path(hdfsFilePath);
            
            // 检查本地文件是否存在
            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                logger.error("本地文件不存在: {}", localFilePath);
                return false;
            }
            
            // 如果HDFS文件已存在，先删除
            if (fileSystem.exists(hdfsPath)) {
                fileSystem.delete(hdfsPath, false);
                logger.info("删除已存在的HDFS文件: {}", hdfsFilePath);
            }
            
            // 上传文件
            fileSystem.copyFromLocalFile(localPath, hdfsPath);
            logger.info("文件上传成功: {} -> {}", localFilePath, hdfsFilePath);
            return true;
        } catch (Exception e) {
            logger.error("文件上传异常: {} -> {}", localFilePath, hdfsFilePath, e);
            return false;
        }
    }
    
    /**
     * 从HDFS下载文件到本地
     * 
     * @param hdfsFilePath HDFS文件路径
     * @param localFilePath 本地文件路径
     * @return 下载成功返回true，否则返回false
     */
    public boolean downloadFile(String hdfsFilePath, String localFilePath) {
        try {
            Path hdfsPath = new Path(hdfsFilePath);
            Path localPath = new Path(localFilePath);
            
            // 检查HDFS文件是否存在
            if (!fileSystem.exists(hdfsPath)) {
                logger.error("HDFS文件不存在: {}", hdfsFilePath);
                return false;
            }
            
            // 下载文件
            fileSystem.copyToLocalFile(hdfsPath, localPath);
            logger.info("文件下载成功: {} -> {}", hdfsFilePath, localFilePath);
            return true;
        } catch (Exception e) {
            logger.error("文件下载异常: {} -> {}", hdfsFilePath, localFilePath, e);
            return false;
        }
    }
    
    /**
     * 删除HDFS文件或目录
     * 
     * @param hdfsPath HDFS路径
     * @param recursive 是否递归删除（用于目录）
     * @return 删除成功返回true，否则返回false
     */
    public boolean deleteFile(String hdfsPath, boolean recursive) {
        try {
            Path path = new Path(hdfsPath);
            
            if (!fileSystem.exists(path)) {
                logger.warn("文件或目录不存在: {}", hdfsPath);
                return true;
            }
            
            boolean result = fileSystem.delete(path, recursive);
            if (result) {
                logger.info("删除成功: {}", hdfsPath);
            } else {
                logger.error("删除失败: {}", hdfsPath);
            }
            return result;
        } catch (Exception e) {
            logger.error("删除异常: {}", hdfsPath, e);
            return false;
        }
    }
    
    /**
     * 列出目录下的文件和子目录
     * 
     * @param dirPath 目录路径
     * @return 文件状态数组
     */
    public FileStatus[] listFiles(String dirPath) {
        try {
            Path path = new Path(dirPath);
            
            if (!fileSystem.exists(path)) {
                logger.error("目录不存在: {}", dirPath);
                return new FileStatus[0];
            }
            
            FileStatus[] fileStatuses = fileSystem.listStatus(path);
            logger.info("目录 {} 包含 {} 个文件/目录", dirPath, fileStatuses.length);
            
            for (FileStatus status : fileStatuses) {
                String type = status.isDirectory() ? "目录" : "文件";
                logger.info("{}: {} (大小: {} 字节)", type, status.getPath().getName(), status.getLen());
            }
            
            return fileStatuses;
        } catch (Exception e) {
            logger.error("列出文件异常: {}", dirPath, e);
            return new FileStatus[0];
        }
    }
    
    /**
     * 读取HDFS文件内容
     * 
     * @param hdfsFilePath HDFS文件路径
     * @return 文件内容字符串
     */
    public String readFile(String hdfsFilePath) {
        FSDataInputStream inputStream = null;
        try {
            Path path = new Path(hdfsFilePath);
            
            if (!fileSystem.exists(path)) {
                logger.error("文件不存在: {}", hdfsFilePath);
                return null;
            }
            
            inputStream = fileSystem.open(path);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copyBytes(inputStream, outputStream, configuration);
            
            String content = outputStream.toString("UTF-8");
            logger.info("文件读取成功: {} (大小: {} 字节)", hdfsFilePath, content.length());
            return content;
        } catch (Exception e) {
            logger.error("文件读取异常: {}", hdfsFilePath, e);
            return null;
        } finally {
            IOUtils.closeStream(inputStream);
        }
    }
    
    /**
     * 写入内容到HDFS文件
     * 
     * @param hdfsFilePath HDFS文件路径
     * @param content 文件内容
     * @return 写入成功返回true，否则返回false
     */
    public boolean writeFile(String hdfsFilePath, String content) {
        FSDataOutputStream outputStream = null;
        try {
            Path path = new Path(hdfsFilePath);
            
            // 如果文件已存在，先删除
            if (fileSystem.exists(path)) {
                fileSystem.delete(path, false);
            }
            
            outputStream = fileSystem.create(path);
            outputStream.write(content.getBytes("UTF-8"));
            outputStream.flush();
            
            logger.info("文件写入成功: {} (大小: {} 字节)", hdfsFilePath, content.length());
            return true;
        } catch (Exception e) {
            logger.error("文件写入异常: {}", hdfsFilePath, e);
            return false;
        } finally {
            IOUtils.closeStream(outputStream);
        }
    }
    
    /**
     * 获取文件信息
     * 
     * @param hdfsFilePath HDFS文件路径
     * @return 文件状态对象
     */
    public FileStatus getFileStatus(String hdfsFilePath) {
        try {
            Path path = new Path(hdfsFilePath);
            
            if (!fileSystem.exists(path)) {
                logger.error("文件不存在: {}", hdfsFilePath);
                return null;
            }
            
            FileStatus status = fileSystem.getFileStatus(path);
            logger.info("文件信息 - 路径: {}, 大小: {} 字节, 修改时间: {}", 
                       status.getPath(), status.getLen(), status.getModificationTime());
            return status;
        } catch (Exception e) {
            logger.error("获取文件信息异常: {}", hdfsFilePath, e);
            return null;
        }
    }
    
    /**
     * 关闭文件系统连接
     */
    public void close() {
        try {
            if (fileSystem != null) {
                fileSystem.close();
                logger.info("HDFS连接已关闭");
            }
        } catch (Exception e) {
            logger.error("关闭HDFS连接异常", e);
        }
    }
}
```

### 5.2 基础操作示例类
```java
package com.bigdata.hdfs.example;

import com.bigdata.hdfs.util.HDFSUtil;
import org.apache.hadoop.fs.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HDFS基础操作示例
 * 演示HDFS的基本文件操作功能
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HDFSBasicExample {
    
    private static final Logger logger = LoggerFactory.getLogger(HDFSBasicExample.class);
    
    public static void main(String[] args) {
        HDFSUtil hdfsUtil = null;
        
        try {
            // 初始化HDFS连接
            hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");
            
            // 1. 创建目录
            logger.info("=== 1. 创建目录测试 ===");
            hdfsUtil.createDirectory("/user/bigdata");
            hdfsUtil.createDirectory("/user/bigdata/input");
            hdfsUtil.createDirectory("/user/bigdata/output");
            
            // 2. 写入文件
            logger.info("=== 2. 写入文件测试 ===");
            String content = "Hello HDFS!\n这是一个HDFS Java API测试文件。\n当前时间: " + 
                           new java.util.Date();
            hdfsUtil.writeFile("/user/bigdata/test.txt", content);
            
            // 3. 读取文件
            logger.info("=== 3. 读取文件测试 ===");
            String readContent = hdfsUtil.readFile("/user/bigdata/test.txt");
            logger.info("读取的文件内容:\n{}", readContent);
            
            // 4. 获取文件信息
            logger.info("=== 4. 获取文件信息测试 ===");
            FileStatus fileStatus = hdfsUtil.getFileStatus("/user/bigdata/test.txt");
            if (fileStatus != null) {
                logger.info("文件路径: {}", fileStatus.getPath());
                logger.info("文件大小: {} 字节", fileStatus.getLen());
                logger.info("是否为目录: {}", fileStatus.isDirectory());
                logger.info("修改时间: {}", new java.util.Date(fileStatus.getModificationTime()));
                logger.info("文件权限: {}", fileStatus.getPermission());
            }
            
            // 5. 列出目录内容
            logger.info("=== 5. 列出目录内容测试 ===");
            hdfsUtil.listFiles("/user/bigdata");
            
            // 6. 创建本地测试文件并上传
            logger.info("=== 6. 文件上传测试 ===");
            createLocalTestFile();
            hdfsUtil.uploadFile("local_test.txt", "/user/bigdata/uploaded_test.txt");
            
            // 7. 下载文件
            logger.info("=== 7. 文件下载测试 ===");
            hdfsUtil.downloadFile("/user/bigdata/uploaded_test.txt", "downloaded_test.txt");
            
            // 8. 删除文件
            logger.info("=== 8. 删除文件测试 ===");
            hdfsUtil.deleteFile("/user/bigdata/test.txt", false);
            
            logger.info("=== HDFS基础操作测试完成 ===");
            
        } catch (Exception e) {
            logger.error("HDFS操作异常", e);
        } finally {
            // 关闭连接
            if (hdfsUtil != null) {
                hdfsUtil.close();
            }
        }
    }
    
    /**
     * 创建本地测试文件
     */
    private static void createLocalTestFile() {
        try {
            java.io.FileWriter writer = new java.io.FileWriter("local_test.txt");
            writer.write("这是一个本地测试文件\n");
            writer.write("用于测试HDFS文件上传功能\n");
            writer.write("创建时间: " + new java.util.Date() + "\n");
            writer.close();
            logger.info("本地测试文件创建成功: local_test.txt");
        } catch (Exception e) {
            logger.error("创建本地测试文件失败", e);
        }
    }
}
```

## 6. 高级操作示例

### 6.1 大文件处理示例
```java
package com.bigdata.hdfs.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * HDFS大文件处理示例
 * 演示如何处理大文件的读写操作
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HDFSLargeFileExample {
    
    private static final Logger logger = LoggerFactory.getLogger(HDFSLargeFileExample.class);
    
    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", "hdfs://10.132.144.24:9000");
            System.setProperty("HADOOP_USER_NAME", "hadoop");
            
            FileSystem fs = FileSystem.get(URI.create("hdfs://10.132.144.24:9000"), conf);
            
            // 1. 创建大文件
            createLargeFile(fs);
            
            // 2. 流式读取大文件
            streamReadLargeFile(fs);
            
            // 3. 分块读取大文件
            blockReadLargeFile(fs);
            
            fs.close();
            
        } catch (Exception e) {
            logger.error("大文件处理异常", e);
        }
    }
    
    /**
     * 创建大文件（模拟）
     * 
     * @param fs 文件系统对象
     */
    private static void createLargeFile(FileSystem fs) {
        FSDataOutputStream out = null;
        try {
            Path largePath = new Path("/user/bigdata/large_file.txt");
            
            // 如果文件存在，先删除
            if (fs.exists(largePath)) {
                fs.delete(largePath, false);
            }
            
            out = fs.create(largePath);
            
            // 写入大量数据（模拟大文件）
            for (int i = 0; i < 100000; i++) {
                String line = String.format("这是第 %d 行数据，包含一些测试内容用于演示大文件处理\n", i + 1);
                out.write(line.getBytes("UTF-8"));
                
                if (i % 10000 == 0) {
                    logger.info("已写入 {} 行数据", i);
                }
            }
            
            out.flush();
            logger.info("大文件创建完成: {}", largePath);
            
        } catch (Exception e) {
            logger.error("创建大文件异常", e);
        } finally {
            IOUtils.closeStream(out);
        }
    }
    
    /**
     * 流式读取大文件
     * 
     * @param fs 文件系统对象
     */
    private static void streamReadLargeFile(FileSystem fs) {
        FSDataInputStream in = null;
        BufferedReader reader = null;
        
        try {
            Path largePath = new Path("/user/bigdata/large_file.txt");
            
            if (!fs.exists(largePath)) {
                logger.error("大文件不存在: {}", largePath);
                return;
            }
            
            in = fs.open(largePath);
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            
            String line;
            int lineCount = 0;
            long startTime = System.currentTimeMillis();
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                // 每读取1000行输出一次进度
                if (lineCount % 1000 == 0) {
                    logger.info("已读取 {} 行", lineCount);
                }
                
                // 这里可以处理每一行数据
                // processLine(line);
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("流式读取完成，总共读取 {} 行，耗时 {} 毫秒", lineCount, endTime - startTime);
            
        } catch (Exception e) {
            logger.error("流式读取大文件异常", e);
        } finally {
            IOUtils.closeStream(reader);
            IOUtils.closeStream(in);
        }
    }
    
    /**
     * 分块读取大文件
     * 
     * @param fs 文件系统对象
     */
    private static void blockReadLargeFile(FileSystem fs) {
        FSDataInputStream in = null;
        
        try {
            Path largePath = new Path("/user/bigdata/large_file.txt");
            
            if (!fs.exists(largePath)) {
                logger.error("大文件不存在: {}", largePath);
                return;
            }
            
            FileStatus fileStatus = fs.getFileStatus(largePath);
            long fileSize = fileStatus.getLen();
            logger.info("文件大小: {} 字节", fileSize);
            
            in = fs.open(largePath);
            
            // 分块读取，每块1MB
            int bufferSize = 1024 * 1024; // 1MB
            byte[] buffer = new byte[bufferSize];
            long totalRead = 0;
            int blockCount = 0;
            
            long startTime = System.currentTimeMillis();
            
            while (totalRead < fileSize) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                
                totalRead += bytesRead;
                blockCount++;
                
                // 处理读取的数据块
                // processBlock(buffer, bytesRead);
                
                logger.info("已读取块 {}, 大小 {} 字节, 总进度 {}/{} 字节", 
                           blockCount, bytesRead, totalRead, fileSize);
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("分块读取完成，总共读取 {} 个块，耗时 {} 毫秒", blockCount, endTime - startTime);
            
        } catch (Exception e) {
            logger.error("分块读取大文件异常", e);
        } finally {
            IOUtils.closeStream(in);
        }
    }
}
```

## 7. 完整项目案例

### 7.1 日志文件分析器
```java
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
```

## 8. 常见问题解决

### 8.1 权限问题
```java
// 设置用户名
System.setProperty("HADOOP_USER_NAME", "hadoop");

// 或者在Configuration中设置
conf.set("hadoop.security.authentication", "simple");
conf.set("hadoop.security.authorization", "false");
```

### 8.2 连接问题
```java
// 检查HDFS服务是否启动
// 确保防火墙设置正确
// 验证配置文件中的地址和端口

// 添加连接超时设置
conf.set("ipc.client.connect.timeout", "10000");
conf.set("ipc.client.connect.max.retries", "3");
```

### 8.3 内存问题
```java
// 处理大文件时设置合适的缓冲区大小
conf.set("io.file.buffer.size", "131072"); // 128KB

// JVM参数设置
// -Xmx2g -Xms1g
```

### 8.4 编码问题
```java
// 统一使用UTF-8编码
String content = new String(bytes, "UTF-8");
outputStream.write(content.getBytes("UTF-8"));
```

## 总结

通过本教程，您已经学会了：

1. **环境配置**: Maven项目配置和依赖管理
2. **基础操作**: 文件和目录的创建、删除、上传、下载
3. **高级操作**: 大文件处理、流式读写
4. **实际应用**: 日志文件分析项目
5. **问题解决**: 常见问题的解决方案

**下一步学习建议：**
- 深入学习MapReduce编程
- 了解HDFS的内部机制
- 学习Hadoop集群管理
- 探索其他Hadoop生态组件

**练习建议：**
1. 实现一个文件备份工具
2. 开发一个日志收集系统
3. 创建一个数据迁移工具
4. 构建一个文件监控系统

祝您学习愉快！🚀