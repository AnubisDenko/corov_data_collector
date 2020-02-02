package refle.corov_data_collector.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import refle.corov_data_collector.model.City
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.Response
import refle.corov_data_collector.persistence.CityRepo
import refle.corov_data_collector.persistence.DataPointRepo

@Component
class DataLoader(@Autowired private val restTemplate: RestTemplate, @Autowired private val dataPointRepo: DataPointRepo, @Autowired private val cityRepo: CityRepo) {
    fun loadData() {
        val response = restTemplate.getForObject("/area", Response::class.java) ?: return
        if(!response.success)
            return

        response.results.forEach { result ->
            with(result){
                var dataPoint = DataPoint(
                        country, provinceName, provinceShortName, confirmedCount, suspectedCount, curedCount, deadCount, comment, updateTime, createTime, modifyTime
                )

                dataPoint = dataPointRepo.save(dataPoint)

                if(result.cities == null || result.cities.isEmpty())
                    return@forEach

                result.cities.forEach {
                    cityRepo.save(City(
                            it.cityName, it.confirmedCount, it.suspectedCount, it.curedCount, it.deadCount, it.locationId, dataPoint
                    ))
                }
            }
        }
    }
}
