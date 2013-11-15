package net.simpleframework.module.bbs.web;

import java.io.File;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsLogRef extends LogRef implements IBbsContextAware {

	public void addLogComponent(final PageParameter pp) {
		pp.addComponentBean("BbsTopicListTPage_logPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(BbsTopicLogPage.class));
		pp.addComponentBean("BbsTopicListTPage_logWin", WindowBean.class)
				.setContentRef("BbsTopicListTPage_logPage").setHeight(600).setWidth(960);
	}

	@Override
	public void logDownload(final Object beanId, final String topic, final File oFile) {
		super.logDownload(beanId, topic, oFile);

		// 更新计数
		final IAttachmentService<Attachment> service = context.getAttachmentService();
		final Attachment attachment = service.getBean(beanId);
		if (attachment != null) {
			attachment.setDownloads(getDownloadLogService().countLog(beanId));
			service.update(new String[] { "downloads" }, attachment);
		}
	}

	public static class BbsTopicLogPage extends EntityUpdateLogPage {
		@Override
		protected IIdBeanAware getBean(final PageParameter pp) {
			return getCacheBean(pp, context.getTopicService(), "topicId");
		}

		@Override
		public String getBeanIdParameter() {
			return "topicId";
		}
	}
}