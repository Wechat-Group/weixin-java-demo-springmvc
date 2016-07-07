package com.github.controller;

import com.github.util.MD5Util;
import com.github.util.ReturnModel;
import com.github.util.XMLUtil;
import com.google.gson.Gson;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpPrepayIdResult;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    protected WxMpConfigStorage configStorage;
    @Autowired
    protected WxMpService wxMpService;

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    /**
     * 用于返回预支付的结果 WxMpPrepayIdResult，一般不需要使用此接口
     *
     * @param response
     * @param openid
     * @param out_trade_no
     * @param total_fee
     * @param body
     * @param trade_type
     * @param spbill_create_ip
     */
    @RequestMapping(value = "getPrepayIdResult")
    public void getPrepayId(HttpServletResponse response,
                            @RequestParam(value = "openid") String openid,
                            @RequestParam(value = "out_trade_no") String out_trade_no,
                            @RequestParam(value = "total_fee") String total_fee,
                            @RequestParam(value = "body") String body,
                            @RequestParam(value = "trade_type") String trade_type,
                            @RequestParam(value = "spbill_create_ip") String spbill_create_ip) {
        WxMpPrepayIdResult wxMpPrepayIdResult;
        Map<String, String> payInfo = new HashMap<String, String>();
        payInfo.put("openid", openid);
        payInfo.put("out_trade_no", out_trade_no);
        payInfo.put("total_fee", total_fee);
        payInfo.put("body", body);
        payInfo.put("trade_type", trade_type);
        payInfo.put("spbill_create_ip", spbill_create_ip);
        payInfo.put("notify_url", "");
        log.info("PartnerKey is :" + configStorage.getPartnerKey());
        wxMpPrepayIdResult = wxMpService.getPrepayId(payInfo);
        log.info(new Gson().toJson(wxMpPrepayIdResult));
        renderString(response, wxMpPrepayIdResult);
    }

    /**
     * 返回前台H5调用JS支付所需要的参数，公众号支付调用此接口
     *
     * @param response
     * @param openid
     * @param out_trade_no
     * @param total_fee
     * @param body
     * @param trade_type
     * @param spbill_create_ip
     */
    @RequestMapping(value = "getJSSDKPayInfo")
    public void getJSSDKPayInfo(HttpServletResponse response,
                                @RequestParam(value = "openid") String openid,
                                @RequestParam(value = "out_trade_no") String out_trade_no,
                                @RequestParam(value = "total_fee") String total_fee,
                                @RequestParam(value = "body") String body,
                                @RequestParam(value = "trade_type") String trade_type,
                                @RequestParam(value = "spbill_create_ip") String spbill_create_ip) {
        ReturnModel returnModel = new ReturnModel();
        Map<String, String> prepayInfo = new HashMap<String, String>();
        prepayInfo.put("openid", openid);
        prepayInfo.put("out_trade_no", out_trade_no);
        prepayInfo.put("total_fee", total_fee);
        prepayInfo.put("body", body);
        prepayInfo.put("trade_type", trade_type);
        prepayInfo.put("spbill_create_ip", spbill_create_ip);
        //微信通知支付结果地址
        prepayInfo.put("notify_url", "");
        try {
            Map<String, String> payInfo = wxMpService.getPayInfo(prepayInfo);
            returnModel.setResult(true);
            returnModel.setDatum(payInfo);
            renderString(response, returnModel);
        } catch (WxErrorException e) {
            returnModel.setResult(false);
            returnModel.setReason(e.getError().toString());
            renderString(response, returnModel);
            log.error(e.getError().toString());
        }
    }

    /**
     * 微信通知支付结果的回调地址，notify_url
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "getJSSDKCallbackData")
    public void getJSSDKCallbackData(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> kvm = XMLUtil.parseRequestXmlToMap(request);
            if (wxMpService.checkJSSDKCallbackDataSignature(kvm, kvm.get("sign"))) {
                //// TODO: 2016/7/7 0007 处理微信服务器通知的支付结果并对业务系统进行操作
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 企业付款方法,传入map,
     *
     * 除用到上面内容外,还用到path(证书磁盘路径),key(商户支付密匙),url(接口地址)
     * @return
     */
    /**
     * @param map,根据map中的 map中包含字段
     *                    mch_appid	微信分配的公众账号ID（企业号corpid即为此appId）
     *                    mchid		微信支付分配的商户号
     *                    nonce_str	随机字符串，不长于32位
     *                    partner_trade_no		商户订单号，需保持唯一性
     *                    openid		商户appid下，某用户的openid
     *                    check_name	是否校验真实姓名,如果需要,则还需要传re_user_name字段    NO_CHECK：不校验真实姓名
     *                    amount		支付金额,以分为单位
     *                    desc		企业付款操作说明信息。必填。
     *                    spbill_create_ip	调用接口的机器Ip地址(随便填,查询订单详情时会显示出来)
     * @param keys        商品平台支付密匙
     * @param paths       证书路径
     * @param uri         接口地址
     * @return 返回Map<String,String>
     * @throws Exception
     */
    public Map<String, String> enterprisePay(TreeMap<String, String> map, String keys, String paths, String uri) throws Exception {
        Map<String, String> returnMap = new HashMap<String, String>();
        String mch_id = map.get("mchid");
        Set<Map.Entry<String, String>> entry2 = map.entrySet();
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> obj : entry2) {
            String k = obj.getKey();
            String v = obj.getValue();
            if (v == null && v.equals(""))
                continue;
            sb.append(k + "=" + v + "&");
        }
        sb.append("key=" + keys);
        String str2 = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
        map.put("sign", str2);
        StringBuffer buffer = new StringBuffer();
        buffer.append("<xml>");
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                buffer.append("<" + entry.getKey() + ">");
                buffer.append(entry.getValue());
                buffer.append("</" + entry.getKey() + ">");
            }
        }
        buffer.append("</xml>");
        String desc = new String(buffer.toString().getBytes("UTF-8"), "ISO-8859-1");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File(paths));
        try {
            keyStore.load(instream, mch_id.toCharArray());
        } finally {
            instream.close();
        }
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mch_id.toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {
            HttpPost httpPost = new HttpPost(uri);
            StringEntity str = new StringEntity(desc);
            httpPost.setEntity(str);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String text;
                    StringBuffer result = new StringBuffer();
                    while ((text = bufferedReader.readLine()) != null) {
                        result.append(text);
                    }
                    returnMap = XMLUtil.parseXmlStringToMap(new String(result.toString().getBytes(), "UTF-8"));//调用统一接口返回的值转换为XML格式
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return returnMap;
    }

}
