package com.eastedu.boot.rocketmq.annotation;

import com.aliyun.openservices.ons.api.PropertyValueConst;

/**
 * 消息订阅方式
 *
 * @author luozhenzhong
 */
public enum MessageMode {
    /**
     *
     */
    BROADCASTING(PropertyValueConst.BROADCASTING), CLUSTERING(PropertyValueConst.CLUSTERING);

    private final String mode;

    MessageMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
