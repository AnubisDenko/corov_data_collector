package refle.corov_data_collector.scheduler

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import refle.corov_data_collector.service.DataLoader

@Component
class DataLoadingJob(@Autowired private val dataLoader: DataLoader): Job {
    override fun execute(context: JobExecutionContext?) {
        dataLoader.loadData()
    }
}

