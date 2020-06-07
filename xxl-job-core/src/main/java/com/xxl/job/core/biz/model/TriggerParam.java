package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * 触发器参数
 * Created by xuxueli on 16/7/22.
 */
public class TriggerParam implements Serializable{
    private static final long serialVersionUID = 42L;

    /**
     * 任务id
     */
    private int jobId;

    /**
     * 执行器处理器
     */
    private String executorHandler;
    /**
     * 执行参数
     */
    private String executorParams;
    /**
     * 执行器阻塞策略
     */
    private String executorBlockStrategy;
    /**
     * 执行器过期时间
     */
    private int executorTimeout;

    /**
     * 记录日志id
     */
    private long logId;
    /**
     * 记录日志的日期
     */
    private long logDateTime;

    /**
     * glue脚本类型
     */
    private String glueType;
    /**
     * glue源代码
     */
    private String glueSource;
    /**
     * glue更新日期
     */
    private long glueUpdatetime;

    /**
     * 广播索引
     */
    private int broadcastIndex;
    /**
     * 广播总量
     */
    private int broadcastTotal;


    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getExecutorHandler() {
        return executorHandler;
    }

    public void setExecutorHandler(String executorHandler) {
        this.executorHandler = executorHandler;
    }

    public String getExecutorParams() {
        return executorParams;
    }

    public void setExecutorParams(String executorParams) {
        this.executorParams = executorParams;
    }

    public String getExecutorBlockStrategy() {
        return executorBlockStrategy;
    }

    public void setExecutorBlockStrategy(String executorBlockStrategy) {
        this.executorBlockStrategy = executorBlockStrategy;
    }

    public int getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(int executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(long logDateTime) {
        this.logDateTime = logDateTime;
    }

    public String getGlueType() {
        return glueType;
    }

    public void setGlueType(String glueType) {
        this.glueType = glueType;
    }

    public String getGlueSource() {
        return glueSource;
    }

    public void setGlueSource(String glueSource) {
        this.glueSource = glueSource;
    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    public void setGlueUpdatetime(long glueUpdatetime) {
        this.glueUpdatetime = glueUpdatetime;
    }

    public int getBroadcastIndex() {
        return broadcastIndex;
    }

    public void setBroadcastIndex(int broadcastIndex) {
        this.broadcastIndex = broadcastIndex;
    }

    public int getBroadcastTotal() {
        return broadcastTotal;
    }

    public void setBroadcastTotal(int broadcastTotal) {
        this.broadcastTotal = broadcastTotal;
    }


    @Override
    public String toString() {
        return "TriggerParam{" +
                "jobId=" + jobId +
                ", executorHandler='" + executorHandler + '\'' +
                ", executorParams='" + executorParams + '\'' +
                ", executorBlockStrategy='" + executorBlockStrategy + '\'' +
                ", executorTimeout=" + executorTimeout +
                ", logId=" + logId +
                ", logDateTime=" + logDateTime +
                ", glueType='" + glueType + '\'' +
                ", glueSource='" + glueSource + '\'' +
                ", glueUpdatetime=" + glueUpdatetime +
                ", broadcastIndex=" + broadcastIndex +
                ", broadcastTotal=" + broadcastTotal +
                '}';
    }

}
