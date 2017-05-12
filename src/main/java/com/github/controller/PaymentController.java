package com.github.controller;

import com.github.binarywang.wxpay.bean.request.WxEntPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxEntPayResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.util.SignUtils;
import com.github.util.MD5Util;
import com.github.util.ReturnModel;
import com.github.util.Sha1Util;
import com.github.util.XMLUtil;
import com.google.gson.Gson;
import me.chanjar.weixin.common.exception.WxErrorException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 微信支付Controller
 * <p>
 * Created by FirenzesEagle on 2016/6/20 0020.
 * Email:liumingbo2008@gmail.com
 */
@Controller
@RequestMapping(value = "wxPay")
public class PaymentController extends GenericController {

	@Autowired
	private WxPayConfig payConfig;
	@Autowired
	private WxPayService payService;

	/**
	 * 用于返回预支付的结果 WxMpPrepayIdResult，一般不需要使用此接口
	 *
	 * @param response
	 * @param request
	 * @throws WxErrorException
	 */
	@RequestMapping(value = "getPrepayIdResult")
	public void getPrepayId(HttpServletResponse response,
	                        HttpServletRequest request) throws WxErrorException {
		WxPayUnifiedOrderRequest payInfo = new WxPayUnifiedOrderRequest();
		payInfo.setOpenid(request.getParameter("openid"));
		payInfo.setOutTradeNo(request.getParameter("out_trade_no"));
		payInfo.setTotalFee(Integer.valueOf(request.getParameter("total_fee")));
		payInfo.setBody(request.getParameter("body"));
		payInfo.setTradeType(request.getParameter("trade_type"));
		payInfo.setSpbillCreateIp(request.getParameter("spbill_create_ip"));
		payInfo.setNotifyURL("");
		this.logger
				.info("PartnerKey is :" + this.payConfig.getMchKey());
		WxPayUnifiedOrderResult result = this.payService.unifiedOrder(payInfo);
		this.logger.info(new Gson().toJson(result));
		renderString(response, result);
	}

	/**
	 * 返回前台H5调用JS支付所需要的参数，公众号支付调用此接口
	 *
	 * @param response
	 * @param request
	 */
	@RequestMapping(value = "getJSSDKPayInfo")
	public void getJSSDKPayInfo(HttpServletResponse response,
	                            HttpServletRequest request) {
		ReturnModel returnModel = new ReturnModel();
		WxPayUnifiedOrderRequest prepayInfo = new WxPayUnifiedOrderRequest();
		prepayInfo.setOpenid(request.getParameter("openid"));
		prepayInfo.setOutTradeNo(request.getParameter("out_trade_no"));
		prepayInfo.setTotalFee(Integer.valueOf(request.getParameter("total_fee")));
		prepayInfo.setBody(request.getParameter("body"));
		prepayInfo.setTradeType(request.getParameter("trade_type"));
		prepayInfo.setSpbillCreateIp(request.getParameter("spbill_create_ip"));
		//TODO(user) 填写通知回调地址
		prepayInfo.setNotifyURL("");

		try {
			Map<String, String> payInfo = this.payService.getPayInfo(prepayInfo);
			returnModel.setResult(true);
			returnModel.setDatum(payInfo);
			renderString(response, returnModel);
		} catch (WxErrorException e) {
			returnModel.setResult(false);
			returnModel.setReason(e.getError().toString());
			renderString(response, returnModel);
			this.logger.error(e.getError().toString());
		}
	}

	/**
	 * 微信通知支付结果的回调地址，notify_url
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "getJSSDKCallbackData")
	public void getJSSDKCallbackData(HttpServletRequest request,
	                                 HttpServletResponse response) {
		try {
			synchronized (this) {
				Map<String, String> kvm = XMLUtil.parseRequestXmlToMap(request);
				if (SignUtils.checkSign(kvm, this.payConfig.getMchKey())) {
					if (kvm.get("result_code").equals("SUCCESS")) {
						//TODO(user) 微信服务器通知此回调接口支付成功后，通知给业务系统做处理
						logger.info("out_trade_no: " + kvm.get("out_trade_no") + " pay SUCCESS!");
						response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[ok]]></return_msg></xml>");
					} else {
						this.logger.error("out_trade_no: "
								+ kvm.get("out_trade_no") + " result_code is FAIL");
						response.getWriter().write(
								"<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[result_code is FAIL]]></return_msg></xml>");
					}
				} else {
					response.getWriter().write(
							"<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[check signature FAIL]]></return_msg></xml>");
					this.logger.error("out_trade_no: " + kvm.get("out_trade_no")
							+ " check signature FAIL");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "entPay")
	public void payToIndividual(HttpServletResponse response,
	                            HttpServletRequest request) {
		WxEntPayRequest wxEntPayRequest = new WxEntPayRequest();
		wxEntPayRequest.setAppid(payConfig.getAppId());
		wxEntPayRequest.setMchId(payConfig.getMchId());
		wxEntPayRequest.setNonceStr(Sha1Util.getNonceStr());
		wxEntPayRequest.setPartnerTradeNo(request.getParameter("partner_trade_no"));
		wxEntPayRequest.setOpenid(request.getParameter("openid"));
		wxEntPayRequest.setCheckName("NO_CHECK");
		wxEntPayRequest.setAmount(Integer.valueOf(request.getParameter("amount")));
		wxEntPayRequest.setDescription(request.getParameter("desc"));
		wxEntPayRequest.setSpbillCreateIp(request.getParameter("spbill_create_ip"));

		try {
			WxEntPayResult wxEntPayResult = payService.entPay(wxEntPayRequest);
			if ("SUCCESS".equals(wxEntPayResult.getResultCode().toUpperCase())
					&& "SUCCESS"
					.equals(wxEntPayResult.getReturnCode().toUpperCase())) {
				this.logger.info("企业对个人付款成功！\n付款信息：\n" + wxEntPayResult.toString());
			} else {
				this.logger.error("err_code: " + wxEntPayResult.getErrCode()
						+ "  err_code_des: " + wxEntPayResult.getErrCodeDes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

