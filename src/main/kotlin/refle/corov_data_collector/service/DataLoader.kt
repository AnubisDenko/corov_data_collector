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
import javax.transaction.Transactional

@Component
class DataLoader(@Autowired private val restTemplate: RestTemplate,
                 @Autowired private val dataPointRepo: DataPointRepo,
                 @Autowired private val translator: Translator,
                 @Autowired private val clock: Clock) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun loadData() {
        var retries = 0
        var completed = false

        do {
            try {
                val importDate = clock.getCurrentDateHK()
                val stopWatch = StopWatch()
                stopWatch.start()
                logger.info("Starting to load latest data")
                val response = restTemplate.getForObject("/area", Response::class.java) ?: return
                if (!response.success)
                    return

                cleanData(importDate)

                response.results.forEach { result ->
                    with(result) {

                        // translation and mapping
                        val translatedCountry = translate(result.countryName)
                        val provinceName = translate(provinceName)
                        val fixedCountry = if (provinceName == "Macao" || provinceName == "Hong Kong") {
                            provinceName
                        } else {
                            translatedCountry
                        }

                        val translatedProvinceShortName = translate(result.provinceShortName)


                        // delta calc
                        val previousDay = dataPointRepo.findByImportDateAndCountryAndProvinceShortName(importDate.minusDays(1), fixedCountry, translatedProvinceShortName)

                        val citiesSet = cities?.map {
                            val translatedCityName = translate(it.cityName)
                            val previousDayCityData = previousDay?.cities?.find { it.cityName == translatedCityName }

                            if (previousDayCityData == null) {
                                City(translate(it.cityName), it.confirmedCount, it.suspectedCount, it.curedCount, it.deadCount, it.locationId, importDate,
                                        confirmedDelta = it.confirmedCount, suspectedDelta = it.suspectedCount, curedDelta = it.curedCount, deadDelta = it.deadCount)
                            } else {
                                City(translate(it.cityName), it.confirmedCount, it.suspectedCount, it.curedCount, it.deadCount, it.locationId, importDate,
                                        confirmedDelta = it.confirmedCount - previousDayCityData.confirmedCount,
                                        suspectedDelta = it.suspectedCount - previousDayCityData.suspectedCount,
                                        curedDelta = it.curedCount - previousDayCityData.curedCount,
                                        deadDelta = it.deadCount - previousDayCityData.deadCount)
                            }
                        }?.toSet() ?: setOf()

                        val updateTime = convertToDateTime(updateTime) ?: return@forEach
                        var dataPoint = DataPoint(
                                fixedCountry,
                                provinceName,
                                translatedProvinceShortName,
                                confirmedCount,
                                suspectedCount,
                                curedCount,
                                deadCount,
                                translate(comment),
                                importDate,
                                updateTime,
                                citiesSet,
                                confirmedDelta = confirmedCount - (previousDay?.confirmedCount ?: 0),
                                suspectedDelta = suspectedCount - (previousDay?.suspectedCount ?: 0),
                                deadDelta = deadCount - (previousDay?.deadCount ?: 0),
                                curedDelta = curedCount - (previousDay?.curedCount ?: 0)
                        )

                        citiesSet.forEach { it.dataPoint = dataPoint }
                        dataPointRepo.save(dataPoint)
                    }
                }
                stopWatch.stop()
                logger.info("Load completed and took ${stopWatch.totalTimeSeconds}s")
                completed = true
            } catch (e: Exception) {
                logger.error("Got a problem and will retry ${5-retries} times: ${e.message}", e)
                completed = if(++retries > 5){
                    logger.error("Error while loading latest data therefore skipping: ${e.message}", e)
                    true
                }else{
                    false
                }
            }
        }while(!completed)
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