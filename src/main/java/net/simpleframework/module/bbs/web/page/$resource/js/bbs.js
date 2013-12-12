$ready(function() {
  var ta = $("idBbsPostViewTPage_editor");
  var edit_bar = ta.previous(".edit_bar");
  var cc_span = edit_bar.down("span");
  var CC = cc_span.innerHTML;
  var input = cc_span.previous("input");

  var _clear = function() {
    input.value = "";
    cc_span.innerHTML = CC;
  };

  var _txt = function(txt, focus) {
    if (focus) {
      ta.htmlEditor.focus();
      edit_bar.scrollTo();
    }
    $Actions.setValue(ta, txt);
  };
  
  var _info = function(_to, del) {
    cc_span.innerHTML = "<span class='reply_btn'>" 
                      + _to
                      + "<span class='delete_img'></span>" 
                      + "</span>";
    cc_span.down(".delete_img").observe("click", del);
  };

  window._BBS = {
    replyFrom : function(btn, params) {
      var c = btn.up(".BbsContent");
      var r = c.previous();
      if (r.innerHTML != "") {
        r.$toggle();
      } else {
        var act = $Actions["BbsPostViewTPage_replyFrom"];
        act.jsCompleteCallback = function(req, responseText, json) {
          r.update(responseText);
          r.$show();
        };
        act(params);
      }
    },

    reply : function(replyId, to) {
      if (replyId) {
        var arr = replyId.split(":");
        input.setAttribute("name", arr[0]);
        input.value = arr[1];

        _info(to, _clear);
      } else {
        _clear();
      }

      _txt("", true);
    },

    edit : function(postId, content, to) {
      var arr = postId.split(":");
      input.setAttribute("name", arr[0]);
      input.value = arr[1];

      _info(to, function(evn) {
        _clear();
        _txt("", true);
      });
      
      _txt(content, true);
    },

    doRemark_callback : function(parentId, params) {
      var r = $("remark_" + parentId);
      r.scrollTo();

      _clear();
      _txt("");

      var act2 = $Actions["BbsPostViewTPage_remark_list"];
      act2.container = r;
      act2(("parentId=" + parentId).addParameter(params));
    },

    doRemark_delete : function(btn, remarkId) {
      var act = $Actions['BbsPostViewTPage_remark_delete'];
      act.jsCompleteCallback = function(req, responseText, json) {
        var act2 = $Actions["BbsPostViewTPage_remark_list"];
        act2.container = btn.up(".BbsContent_Remark_List");
        act2(json.params);
      };
      act("remarkId=" + remarkId);
    }
  };
});
