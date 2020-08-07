package com.x.program.center.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.x.base.core.project.bean.NameValuePair;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.program.center.ThisApplication;
import com.x.program.center.core.entity.PromptErrorLog;

public class CleanupPromptErrorLog extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(CleanupPromptErrorLog.class);

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			if (pirmaryCenter()) {
				ThisApplication.logQueue.send(new NameValuePair(PromptErrorLog.class.getName(), null));
			}
		} catch (Exception e) {
			logger.error(e);
			throw new JobExecutionException(e);
		}
	}
}
