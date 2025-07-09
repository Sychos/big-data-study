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