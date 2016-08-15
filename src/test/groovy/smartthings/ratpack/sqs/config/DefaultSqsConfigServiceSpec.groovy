package smartthings.ratpack.sqs.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSCredentialsProviderChain
import smartthings.ratpack.sqs.SqsModule
import spock.lang.Specification

class DefaultSqsConfigServiceSpec extends Specification {

    void 'it should create an aws credentials provider'() {
        given:
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'

        DefaultSqsConfigService service = new DefaultSqsConfigService(config)

        when:
        AWSCredentialsProvider provider = service.getAwsCredentialsProvider()

        then:
        assert provider.getClass() == AWSCredentialsProviderChain
    }

    void 'it should return null when creating AWS client configuration'() {
        given:
        SqsModule.Config config = new SqsModule.Config()
        config.awsAccessKey = 'access-key'
        config.awsSecretKey = 'secret-key'

        DefaultSqsConfigService service = new DefaultSqsConfigService(config)

        when:
        ClientConfiguration clientConfiguration = service.getClientConfiguration()

        then:
        assert clientConfiguration == null
    }

}
