package com.eastedu.boot.rocketmq.error;

/**
 * @author luozhenzhong
 */
public interface MessageErrorHandler {

    /**
     * 异常处理
     *
     * @param throwable
     *
     * @throws Throwable
     */
    void onError(Throwable throwable) throws Throwable;
}
