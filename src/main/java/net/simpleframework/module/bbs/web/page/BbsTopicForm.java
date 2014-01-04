package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.common.web.html.HtmlUtils.IElementVisitor;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.EBbsType;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.ContentException;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.web.hdl.AbstractAttachmentLogHandler;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.common.element.TextButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ext.attachments.AttachmentBean;
import net.simpleframework.mvc.component.ext.attachments.AttachmentUtils;
import net.simpleframework.mvc.component.ext.attachments.IAttachmentSaveCallback;
import net.simpleframework.mvc.component.ext.ckeditor.HtmlEditorBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsTopicForm extends FormTableRowTemplatePage implements IBbsContextAware {
	static BbsTopic getTopic(final PageParameter pp) {
		return getCacheBean(pp, context.getTopicService(), "topicId");
	}

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);
		// 验证
		addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#te_topic, #te_categoryText, #te_content"));

		// Html编辑器
		addHtmlEditorBean(pp).setTextarea("te_content").setToolbarCanCollapse(false);

		// 上传
		addComponentBean(pp, "BbsTopicForm_upload_page", AttachmentBean.class).setInsertTextarea(
				"te_content").setHandleClass(BbsTopicAttachmentAction.class);
		addComponentBean(pp, "BbsTopicForm_upload", WindowBean.class)
				.setContentRef("BbsTopicForm_upload_page").setTitle($m("BbsTopicFormTPage.4"))
				.setPopup(true).setHeight(480).setWidth(400);
	}

	protected HtmlEditorBean addHtmlEditorBean(final PageParameter pp) {
		return (HtmlEditorBean) addHtmlEditorBean(pp, "BbsTopicForm_editor").setElementsPath(false)
				.setHeight("380");
	}

	protected void createCategoryDict(final PageParameter pp) {
		addComponentBean(pp, "BbsTopicForm_dict_tree", TreeBean.class).setHandleClass(
				CategorySelectedTree.class);
		addComponentBean(pp, "BbsTopicForm_dict", DictionaryBean.class).setBindingId("te_categoryId")
				.setBindingText("te_categoryText").addTreeRef(pp, "BbsTopicForm_dict_tree")
				.setTitle($m("BbsTopicFormTPage.5"));
	}

	@Transaction(context = IBbsContext.class)
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws IOException {
		final BbsCategory category = context.getCategoryService().getBean(
				cp.getParameter("te_categoryId"));
		if (category == null) {
			throw ContentException.of($m("BbsTopicFormTPage.2"));
		}

		final IBbsTopicService service = context.getTopicService();
		BbsTopic topic = service.getBean(cp.getParameter("te_id"));
		final boolean insert = topic == null;
		if (insert) {
			topic = service.createBean();
			topic.setCreateDate(new Date());
			topic.setUserId(cp.getLoginId());

			final EBbsType bbsType = Convert.toEnum(EBbsType.class, cp.getParameter("te_t"));
			if (bbsType != null) {
				topic.setBbsType(bbsType);
			}
		}
		topic.setCategoryId(category.getId());
		topic.setTopic(cp.getParameter("te_topic"));

		topic.setDescription(cp.getParameter("te_description"));

		final Document doc = HtmlUtils.createHtmlDocument(cp.getParameter("te_content"));
		topic.setContent(doTopicContent(cp, topic, doc));

		final BbsTopic topic2 = topic;
		final ComponentParameter nCP = ComponentParameter.get(cp,
				cp.getComponentBeanByName("BbsTopicForm_upload_page"));
		AttachmentUtils.doSave(nCP, new IAttachmentSaveCallback() {
			@Override
			public void save(final Map<String, AttachmentFile> addQueue, final Set<String> deleteQueue) {
				final IAttachmentService<Attachment> aService = context.getAttachmentService();
				if (insert) {
					service.insert(topic2);
				} else {
					service.update(topic2);
					if (deleteQueue != null) {
						aService.delete(deleteQueue.toArray(new Object[] { deleteQueue.size() }));
					}
				}
				aService.insert(topic2.getId(), cp.getLoginId(), addQueue);
			}
		});
		final JavascriptForward js = new JavascriptForward();
		js.append("$Actions.loc('")
				.append(((IBbsWebContext) context).getUrlsFactory().getPostViewUrl(cp, topic))
				.append("');");
		return js;
	}

	public String doTopicContent(final PageParameter pp, final Object bean, final Document doc) {
		final ArrayList<IElementVisitor> al = new ArrayList<IElementVisitor>();
		al.add(HtmlUtils.REMOVE_TAG_VISITOR("script"));
		al.add(HtmlUtils.STRIP_CONTEXTPATH_VISITOR(pp.request));
		setVisitor_targetBlank(bean, al);
		setVisitor_removeClass(bean, al);
		setVisitor_removeStyle(bean, al);
		return HtmlUtils.doDocument(doc, al.toArray(new IElementVisitor[al.size()])).html();
	}

	protected void setVisitor_removeStyle(final Object bean, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("div", new String[] { "style" }));
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("p", new String[] { "style" }));
	}

	protected void setVisitor_targetBlank(final Object bean, final List<IElementVisitor> al) {
		al.add(HtmlUtils.TARGET_BLANK_VISITOR);
	}

	protected void setVisitor_removeClass(final Object bean, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("class"));
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final InputElement te_id = InputElement.hidden("te_id");
		final InputElement te_topic = new InputElement("te_topic");
		InputElement te_t = null;
		final InputElement te_description = InputElement.textarea("te_description").setRows(3);
		final InputElement te_content = InputElement.textarea("te_content")
				.setStyle("display: none;");
		BbsCategory category = null;
		final BbsTopic topic = getTopic(pp);
		if (topic != null) {
			te_id.setText(topic.getId());
			te_topic.setText(topic.getTopic());
			te_content.setText(HtmlUtils.wrapContextPath(pp.request, topic.getContent()));
			te_description.setText(topic.getDescription());
			category = context.getCategoryService().getBean(topic.getCategoryId());
		} else {
			// insert
			final EBbsType bbsType = Convert.toEnum(EBbsType.class, pp.getParameter("t"));
			if (bbsType != null) {
				te_t = InputElement.hidden("te_t").setText(bbsType.name());
			}
		}
		if (category == null) {
			category = context.getCategoryService().getBean(pp.getParameter("categoryId"));
		}

		final InputElement te_categoryId = InputElement.hidden("te_categoryId");
		if (category != null) {
			te_categoryId.setText(category.getId());
		}
		//
		TableRow r1;
		if (category != null && (topic == null || !pp.getLogin().isManager())) {
			r1 = new TableRow(
					new RowField($m("BbsTopicListTPage.0"), te_id, te_categoryId, te_topic)
							.setStarMark(true));
		} else {
			// 类目字典
			createCategoryDict(pp);

			final TextButton te_categoryText = new TextButton("te_categoryText")
					.setOnclick("$Actions['BbsTopicForm_dict']()");
			if (category != null) {
				te_categoryText.setText(category.getText());
			}
			r1 = new TableRow(
					new RowField($m("BbsTopicListTPage.0"), te_id, te_topic).setStarMark(true),
					new RowField($m("BbsTopicFormTPage.1"), te_categoryId, te_categoryText)
							.setElementsStyle("width:135px;").setStarMark(true));
		}

		final TableRows rows = TableRows.of(r1);
		// if (bbsType == EBbsType.vote) {
		// final BlockElement block = new BlockElement();
		// rows.add(new TableRow().setBlankElement(block));
		// }
		final TableRow r2 = new TableRow(new RowField(null, te_t, te_content));
		final TableRow r3 = new TableRow(new RowField($m("Description"), te_t, te_description));
		return rows.append(r2).append(r3);
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final Checkbox opt_viewer = new Checkbox("opt_viewer", $m("BbsTopicFormTPage.3"))
				.setChecked(true);

		final BbsTopic topic = getTopic(pp);
		String attachClick = "$Actions['BbsTopicForm_upload']('opt_viewer=' + $F('opt_viewer')";
		if (topic != null) {
			attachClick += " + '&topicId=" + topic.getId() + "'";
		}
		attachClick += ");";

		final ElementList el = ElementList.of();
		el.append(opt_viewer).append(SpanElement.SPACE);
		el.append(new LinkButton($m("BbsTopicFormTPage.4")).setOnclick(attachClick));
		return el;
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		return ElementList.of(SAVE_BTN());
	}

	@Override
	protected ButtonElement SAVE_BTN() {
		return super.SAVE_BTN().setText($m("BbsTopicFormTPage.6"));
	}

	@Override
	public int getLabelWidth(final PageParameter pp) {
		return 45;
	}

	@Override
	public boolean isButtonsOnTop(final PageParameter pp) {
		return true;
	}

	public static class CategorySelectedTree extends DictionaryTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final IBbsCategoryService service = context.getCategoryService();
			final IDataQuery<BbsCategory> dq = service
					.queryChildren((BbsCategory) (parent != null ? parent.getDataObject() : null));
			BbsCategory category;
			final TreeNodes nodes = TreeNodes.of();
			while ((category = dq.next()) != null) {
				final TreeNode tn = new TreeNode((TreeBean) cp.componentBean, parent, category);
				nodes.add(tn);
			}
			return nodes;
		}

		@Override
		public Map<String, Object> getTreenodeAttributes(final ComponentParameter cp,
				final TreeNode treeNode, final TreeNodes children) {
			final Map<String, Object> attri = super.getTreenodeAttributes(cp, treeNode, children);
			attri.put(TN_ATTRI_SELECT_DISABLE, children != null && children.size() > 0);
			return attri;
		}
	}

	public static class BbsTopicAttachmentAction extends
			AbstractAttachmentLogHandler<Attachment, BbsTopic> {

		@Override
		protected IAttachmentService<Attachment> getAttachmentService() {
			return context.getAttachmentService();
		}

		@Override
		protected IDbBeanService<BbsTopic> getOwnerService() {
			return context.getTopicService();
		}

		@Override
		protected String getOwnerIdParameterKey() {
			return "topicId";
		}

		@Override
		public AbstractElement<?> getDownloadLink(final ComponentParameter cp,
				final AttachmentFile attachmentFile, final String id) {
			if (Convert.toBool(cp.getParameter("opt_viewer"))) {
				final ImageElement iElement = createImageViewer(cp, attachmentFile, id);
				if (iElement != null) {
					return iElement;
				}
			}
			return new LinkElement(attachmentFile.getTopic())
					.setOnclick("$Actions['BbsPostViewTPage_download']('id=" + id + "');");
		}
	}
}