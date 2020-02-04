package refle.corov_data_collector.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZonedDateTime

@Component
@Profile("test")
class TestClock:Clock {

    var current: ZonedDateTime = ZonedDateTime.now()

    override fun getCurrentDateHK(): LocalDate {
        return current.toLocalDate()
    }

    override fun getCurrentDatetimeHK(): ZonedDateTime {
        return current
    }
}