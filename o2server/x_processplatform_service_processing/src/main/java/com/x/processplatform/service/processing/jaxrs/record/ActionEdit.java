package com.x.processplatform.service.processing.jaxrs.record;

import java.util.concurrent.Callable;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.executor.ProcessPlatformExecutorFactory;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.processplatform.core.entity.content.Record;
import com.x.processplatform.core.entity.content.Work;
import com.x.processplatform.core.entity.content.WorkCompleted;

import org.apache.commons.lang3.StringUtils;

class ActionEdit extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(ActionEdit.class);

	ActionResult<Wo> execute(EffectivePerson effectivePerson, String id, JsonElement jsonElement) throws Exception {

		final Bag bag = new Bag();

		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			bag.wi = this.convertToWrapIn(jsonElement, Wi.class);
			bag.record = emc.find(id, Record.class);
			if (null == bag.record) {
				throw new ExceptionEntityNotExist(id, Record.class);
			}
			bag.job = bag.record.getJob();
		}

		Callable<ActionResult<Wo>> callable = new Callable<ActionResult<Wo>>() {
			public ActionResult<Wo> call() throws Exception {
				try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
					Record record = emc.find(id, Record.class);
					Wi.copier.copy(bag.wi, record);
					if (StringUtils.isNotEmpty(record.getWorkCompleted())) {
						WorkCompleted workCompleted = emc.find(record.getWorkCompleted(), WorkCompleted.class);
						if (null == workCompleted) {
							throw new ExceptionEntityNotExist(record.getWorkCompleted(), WorkCompleted.class);
						}
						record.setJob(workCompleted.getJob());
					} else {
						Work work = emc.find(record.getWork(), Work.class);
						if (null == work) {
							throw new ExceptionEntityNotExist(record.getWork(), Work.class);
						}
						record.setJob(work.getJob());
					}
					emc.beginTransaction(Record.class);
					emc.check(record, CheckPersistType.all);
					emc.commit();
					ActionResult<Wo> result = new ActionResult<>();
					Wo wo = new Wo();
					wo.setId(record.getId());
					result.setData(wo);
					return result;
				}
			}
		};

		return ProcessPlatformExecutorFactory.get(bag.job).submit(callable).get();

	}

	public static class Bag {
		String job;
		Wi wi;
		Record record;
	}

	public static class Wi extends Record {

		private static final long serialVersionUID = 4179509440650818001L;

		static WrapCopier<Wi, Record> copier = WrapCopierFactory.wi(Wi.class, Record.class, null,
				JpaObject.FieldsUnmodify);

	}

	public static class Wo extends WoId {

	}

}