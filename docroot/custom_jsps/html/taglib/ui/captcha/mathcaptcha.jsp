<%
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>

<%@ include file="/html/taglib/ui/captcha/init.jsp" %>

<%
String url = (String)request.getAttribute("liferay-ui:captcha:url");

boolean captchaEnabled = false;

try {
	if (portletRequest != null) {
		captchaEnabled = CaptchaUtil.isEnabled(portletRequest);
	}
	else {
		captchaEnabled = CaptchaUtil.isEnabled(request);
	}
}
catch (CaptchaMaxChallengesException cmce) {
	captchaEnabled = true;
}
%>

<c:if test="<%= captchaEnabled %>">
	<div class="taglib-captcha">
		<iframe id="mathcaptcha_result_frame" src="<%= url %>" style="visibility:hidden;" width="1" height="1" scrolling="no" frameborder="0"></iframe>
		<div id="mathcaptcha_result_panel" class="mathcaptcha-result-panel" style="font-weight:bold"></div>
		<script type="text/javascript">
			/*<![CDATA[*/
			var oMathCaptchaResultFrame = document.getElementById('mathcaptcha_result_frame');			
			var pInitMathCaptcha = function(oEvt) {
				var oMathCaptchaResultPanel = document.getElementById('mathcaptcha_result_panel');
				if(!oMathCaptchaResultPanel)
					return;
				oMathCaptchaResultPanel.innerHTML = oMathCaptchaResultFrame.contentDocument.body.innerHTML; 
			};
			if(window.attachEvent) {
				window.attachEvent("onload",pInitMathCaptcha);	
			} else if(oMathCaptchaResultFrame.contentWindow.addEventListener) {
				window.addEventListener("load",pInitMathCaptcha,false);
			}			
			/*]]>*/           
		</script>
 		<table class="lfr-table">
		<tr>
			<td>
				Ergebnis
				<!-- <liferay-ui:message key="text-verification" /> -->
			</td>
			<td>
				<input name="<%= namespace %>captchaText" size="10" type="text" value="" />
			</td>
		</tr>
		</table>
	</div>
</c:if>