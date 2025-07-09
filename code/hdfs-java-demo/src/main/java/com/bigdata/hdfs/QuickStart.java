package com.bigdata.hdfs;

import com.bigdata.hdfs.util.HDFSUtil;
import org.apache.hadoop.fs.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HDFS Java API 快速入门示例
 * 这是一个简单的入门示例，演示最基本的HDFS操作
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class QuickStart {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickStart.class);
    
    public static void main(String[] args) {
        logger.info("=== HDFS Java API 快速入门示例 ===");
        
        HDFSUtil hdfsUtil = null;
        
        try {
            // 1. 初始化HDFS连接
            logger.info("1. 初始化HDFS连接...");
            hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");
            
            // 2. 创建目录
            logger.info("2. 创建目录...");
            hdfsUtil.createDirectory("/user/quickstart");
            
            // 3. 写入文件
            logger.info("3. 写入文件...");
            String content = "欢迎使用HDFS Java API!\n" +
                           "这是一个快速入门示例。\n" +
                           "当前时间: " + new java.util.Date() + "\n" +
                           "HDFS让大数据存储变得简单！";
            hdfsUtil.writeFile("/user/quickstart/welcome.txt", content);
            
            // 4. 读取文件
            logger.info("4. 读取文件...");
            String readContent = hdfsUtil.readFile("/user/quickstart/welcome.txt");
            logger.info("读取到的文件内容:\n{}", readContent);
            
            // 5. 获取文件信息
            logger.info("5. 获取文件信息...");
            FileStatus fileStatus = hdfsUtil.getFileStatus("/user/quickstart/welcome.txt");
            if (fileStatus != null) {
                logger.info("文件大小: {} 字节", fileStatus.getLen());
                logger.info("修改时间: {}", new java.util.Date(fileStatus.getModificationTime()));
            }
            
            // 6. 列出目录内容
            logger.info("6. 列出目录内容...");
            hdfsUtil.listFiles("/user/quickstart");
            
            // 7. 创建更多示例文件
            logger.info("7. 创建更多示例文件...");
            hdfsUtil.writeFile("/user/quickstart/data1.txt", "这是数据文件1\n包含一些示例数据");
            hdfsUtil.writeFile("/user/quickstart/data2.txt", "这是数据文件2\n包含更多示例数据");
            
            // 8. 再次列出目录，查看所有文件
            logger.info("8. 查看所有创建的文件...");
            hdfsUtil.listFiles("/user/quickstart");
            
            logger.info("=== 快速入门示例完成！===");
            logger.info("恭喜！您已经成功完成了HDFS Java API的基本操作。");
            logger.info("接下来可以尝试运行其他示例程序：");
            logger.info("- HDFSBasicExample: 更全面的基础操作示例");
            logger.info("- HDFSLargeFileExample: 大文件处理示例");
            logger.info("- LogAnalyzer: 日志分析项目案例");
            
        } catch (Exception e) {
            logger.error("快速入门示例执行异常", e);
            logger.error("请检查：");
            logger.error("1. Hadoop服务是否正常启动");
            logger.error("2. HDFS地址配置是否正确");
            logger.error("3. 网络连接是否正常");
        } finally {
            // 关闭连接
            if (hdfsUtil != null) {
                hdfsUtil.close();
            }
        }
    }
}