package net.simpleframework.module.bbs.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.service.ado.IADOBeanService;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.common.web.content.ListRowHandler;
import net.simpleframework.module.common.web.content.PageletCreator;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.struct.Pagelet;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsPageletCreator extends PageletCreator<BbsTopic> implements IBbsContextAware {

	public Pagelet getHistoryPagelet(final PageParameter pp) {
		return getHistoryPagelet(pp, "bbs_views");
	}

	@Override
	protected ListRowHandler<BbsTopic> getDefaultListRowHandler() {
		return DEFAULT_HANDLER;
	}

	private final BbsListRowHandler DEFAULT_HANDLER = new BbsListRowHandler();

	public static class BbsListRowHandler extends ListRowHandler<BbsTopic> {
		@Override
		protected String getHref(final BbsTopic bean) {
			return ((IBbsWebContext) context).getUrlsFactory().getPostViewUrl(bean);
		}

		@Override
		protected String[] getShortDesc(final BbsTopic topic) {
			final int c = topic.getPosts();
			final long v = topic.getViews();
			return new String[] { c + "/" + v, $m("BbsPageletCreator.0", c, v) };
		}

		@Override
		protected IADOBeanService<BbsTopic> getBeanService() {
			return context.getTopicService();
		}
	}
}