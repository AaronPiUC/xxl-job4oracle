package com.xxl.job.admin.core.scheduler;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.thread.*;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author xuxueli 2018-10-28 00:18:17
 */

public class XxlJobScheduler  {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobScheduler.class);


    public void init() throws Exception {
        // init i18n
        initI18n();

        // admin registry monitor run
        // 注册监控
        JobRegistryMonitorHelper.getInstance().start();

        // admin fail-monitor run
        // 失败任务监控
        JobFailMonitorHelper.getInstance().start();

        // admin lose-monitor run
        // 丢失监控
        JobLosedMonitorHelper.getInstance().start();

        // admin trigger pool start
        // 触发器池
        JobTriggerPoolHelper.toStart();

        // admin log report start
        // 日志报告
        JobLogReportHelper.getInstance().start();

        // start-schedule
        // 定时
        JobScheduleHelper.getInstance().start();

        logger.info(">>>>>>>>> init xxl-job admin success.");
    }

    
    public void destroy() throws Exception {

        // stop-schedule
        JobScheduleHelper.getInstance().toStop();

        // admin log report stop
        JobLogReportHelper.getInstance().toStop();

        // admin trigger pool stop
        JobTriggerPoolHelper.toStop();

        // admin lose-monitor stop
        JobLosedMonitorHelper.getInstance().toStop();

        // admin fail-monitor stop
        JobFailMonitorHelper.getInstance().toStop();

        // admin registry stop
        JobRegistryMonitorHelper.getInstance().toStop();

    }

    // ---------------------- I18n ----------------------

    private void initI18n(){
        for (ExecutorBlockStrategyEnum item:ExecutorBlockStrategyEnum.values()) {
            item.setTitle(I18nUtil.getString("jobconf_block_".concat(item.name())));
        }
    }

    // ---------------------- executor-client ----------------------
    /**
     * 执行器注册执行仓库
     */
    private static ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<String, ExecutorBiz>();

    /**
     * 获取执行器控制器
     * @param address
     * @return
     * @throws Exception
     */
    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // 先验证。传入地址是否为空
        if (address==null || address.trim().length()==0) {
            return null;
        }

        // 从缓存中查询，就是那个线程安全存在本地的executorBizRepository
        address = address.trim();
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }

        // 缓存中没有就新建，然后缓存到Repository中
        executorBiz = new ExecutorBizClient(address, XxlJobAdminConfig.getAdminConfig().getAccessToken());

        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }

}
