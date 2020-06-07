package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.core.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 任务注册
 *
 * @author xuxueli 2016-10-02 19:10:24
 */
public class JobRegistryMonitorHelper {
    private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);
    // 饿汉模式的单例，天生线程安全
    private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();

    public static JobRegistryMonitorHelper getInstance() {
        return instance;
    }

    // 注册
    private Thread registryThread;
    // 是否停止标志位，遇到异常即修改为true=>停止线程
    private volatile boolean toStop = false;

    /**
     * 30秒执行一次，去查找所有注册上的job,并且查找当前在线的（在3倍DEAD_TIMEOUT以内就算活着）的任务，找到关联的注册地址，并更新
     */
    public void start() {
        registryThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // 查找自动注册的APP
                    List<XxlJobGroup> groupList = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().findByAddressType(0);
                    if (groupList != null && !groupList.isEmpty()) {

                        // remove dead address (admin/executor)
                        // 查找已经挂掉的已注册job并移除
                        List<Integer> ids = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().findDead(RegistryConfig.DEAD_TIMEOUT, new Date());
                        if (ids != null && ids.size() > 0) {
                            XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().removeDead(ids);
                        }

                        // 刷新当前在线执行器的注册地址
                        HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
                        List<XxlJobRegistry> list = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
                        if (list != null) {
                            for (XxlJobRegistry item : list) {
                                // 过滤出执行器job
                                if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
                                    String appname = item.getRegistryKey();
                                    List<String> registryList = appAddressMap.get(appname);
                                    if (registryList == null) {
                                        registryList = new ArrayList<String>();
                                    }
                                    // 如果不包含此注册地址，就添加进来
                                    if (!registryList.contains(item.getRegistryValue())) {
                                        registryList.add(item.getRegistryValue());
                                    }
                                    appAddressMap.put(appname, registryList);
                                }
                            }
                        }

                        // 把刷新出来的执行器地址重新更新进数据库
                        for (XxlJobGroup group : groupList) {
                            List<String> registryList = appAddressMap.get(group.getAppname());
                            String addressListStr = null;
                            if (registryList != null && !registryList.isEmpty()) {
                                Collections.sort(registryList);
                                addressListStr = "";
                                for (String item : registryList) {
                                    addressListStr += item + ",";
                                }
                                addressListStr = addressListStr.substring(0, addressListStr.length() - 1);
                            }
                            group.setAddressList(addressListStr);
                            XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().update(group);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
                try {
                    //睡tm的，默认睡30秒
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, job registry monitor thread stop");
        });
        registryThread.setDaemon(true);
        registryThread.setName("xxl-job, admin JobRegistryMonitorHelper");
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
