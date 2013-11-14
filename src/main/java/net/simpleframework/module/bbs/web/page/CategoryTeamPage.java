package net.simpleframework.module.bbs.web.page;

import net.simpleframework.common.ID;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTeam;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.common.team.ITeamService;
import net.simpleframework.module.common.web.team.page.AbstractTeamMgrPage;
import net.simpleframework.mvc.PageParameter;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class CategoryTeamPage extends AbstractTeamMgrPage<BbsTeam> implements IBbsContextAware {

	@Override
	protected ITeamService<BbsTeam> getTeamService() {
		return context.getTeamService();
	}

	@Override
	protected ID getOwnerId(final PageParameter pp) {
		final BbsCategory category = getCacheBean(pp, context.getCategoryService(), getOwnerIdKey());
		return category != null ? category.getId() : null;
	}
}