package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * job info
 *
 * @author xuxueli 2016-1-12 18:03:45
 */
@Mapper
public interface XxlJobInfoDao {

    List<XxlJobInfo> pageList(@Param("offset") int offset,
                              @Param("pagesize") int pagesize,
                              @Param("jobGroup") int jobGroup,
                              @Param("triggerStatus") int triggerStatus,
                              @Param("jobDesc") String jobDesc,
                              @Param("executorHandler") String executorHandler,
                              @Param("author") String author);

    int pageListCount(@Param("offset") int offset,
                      @Param("pagesize") int pagesize,
                      @Param("jobGroup") int jobGroup,
                      @Param("triggerStatus") int triggerStatus,
                      @Param("jobDesc") String jobDesc,
                      @Param("executorHandler") String executorHandler,
                      @Param("author") String author);

    int save(@Param("info") XxlJobInfo info);

    /**
     * 通过id获取job信息
     * @param id
     * @return
     */
    XxlJobInfo loadById(@Param("id") int id);

    int update(@Param("xxlJobInfo") XxlJobInfo xxlJobInfo);

    int delete(@Param("id") long id);

    List<XxlJobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

    int findAllCount();

    /**
     * 查询maxNextTime时间范围内、处于运行状态的前pagesize条的任务
     * @param maxNextTime 最大下次执行时间
     * @param pagesize 打算查出的条数
     * @return
     */
    List<XxlJobInfo> scheduleJobQuery(@Param("maxNextTime") long maxNextTime, @Param("pagesize") int pagesize);

    int scheduleUpdate(@Param("xxlJobInfo") XxlJobInfo xxlJobInfo);
}
