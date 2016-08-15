package smartthings.ratpack.sqs.consumer;

import ratpack.service.Service;
import smartthings.ratpack.sqs.event.SqsConsumerEventListener;

/**
 * Public interface for working with an SQS Consumer Service.
 */
public interface SqsConsumerService extends Service {

    void registerEventListener(SqsConsumerEventListener listener);
}
