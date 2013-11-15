package net.simpleframework.module.bbs.web;

import java.io.File;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.common.web.content.IContentRefAware;
import net.simpleframework.module.log.web.page.DownloadLogPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.IDownloadHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IBbsWebContext extends IBbsContext, IContentRefAware {

	/**
	 * 获取小页面的创建类
	 * 
	 * @return
	 */
	BbsPageletCreator getPageletCreator();

	/**
	 * 获取url的构建工厂类
	 * 
	 * 子类覆盖
	 * 
	 * @return
	 */
	BbsUrlsFactory getUrlsFactory();

	public static class BbsDownloadLogPage extends DownloadLogPage implements IBbsContextAware {

		@Override
		protected IIdBeanAware getBean(final PageParameter pp) {
			return context.getAttachmentService().getBean(pp.getParameter(getBeanIdParameter()));
		}
	}

	public static class AttachmentDownloadHandler implements IDownloadHandler, IBbsContextAware {

		@Override
		public void onDownloaded(final Object beanId, final String topic, final File oFile) {
			final IModuleRef ref = ((IBbsWebContext) context).getLogRef();
			if (ref != null) {
				// 记录下载日志
				((BbsLogRef) ref).logDownload(beanId, topic, oFile);
			}
		}
	}
}
