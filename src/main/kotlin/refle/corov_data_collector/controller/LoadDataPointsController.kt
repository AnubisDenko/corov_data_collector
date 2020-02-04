package refle.corov_data_collector.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.service.Clock
import java.time.LocalDate

@RestController
class LoadDataPointsController(@Autowired private val dataPointRepo: DataPointRepo, @Autowired private val clock: Clock) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping(path = ["/loadLatestDataPoints"])
    fun loadLatestDataPoints(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?): List<DataPoint>{
        var searchDate = date ?: clock.getCurrentDateHK()

        (0..5).forEach {
            searchDate = searchDate.minusDays(it.toLong())

            logger.info("Fetching data points for $searchDate")
            val result = dataPointRepo.findByImportDate(searchDate)
            logger.info("Found ${result.size} entries for $date")

            if(result.isNotEmpty()){
                return result
            }
        }

        return listOf()
    }
}