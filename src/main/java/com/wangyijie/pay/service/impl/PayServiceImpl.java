package com.wangyijie.pay.service.impl;

import com.google.gson.Gson;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.wangyijie.pay.dao.PayInfoMapper;
import com.wangyijie.pay.enums.PayPlatformEnum;
import com.wangyijie.pay.pojo.PayInfo;
import com.wangyijie.pay.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Slf4j
@Service
public class PayServiceImpl implements IPayService {
    private final static String QUEUE_PAY_NOTIFY = "payNotify";
    @Autowired
    // 获取配置类里面@bean方法返回的bestPayService对象，类型为BestPayService
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
        // 写入数据库
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayRequest request = new PayRequest();
        request.setOrderName("7125491-最好的支付sdk");
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setPayTypeEnum(bestPayTypeEnum);
        PayResponse response = bestPayService.pay(request);
        // 重要！需要手动设定
        response.setOrderAmount(request.getOrderAmount());
        response.setOrderId(request.getOrderId());

        log.info("request={}", request);
        log.info("发起支付 response={}", response);

        return response;
    }

    @Override
    public String asyncNotify(String notifyData) {
        // 1. 签名校验
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 payResponse={}", payResponse);
        // 2. 金额校验(数据库中查订单)
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if (payInfo == null) {
            throw new RuntimeException("通过orderNo查询到的结果是null");
        }
        // 如果订单状态不是已支付
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0) {
                // 告警
                throw new RuntimeException("异步通知金额和数据库中的不一样, orderNo = " + payResponse.getOrderId());
            }
            // 3. 修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            // 交易流水号
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfoMapper.updateByPrimaryKeySelective(payInfo);

        }
        //TODO 发送MQ消息，mall接受MQ消息
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, new Gson().toJson(payInfo));

        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            // 4. 告诉微信不要再通知了
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        } else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        log.info("查询支付记录");
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
