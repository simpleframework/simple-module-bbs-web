package net.simpleframework.module.bbs.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.module.bbs.impl.BbsContext;
import net.simpleframework.mvc.ctx.WebModuleFunction;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class BbsWebContext extends BbsContext implements IBbsWebContext {

	@Override
	public IModuleRef getFavoriteRef() {
		return getRef("net.simpleframework.module.bbs.web.BbsFavoriteRef");
	}

	@Override
	public IModuleRef getLogRef() {
		return getRef("net.simpleframework.module.bbs.web.BbsLogRef");
	}

	@Override
	public IModuleRef getPDFRef() {
		return getRef("net.simpleframework.module.bbs.web.BbsPDFRef");
	}

	@Override
	public BbsPageletCreator getPageletCreator() {
		return singleton(BbsPageletCreator.class);
	}

	@Override
	public BbsUrlsFactory getUrlsFactory() {
		return singleton(BbsUrlsFactory.class);
	}

	@Override
	protected Module createModule() {
		return super.createModule().setDefaultFunction(
				new WebModuleFunction().setUrl(getUrlsFactory().getCategoryUrl())
						.setName(MODULE_NAME + "-BbsCategoryTPage").setText($m("BbsContext.0")));
	}
}