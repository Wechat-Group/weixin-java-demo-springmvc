package com.github.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;

/**
 * Created by FirenzesEagle on 2016/5/30 0030.
 * Email:liumingbo2008@gmail.com
 *
 * @author FirenzesEagle
 * @author BinaryWang
 */
@Configuration
public class MainConfig {

    @Value("#{wxProperties.appid}")
    private String appid;

    @Value("#{wxProperties.appsecret}")
    private String appsecret;

    @Value("#{wxProperties.token}")
    private String token;

    @Value("#{wxProperties.aeskey}")
    private String aesKey;

    @Value("#{wxProperties.partener_id}")
    private String partenerId;

    @Value("#{wxProperties.partener_key}")
    private String partenerKey;

    /**
     * 如果出现 org.springframework.beans.BeanInstantiationException
     * https://github.com/Wechat-Group/weixin-java-tools-springmvc/issues/7
     * 请添加以下默认无参构造函数
     */
    // protected MainConfig(){}
    
    /**
     * 为了生成自定义菜单使用的构造函数，其他情况Spring框架可以直接注入
     *
     * @param appid
     * @param appsecret
     * @param token
     * @param aesKey
     */
    protected MainConfig(String appid, String appsecret, String token, String aesKey) {
        this.appid = appid;
        this.appsecret = appsecret;
        this.token = token;
        this.aesKey = aesKey;
    }

    @Bean
    public WxMpConfigStorage wxMpConfigStorage() {
        WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
        configStorage.setAppId(this.appid);
        configStorage.setSecret(this.appsecret);
        configStorage.setToken(this.token);
        configStorage.setAesKey(this.aesKey);
        return configStorage;
    }

    @Bean
    public WxPayConfig payConfig() {
        WxPayConfig configStorage = new WxPayConfig();
        configStorage.setAppId(this.appid);
        configStorage.setMchId(this.partenerId);
        configStorage.setMchKey(this.partenerKey);
        return configStorage;
    }

    @Bean
    public WxMpService wxMpService() {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage());
        return wxMpService;
    }

    @Bean
    public WxPayService payService() {
        WxPayService payService = new WxPayServiceImpl();
        payService.setConfig(payConfig());
        return payService;
    }

}
