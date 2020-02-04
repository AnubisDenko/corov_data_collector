package refle.corov_data_collector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CorovDataCollectorApplication

fun main(args: Array<String>) {
    runApplication<CorovDataCollectorApplication>(*args)
}
