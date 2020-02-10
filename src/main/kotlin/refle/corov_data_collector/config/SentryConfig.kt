//package refle.corov_data_collector.config
//
//import io.sentry.spring.SentryExceptionResolver
//import io.sentry.spring.SentryServletContextInitializer
//import org.springframework.boot.web.servlet.ServletContextInitializer
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Profile
//
//
//@Configuration
//@Profile("prod")
//class SentryConfig {
//    @Bean
//    fun sentryExceptionResolver(): SentryExceptionResolver {
//        return SentryExceptionResolver()
//    }
//
//    @Bean
//    fun sentryServletContextInitializer(): ServletContextInitializer? {
//        return SentryServletContextInitializer()
//    }
//}