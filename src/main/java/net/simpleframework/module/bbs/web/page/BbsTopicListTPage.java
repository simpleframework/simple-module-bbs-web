package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.EFilterRelation;
import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.common.bean.ETimePeriod;
import net.simpleframework.ctx.common.bean.TimePeriod;
import net.simpleframework.ctx.permission.IPermissionConst;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.web.BbsLogRef;
import net.simpleframework.module.bbs.web.BbsUtils;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.common.DescriptionLocalUtils;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.content.page.AbstractRecommendationPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.PhotoImage;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SearchInput;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.AdvSearchPage;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.mvc.template.struct.FilterButton;
import net.simpleframework.mvc.template.struct.FilterButtons;
import net.simpleframework.mvc.template.struct.NavigationButtons;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class BbsTopicListTPage extends AbstractBbsTPage {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		// 菜单
		addTopicMenuBean(pp);
		// 类目选择
		addCategoryDict(pp);

		// 高级搜索
		addSearchWindow(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp);

		if (BbsUtils.isManager(pp, getCategory(pp))) {
			tablePager.addColumn(TablePagerColumn.OPE().setWidth(75).setResize(false));

			// 删除
			addDeleteAjaxRequest(pp, "BbsTopicListTPage_delete");
			// 推荐
			addAjaxRequest(pp, "BbsTopicListTPage_recommendationPage", RecommendationPage.class);
			addComponentBean(pp, "BbsTopicListTPage_recommendation", WindowBean.class)
					.setContentRef("BbsTopicListTPage_recommendationPage").setHeight(240).setWidth(450)
					.setTitle($m("AbstractContentBean.2"));

			// 精华
			addAjaxRequest(pp, "BbsTopicListTPage_bestPage", BestPage.class);
			addComponentBean(pp, "BbsTopicListTPage_best", WindowBean.class)
					.setContentRef("BbsTopicListTPage_bestPage").setHeight(240).setWidth(450)
					.setTitle($m("BbsTopic.0"));

			// 修改日志
			final IModuleRef ref = ((IBbsWebContext) context).getLogRef();
			if (ref != null) {
				((BbsLogRef) ref).addLogComponent(pp);
			}
		}
	}

	@Override
	protected WindowBean addSearchWindow(final PageParameter pp) {
		return super.addSearchWindow(pp).setXdelta(-150);
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
				"BbsTopicListTPage_tbl", TablePagerBean.class).setShowFilterBar(false)
				.setShowCheckbox(false).setPageItems(50).setPagerBarLayout(EPagerBarLayout.bottom)
				.setContainerId("tbl_" + hashId).setHandleClass(TopicList.class);
		tablePager
				.addColumn(TablePagerColumn.ICON())
				.addColumn(
						new TablePagerColumn("topic", $m("BbsTopicListTPage.0"))
								.setTextAlign(ETextAlign.left).setResize(false).setNowrap(false)
								.setSort(false))
				.addColumn(
						new TablePagerColumn("userId", $m("BbsTopicListTPage.1")).setWidth(100)
								.setResize(false).setSort(false).setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("posts", $m("BbsTopicListTPage.2")).setWidth(45).setResize(
								false))
				.addColumn(
						new TablePagerColumn("views", $m("BbsTopicListTPage.3")).setWidth(45).setResize(
								false))
				.addColumn(
						new TablePagerColumn("favorites", $m("BbsTopicListTPage.4")).setWidth(45)
								.setResize(false))
				.addColumn(
						new TablePagerColumn("lastPost", $m("BbsTopicListTPage.5")).setWidth(110)
								.setResize(false).setSort(false).setTextAlign(ETextAlign.left));
		return tablePager;
	}

	@Override
	public String getRole(final PageParameter pp) {
		final String list = pp.getParameter("list");
		if ("my".equals(list)) {
			return IPermissionConst.ROLE_ALL_ACCOUNT;
		}
		return super.getRole(pp);
	}

	@Transaction(context = IBbsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("topicId"));
		if (ids != null) {
			context.getTopicService().delete(ids);
		}
		return new JavascriptForward("$Actions['BbsTopicListTPage_tbl']();");
	}

	@Override
	protected boolean isShowPagelets(final PageParameter pp) {
		return false;
	}

	@Override
	protected TabButtons getCategoryTabs(final PageParameter pp) {
		final IBbsCategoryService service = context.getCategoryService();
		final TabButtons tabs = TabButtons.of();
		final BbsCategory category = getCategory(pp);
		if (category != null) {
			final IDataQuery<BbsCategory> dq = service.queryChildren(service.getBean(category
					.getParentId()));
			BbsCategory tmp;
			int i = 0;
			while ((tmp = dq.next()) != null) {
				tabs.add(new TabButton(tmp, getUrlsFactory().getTopicListUrl(tmp))
						.setTabMatch(ETabMatch.params));
				if (++i == 8) {
					break;
				}
			}
		} else {
			tabs.addAll(singleton(BbsCategoryTPage.class).getCategoryTabs(pp));
			addSearchTab(pp, tabs);
		}
		return tabs;
	}

	protected void addSearchTab(final PageParameter pp, final TabButtons btns) {
		final String t = pp.getParameter("s");
		if (StringUtils.hasText(t)) {
			btns.append(new TabButton($m("BbsTopicListTPage.9"), "#")).setSelectedIndex(
					btns.size() - 1);
		}
	}

	@Override
	public FilterButtons getFilterButtons(final PageParameter pp) {
		final String url = getUrlsFactory().getTopicListUrl(pp, getCategory(pp));
		final FilterButtons btns = FilterButtons.of();
		final BbsAdvSearchPage sPage = singleton(BbsAdvSearchPage.class);
		FilterButton btn = sPage.createFilterButton(pp, url, "as_topic");
		if (btn != null) {
			btns.add(btn.setLabel($m("BbsAdvSearchPage.0")));
		}
		btn = sPage.createFilterButton(pp, url, "as_author");
		if (btn != null) {
			btns.add(btn.setLabel($m("BbsAdvSearchPage.1")));
		}
		btn = sPage.createFilterDateButton(pp, url, "as_time");
		if (btn != null) {
			btns.add(btn.setLabel($m("AdvSearchPage.0")));
		}
		return btns;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(new LinkButton($m("BbsTopicListTPage.6"))
				.setId("idBbsTopicListTPage_topicMenu").setIconClass(Icon.file).setMenuIcon(true));
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final BbsCategory category = getCategory(pp);
		return ElementList.of(new SearchInput("AbstractBbsTPage_search")
				.setOnSearchClick(
						"$Actions.loc('"
								+ HttpUtils.addParameters(getUrlsFactory().getTopicListUrl(null), "s=")
								+ "' + encodeURIComponent($F('AbstractBbsTPage_search')))")
				.setOnAdvClick(
						"$Actions['AbstractBbsTPage_SearchWindow']('"
								+ AdvSearchPage.encodeRefererUrl(getUrlsFactory().getTopicListUrl(pp,
										category)) + "');")
				.setText(StringUtils.blank(pp.getLocaleParameter("s"))));
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final LinkElement home = new LinkElement(context.getModule()).setHref(getUrlsFactory()
				.getCategoryUrl());
		final BbsCategory category = getCategory(pp);
		final NavigationButtons btns = NavigationButtons.of();
		String cText = null;
		if (category != null) {
			cText = category.getText();
		} else {
			final String list = pp.getParameter("list");
			if ("my".equals(list)) {
				cText = $m("BbsCategoryTPage.7");
			} else {
				cText = $m("BbsCategoryTPage.6");
			}
		}
		btns.append(home,
				new SpanElement().addElements(new SpanElement(cText), createCategoryDictMenu(pp)));
		return btns;
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='BbsTopicListTPage'>");
		sb.append(" <div id='tbl_").append(hashId).append("'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class TopicList extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final String s = cp.getLocaleParameter("s");
			final IBbsTopicService service = context.getTopicService();
			if (StringUtils.hasText(s)) {
				return service.getLuceneService().query(s, BbsTopic.class);
			}
			final BbsCategory category = getCategory(cp);
			if (category != null) {
				cp.addFormParameter("categoryId", category.getId());
			}

			// 综合查询
			final FilterItems params = FilterItems
					.of(new FilterItem("status", EContentStatus.publish));
			if (category != null) {
				params.add(new FilterItem("categoryId", category.getId()));
			}

			final String list = cp.getParameter("list");
			if ("best".equals(list)) {
				params.add(new FilterItem("best", true));
			} else if ("my".equals(list)) {
				final Object id = cp.getLoginId();
				if (id == null) {
					return DataQueryUtils.nullQuery();
				}
				params.add(new FilterItem("userId", id));
			} else {
				final PermissionUser user = cp.getUser(cp.getParameter("userId"));
				if (user.getId() != null) {
					params.append(new FilterItem("userId", user.getId()));
				}
			}

			params.append(new FilterItem("topic", EFilterRelation.like, cp.getParameter("as_topic")))
					.append(new FilterItem("createDate", new TimePeriod(cp.getParameter("as_time"))));
			return service.queryByParams(params);
		}

		private final MenuItems CONTEXT_MENUS = MenuItems
				.of()
				.append(
						MenuItem.itemEdit().setOnclick(
								"$Actions.loc('" + getUrlsFactory().getTopicFormUrl(null)
										+ "?topicId=' + $pager_action(item).rowId());"))
				.append(MenuItem.sep())
				.append(
						MenuItem.of($m("AbstractContentBean.2")).setOnclick_act(
								"BbsTopicListTPage_recommendation", "topicId"))
				.append(
						MenuItem.of($m("BbsTopic.0")).setOnclick_act("BbsTopicListTPage_best", "topicId"))
				.append(MenuItem.sep())
				.append(MenuItem.itemDelete().setOnclick_act("BbsTopicListTPage_delete", "topicId"))
				.append(MenuItem.sep())
				.append(MenuItem.itemLog().setOnclick_act("BbsTopicListTPage_logWin", "topicId"));

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			return menuItem == null ? CONTEXT_MENUS : null;
		}

		@Override
		public AbstractTablePagerSchema createTablePagerSchema() {
			return new DefaultTablePagerSchema() {
				@Override
				public Map<String, Object> getRowData(final ComponentParameter cp,
						final Object dataObject) {
					final BbsTopic topic = (BbsTopic) dataObject;
					final KVMap kv = new KVMap();
					final StringBuilder sb = new StringBuilder();
					sb.append(new SpanElement().setClassName("type_" + topic.getBbsType().name()));
					final int posts = context.getPostService()
							.query(topic, new TimePeriod(ETimePeriod.day)).getCount();
					if (posts > 0) {
						sb.append(new SupElement(posts).setTitle($m("BbsTopicListTPage.8", posts)));
					}
					kv.put(TablePagerColumn.ICON, sb.toString());
					BbsCategory category = getCategory(cp);
					sb.setLength(0);
					if (category == null) {
						category = context.getCategoryService().getBean(topic.getCategoryId());
						if (category != null) {
							sb.append("<span class='categoryTxt'>[");
							sb.append(new LinkElement(category.getText()).setHref(getUrlsFactory()
									.getTopicListUrl(category)));
							sb.append("]</span>");
						}
					}
					final LinkElement le = new LinkElement(topic.getTopic()).setHref(
							getUrlsFactory().getPostViewUrl(topic)).setClassName("bbsTopic");
					if (topic.getRecommendation() > 0) {
						le.addClassName("recommendation");
					}
					sb.append(le);
					if (topic.isBest()) {
						sb.append(new SpanElement().setClassName("imgBest").setTitle($m("BbsTopic.0")));
					}
					kv.put("topic", sb.toString());
					kv.put("userId", getUserStat(cp, topic.getUserId(), topic.getCreateDate()));
					kv.put("posts", SpanElement.num(topic.getPosts()));
					kv.put("views", SpanElement.num(topic.getViews()));
					kv.put("favorites", SpanElement.num(topic.getFavorites()));
					final Date lastPostDate = topic.getLastPostDate();
					if (lastPostDate != null) {
						kv.put("lastPost", getUserStat(cp, topic.getLastUserId(), lastPostDate));
					} else {
						kv.put("lastPost", getUserStat(cp, topic.getUserId(), topic.getCreateDate()));
					}
					if (getTablePagerColumns(cp).get(TablePagerColumn.OPE) != null) {
						sb.setLength(0);
						sb.append(ButtonElement.editBtn().setOnclick(
								"$Actions.loc('" + getUrlsFactory().getTopicFormUrl(topic) + "');"));
						sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
						kv.put(TablePagerColumn.OPE, sb.toString());
					}
					return kv;
				}

				private String getUserStat(final PageParameter pp, final ID userId, final Date date) {
					final StringBuilder sb = new StringBuilder();
					sb.append(PhotoImage.icon16(pp.getPhotoUrl(userId)));
					sb.append("<span class='userStat'>");
					sb.append(" <span class='us_1'>").append(createTopicsLink(pp.getUser(userId)))
							.append("</span>");
					sb.append(" <span class='us_2'>")
							.append(DateUtils.getRelativeDate(date, _NUMBERCONVERT)).append("</span>");
					sb.append("</span>");
					return sb.toString();
				}
			};
		}
	}

	public static class RecommendationPage extends AbstractRecommendationPage<BbsTopic> {
		@Override
		protected IBbsTopicService getBeanService() {
			return context.getTopicService();
		}

		@Override
		protected BbsTopic getBean(final PageParameter pp) {
			return getTopic(pp);
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) {
			final JavascriptForward js = super.onSave(cp);
			js.append("$Actions['BbsTopicListTPage_tbl']();");
			return js;
		}
	}

	public static class BestPage extends FormTableRowTemplatePage {
		@Override
		protected void addComponents(final PageParameter pp) {
			super.addComponents(pp);

			addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#b_description"));
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) {
			final BbsTopic topic = getTopic(cp);
			if (topic != null) {
				topic.setBest(!topic.isBest());
				DescriptionLocalUtils.set(topic, cp.getParameter("b_description"));
				context.getTopicService().update(new String[] { "best" }, topic);
			}
			final JavascriptForward js = super.onSave(cp);
			js.append("$Actions['BbsTopicListTPage_tbl']();");
			return js;
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			return TableRows.of(new TableRow(new RowField($m("BestPage.0"), InputElement.textarea(
					"b_description").setRows(5))));
		}

		@Override
		public ElementList getLeftElements(final PageParameter pp) {
			final BbsTopic topic = getTopic(pp);
			return ElementList
					.of(new SpanElement(topic.isBest() ? $m("BestPage.1") : $m("BestPage.2"))
							.setStyle("color: #a00; font-size: 9.5pt"));
		}
	}

	private static BbsTopic getTopic(final PageParameter pp) {
		return getCacheBean(pp, context.getTopicService(), "topicId");
	}
}
