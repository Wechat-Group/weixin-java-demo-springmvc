package com.github.weixin.demo.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

/**
 * 模板消息Controller
 * <p>
 * Created by FirenzesEagle on 2016/7/11 0011.
 * Email:liumingbo2008@gmail.com
 */
@Controller
@RequestMapping(value = "templateMessage")
public class TemplateMessageController extends GenericController {

    // 模板消息字体颜色
    private static final String TEMPLATE_FRONT_COLOR = "#32CD32";
    @Autowired
    protected WxMpConfigStorage configStorage;
    @Autowired
    protected WxMpService wxMpService;

    @RequestMapping(value = "notifyOrderPaySuccessTemplate")
    public void notifyOrderPaySuccessTemplate(HttpServletResponse response,
                                              HttpServletRequest request) {
        WxMpTemplateMessage orderPaySuccessTemplate = WxMpTemplateMessage.builder().build();
        orderPaySuccessTemplate.setToUser(request.getParameter("openid"));
        orderPaySuccessTemplate.setTemplateId("ENp7UwpOtlhvieebUvDm0mK4n0hTvbH0Me83HdBUvC0");
        orderPaySuccessTemplate.setUrl(request.getParameter("url"));
        WxMpTemplateData firstData = new WxMpTemplateData("first", "订单支付成功", TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderMoneySumData = new WxMpTemplateData("orderMoneySum", request.getParameter("orderMoneySum"), TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderProductNameData = new WxMpTemplateData("orderProductName", request.getParameter("orderProductName"), TEMPLATE_FRONT_COLOR);
        WxMpTemplateData remarkData = new WxMpTemplateData("Remark", request.getParameter("remark"), TEMPLATE_FRONT_COLOR);
        orderPaySuccessTemplate.addData(firstData)
            .addData(orderMoneySumData)
            .addData(orderProductNameData)
            .addData(remarkData);
        try {
            wxMpService.getTemplateMsgService()
                .sendTemplateMsg(orderPaySuccessTemplate);
        } catch (WxErrorException e) {
            logger.error(e.getMessage());
        }
    }

    @RequestMapping(value = "notifyOrderStatusUpdateTemplate")
    public void notifyOrderStatusUpdateTemplate(HttpServletResponse response,
                                                HttpServletRequest request) {
        WxMpTemplateMessage orderPaySuccessTemplate = WxMpTemplateMessage.builder().build();
        orderPaySuccessTemplate.setToUser(request.getParameter("openid"));
        orderPaySuccessTemplate.setTemplateId("X8ccwRF4EAx7VHFQGzi78Gl0C3GcpGpYgWk-HFFOWA0");
        orderPaySuccessTemplate.setUrl(request.getParameter("url"));
        WxMpTemplateData firstData = new WxMpTemplateData("first", "订单状态更新", TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderMoneySumData = new WxMpTemplateData("OrderSn", request.getParameter("OrderSn"), TEMPLATE_FRONT_COLOR);
        WxMpTemplateData orderProductNameData = new WxMpTemplateData("OrderStatus", request.getParameter("OrderStatus"), TEMPLATE_FRONT_COLOR);
        WxMpTemplateData remarkData = new WxMpTemplateData("remark", request.getParameter("remark"), TEMPLATE_FRONT_COLOR);
        orderPaySuccessTemplate.addData(firstData)
            .addData(orderMoneySumData)
            .addData(orderProductNameData)
            .addData(remarkData);
        try {
            wxMpService.getTemplateMsgService()
                .sendTemplateMsg(orderPaySuccessTemplate);
        } catch (WxErrorException e) {
            logger.error(e.getMessage());
        }
    }

}
