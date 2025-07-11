package com.bigdata.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * WordCount Mapper类
 * 负责将输入文本分词并输出<单词, 1>键值对
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    
    private static final Logger logger = LoggerFactory.getLogger(WordCountMapper.class);
    
    // 可重用的输出对象，避免频繁创建对象
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    
    /**
     * Map方法：处理输入的每一行文本
     * 
     * @param key 输入键（行偏移量）
     * @param value 输入值（一行文本）
     * @param context 上下文对象，用于输出结果
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public void map(LongWritable key, Text value, Context context) 
            throws IOException, InterruptedException {
        
        // 将输入文本转换为小写并分词
        String line = value.toString().toLowerCase();
        
        // 使用StringTokenizer进行分词
        StringTokenizer tokenizer = new StringTokenizer(line);
        
        // 遍历每个单词
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            
            // 简单的单词清理：移除标点符号
            token = cleanWord(token);
            
            // 过滤空字符串和长度小于2的单词
            if (token.length() >= 2) {
                word.set(token);
                // 输出<单词, 1>键值对
                context.write(word, one);
                
                // 记录处理的单词（仅在调试模式下）
                if (logger.isDebugEnabled()) {
                    logger.debug("Mapped word: {}", token);
                }
            }
        }
    }
    
    /**
     * 清理单词：移除标点符号和特殊字符
     * 
     * @param word 原始单词
     * @return 清理后的单词
     */
    private String cleanWord(String word) {
        // 移除标点符号，只保留字母和数字
        return word.replaceAll("[^a-zA-Z0-9]", "");
    }
    
    /**
     * setup方法：在Map任务开始前调用
     * 可以用于初始化资源
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        logger.info("WordCountMapper started for task: {}", context.getTaskAttemptID());
    }
    
    /**
     * cleanup方法：在Map任务结束后调用
     * 可以用于清理资源
     */
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        logger.info("WordCountMapper completed for task: {}", context.getTaskAttemptID());
    }
}