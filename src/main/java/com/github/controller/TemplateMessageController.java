package com.github.controller;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

/**
 * 模板消息Controller
 * <p>
 * Created by FirenzesEagle on 2016/7/11 0011.
 * Email:liumingbo2008@gmail.com
 */
@Controller
@RequestMapping(value = "templateMessage")
public class TemplateMessageController extends GenericController {

    @Autowired
    protected WxMpConfigStorage configStorage;
    @Autowired
    protected WxMpService wxMpService;

    // 模板消息字体颜色
    private static final String TEMPLATE_FRONT_COLOR = "#32CD32";

    @RequestMapping(value = "notifyOrderPaySuccessTemplate")
    public void notifyOrderPaySuccessTemplate(HttpServletResponse response,
                                              @RequestParam(value = "openid") String openid,
                                              @RequestParam(value = "orderMoneySum") String orderMoneySum,
                                              @RequestParam(value = "orderProductName") String orderProductName,
                                              @RequestParam(value = "remark") String remark,
                                              @RequestParam(value = "url") String url) {
        WxMpTemplateMessage orderPaySuccessTemplate = new WxMpTemplateMessage();
        orderPaySuccessTemplate.setToUser(openid);
        orderPaySuccessTemplate.setTemplateId("ENp7UwpOtlhvieebUvDm0mK4n0hTvbH0Me83HdBUvC0");
        orderPaySuccessTemplate.setUrl(url);
        orderPaySuccessTemplate.setTopColor(TEMPLATE_FRONT_COLOR);
        WxMpTemplateData firstData = new WxMpTemplateData("first", "订单支付成功", TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderMoneySumData = new WxMpTemplateData("orderMoneySum", orderMoneySum, TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderProductNameData = new WxMpTemplateData("orderProductName", orderProductName, TEMPLATE_FRONT_COLOR);
        WxMpTemplateData remarkData = new WxMpTemplateData("Remark", remark, TEMPLATE_FRONT_COLOR);
        orderPaySuccessTemplate.addWxMpTemplateData(firstData);
        orderPaySuccessTemplate.addWxMpTemplateData(orderMoneySumData);
        orderPaySuccessTemplate.addWxMpTemplateData(orderProductNameData);
        orderPaySuccessTemplate.addWxMpTemplateData(remarkData);
        try {
            wxMpService.templateSend(orderPaySuccessTemplate);
        } catch (WxErrorException e) {
            logger.error(e.getMessage().toString());
        }
    }

    @RequestMapping(value = "notifyOrderStatusUpdateTemplate")
    public void notifyOrderStatusUpdateTemplate(HttpServletResponse response,
                                                @RequestParam(value = "openid") String openid,
                                                @RequestParam(value = "OrderSn") String OrderSn,
                                                @RequestParam(value = "OrderStatus") String OrderStatus,
                                                @RequestParam(value = "remark") String remark,
                                                @RequestParam(value = "url") String url) {
        WxMpTemplateMessage orderPaySuccessTemplate = new WxMpTemplateMessage();
        orderPaySuccessTemplate.setToUser(openid);
        orderPaySuccessTemplate.setTemplateId("X8ccwRF4EAx7VHFQGzi78Gl0C3GcpGpYgWk-HFFOWA0");
        orderPaySuccessTemplate.setUrl(url);
        orderPaySuccessTemplate.setTopColor(TEMPLATE_FRONT_COLOR);
        WxMpTemplateData firstData = new WxMpTemplateData("first", "订单状态更新", TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderMoneySumData = new WxMpTemplateData("OrderSn", OrderSn, TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderProductNameData = new WxMpTemplateData("OrderStatus", OrderStatus, TEMPLATE_FRONT_COLOR);
        WxMpTemplateData remarkData = new WxMpTemplateData("remark", remark, TEMPLATE_FRONT_COLOR);
        orderPaySuccessTemplate.addWxMpTemplateData(firstData);
        orderPaySuccessTemplate.addWxMpTemplateData(orderMoneySumData);
        orderPaySuccessTemplate.addWxMpTemplateData(orderProductNameData);
        orderPaySuccessTemplate.addWxMpTemplateData(remarkData);
        try {
            wxMpService.templateSend(orderPaySuccessTemplate);
        } catch (WxErrorException e) {
            logger.error(e.getMessage().toString());
        }
    }

}
