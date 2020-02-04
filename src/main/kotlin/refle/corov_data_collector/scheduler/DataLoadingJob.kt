package refle.corov_data_collector.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import refle.corov_data_collector.model.ScheduledJobRun
import refle.corov_data_collector.persistence.ScheduledJobRepo
import refle.corov_data_collector.service.DataLoader
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class DataLoadingJob(@Autowired private val dataLoader: DataLoader, @Autowired private val scheduledJobRepo: ScheduledJobRepo){

    @Scheduled(zone = "Asia/Hong_Kong", cron = "* 8 * * * ?")
    fun execute() {
        val stopWatch = StopWatch()
        stopWatch.start()
        dataLoader.loadData()
        stopWatch.stop()

        scheduledJobRepo.save(ScheduledJobRun("DataLoadingJob", stopWatch.lastTaskTimeMillis, ZonedDateTime.now(ZoneId.of("Asia/Hong_Kong"))))

    }
}