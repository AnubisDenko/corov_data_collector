package refle.corov_data_collector.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppConfig() {

    constructor(passphrase: String) : this() {
        this.passphrase = passphrase
    }

    lateinit var passphrase: String
}