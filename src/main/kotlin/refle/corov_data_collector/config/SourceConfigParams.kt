package refle.corov_data_collector.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "source")
class SourceConfigParams {
    lateinit var baseUrl: String
}