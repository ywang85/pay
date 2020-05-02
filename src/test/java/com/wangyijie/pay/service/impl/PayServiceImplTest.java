package com.wangyijie.pay.service.impl;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.wangyijie.pay.PayApplicationTests;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceImplTest extends PayApplicationTests {
    @Autowired
    private PayServiceImpl payServiceImpl;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void create() {
        payServiceImpl.create("123474598654457", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    public void sendMQMsg() {
        amqpTemplate.convertAndSend("payNotify", "hello");
    }
}