package com.eastedu.boot.rocketmq.error;

/**
 * @author luozhenzhong
 */
public interface MessageErrorHandler {

    /**
     * 异常处理
     *
     * @param throwable 异常信息
     */
    void onError(Throwable throwable);
}
