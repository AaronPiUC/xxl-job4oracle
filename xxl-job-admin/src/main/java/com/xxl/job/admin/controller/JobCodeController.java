package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLogGlue;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogGlueDao;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * job code controller
 * 定时任务Glue相关的控制类
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/jobcode")
public class JobCodeController {
	
	@Resource
	private XxlJobInfoDao xxlJobInfoDao;
	@Resource
	private XxlJobLogGlueDao xxlJobLogGlueDao;

	@RequestMapping
	public String index(HttpServletRequest request, Model model, int jobId) {
		XxlJobInfo jobInfo = xxlJobInfoDao.loadById(jobId);
		List<XxlJobLogGlue> jobLogGlues = xxlJobLogGlueDao.findByJobId(jobId);

		if (jobInfo == null) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		if (GlueTypeEnum.BEAN == GlueTypeEnum.match(jobInfo.getGlueType())) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_gluetype_unvalid"));
		}

		// valid permission
		JobInfoController.validPermission(request, jobInfo.getJobGroup());

		// Glue类型-字典
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());

		model.addAttribute("jobInfo", jobInfo);
		model.addAttribute("jobLogGlues", jobLogGlues);
		return "jobcode/jobcode.index";
	}


	/**
	 * 保存定时任务
	 *
	 * @param model 模型
	 * @param id 主键id
	 * @param glueSource 源代码
	 * @param glueRemark 备注
	 * @return
	 */
	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(Model model, int id, String glueSource, String glueRemark) {
		// 校验
		// glue的备注不可为空
		if (glueRemark==null) {
			return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_glue_remark")) );
		}
		// glue的长度应该在4-100之间
		if (glueRemark.length()<4 || glueRemark.length()>100) {
			return new ReturnT<String>(500, I18nUtil.getString("jobinfo_glue_remark_limit"));
		}
		// 检查定时任务信息是否存在了
		XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(id);
		if (exists_jobInfo == null) {
			return new ReturnT<String>(500, I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		
		// 对xxl_job_info中的job信息进行更新
		exists_jobInfo.setGlueSource(glueSource);
		exists_jobInfo.setGlueRemark(glueRemark);
		exists_jobInfo.setGlueUpdatetime(new Date());

		exists_jobInfo.setUpdateTime(new Date());
		xxlJobInfoDao.update(exists_jobInfo);

		// 更新后将原记录保存到xxl_job_infoglue
		XxlJobLogGlue xxlJobLogGlue = new XxlJobLogGlue();
		xxlJobLogGlue.setJobId(exists_jobInfo.getId());
		xxlJobLogGlue.setGlueType(exists_jobInfo.getGlueType());
		xxlJobLogGlue.setGlueSource(glueSource);
		xxlJobLogGlue.setGlueRemark(glueRemark);
		xxlJobLogGlue.setAddTime(new Date());
		xxlJobLogGlue.setUpdateTime(new Date());
		xxlJobLogGlueDao.save(xxlJobLogGlue);

		// 仅保留最近30条更新记录，其他删除
		xxlJobLogGlueDao.removeOld(exists_jobInfo.getId(), 30);

		return ReturnT.SUCCESS;
	}
	
}
