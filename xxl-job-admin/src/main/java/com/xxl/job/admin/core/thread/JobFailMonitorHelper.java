package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 失败任务监控
 * job monitor instance
 *
 * @author xuxueli 2015-9-1 18:05:56
 */
public class JobFailMonitorHelper {
    private static Logger logger = LoggerFactory.getLogger(JobFailMonitorHelper.class);

    private static JobFailMonitorHelper instance = new JobFailMonitorHelper();

    public static JobFailMonitorHelper getInstance() {
        return instance;
    }

    // ---------------------- monitor ----------------------

    private Thread monitorThread;
    private volatile boolean toStop = false;

    public void start() {
        monitorThread = new Thread(() -> {

            // monitor
            while (!toStop) {
                try {
                    // 查找失败任务前1000条
                    List<Long> failLogIds = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().findFailJobLogIds(1000);
                    if (failLogIds != null && !failLogIds.isEmpty()) {
                        // 对每一条个失败id
                        for (long failLogId : failLogIds) {

                            // 从默认状态转为锁定状态
                            int lockRet = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateAlarmStatus(failLogId, 0, -1);
                            if (lockRet < 1) {
                                continue;
                            }
                            // 查询日志
                            XxlJobLog log = XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().load(failLogId);
                            // 查询所属任务的信息
                            XxlJobInfo info = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(log.getJobId());

                            // 1、首先失败重试
                            if (log.getExecutorFailRetryCount() > 0) {
                                // 失败重试，返回触发信息【正在触发】，更新log信息
                                JobTriggerPoolHelper.trigger(log.getJobId(), TriggerTypeEnum.RETRY, (log.getExecutorFailRetryCount() - 1), log.getExecutorShardingParam(), log.getExecutorParam(), null);
                                String retryMsg = "<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_type_retry") + "<<<<<<<<<<< </span><br>";
                                log.setTriggerMsg(log.getTriggerMsg() + retryMsg);
                                XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateTriggerInfo(log);
                            }

                            // 2、fail alarm monitor
                            int newAlarmStatus = 0;        // 告警状态：0-默认、-1=锁定状态、1-无需告警、2-告警成功、3-告警失败
                            if (info != null && info.getAlarmEmail() != null && info.getAlarmEmail().trim().length() > 0) {
                                boolean alarmResult = XxlJobAdminConfig.getAdminConfig().getJobAlarmer().alarm(info, log);
                                newAlarmStatus = alarmResult ? 2 : 3;
                            } else {
                                newAlarmStatus = 1;
                            }
                            //再次更新失败状态
                            XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateAlarmStatus(failLogId, -1, newAlarmStatus);
                        }
                    }

                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job fail monitor thread error:{}", e);
                    }
                }

                try {
                    // 睡tmd,等十秒再干活
                    TimeUnit.SECONDS.sleep(10);
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }

            }

            logger.info(">>>>>>>>>>> xxl-job, job fail monitor thread stop");

        }
        );
        monitorThread.setDaemon(true);
        monitorThread.setName("xxl-job, admin JobFailMonitorHelper");
        monitorThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        monitorThread.interrupt();
        try {
            // 等待吧
            // 这边要等子线程（对当前类来讲，子线程应该是 JobTriggerPoolHelper.trigger(...)这句话）结束后再运行
            monitorThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
