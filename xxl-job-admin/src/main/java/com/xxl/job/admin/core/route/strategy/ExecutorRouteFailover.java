package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.scheduler.XxlJobScheduler;
import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * 故障转移策略
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFailover extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {

        StringBuffer beatResultSB = new StringBuffer();
        //对每个地址进行访问，没找到心跳监测成功的，一旦找到一个成功的就认为成功了，一直没找到的话并且遍历结束，就把所有错误信息直接返回。
        for (String address : addressList) {
            // beat
            ReturnT<String> beatResult = null;
            try {
                ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(address);
                // 监听一下client的心跳，观察是否活泼
                beatResult = executorBiz.beat();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                beatResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
            }
            beatResultSB.append( (beatResultSB.length()>0)?"<br><br>":"")
                    .append(I18nUtil.getString("jobconf_beat") + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(beatResult.getCode())
                    .append("<br>msg：").append(beatResult.getMsg());

            // beat success
            if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {

                beatResult.setMsg(beatResultSB.toString());
                beatResult.setContent(address);
                return beatResult;
            }
        }
        return new ReturnT<String>(ReturnT.FAIL_CODE, beatResultSB.toString());

    }
}
