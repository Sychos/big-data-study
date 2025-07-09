package com.bigdata.hdfs.util;

import org.apache.hadoop.fs.FileStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.*;

/**
 * HDFS工具类单元测试
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class HDFSUtilTest {
    
    private static final Logger logger = LoggerFactory.getLogger(HDFSUtilTest.class);
    
    private HDFSUtil hdfsUtil;
    private static final String HDFS_URI = "hdfs://10.132.144.24:9000";
    private static final String TEST_DIR = "/user/test";
    private static final String TEST_FILE = "/user/test/test.txt";
    private static final String LOCAL_TEST_FILE = "test_local.txt";
    
    @Before
    public void setUp() throws Exception {
        // 初始化HDFS连接
        hdfsUtil = new HDFSUtil(HDFS_URI);
        
        // 创建测试目录
        hdfsUtil.createDirectory(TEST_DIR);
        
        // 创建本地测试文件
        createLocalTestFile();
    }
    
    @After
    public void tearDown() {
        try {
            // 清理测试数据
            hdfsUtil.deleteFile(TEST_DIR, true);
            
            // 删除本地测试文件
            File localFile = new File(LOCAL_TEST_FILE);
            if (localFile.exists()) {
                localFile.delete();
            }
            
            // 关闭连接
            hdfsUtil.close();
        } catch (Exception e) {
            logger.error("清理测试环境异常", e);
        }
    }
    
    @Test
    public void testCreateDirectory() {
        String testDir = "/user/test/subdir";
        boolean result = hdfsUtil.createDirectory(testDir);
        assertTrue("创建目录应该成功", result);
        
        // 再次创建相同目录，应该返回true（目录已存在）
        result = hdfsUtil.createDirectory(testDir);
        assertTrue("创建已存在的目录应该返回true", result);
    }
    
    @Test
    public void testWriteAndReadFile() {
        String content = "这是一个测试文件\n包含中文内容\n当前时间: " + new java.util.Date();
        
        // 写入文件
        boolean writeResult = hdfsUtil.writeFile(TEST_FILE, content);
        assertTrue("写入文件应该成功", writeResult);
        
        // 读取文件
        String readContent = hdfsUtil.readFile(TEST_FILE);
        assertNotNull("读取的内容不应该为null", readContent);
        assertEquals("读取的内容应该与写入的内容一致", content, readContent);
    }
    
    @Test
    public void testUploadAndDownloadFile() {
        String hdfsFile = "/user/test/uploaded.txt";
        String downloadedFile = "downloaded.txt";
        
        // 上传文件
        boolean uploadResult = hdfsUtil.uploadFile(LOCAL_TEST_FILE, hdfsFile);
        assertTrue("上传文件应该成功", uploadResult);
        
        // 下载文件
        boolean downloadResult = hdfsUtil.downloadFile(hdfsFile, downloadedFile);
        assertTrue("下载文件应该成功", downloadResult);
        
        // 验证下载的文件存在
        File downloaded = new File(downloadedFile);
        assertTrue("下载的文件应该存在", downloaded.exists());
        
        // 清理下载的文件
        downloaded.delete();
    }
    
    @Test
    public void testGetFileStatus() {
        String content = "测试文件状态";
        hdfsUtil.writeFile(TEST_FILE, content);
        
        FileStatus status = hdfsUtil.getFileStatus(TEST_FILE);
        assertNotNull("文件状态不应该为null", status);
        assertEquals("文件大小应该正确", content.getBytes().length, status.getLen());
        assertFalse("应该不是目录", status.isDirectory());
    }
    
    @Test
    public void testListFiles() {
        // 创建几个测试文件
        hdfsUtil.writeFile("/user/test/file1.txt", "文件1");
        hdfsUtil.writeFile("/user/test/file2.txt", "文件2");
        hdfsUtil.createDirectory("/user/test/subdir");
        
        FileStatus[] files = hdfsUtil.listFiles(TEST_DIR);
        assertNotNull("文件列表不应该为null", files);
        assertTrue("应该至少有3个文件/目录", files.length >= 3);
    }
    
    @Test
    public void testDeleteFile() {
        // 创建测试文件
        hdfsUtil.writeFile(TEST_FILE, "要删除的文件");
        
        // 删除文件
        boolean result = hdfsUtil.deleteFile(TEST_FILE, false);
        assertTrue("删除文件应该成功", result);
        
        // 验证文件已被删除
        String content = hdfsUtil.readFile(TEST_FILE);
        assertNull("删除后读取文件应该返回null", content);
    }
    
    @Test
    public void testDeleteNonExistentFile() {
        String nonExistentFile = "/user/test/non_existent.txt";
        
        // 删除不存在的文件应该返回true
        boolean result = hdfsUtil.deleteFile(nonExistentFile, false);
        assertTrue("删除不存在的文件应该返回true", result);
    }
    
    @Test
    public void testReadNonExistentFile() {
        String nonExistentFile = "/user/test/non_existent.txt";
        
        String content = hdfsUtil.readFile(nonExistentFile);
        assertNull("读取不存在的文件应该返回null", content);
    }
    
    @Test
    public void testUploadNonExistentLocalFile() {
        String nonExistentLocal = "non_existent_local.txt";
        String hdfsFile = "/user/test/upload_test.txt";
        
        boolean result = hdfsUtil.uploadFile(nonExistentLocal, hdfsFile);
        assertFalse("上传不存在的本地文件应该失败", result);
    }
    
    /**
     * 创建本地测试文件
     */
    private void createLocalTestFile() {
        try {
            FileWriter writer = new FileWriter(LOCAL_TEST_FILE);
            writer.write("这是一个本地测试文件\n");
            writer.write("用于单元测试\n");
            writer.write("创建时间: " + new java.util.Date() + "\n");
            writer.close();
        } catch (Exception e) {
            logger.error("创建本地测试文件失败", e);
        }
    }
}