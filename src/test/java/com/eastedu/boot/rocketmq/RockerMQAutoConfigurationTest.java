package com.eastedu.boot.rocketmq;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SpringBootApplication(scanBasePackages = "com.eastedu")
public class RockerMQAutoConfigurationTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSend() throws InterruptedException {
        for (int i = 0; i < 10; i++) {

            rocketMQTemplate.sendAsync(new Message("devp_question_service", "putway-callback", ("hello" + i).getBytes()), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("发送成功 ===》 " + sendResult.toString());
                }

                @Override
                public void onException(OnExceptionContext context) {
                    System.err.println("发送失败 ===》 " + context.toString());
                }
            });
        }

    }
}