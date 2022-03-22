package smartthings.ratpack.sqs


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.func.Action
import ratpack.guice.Guice
import ratpack.handling.Context
import ratpack.handling.InjectionHandler
import ratpack.http.HttpMethod
import ratpack.server.RatpackServerSpec
import ratpack.test.embed.EmbeddedApp
import smartthings.ratpack.aws.AwsModule
import smartthings.ratpack.sns.SnsModule
import smartthings.ratpack.sns.SnsService
import smartthings.ratpack.sqs.internal.consumer.ConsumerManager
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sqs.model.Message
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import java.util.concurrent.atomic.AtomicInteger

class ConsumerFunctionalSpec extends Specification {

    @Shared
    String topicArn = 'arn:aws:sns:local:000000000000:functional_test_queue'

    @Shared
    String awsEndpointUrl = getSetting('awsEndpointUrl', 'http://localhost:4100')

    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    @Shared
    TestConsumer consumer = new TestConsumer(objectMapper)

    @Delegate
    EmbeddedApp app = of({ RatpackServerSpec spec ->
        spec.registry(Guice.registry({ binder ->
            binder.module(AwsModule, { AwsModule.Config config ->
                config.with {
                    awsAccessKey = '<access-key>'
                    awsSecretKey = '<secret-key>'
                }
            })

            binder.module(SnsModule, { SnsModule.Config config ->
                config.enabled = true
                config.endpoints = [
                    new SnsModule.EndpointConfig(
                        regionName: 'us-east-1',
                        endpoint: awsEndpointUrl
                    )
                ]
                config
            })

            binder.module(SqsModule, { SqsModule.Config config ->
                config.enabled = true
                config.consumers = [
                    new SqsModule.ConsumerConfig(
                        consumer: TestConsumer,
                        endpoints: [
                            new SqsModule.EndpointConfig(
                                queueName: 'functional_test_queue',
                                regionName: 'us-east-1',
                                endpoint: awsEndpointUrl
                            )
                        ]
                    )
                ]
                config
            })
            binder.bindInstance(ObjectMapper, objectMapper)
            binder.bindInstance(TestConsumer, consumer)
        }))
        spec.handlers({ chain ->
            chain
                .post('publish', new InjectionHandler() {
                    void handle(Context ctx, ObjectMapper objectMapper, SnsService sns) throws Exception {
                        ctx.parse(TestMessage)
                            .flatMap({ request ->
                                sns.publish(PublishRequest.builder()
                                        .topicArn(topicArn)
                                        .message(objectMapper.writeValueAsString(request))
                                        .build()
                                )
                            })
                            .onError(ctx.&error)
                            .then({
                                ctx.response.status(204)
                                ctx.response.send()
                            })
                    }
                })
                .post('circuit/open', new InjectionHandler() {
                    void handle(Context ctx, ConsumerManager manager) throws Exception {
                        manager.pause()
                        ctx.response.status(204)
                        ctx.response.send()

                    }
                })
                .post('circuit/close', new InjectionHandler() {
                    void handle(Context ctx, ConsumerManager manager) throws Exception {
                        manager.resume()
                        ctx.response.status(204)
                        ctx.response.send()

                    }
                })
        })
    } as Action<RatpackServerSpec>)

    void 'it should publish and consume a message'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())

        when:
        def response = httpClient.request('publish',{ spec ->
            spec.method(HttpMethod.POST)
            spec.headers({
                it.set('Accept', 'application/json')
                it.set('Content-Type', 'application/json')
            })
            spec.body.text(objectMapper.writeValueAsString(request))
        })

        then:
        assert response.status.'2xx'
        conditions.within(5, {
            consumer.callCount(request) == 1
        })
    }

    void 'it should not delete message on a failure to consume'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())
        consumer.setConsumer({
            throw new RuntimeException('ohh no')
        })

        when:
        def response = httpClient.request('publish',{ spec ->
            spec.method(HttpMethod.POST)
            spec.headers({
                it.set('Accept', 'application/json')
                it.set('Content-Type', 'application/json')
            })
            spec.body.text(objectMapper.writeValueAsString(request))
        })

        then:
        assert response.status.'2xx'
        conditions.within(60, {
            consumer.callCount(request) >= 2
        })
    }

    void 'it should be able to recover from circuit being opened'() {
        given:
        PollingConditions conditions = new PollingConditions()
        TestMessage request = new TestMessage(message: UUID.randomUUID())
        consumer.setConsumer({})

        when:
        def response = httpClient.request('circuit/open',{ spec ->
            spec.method(HttpMethod.POST)
        })

        then:
        assert response.status.'2xx'

        when:
        response = httpClient.request('publish',{ spec ->
            spec.method(HttpMethod.POST)
            spec.headers({
                it.set('Accept', 'application/json')
                it.set('Content-Type', 'application/json')
            })
            spec.body.text(objectMapper.writeValueAsString(request))
        })

        then:
        assert response.status.'2xx'

        when:
        response = httpClient.request('circuit/close',{ spec ->
            spec.method(HttpMethod.POST)
        })

        then:
        assert response.status.'2xx'
        conditions.within(60, {
            consumer.callCount(request) == 1
        })
    }

    static String getSetting(String name, String defaultValue = null) {
        return System.getProperty(name) ?: System.getenv(toEnvFormat(name)) ?: defaultValue
    }

    static String toEnvFormat(String text) {
        text ==~ /^[A-Z_]+$/ ?
            text :
            text.replaceAll(/([A-Z])/, /_$1/).toUpperCase()
                .replaceAll(/^_/, '')
                .replaceAll(/\._?/, '__')
    }
}

class TestMessage {
    String message

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TestMessage that = (TestMessage) o

        if (message != that.message) return false

        return true
    }

    int hashCode() {
        return (message != null ? message.hashCode() : 0)
    }
}

class TestConsumer implements Consumer {
    ObjectMapper mapper
    Map<TestMessage, AtomicInteger> messages = [:]
    java.util.function.Consumer<Message> consumer = {}

    TestConsumer(ObjectMapper mapper) {
        this.mapper = mapper
    }

    @Override
    void consume(Message message) throws Exception {
        Map body = mapper.readValue(message.body(), new TypeReference<Map<String, Object>>() {})
        TestMessage testMessage = mapper.readValue(body['Message'] as String, TestMessage)
        if (messages.containsKey(testMessage)) {
            messages.get(testMessage).incrementAndGet()
        } else {
            messages.put(testMessage, new AtomicInteger(1))
        }
        consumer.accept(message)
    }

    int callCount(TestMessage message) {
        return messages.get(message)?.get() ?: 0
    }

    void setConsumer(java.util.function.Consumer<Message> consumer) {
        this.consumer = consumer
    }
}
