package com.github.weixin.demo.service.impl;

import com.github.weixin.demo.handler.LogHandler;
import com.github.weixin.demo.handler.MsgHandler;
import com.github.weixin.demo.handler.SubscribeHandler;
import com.github.weixin.demo.service.CoreService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by FirenzesEagle on 2016/5/30 0030.
 * Email:liumingbo2008@gmail.com
 */
@Service
public class CoreServiceImpl implements CoreService {

    @Autowired
    protected WxMpService wxMpService;
    @Autowired
    protected LogHandler logHandler;
    @Autowired
    protected SubscribeHandler subscribeHandler;
    @Autowired
    protected MsgHandler msgHandler;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private WxMpMessageRouter router;

    @PostConstruct
    public void init() {
        this.refreshRouter();
    }

    @Override
    public void requestGet(String urlWithParams) throws IOException {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpget = new HttpGet(urlWithParams);
        httpget.addHeader("Content-Type", "text/html;charset=UTF-8");
        //配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(50)
            .setConnectTimeout(50)
            .setSocketTimeout(50).build();
        httpget.setConfig(requestConfig);

        CloseableHttpResponse response = httpclient.execute(httpget);
        System.out.println("StatusCode -> " + response.getStatusLine().getStatusCode());

        HttpEntity entity = response.getEntity();
        String jsonStr = EntityUtils.toString(entity);
        System.out.println(jsonStr);

        httpget.releaseConnection();
    }

    @Override
    public void requestPost(String url, List<NameValuePair> params) throws ClientProtocolException, IOException {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        CloseableHttpResponse response = httpclient.execute(httppost);
        System.out.println(response.toString());

        HttpEntity entity = response.getEntity();
        String jsonStr = EntityUtils.toString(entity, "utf-8");
        System.out.println(jsonStr);

        httppost.releaseConnection();
    }

    @Override
    public void refreshRouter() {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(
            this.wxMpService);
        // 记录所有事件的日志
        newRouter.rule().handler(this.logHandler).next();
        // 关注事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
            .event(WxConsts.EventType.SUBSCRIBE).handler(this.subscribeHandler)
                .end();
        // 默认,转发消息给客服人员
        newRouter.rule().async(false).handler(this.msgHandler).end();
        this.router = newRouter;
    }

    @Override
    public WxMpXmlOutMessage route(WxMpXmlMessage inMessage) {
        try {
            return this.router.route(inMessage);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public WxMpUser getUserInfo(String openid, String lang) {
        WxMpUser wxMpUser = null;
        try {
            wxMpUser = this.wxMpService.getUserService().userInfo(openid, lang);
        } catch (WxErrorException e) {
            this.logger.error(e.getError().toString());
        }
        return wxMpUser;
    }

}
