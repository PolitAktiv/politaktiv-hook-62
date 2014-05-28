/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.politaktiv.captcha;

import com.liferay.portal.kernel.captcha.Captcha;
import com.liferay.portal.kernel.captcha.CaptchaException;
import com.liferay.portal.kernel.captcha.CaptchaMaxChallengesException;
import com.liferay.portal.kernel.captcha.CaptchaTextException;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;

import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;

import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;

import org.politaktiv.captcha.MathCaptchaFormula;

public class MathCaptcha implements Captcha {

    private static final String CAPTCHA_COUNT = "CAPTCHA_COUNT";
    private static final String CAPTCHA_TEXT_EXPECTED = "CAPTCHA_TEXT_EXPECTED";
    private static final String CAPTCHA_TEXT_ENTERED = "captchaText";
    private static final int CAPTCHA_MAX_CHALLENGES = GetterUtil.getInteger(PropsUtil
	    .get(PropsKeys.CAPTCHA_MAX_CHALLENGES));
    private static final String _TAGLIB_PATH = "/html/taglib/ui/captcha/mathcaptcha.jsp";
    private static final RandomUtil RANDOM_UTIL = new RandomUtil();

    private ArrayList<Class<? extends MathCaptchaFormula>> aFormulaClasses;

    public MathCaptcha() {
	aFormulaClasses = new ArrayList<Class<? extends MathCaptchaFormula>>();
	this.registerFormulaClass(FormulaMul2AndAddOne.class);
	this.registerFormulaClass(FormulaAdd2AndMulOne.class);
    }

    public String getTaglibPath() {
	return _TAGLIB_PATH;
    }

    public boolean isEnabled(HttpServletRequest request) throws CaptchaException {

	HttpSession session = request.getSession();
	Integer count = (Integer) session.getAttribute(CAPTCHA_COUNT);

	return isEnabled(count);
    }

    public boolean isEnabled(PortletRequest portletRequest) throws CaptchaException {

	PortletSession portletSession = portletRequest.getPortletSession();
	Integer count = (Integer) portletSession.getAttribute(CAPTCHA_COUNT);

	return isEnabled(count);
    }

    public void check(HttpServletRequest request) throws CaptchaException {
	if (!isEnabled(request)) {
	    return;
	}

	HttpSession session = request.getSession();
	String captchaTextExpected = (String) session.getAttribute(CAPTCHA_TEXT_EXPECTED);
	String captchaTextEnterd = ParamUtil.getString(request, CAPTCHA_TEXT_ENTERED);
	Integer captchaCount = (Integer) session.getAttribute(CAPTCHA_COUNT);
	if ((CAPTCHA_MAX_CHALLENGES > 0) && (Validator.isNotNull(request.getRemoteUser()))) {
	    session.setAttribute(CAPTCHA_COUNT, incrementCount(captchaCount));
	}

	check(captchaTextExpected, captchaTextEnterd);
	session.removeAttribute(CAPTCHA_TEXT_EXPECTED);
    }

    public void check(PortletRequest portletRequest) throws CaptchaException {
	PortletSession portletSession = portletRequest.getPortletSession();
	String captchaTextExpected = (String) portletSession.getAttribute(CAPTCHA_TEXT_EXPECTED);
	String captchaTextEnterd = ParamUtil.getString(portletRequest, CAPTCHA_TEXT_ENTERED);
	Integer captchaCount = (Integer) portletSession.getAttribute(CAPTCHA_COUNT);
	if ((CAPTCHA_MAX_CHALLENGES > 0) && (Validator.isNotNull(portletRequest.getRemoteUser()))) {
	    portletSession.setAttribute(CAPTCHA_COUNT, incrementCount(captchaCount));
	}

	check(captchaTextExpected, captchaTextEnterd);
	portletSession.removeAttribute(CAPTCHA_TEXT_EXPECTED);
    }

    public void serveImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
	MathCaptchaFormula oFormula = this.createRandomFormulaObj();
	String sQ = oFormula.getQuestion();
	String sR = String.valueOf(oFormula.calculateResult());

	HttpSession oSession = request.getSession();
	oSession.setAttribute(CAPTCHA_TEXT_EXPECTED, sR);

	response.setContentType(ContentTypes.TEXT_HTML);
	response.getWriter().print(this.buildIFrameContent(sQ));
    }

    @Override
    public void serveImage(ResourceRequest request, ResourceResponse response) throws IOException {
	MathCaptchaFormula oFormula = this.createRandomFormulaObj();
	String sQ = oFormula.getQuestion();
	String sR = String.valueOf(oFormula.calculateResult());

	PortletSession oSession = request.getPortletSession();
	oSession.setAttribute(CAPTCHA_TEXT_EXPECTED, sR);

	response.setContentType(ContentTypes.TEXT_HTML);
	response.getWriter().print(this.buildIFrameContent(sQ));
    }

    Integer incrementCount(Integer captchaCount) {
	Integer result;
	if (captchaCount == null) {
	    result = new Integer(1);
	} else {
	    result = new Integer(captchaCount.intValue() + 1);
	}
	return result;
    }

    void check(String captchaTextExpected, String captchaTextEnterd)
	    throws CaptchaTextException {
	if (captchaTextExpected == null) {
	    throw new CaptchaTextException("the expected result was not set for current session.");
	}

	if (!captchaTextExpected.equals(captchaTextEnterd)) {
	    throw new CaptchaTextException("wrong result");
	}
    }

    boolean isEnabled(Integer count) throws CaptchaMaxChallengesException {
	if (CAPTCHA_MAX_CHALLENGES > 0) {
	    if (count != null && count >= CAPTCHA_MAX_CHALLENGES) {
		throw new CaptchaMaxChallengesException();
	    }

	    if ((count != null) && (CAPTCHA_MAX_CHALLENGES <= count.intValue())) {
		return false;
	    } else {
		return true;
	    }
	} else if (CAPTCHA_MAX_CHALLENGES < 0) {
	    return false;
	} else {
	    return true;
	}
    }

    private String buildIFrameContent(String sQuestion) {
	String sDoc = "";
	sDoc += "<!DOCTYPE html>\r\n";
	sDoc += "<html>\r\n";
	sDoc += "<head></head>\r\n";
	sDoc += "<body>\r\n";
	sDoc += "<div class=\"matchcaptcha-question\">";
	sDoc += sQuestion;
	sDoc += "</div>\r\n";
	sDoc += "</body>\r\n";
	sDoc += "</html>";

	return sDoc;
    }

    private MathCaptchaFormula createRandomFormulaObj() {
	int uCount = this.aFormulaClasses.size();

	Class<? extends MathCaptchaFormula> oFormulaClass = null;

	MathCaptchaFormula oFormula = null;

	if (uCount == 1)
	    oFormulaClass = this.aFormulaClasses.get(0);
	else if (uCount > 1)
	    oFormulaClass = this.aFormulaClasses.get(RANDOM_UTIL.getRandomUnsignedInt(0, uCount - 1));

	if (oFormulaClass != null) {
	    try {
		oFormula = oFormulaClass.newInstance();
		oFormula.initFormulaVars();
	    } catch (Exception e) {
		throw new Error(e);
	    }
	}

	return oFormula;
    }

    private boolean registerFormulaClass(Class<? extends MathCaptchaFormula> clazz) {
	if (clazz == null)
	    return false;
	if (!this.aFormulaClasses.contains(clazz))
	    this.aFormulaClasses.add(clazz);
	return true;
    }

}