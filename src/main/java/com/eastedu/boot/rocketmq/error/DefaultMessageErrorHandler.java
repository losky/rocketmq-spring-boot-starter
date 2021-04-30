package com.eastedu.boot.rocketmq.error;

/**
 * @author luozhenzhong
 */
public class DefaultMessageErrorHandler implements MessageErrorHandler {
    @Override
    public void onError(Throwable throwable) throws Throwable {
        throw throwable;
    }
}
