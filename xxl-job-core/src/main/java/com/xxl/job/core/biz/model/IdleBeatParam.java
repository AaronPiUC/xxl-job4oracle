package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * 空闲心跳的参数
 * @author xuxueli 2020-04-11 22:27
 */
public class IdleBeatParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 任务id
     */
    private int jobId;

    public IdleBeatParam() {
    }

    public IdleBeatParam(int jobId) {
        this.jobId = jobId;
    }




    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

}