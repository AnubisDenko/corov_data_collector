package refle.corov_data_collector.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.client.RestTemplate
import refle.corov_data_collector.model.City
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.Response
import refle.corov_data_collector.persistence.DataPointRepo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.transaction.Transactional

@Component
class DataLoader(@Autowired private val restTemplate: RestTemplate,
                 @Autowired private val dataPointRepo: DataPointRepo,
                 @Autowired private val translator: Translator,
                 @Autowired private val clock: Clock) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun loadData() {
        val importDate = clock.getCurrentDateHK()
        val stopWatch = StopWatch()
        stopWatch.start()
        logger.info("Starting to load latest data")
        val response = restTemplate.getForObject("/area", Response::class.java) ?: return
        if(!response.success)
            return

        cleanData(importDate)

        response.results.forEach { result ->
            with(result){
                val citiesSet = cities?.map {
                    City(translate(it.cityName), it.confirmedCount, it.suspectedCount, it.curedCount, it.deadCount, it.locationId, importDate)
                }?.toSet() ?: setOf()

                val updateTime = convertToDateTime(updateTime) ?: return@forEach

                val provinceName = translate(provinceName)
                val fixedCountry = if(provinceName == "Macao" || provinceName == "Hong Kong"){
                    provinceName
                }else{
                    translate(country)
                }
                var dataPoint = DataPoint(
                        fixedCountry,
                        provinceName,
                        translate(provinceShortName),
                        confirmedCount,
                        suspectedCount,
                        curedCount,
                        deadCount,
                        translate(comment),
                        importDate,
                        updateTime,
                        convertToDateTime(createTime),
                        convertToDateTime(modifyTime),
                        citiesSet
                )

                citiesSet.forEach { it.dataPoint = dataPoint }
                dataPointRepo.save(dataPoint)
            }
        }
        stopWatch.stop()
        logger.info("Load completed and took ${stopWatch.totalTimeSeconds}s")
    }

    private val convertToDateTime = { milli:Long ->
        if(milli == 0L)
            null
        else
            Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    private val cleanData = { forDate: LocalDate -> dataPointRepo.deleteByImportDate(forDate)}

    private val translate = { chinese:String -> translator.translateChineseToEnglish(chinese)}

}
