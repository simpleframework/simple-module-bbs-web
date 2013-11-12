package net.simpleframework.module.bbs.web;

import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.mvc.PageParameter;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class BbsUtils implements IBbsContextAware {

	static final String CATEGORY_MANAGER = "category_manager";

	public static boolean isManager(final PageParameter pp, final BbsCategory category) {
		Boolean b = (Boolean) pp.getRequestAttr(CATEGORY_MANAGER);
		if (b == null) {
			pp.setRequestAttr(CATEGORY_MANAGER,
					b = context.getTeamService().isManager(category, pp.getLogin()));
		}
		return b;
	}
}
