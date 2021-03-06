package com.louis.message.spring.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author louis
 * <p>
 * Date: 2019/7/24
 * Description:
 * // TODO: 2019/9/26  如何将项目中的消息设计得灵活，动态
 */
@Slf4j
@Component
public class MessageConsumer {


    @KafkaListener(groupId = "${stream.service.group}",topics = "${stream.service.topic}")
    public void consumer(ConsumerRecord<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Consumer consumer) {

        log.info("收到的消息为：{}，topic为：{}", record.value(), topic);
    }




}
