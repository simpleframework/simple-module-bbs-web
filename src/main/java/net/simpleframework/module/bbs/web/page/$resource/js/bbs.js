var _BBS = {
    
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

  reply : function(_replyId, _to) {
    var ta = $("idBbsPostViewTPage_editor");
    var bar = ta.previous(".edit_bar");
    var cc = bar.down("span");
    var replyId = cc.previous();
    replyId.setAttribute("name", "replyId");
    
    var clear = function() {
      replyId.value = "";
      cc.innerHTML = "&nbsp;";
    };
    if (_replyId) {
      replyId.value = _replyId;
      cc.title = "reply: " + _to;
      cc.innerHTML = "<span class='reply_btn'>" 
                   +   _to 
                   +   "<span class='delete_img'></span>"
                   + "</span>";
      cc.down(".delete_img").observe("click", clear);
    } else {
      clear();
    }
    
    bar.scrollTo();
    ta.htmlEditor.focus();
    $Actions.setValue(ta, "");
  },
  
  edit : function(_postId, _content, _cancelBtn) {
    var ta = $("idBbsPostViewTPage_editor");
    var bar = ta.previous(".edit_bar");
    var cc = bar.down("span");
    if (_postId) {
      var postId = cc.previous();
      postId.setAttribute("name", "postId");
      postId.value = _postId;
      
      cc.innerHTML = _cancelBtn;
      cc.down().observe("click", function(evn) {
        postId.value = "";
        cc.innerHTML = "&nbsp;";
        $Actions.setValue(ta, "");
      });
    }
    
    bar.scrollTo();
    ta.htmlEditor.focus();
    $Actions.setValue(ta, _content);
  },
  
  remark : function(btn) {
    var c = btn.up(".BbsContent_Bar");
    var r = c.next();
    var ta = r.down("textarea");
    ta.clear();
    r.$toggle({
      afterFinish: function() {
        ta.focus();
      }
    });
  },
  
  doRemark : function(btn, postId) {
    var c = btn.up(".BbsContent_Remark_Edit");
    var ta = c.down("textarea");
    var act = $Actions['BbsPostViewTPage_remark'];
    act.jsCompleteCallback = function(req, responseText, json) {
      c.hide();
      var cc = c.previous(".BbsPostContent");
      var act2 = $Actions["BbsPostViewTPage_remark_list"];
      act2.container = cc.down(".BbsContent_Remark_List");
      act2("postId=" + postId);
    };
    act("postId=" + postId + "&ta=" + $F(ta));
  }
};