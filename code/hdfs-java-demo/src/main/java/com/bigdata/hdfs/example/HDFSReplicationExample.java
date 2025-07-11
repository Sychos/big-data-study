package com.bigdata.hdfs.example;

import com.bigdata.hdfs.util.HDFSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HDFS副本数管理示例
 * 演示如何使用HDFSUtil进行副本数的设置和管理
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HDFSReplicationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(HDFSReplicationExample.class);
    
    public static void main(String[] args) {
        HDFSUtil hdfsUtil = null;
        
        try {
            // 1. 创建HDFSUtil实例，设置默认副本数为2
            String hdfsUri = "hdfs://10.132.144.24:9000";
            hdfsUtil = new HDFSUtil(hdfsUri, (short) 2);
            
            logger.info("=== HDFS副本数管理示例 ===");
            
            // 2. 演示基本副本数操作
            demonstrateBasicReplication(hdfsUtil);
            
            // 3. 演示文件上传时指定副本数
            demonstrateUploadWithReplication(hdfsUtil);
            
            // 4. 演示文件写入时指定副本数
            demonstrateWriteWithReplication(hdfsUtil);
            
            // 5. 演示修改现有文件的副本数
            demonstrateModifyReplication(hdfsUtil);
            
            // 6. 演示批量设置副本数
            demonstrateBatchReplication(hdfsUtil);
            
            // 7. 演示副本数查询
            demonstrateQueryReplication(hdfsUtil);
            
        } catch (Exception e) {
            logger.error("HDFS副本数示例执行异常", e);
        } finally {
            if (hdfsUtil != null) {
                hdfsUtil.close();
            }
        }
    }
    
    /**
     * 演示基本副本数操作
     */
    private static void demonstrateBasicReplication(HDFSUtil hdfsUtil) {
        logger.info("\n--- 1. 基本副本数操作 ---");
        
        // 获取当前默认副本数
        short currentReplication = hdfsUtil.getReplicationFactor();
        logger.info("当前默认副本数: {}", currentReplication);
        
        // 修改默认副本数
        hdfsUtil.setReplicationFactor((short) 3);
        logger.info("修改后的默认副本数: {}", hdfsUtil.getReplicationFactor());
    }
    
    /**
     * 演示文件上传时指定副本数
     */
    private static void demonstrateUploadWithReplication(HDFSUtil hdfsUtil) {
        logger.info("\n--- 2. 文件上传时指定副本数 ---");
        
        try {
            // 创建测试文件
            String localFile = "replication_test.txt";
            String content = "这是一个测试副本数功能的文件\n当前时间: " + System.currentTimeMillis();
            
            // 写入本地文件
            java.nio.file.Files.write(
                java.nio.file.Paths.get(localFile), 
                content.getBytes("UTF-8")
            );
            
            // 上传文件并指定副本数为1
            String hdfsPath1 = "/test/replication_1.txt";
            hdfsUtil.uploadFile(localFile, hdfsPath1, (short) 1);
            
            // 上传文件并指定副本数为3
            String hdfsPath3 = "/test/replication_3.txt";
            hdfsUtil.uploadFile(localFile, hdfsPath3, (short) 3);
            
            // 使用默认副本数上传
            String hdfsPathDefault = "/test/replication_default.txt";
            hdfsUtil.uploadFile(localFile, hdfsPathDefault);
            
            // 删除本地测试文件
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(localFile));
            
        } catch (Exception e) {
            logger.error("文件上传示例异常", e);
        }
    }
    
    /**
     * 演示文件写入时指定副本数
     */
    private static void demonstrateWriteWithReplication(HDFSUtil hdfsUtil) {
        logger.info("\n--- 3. 文件写入时指定副本数 ---");
        
        String content = "直接写入HDFS的测试内容\n副本数测试\n时间戳: " + System.currentTimeMillis();
        
        // 写入文件并指定副本数为2
        hdfsUtil.writeFile("/test/write_replication_2.txt", content, (short) 2);
        
        // 写入文件并指定副本数为4
        hdfsUtil.writeFile("/test/write_replication_4.txt", content, (short) 4);
        
        // 使用默认副本数写入
        hdfsUtil.writeFile("/test/write_replication_default.txt", content);
    }
    
    /**
     * 演示修改现有文件的副本数
     */
    private static void demonstrateModifyReplication(HDFSUtil hdfsUtil) {
        logger.info("\n--- 4. 修改现有文件的副本数 ---");
        
        String filePath = "/test/replication_1.txt";
        
        // 查看当前副本数
        short currentReplication = hdfsUtil.getFileReplication(filePath);
        logger.info("文件 {} 当前副本数: {}", filePath, currentReplication);
        
        // 修改副本数为5
        boolean success = hdfsUtil.setFileReplication(filePath, (short) 5);
        if (success) {
            // 再次查看副本数
            short newReplication = hdfsUtil.getFileReplication(filePath);
            logger.info("文件 {} 修改后副本数: {}", filePath, newReplication);
        }
    }
    
    /**
     * 演示批量设置副本数
     */
    private static void demonstrateBatchReplication(HDFSUtil hdfsUtil) {
        logger.info("\n--- 5. 批量设置副本数 ---");
        
        // 创建测试目录和文件
        hdfsUtil.createDirectory("/test/batch_replication");
        hdfsUtil.writeFile("/test/batch_replication/file1.txt", "批量测试文件1", (short) 1);
        hdfsUtil.writeFile("/test/batch_replication/file2.txt", "批量测试文件2", (short) 2);
        hdfsUtil.writeFile("/test/batch_replication/file3.txt", "批量测试文件3", (short) 3);
        
        // 创建子目录
        hdfsUtil.createDirectory("/test/batch_replication/subdir");
        hdfsUtil.writeFile("/test/batch_replication/subdir/file4.txt", "子目录测试文件", (short) 1);
        
        // 批量设置副本数（不递归）
        logger.info("批量设置副本数（不递归）:");
        int count1 = hdfsUtil.setBatchReplication("/test/batch_replication", (short) 2, false);
        logger.info("成功设置 {} 个文件的副本数", count1);
        
        // 批量设置副本数（递归）
        logger.info("批量设置副本数（递归）:");
        int count2 = hdfsUtil.setBatchReplication("/test/batch_replication", (short) 3, true);
        logger.info("成功设置 {} 个文件的副本数", count2);
    }
    
    /**
     * 演示副本数查询
     */
    private static void demonstrateQueryReplication(HDFSUtil hdfsUtil) {
        logger.info("\n--- 6. 副本数查询 ---");
        
        // 查询多个文件的副本数
        String[] testFiles = {
            "/test/replication_1.txt",
            "/test/replication_3.txt",
            "/test/write_replication_2.txt",
            "/test/write_replication_4.txt",
            "/test/batch_replication/file1.txt"
        };
        
        for (String filePath : testFiles) {
            short replication = hdfsUtil.getFileReplication(filePath);
            if (replication != -1) {
                logger.info("文件: {} -> 副本数: {}", filePath, replication);
            }
        }
        
        // 列出目录信息，查看文件详情
        logger.info("\n目录文件详情:");
        hdfsUtil.listFiles("/test");
    }
    
    /**
     * 清理测试数据
     */
    @SuppressWarnings("unused")
    private static void cleanupTestData(HDFSUtil hdfsUtil) {
        logger.info("\n--- 清理测试数据 ---");
        
        // 删除测试目录及其所有内容
        boolean success = hdfsUtil.deleteFile("/test", true);
        if (success) {
            logger.info("测试数据清理完成");
        } else {
            logger.warn("测试数据清理失败");
        }
    }
}