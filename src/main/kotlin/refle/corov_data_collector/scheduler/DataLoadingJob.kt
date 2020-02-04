package refle.corov_data_collector.scheduler

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import refle.corov_data_collector.service.DataLoader

@Component
class DataLoadingJob(@Autowired private val dataLoader: DataLoader){

    @Scheduled(zone = "Asia/Hong_Kong", cron = "* 8 * * * ?")
    fun execute() {
        dataLoader.loadData()
    }
}

