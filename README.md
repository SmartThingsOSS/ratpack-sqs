# ratpack-sqs
A RatPack module for working with Amazon's Simple Queue Service

[![codecov](https://codecov.io/gh/SmartThingsOSS/ratpack-sqs/branch/master/graph/badge.svg)](https://codecov.io/gh/SmartThingsOSS/ratpack-sqs)


## Enabling SQS Ratpack module
1) Add dependency to Gradle
```
    compile "smartthings:ratpack-sqs:0.1.0"

```

2) Add module binding to Ratpack main.  Your usage may vary depending on your configuration strategy, but it would look something like:
```
bindings.moduleConfig(
    SqsModule.class,
    serverConfig.get.get("/sqs", SqsModule.Config.class)
)
```

## Creating an SQS Consumer
Create a consumer class that implments *smartthings.ratpack.sqs.consumer.Consumer* like:
```
public class MyConsumer implements Consumer {

    private static final Logger log = LoggerFactory.getLogger(MyConsumer.class);

    @Inject
    public MyConsumer() {

    }

    @Override
    public void consume(Message msg) throws Exception {
        log.debug("Polled message from SQS " + msg.toString());
    }

    @Override
    public String getQueueName() {
        return "my-sqs-queue
    }

    @Override
    public ReceiveMessageRequest getReceiveMessageRequest() {
        // Provide polling property overrides.    
        return new ReceiveMessageRequest()
            .withWaitTimeSeconds(20)
            .withMaxNumberOfMessages(10);
    }

    @Override
    public Integer getConcurrencyLevel() {
        // How many consumer instannces to run in parallel.
        return 1;
    }

}

```

That's pretty much it.  With the above the plugin will automatically take care of deleting the messages after each poll.
Retry logic will be added in an upcoming version.  See the SqsModule.java file for configuration options.


## SQS Producer
To produce SQS messages inject an instance of the smartthings.ratpack.sqs.SqsService into your class, and utilize the non-blocking methods to interact with SQS.



