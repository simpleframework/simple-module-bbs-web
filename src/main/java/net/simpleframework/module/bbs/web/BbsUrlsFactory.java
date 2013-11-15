package net.simpleframework.module.bbs.web;

import net.simpleframework.ado.bean.AbstractIdBean;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.module.bbs.web.page.t2.BbsPostViewPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicFormPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.UrlsCache;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsUrlsFactory extends UrlsCache {

	public String getCategoryUrl() {
		return AbstractMVCPage.url(getCategoryPage());
	}

	public String getTopicListUrl(final BbsCategory category) {
		String url = AbstractMVCPage.url(getTopicListPage());
		if (category != null) {
			url += "?categoryId=" + category.getId();
		}
		return url;
	}

	public String getTopicUserListUrl(final PermissionUser user) {
		return HttpUtils.addParameters(getTopicListUrl(null), "userId=" + user.getId());
	}

	public String getTopicListUrl(final PageParameter pp, final BbsCategory category) {
		String url = getTopicListUrl(category);
		final String list = pp.getParameter("list");
		if ("my".equals(list)) {
			url = HttpUtils.addParameters(url, "list=my");
		}
		return url;
	}

	public String getTopicFormUrl(final AbstractIdBean bean) {
		String url = AbstractMVCPage.url(getTopicFormPage());
		if (bean instanceof BbsCategory) {
			url += "?categoryId=" + bean.getId();
		} else if (bean instanceof BbsTopic) {
			url += "?topicId=" + bean.getId();
		}
		return url;
	}

	public String getPostViewUrl(final BbsTopic topic) {
		String url = AbstractMVCPage.url(getPostViewPage());
		if (topic != null) {
			url += "?topicId=" + topic.getId();
		}
		return url;
	}

	protected Class<? extends AbstractMVCPage> getCategoryPage() {
		return BbsCategoryPage.class;
	}

	protected Class<? extends AbstractMVCPage> getTopicListPage() {
		return BbsTopicListPage.class;
	}

	protected Class<? extends AbstractMVCPage> getTopicFormPage() {
		return BbsTopicFormPage.class;
	}

	protected Class<? extends AbstractMVCPage> getPostViewPage() {
		return BbsPostViewPage.class;
	}
}
