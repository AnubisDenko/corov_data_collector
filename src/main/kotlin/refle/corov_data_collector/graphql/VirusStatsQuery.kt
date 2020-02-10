package refle.corov_data_collector.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import refle.corov_data_collector.model.CityData
import refle.corov_data_collector.model.CountryData
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.ProvinceData
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.service.Clock
import refle.corov_data_collector.service.DataLoader
import java.time.LocalDate

@Component
class VirusStatsQuery(@Autowired private val dataLoader: DataLoader, @Autowired private val clock: Clock, @Autowired private val dataPointRepo: DataPointRepo) : GraphQLQueryResolver {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getStatsByCountry(date: String): List<CountryData>{
        val dataPoints = loadLatestDataPoints(date)
        val pivot = dataPoints.groupBy { it.country }

        return pivot.map { (country, provinces) ->
            val aggregate = provinces.reduce { left, right -> DataPoint(country, "","",
                    left.confirmedCount + right.confirmedCount,
                    left.suspectedCount + right.suspectedCount,
                    left.curedCount + right.curedCount,
                    left.deadCount + right.deadCount,
                    left.comment + right.comment,
                    left.importDate,
                    left.updateTime,
                    null,null, setOf(),
                    confirmedDelta = left.confirmedDelta + right.confirmedDelta,
                    suspectedDelta = left.suspectedDelta + right.suspectedDelta,
                    curedDelta = left.curedDelta + right.curedDelta,
                    deadDelta = left.deadDelta + right.deadDelta)
            }
            with(aggregate) {
                return@map refle.corov_data_collector.model.CountryData(importDate, country, confirmedCount, curedCount, suspectedCount, deadCount, suspectedDelta, deadDelta, curedDelta, confirmedDelta)
            }
        }
    }

    fun getStatsByProvince(date: String, country: String): List<ProvinceData> {
        val dataPoints = loadLatestDataPoints(date, country)
        return dataPoints.map {
            ProvinceData(it.importDate, it.country, it.provinceName, it.provinceShortName, it.confirmedCount, it.curedCount, it.suspectedCount, it.deadCount, it.suspectedDelta, it.deadDelta, it.curedDelta, it.confirmedDelta)
        }
    }

    fun getProvinceDetails(country: String, provinceName: String, date: String): List<CityData> {
        var searchDate = LocalDate.parse(date)

        (0..5).forEach {
            searchDate = searchDate.minusDays(it.toLong())

            // for now I assume there is only one data point
            val result = dataPointRepo.findByImportDateAndCountryAndProvinceShortName(searchDate, country, provinceName) ?: return emptyList()
            return result.cities.map { city ->  CityData(result.importDate,
                    result.country,
                    result.provinceName,
                    result.provinceShortName,
                    city.cityName,
                    city.confirmedCount,
                    city.curedCount,
                    city.suspectedCount,
                    city.locationId,
                    city.deadCount,
                    city.suspectedDelta,
                    city.deadDelta,
                    city.curedDelta,
                    city.confirmedDelta) }
        }

        return listOf()
    }

    private fun loadLatestDataPoints(dateString: String, country: String?= null): List<DataPoint>{
        var searchDate = LocalDate.parse(dateString)


        (0..5).forEach {
            searchDate = searchDate.minusDays(it.toLong())

            logger.info("Fetching data points for $searchDate")
            val result = if(country == null) {
                dataPointRepo.findByImportDate(searchDate)
            }else{
                dataPointRepo.findByImportDateAndCountry(searchDate, country)
            }

            logger.info("Found ${result.size} entries for $dateString")

            if(result.isNotEmpty()){
                return result
            }
        }
        return emptyList()
    }

}