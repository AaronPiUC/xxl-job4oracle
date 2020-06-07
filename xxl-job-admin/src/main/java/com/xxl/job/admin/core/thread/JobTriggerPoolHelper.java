package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * job trigger thread pool helper
 *
 * @author xuxueli 2018-07-03 21:08:07
 */
public class JobTriggerPoolHelper {
    private static Logger logger = LoggerFactory.getLogger(JobTriggerPoolHelper.class);


    // ---------------------- trigger pool ----------------------

    // 分为两个线程池：快慢池，区分标准为：jobTimeoutCountMap中存在此任务超期次数，且大于10次
    private ThreadPoolExecutor fastTriggerPool = null;
    private ThreadPoolExecutor slowTriggerPool = null;

    public void start() {
        fastTriggerPool = new ThreadPoolExecutor(
                10,
                XxlJobAdminConfig.getAdminConfig().getTriggerPoolFastMax(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "xxl-job, admin JobTriggerPoolHelper-fastTriggerPool-" + r.hashCode());
                    }
                });

        slowTriggerPool = new ThreadPoolExecutor(
                10,
                XxlJobAdminConfig.getAdminConfig().getTriggerPoolSlowMax(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "xxl-job, admin JobTriggerPoolHelper-slowTriggerPool-" + r.hashCode());
                    }
                });
    }


    public void stop() {
        //triggerPool.shutdown();
        fastTriggerPool.shutdownNow();
        slowTriggerPool.shutdownNow();
        logger.info(">>>>>>>>> xxl-job trigger thread pool shutdown success.");
    }


    // job timeout count
    private volatile long minTim = System.currentTimeMillis() / 60000;     // ms > min

    /**
     * 放置定时任务将超时次数数量的Map
     */
    private volatile ConcurrentMap<Integer, AtomicInteger> jobTimeoutCountMap = new ConcurrentHashMap<>();


    /**
     * 触发执行（把任务扔进池子执行）
     *
     * @param jobId                 任务Id
     * @param triggerType           触发器类型
     * @param failRetryCount        失败重试次数
     * @param executorShardingParam 执行器分片参数
     * @param executorParam         执行器参数
     * @param addressList           注册地址
     */
    public void addTrigger(final int jobId,
                           final TriggerTypeEnum triggerType,
                           final int failRetryCount,
                           final String executorShardingParam,
                           final String executorParam,
                           final String addressList) {

        // choose thread pool
        ThreadPoolExecutor triggerPool_ = fastTriggerPool;
        AtomicInteger jobTimeoutCount = jobTimeoutCountMap.get(jobId);
        if (jobTimeoutCount != null && jobTimeoutCount.get() > 10) {      // job-timeout 10 times in 1 min
            //不急的任务进慢触发器线程池
            triggerPool_ = slowTriggerPool;
        }

        // 执行池执行
        triggerPool_.execute(() -> {

            long start = System.currentTimeMillis();

            try {
                // do trigger
                XxlJobTrigger.trigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, addressList);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {

                // check timeout-count-map
                long minTim_now = System.currentTimeMillis() / 60000;
                if (minTim != minTim_now) {
                    //到触发时间了（当前时间与到期时间相同），清空Map
                    minTim = minTim_now;
                    jobTimeoutCountMap.clear();
                }

                // incr timeout-count-map
                long cost = System.currentTimeMillis() - start;
                if (cost > 500) {       // ob-timeout threshold 500ms
                    AtomicInteger timeoutCount = jobTimeoutCountMap.putIfAbsent(jobId, new AtomicInteger(1));
                    if (timeoutCount != null) {
                        timeoutCount.incrementAndGet();
                    }
                }

            }

        });
    }


    // ---------------------- helper ----------------------

    private static JobTriggerPoolHelper helper = new JobTriggerPoolHelper();

    public static void toStart() {
        helper.start();
    }

    public static void toStop() {
        helper.stop();
    }

    /**
     * @param jobId
     * @param triggerType
     * @param failRetryCount        >=0: use this param
     *                              <0: use param from job info config
     * @param executorShardingParam
     * @param executorParam         null: use job param
     *                              not null: cover job param
     */
    public static void trigger(int jobId, TriggerTypeEnum triggerType, int failRetryCount, String executorShardingParam, String executorParam, String addressList) {
        helper.addTrigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, addressList);
    }
}
