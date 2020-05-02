package com.wangyijie.pay.enums;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import lombok.Getter;

@Getter
public enum PayPlatformEnum {
    // 1. 支付宝 2. 微信
    ALIPAY(1),
    WX(2),
    ;

    Integer code;

    PayPlatformEnum(Integer code) {
        this.code = code;
    }

    public static PayPlatformEnum getByBestPayTypeEnum(BestPayTypeEnum bestPayTypeEnum) {
//        if (bestPayTypeEnum.getPlatform().name().equals(PayPlatform.ALIPAY.name())) {
//            return PayPlatform.ALIPAY;
//        } else if (bestPayTypeEnum.getPlatform().name().equals(PayPlatform.WX.name())) {
//            return PayPlatform.WX;
//        }
        for (PayPlatformEnum payPlatformEnum : PayPlatformEnum.values()) {
            if (payPlatformEnum.name().equals(bestPayTypeEnum.getPlatform().name())) {
                return payPlatformEnum;
            }
        }
        throw new RuntimeException("错误的支付平台: " + bestPayTypeEnum.name());
    }
}
