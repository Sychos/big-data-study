package com.bigdata.mapreduce.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WordCount Driver类
 * MapReduce作业的主入口，负责配置和提交作业到YARN集群
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class WordCountDriver {
    
    private static final Logger logger = LoggerFactory.getLogger(WordCountDriver.class);
    
    /**
     * 主方法：程序入口
     * 
     * @param args 命令行参数 [输入路径] [输出路径]
     * @throws Exception 异常
     */
    public static void main(String[] args) throws Exception {
        
        // 创建配置对象
        Configuration conf = new Configuration();
        
        // 解析命令行参数
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        
        // 检查参数数量
        if (otherArgs.length != 2) {
            System.err.println("Usage: WordCountDriver <input path> <output path>");
            System.err.println("Example: WordCountDriver /input/text /output/wordcount");
            System.exit(2);
        }
        
        String inputPath = otherArgs[0];
        String outputPath = otherArgs[1];
        
        logger.info("Starting WordCount job with input: {} and output: {}", inputPath, outputPath);
        
        // 运行WordCount作业
        boolean success = runWordCountJob(conf, inputPath, outputPath);
        
        // 根据作业结果退出
        System.exit(success ? 0 : 1);
    }
    
    /**
     * 运行WordCount MapReduce作业
     * 
     * @param conf 配置对象
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     * @return 作业是否成功
     * @throws Exception 异常
     */
    public static boolean runWordCountJob(Configuration conf, String inputPath, String outputPath) 
            throws Exception {
        
        // 设置YARN相关配置
        configureYarn(conf);
        
        // 创建作业对象
        Job job = Job.getInstance(conf, "word count");
        
        // 设置作业的主类
        job.setJarByClass(WordCountDriver.class);
        
        // 设置Mapper和Reducer类
        job.setMapperClass(WordCountMapper.class);
        job.setCombinerClass(WordCountReducer.class);  // 使用Reducer作为Combiner
        job.setReducerClass(WordCountReducer.class);
        
        // 设置输出键值类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        // 设置输入输出格式
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        // 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        // 设置Reduce任务数量
        job.setNumReduceTasks(2);
        
        // 设置作业优先级
        job.setPriority(org.apache.hadoop.mapreduce.JobPriority.NORMAL);
        
        // 设置作业队列（如果配置了多队列）
        // job.setQueueName("default");
        
        // 添加自定义计数器
        setupCounters(job);
        
        // 提交作业并等待完成
        logger.info("Submitting WordCount job to YARN cluster...");
        
        long startTime = System.currentTimeMillis();
        boolean success = job.waitForCompletion(true);
        long endTime = System.currentTimeMillis();
        
        if (success) {
            logger.info("WordCount job completed successfully in {} ms", (endTime - startTime));
            printJobStatistics(job);
        } else {
            logger.error("WordCount job failed");
        }
        
        return success;
    }
    
    /**
     * 配置YARN相关参数
     * 
     * @param conf 配置对象
     */
    private static void configureYarn(Configuration conf) {
        
        // 设置ResourceManager地址（如果需要）
        // conf.set("yarn.resourcemanager.hostname", "10.132.144.24");
        // conf.set("yarn.resourcemanager.address", "10.132.144.24:8032");
        
        // 设置MapReduce框架为YARN
        conf.set("mapreduce.framework.name", "yarn");
        
        // 设置应用程序类型
        conf.set("yarn.app.mapreduce.am.env", "HADOOP_MAPRED_HOME=$HADOOP_HOME");
        conf.set("mapreduce.map.env", "HADOOP_MAPRED_HOME=$HADOOP_HOME");
        conf.set("mapreduce.reduce.env", "HADOOP_MAPRED_HOME=$HADOOP_HOME");
        
        // 设置内存配置
        conf.setInt("mapreduce.map.memory.mb", 1024);
        conf.setInt("mapreduce.reduce.memory.mb", 2048);
        
        // 设置JVM参数
        conf.set("mapreduce.map.java.opts", "-Xmx768m");
        conf.set("mapreduce.reduce.java.opts", "-Xmx1536m");
        
        // 设置ApplicationMaster内存
        conf.setInt("yarn.app.mapreduce.am.resource.mb", 1024);
        
        // 启用推测执行
        conf.setBoolean("mapreduce.map.speculative", true);
        conf.setBoolean("mapreduce.reduce.speculative", true);
        
        logger.info("YARN configuration completed");
    }
    
    /**
     * 设置自定义计数器
     * 
     * @param job 作业对象
     */
    private static void setupCounters(Job job) {
        // 可以在这里设置自定义计数器组
        // 计数器会在Mapper和Reducer中使用
        logger.debug("Custom counters setup completed");
    }
    
    /**
     * 打印作业统计信息
     * 
     * @param job 作业对象
     * @throws Exception 异常
     */
    private static void printJobStatistics(Job job) throws Exception {
        
        // 获取作业计数器
        org.apache.hadoop.mapreduce.Counters counters = job.getCounters();
        
        // 打印基本统计信息
        logger.info("=== Job Statistics ===");
        logger.info("Job ID: {}", job.getJobID());
        logger.info("Job Name: {}", job.getJobName());
        logger.info("Job State: {}", job.getJobState());
        
        // 打印Map任务统计
        if (counters != null) {
            long mapInputRecords = counters.findCounter(
                org.apache.hadoop.mapreduce.TaskCounter.MAP_INPUT_RECORDS).getValue();
            long mapOutputRecords = counters.findCounter(
                org.apache.hadoop.mapreduce.TaskCounter.MAP_OUTPUT_RECORDS).getValue();
            
            logger.info("Map Input Records: {}", mapInputRecords);
            logger.info("Map Output Records: {}", mapOutputRecords);
            
            // 打印Reduce任务统计
            long reduceInputRecords = counters.findCounter(
                org.apache.hadoop.mapreduce.TaskCounter.REDUCE_INPUT_RECORDS).getValue();
            long reduceOutputRecords = counters.findCounter(
                org.apache.hadoop.mapreduce.TaskCounter.REDUCE_OUTPUT_RECORDS).getValue();
            
            logger.info("Reduce Input Records: {}", reduceInputRecords);
            logger.info("Reduce Output Records: {}", reduceOutputRecords);
            
            // 打印文件系统统计
            long hdfsReadBytes = counters.findCounter(
                "org.apache.hadoop.mapreduce.FileSystemCounter", "HDFS_BYTES_READ").getValue();
            long hdfsWriteBytes = counters.findCounter(
                "org.apache.hadoop.mapreduce.FileSystemCounter", "HDFS_BYTES_WRITTEN").getValue();
            
            logger.info("HDFS Bytes Read: {} MB", hdfsReadBytes / (1024 * 1024));
            logger.info("HDFS Bytes Written: {} MB", hdfsWriteBytes / (1024 * 1024));
        }
        
        logger.info("=== End of Statistics ===");
    }
    

}