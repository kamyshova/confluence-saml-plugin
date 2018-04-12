/**
 * Confluence SAML Plugin - a confluence plugin to allow SAML 2.0
 *  authentication. 
 *
 *  Copyright (C) 2014 Bitium, Inc.
 *  
 *  This file is part of Confluence SAML Plugin.
 *  
 *  Confluence SAML Plugin is free software: you can redistribute it 
 *  and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *  
 *  Confluence SAML Plugin is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with Pineapple. If not, see <http://www.gnu.org/licenses/>.
 */
AJS.$(function() {
  if (AJS.$(".aui.login-form-container").length) {
    AJS.$(".aui.login-form-container").hide();
    AJS.$('<div class="field-group"><a class="aui-button aui-style aui-button-primary" href="plugins/servlet/globalconsumer" style="align:center;">Use Corporate Login</a></div><h2 style="margin-top:10px"></h2>').insertAfter(AJS.$(".aui.login-form-container #action-messages"));

    var query = location.search.substr(1);
    query.split("&").forEach(function(part) {
      var item = part.split("=");
      if (item.length == 2 && item[0] == "samlerror") {
        var errorKeys = {};
        errorKeys["general"] = "General SAML configuration error";
        errorKeys["user_not_found"] = "User was not found";
        errorKeys["plugin_exception"] = "SAML plugin internal error";
        AJS.$(".aui.login-form-container").show();
        var message = '<div class="aui-message closeable error">' + errorKeys[item[1]] + '</div>';
        AJS.$(message).insertBefore(AJS.$(".aui.login-form-container"));
      }
    });

    if (location.search == '?logout=true') {
      $.ajax({
        url : AJS.contextPath() + "/plugins/servlet/saml/getajaxconfig?param=logoutUrl",
        type : "GET",
        error : function() {
        },
        success : function(response) {
          if (response != "") {
            AJS.$('<p>Please wait while we redirect you to your company log out page</p>').insertBefore(AJS.$(".aui.login-form-container"));
            window.location.href = response;
            return;
          }
          AJS.$(".aui.login-form-container").show();
        }
      });
      return;
    }

    $.ajax({
      url : AJS.contextPath() + "/plugins/servlet/saml/getajaxconfig?param=idpRequired",
      type : "GET",
      error : function() {
      },
      success : function(response) {
        if (response == "true") {
          // AJS.$('<img src="download/resources/com.bitium.confluence.SAML2Plugin/images/progress.png"/>').insertBefore(AJS.$(".aui.login-form-container"));
          AJS.$('<p>Please wait while we redirect you to your company log in page</p>').insertBefore(AJS.$(".aui.login-form-container"));
          window.location.href = 'plugins/servlet/globalconsumer';

        } else {
          AJS.$(".aui.login-form-container").show();
        }
      }
    });
  }
});
