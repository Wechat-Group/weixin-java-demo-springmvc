package com.github.controller;

import com.github.service.CoreService;
import com.github.util.MD5Util;
import com.github.util.ReturnModel;
import com.github.util.Sha1Util;
import com.github.util.XMLUtil;
import com.google.gson.Gson;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.pay.request.WxPayUnifiedOrderRequest;
import me.chanjar.weixin.mp.bean.pay.result.WxPayUnifiedOrderResult;
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


    //企业向个人转账微信API路径
    private static final String ENTERPRISE_PAY_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
    //apiclient_cert.p12证书存放路径
    private static final String CERTIFICATE_LOCATION = "";
    @Autowired
    protected WxMpConfigStorage configStorage;
    @Autowired
    protected WxMpService wxMpService;
    @Autowired
    protected CoreService coreService;

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
                .info("PartnerKey is :" + this.configStorage.getPartnerKey());
        WxPayUnifiedOrderResult result = this.wxMpService.getPayService()
                .unifiedOrder(payInfo);
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
        prepayInfo
                .setTotalFee(Integer.valueOf(request.getParameter("total_fee")));
        prepayInfo.setBody(request.getParameter("body"));
        prepayInfo.setTradeType(request.getParameter("trade_type"));
        prepayInfo.setSpbillCreateIp(request.getParameter("spbill_create_ip"));
        //TODO(user) 填写通知回调地址
        prepayInfo.setNotifyURL("");

        try {
            Map<String, String> payInfo = this.wxMpService.getPayService()
                    .getPayInfo(prepayInfo);
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
                if (this.wxMpService.getPayService().checkSign(kvm,
                        configStorage.getPartnerKey())) {
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

    @RequestMapping(value = "payToIndividual")
    public void payToIndividual(HttpServletResponse response,
                                HttpServletRequest request) {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("mch_appid", this.configStorage.getAppId());
        map.put("mchid", this.configStorage.getPartnerId());
        map.put("nonce_str", Sha1Util.getNonceStr());
        map.put("partner_trade_no", request.getParameter("partner_trade_no"));
        map.put("openid", request.getParameter("openid"));
        map.put("check_name", "NO_CHECK");
        map.put("amount", request.getParameter("amount"));
        map.put("desc", request.getParameter("desc"));
        map.put("spbill_create_ip", request.getParameter("spbill_create_ip"));
        try {
            Map<String, String> returnMap = enterprisePay(map,
                    this.configStorage.getPartnerKey(), CERTIFICATE_LOCATION,
                    ENTERPRISE_PAY_URL);
            if ("SUCCESS".equals(returnMap.get("result_code").toUpperCase())
                    && "SUCCESS"
                    .equals(returnMap.get("return_code").toUpperCase())) {
                this.logger.info("企业对个人付款成功！\n付款信息：\n" + returnMap.toString());
            } else {
                this.logger.error("err_code: " + returnMap.get("err_code")
                        + "  err_code_des: " + returnMap.get("err_code_des"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 企业付款方法,传入map,
     * 除用到上面内容外,还用到path(证书磁盘路径),key(商户支付密匙),url(接口地址)
     *
     * @return
     */
    /**
     * @param map,根据map中的 map中包含字段
     *                    mch_appid         微信分配的公众账号ID（企业号corpid即为此appId）
     *                    mchid             微信支付分配的商户号
     *                    nonce_str         随机字符串，不长于32位
     *                    partner_trade_no  商户订单号，需保持唯一性
     *                    openid            商户appid下，某用户的openid
     *                    check_name        是否校验真实姓名,如果需要,则还需要传re_user_name字段    NO_CHECK：不校验真实姓名
     *                    amount            支付金额,以分为单位
     *                    desc              企业付款操作说明信息。必填。
     *                    spbill_create_ip  调用接口的机器Ip地址(随便填,查询订单详情时会显示出来)
     * @param keys        商品平台支付密匙
     * @param paths       证书路径
     * @param uri         接口地址
     * @return 返回Map<String,String>
     * @throws Exception
     */
    public Map<String, String> enterprisePay(Map<String, String> map, String keys, String paths, String uri) throws Exception {
        String mchId = map.get("mchid");
        Set<Map.Entry<String, String>> entry2 = map.entrySet();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> obj : entry2) {
            String k = obj.getKey();
            String v = obj.getValue();
            if (null == v || "".equals(v)) continue;
            sb.append(k).append('=').append(v).append('&');
        }
        sb.append("key=").append(keys);
        String str2 = MD5Util.md5Encode(sb.toString(), "UTF-8").toUpperCase();
        map.put("sign", str2);
        StringBuilder builder = new StringBuilder();
        builder.append("<xml>");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append('<').append(entry.getKey()).append('>')
                    .append(entry.getValue())
                    .append("</").append(entry.getKey()).append('>');
        }
        builder.append("</xml>");
        String desc = new String(builder.toString().getBytes("UTF-8"), "ISO-8859-1");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream instream = new FileInputStream(new File(paths))) {
            keyStore.load(instream, mchId.toCharArray());
        }
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mchId.toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        Map<String, String> returnMap;
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
            HttpPost httpPost = new HttpPost(uri);
            StringEntity str = new StringEntity(desc);
            httpPost.setEntity(str);
            returnMap = getMap(httpclient, httpPost);
        }
        return returnMap;
    }

    private Map<String, String> getMap(CloseableHttpClient httpclient, HttpPost httpPost) throws Exception {
        Map<String, String> returnMap;
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            returnMap = getReturnMap(response);
        }
        return returnMap;
    }

    private Map<String, String> getReturnMap(CloseableHttpResponse response) throws Exception {
        Map<String, String> returnMap = new HashMap<String, String>();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), UTF_8))) {
                String text = bufferedReader.readLine();
                StringBuilder result = new StringBuilder();
                while (null != text) {
                    result.append(text);
                    text = bufferedReader.readLine();
                }
                //调用统一接口返回的值转换为XML格式
                returnMap = XMLUtil.parseXmlStringToMap(new String(result.toString().getBytes(UTF_8), "UTF-8"));
            }
        }
        EntityUtils.consume(entity);
        return returnMap;
    }
}

