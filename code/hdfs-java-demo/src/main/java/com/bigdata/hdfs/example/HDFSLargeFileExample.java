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