package org.folio.login.integration.kafka;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Log4j2
public abstract class EventPublisher {

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * Sends event using provided body.
   *
   * @param body - event body as {@link Object}
   */
  protected void send(Object body) {
    kafkaTemplate.send(getTopicName(), getMessageKey(), body);
    log.debug("Sent event to topic: {}", () -> getTopicName());
  }

  /**
   * Returns topic name for the event.
   *
   * @return topic name
   */
  protected abstract String getTopicName();

  /**
   * Returns message key for the event.
   *
   * @return message key
   */
  protected abstract String getMessageKey();
}
