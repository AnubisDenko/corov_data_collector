package refle.corov_data_collector.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import refle.corov_data_collector.model.City
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.Response
import refle.corov_data_collector.persistence.DataPointRepo
import java.time.Instant
import java.time.ZoneId

@Component
class DataLoader(@Autowired private val restTemplate: RestTemplate,
                 @Autowired private val dataPointRepo: DataPointRepo,
                 @Autowired private val translator: Translator) {
    fun loadData() {
        val response = restTemplate.getForObject("/area", Response::class.java) ?: return
        if(!response.success)
            return

        response.results.forEach { result ->
            with(result){
                val citiesSet = cities?.map {
                    City(translate(it.cityName), it.confirmedCount, it.suspectedCount, it.curedCount, it.deadCount, it.locationId)
                }?.toSet() ?: setOf()

                val updateTime = convertToDateTime(updateTime) ?: return@forEach

                var dataPoint = DataPoint(
                        translate(country),
                        translate(provinceName),
                        translate(provinceShortName),
                        confirmedCount,
                        suspectedCount,
                        curedCount,
                        deadCount,
                        comment,
                        updateTime,
                        convertToDateTime(createTime),
                        convertToDateTime(modifyTime),
                        citiesSet
                )

                dataPointRepo.save(dataPoint)
            }
        }
    }

    private val convertToDateTime = { milli:Long ->
        if(milli == 0L)
            null
        else
            Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    private val translate = { chinese:String -> translator.translateChineseToEnglish(chinese)}

}
