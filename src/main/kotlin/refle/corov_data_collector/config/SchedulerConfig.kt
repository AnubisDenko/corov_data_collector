package refle.corov_data_collector.config

import org.quartz.JobDetail
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.CronTriggerFactoryBean
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import refle.corov_data_collector.scheduler.DataLoadingJob

@Configuration
class SchedulerConfig {
    @Bean
    fun getJobDetail(): JobDetailFactoryBean {
        val factory = JobDetailFactoryBean()
        with(factory){
            setJobClass(DataLoadingJob::class.java)
            setDescription("Job to periodically load data from virus crawler")
            setDurability(true)
        }
        return factory
    }

    @Bean
    fun getTrigger(jobDetail: JobDetail): CronTriggerFactoryBean{
        val triggerFactory = CronTriggerFactoryBean()
        with(triggerFactory){
            setJobDetail(jobDetail)
            setCronExpression("0 0 7 ? * * *")
            setMisfireInstructionName("MISFIRE_INSTRUCTION_FIRE_ONCE_NOW")
        }
        return triggerFactory
    }
}