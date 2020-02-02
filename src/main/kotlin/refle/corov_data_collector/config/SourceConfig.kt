package refle.corov_data_collector.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class SourceConfig(@Autowired private val sourceConfigParams: SourceConfigParams) {

    @Bean
    fun getRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
                .rootUri(sourceConfigParams.baseUrl)
                .build()
    }
}