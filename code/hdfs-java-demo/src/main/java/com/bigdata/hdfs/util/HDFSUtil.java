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
    private short replicationFactor = 3; // 默认副本数为3
    
    /**
     * 构造函数，初始化HDFS连接
     * 
     * @param hdfsUri HDFS地址，例如：hdfs://10.132.144.24:9000
     * @throws Exception 初始化异常
     */
    public HDFSUtil(String hdfsUri) throws Exception {
        this(hdfsUri, (short) 3); // 默认副本数为3
    }
    
    /**
     * 构造函数，初始化HDFS连接并设置副本数
     * 
     * @param hdfsUri HDFS地址，例如：hdfs://10.132.144.24:9000
     * @param replicationFactor 副本数
     * @throws Exception 初始化异常
     */
    public HDFSUtil(String hdfsUri, short replicationFactor) throws Exception {
        this.configuration = new Configuration();
        this.configuration.set("fs.defaultFS", hdfsUri);
        this.replicationFactor = replicationFactor;
        
        // 设置默认副本数
        this.configuration.setInt("dfs.replication", replicationFactor);
        
        // 设置用户名，避免权限问题
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        
        // 获取文件系统实例
        this.fileSystem = FileSystem.get(URI.create(hdfsUri), configuration);
        
        logger.info("HDFS连接初始化成功: {}, 副本数: {}", hdfsUri, replicationFactor);
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
        return uploadFile(localFilePath, hdfsFilePath, this.replicationFactor);
    }
    
    /**
     * 上传本地文件到HDFS并指定副本数
     * 
     * @param localFilePath 本地文件路径
     * @param hdfsFilePath HDFS文件路径
     * @param replicationFactor 副本数
     * @return 上传成功返回true，否则返回false
     */
    public boolean uploadFile(String localFilePath, String hdfsFilePath, short replicationFactor) {
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
            
            // 设置副本数
            fileSystem.setReplication(hdfsPath, replicationFactor);
            
            logger.info("文件上传成功: {} -> {}, 副本数: {}", localFilePath, hdfsFilePath, replicationFactor);
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
        return writeFile(hdfsFilePath, content, this.replicationFactor);
    }
    
    /**
     * 写入内容到HDFS文件并指定副本数
     * 
     * @param hdfsFilePath HDFS文件路径
     * @param content 文件内容
     * @param replicationFactor 副本数
     * @return 写入成功返回true，否则返回false
     */
    public boolean writeFile(String hdfsFilePath, String content, short replicationFactor) {
        FSDataOutputStream outputStream = null;
        try {
            Path path = new Path(hdfsFilePath);
            
            // 如果文件已存在，先删除
            if (fileSystem.exists(path)) {
                fileSystem.delete(path, false);
            }
            
            // 创建文件时指定副本数
            outputStream = fileSystem.create(path, true, 4096, replicationFactor, 134217728L);
            outputStream.write(content.getBytes("UTF-8"));
            outputStream.flush();
            
            logger.info("文件写入成功: {} (大小: {} 字节, 副本数: {})", hdfsFilePath, content.length(), replicationFactor);
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
     * 设置副本数
     * 
     * @param replicationFactor 副本数
     */
    public void setReplicationFactor(short replicationFactor) {
        this.replicationFactor = replicationFactor;
        this.configuration.setInt("dfs.replication", replicationFactor);
        logger.info("副本数已设置为: {}", replicationFactor);
    }
    
    /**
     * 获取当前设置的副本数
     * 
     * @return 当前副本数
     */
    public short getReplicationFactor() {
        return this.replicationFactor;
    }
    
    /**
     * 设置指定文件的副本数
     * 
     * @param hdfsFilePath HDFS文件路径
     * @param replicationFactor 副本数
     * @return 设置成功返回true，否则返回false
     */
    public boolean setFileReplication(String hdfsFilePath, short replicationFactor) {
        try {
            Path path = new Path(hdfsFilePath);
            
            if (!fileSystem.exists(path)) {
                logger.error("文件不存在: {}", hdfsFilePath);
                return false;
            }
            
            boolean result = fileSystem.setReplication(path, replicationFactor);
            if (result) {
                logger.info("文件副本数设置成功: {} -> {}", hdfsFilePath, replicationFactor);
            } else {
                logger.error("文件副本数设置失败: {}", hdfsFilePath);
            }
            return result;
        } catch (Exception e) {
            logger.error("设置文件副本数异常: {}", hdfsFilePath, e);
            return false;
        }
    }
    
    /**
     * 获取指定文件的副本数
     * 
     * @param hdfsFilePath HDFS文件路径
     * @return 文件的副本数，如果文件不存在或出错返回-1
     */
    public short getFileReplication(String hdfsFilePath) {
        try {
            Path path = new Path(hdfsFilePath);
            
            if (!fileSystem.exists(path)) {
                logger.error("文件不存在: {}", hdfsFilePath);
                return -1;
            }
            
            FileStatus status = fileSystem.getFileStatus(path);
            short replication = status.getReplication();
            logger.info("文件 {} 的副本数为: {}", hdfsFilePath, replication);
            return replication;
        } catch (Exception e) {
            logger.error("获取文件副本数异常: {}", hdfsFilePath, e);
            return -1;
        }
    }
    
    /**
     * 批量设置目录下所有文件的副本数
     * 
     * @param dirPath 目录路径
     * @param replicationFactor 副本数
     * @param recursive 是否递归处理子目录
     * @return 设置成功的文件数量
     */
    public int setBatchReplication(String dirPath, short replicationFactor, boolean recursive) {
        int successCount = 0;
        try {
            Path path = new Path(dirPath);
            
            if (!fileSystem.exists(path)) {
                logger.error("目录不存在: {}", dirPath);
                return 0;
            }
            
            FileStatus[] fileStatuses = fileSystem.listStatus(path);
            
            for (FileStatus status : fileStatuses) {
                if (status.isFile()) {
                    // 处理文件
                    if (fileSystem.setReplication(status.getPath(), replicationFactor)) {
                        successCount++;
                        logger.info("设置文件副本数成功: {} -> {}", status.getPath(), replicationFactor);
                    }
                } else if (status.isDirectory() && recursive) {
                    // 递归处理子目录
                    successCount += setBatchReplication(status.getPath().toString(), replicationFactor, recursive);
                }
            }
            
            logger.info("批量设置副本数完成，成功处理 {} 个文件", successCount);
        } catch (Exception e) {
            logger.error("批量设置副本数异常: {}", dirPath, e);
        }
        return successCount;
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