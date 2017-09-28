# ratpack-sqs
A RatPack module for working with Amazon's Simple Queue Service and Simple Notification Service

[![codecov](https://codecov.io/gh/SmartThingsOSS/ratpack-sqs/branch/master/graph/badge.svg)](https://codecov.io/gh/SmartThingsOSS/ratpack-sqs)


## Enabling SQS Ratpack module
1) Add dependency to Gradle
```
    compile "smartthings:ratpack-sqs:0.1.0"

```

2) Add module binding to Ratpack main.  Your usage may vary depending on your configuration strategy, but it would look something like:
```
    ServerConfig configData = bindings.getServerConfig();
    
    // The AwsModule is required for usage of both SnsModule and SqsModule.
    bindings.moduleConfig(
        AwsModule.class,
        configData.get("/aws", AwsModule.Config.class)
    );
    
    // Optionally include SnsModule.
    bindings.moduleConfig(
        SnsModule.class,
        configData.get("/sns", SnsModule.Config.class)
    );
    
    // Optionally include SqsModule.
    bindings.moduleConfig(
        SqsModule.class,
        configData.get("/sqs", SqsModule.Config.class)
    );

```
## Ratpack SQS AWS regional failover support
The Ratpack SQS modules supports AWS regional failover by supporting the ability to write to many AWS regions and
consume from many AWS regions.  To enable this feature simply provide either the SnsModule or SqsModule with a list
of AWS endpoints in the desired regions.  If multiple endpoints are configured, the clients will attempt to failover to
another region when a circuit breaker is opened due to failures communicating with AWS in the currently active endpoint. 

## Using the SnsModule
The SnsModule allows for the configuring of multiple endpoints as a means of failover in case a specific AWS Region
is down.  If regional failover is not a concern for your use case, simply configure a single endpoint.

With the SnsModule properly configured, simply inject the `SnsService` into your Guice bean, and use the provided API
to interact with AWS SNS.  For more complete examples view the tests contained in project.

Example:
```
    new InjectionHandler() {
        void handle(Context ctx, ObjectMapper objectMapper, SnsService sns) throws Exception {
            ctx.parse(TestMessage)
                .flatMap({ request ->
                    sns.publish(
                        new PublishRequest(
                            topicArn,
                            objectMapper.writeValueAsString(request)
                        )
                    )
                })
                .onError(ctx.&error)
                .then({
                    ctx.response.status(204)
                    ctx.response.send()
                })
        }
    }
``` 

## Using the SqsModule
The SqsModule support configuring of multiple consumers, with each consumer capable of consuming from a list of 
configured endpoints.  

### SQS as a Consumer
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
}

```

Additionally, you'll need to provide the SqsModule with configuration to wire up that consumer. An example configuration
in YAML format is provided below:

```
sqs:
  enabled: true
  consumers:
    - consumer: 'com.smartthings.consumers.MyConsumer' # Java package + class name pointing to your consumer.
      concurrency: 1 # Number of consumer instances you'd like to run in parallel.
      endpoints:
        - regionName: 'us-east-1'            # AWS Region
          endpoint: 'http://localhost:4100'  # AWS Endpoint URL
          queueName: 'my-sqs-queue-name'     # AWS SQS Queue Name
```



### SQS as a Producer
To produce SQS messages inject an instance of the smartthings.ratpack.sqs.SqsService into your class, and utilize the non-blocking methods to interact with SQS.



