package com.wangyijie.pay.controller;

import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import com.wangyijie.pay.pojo.PayInfo;
import com.wangyijie.pay.service.impl.PayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private PayServiceImpl payServiceImpl;

    @Autowired
    private WxPayConfig wxPayConfig;

    @GetMapping("/create")
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("payType")BestPayTypeEnum bestPayTypeEnum) {
        PayResponse payResponse = payServiceImpl.create(orderId, amount, bestPayTypeEnum);
        // 支付方式不同，渲染就不同，WXPAY_NATIVE使用codeUrl，ALIPAY_PC使用body
        Map<String, String> map = new HashMap<>();
        if (bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE) {
            map.put("codeUrl", payResponse.getCodeUrl());
            map.put("orderId", orderId);
            map.put("returnUrl", wxPayConfig.getReturnUrl());
            return new ModelAndView("createForWxNative", map);
        } else if (bestPayTypeEnum == BestPayTypeEnum.ALIPAY_PC) {
            map.put("body", payResponse.getBody());
            return new ModelAndView("createForAlipayPc", map);
        }
        throw new RuntimeException("暂不支持的支付类型");
    }

    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData) {
        return payServiceImpl.asyncNotify(notifyData);
    }

    @GetMapping("/queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam String orderId) {
        return payServiceImpl.queryByOrderId(orderId);
    }
}
