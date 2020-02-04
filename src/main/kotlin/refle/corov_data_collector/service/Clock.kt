package refle.corov_data_collector.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


interface Clock {
    fun getCurrentDateHK(): LocalDate
    fun getCurrentDatetimeHK(): ZonedDateTime
}

@Component
@Profile("!test")
class ClockImpl: Clock{
    override fun getCurrentDateHK(): LocalDate {
        return getCurrentDatetimeHK().toLocalDate()
    }

    override fun getCurrentDatetimeHK(): ZonedDateTime {
        return ZonedDateTime.now(ZoneId.of("Asia/Hong_Kong"))
    }
}