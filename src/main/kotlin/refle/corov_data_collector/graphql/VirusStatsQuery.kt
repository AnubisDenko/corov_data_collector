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

    fun getStatsByCountry(from: String, to: String): List<CountryData> {

        val dataPoints = loadDataPoints(from, to)
        val pivot = dataPoints.groupBy { CountryAndDateKey(it.country, it.importDate) }

        return pivot.map { (key, provinces) ->
            val aggregate = provinces.reduce { left, right ->
                DataPoint(key.country, "", "",
                        left.confirmedCount + right.confirmedCount,
                        left.suspectedCount + right.suspectedCount,
                        left.curedCount + right.curedCount,
                        left.deadCount + right.deadCount,
                        left.comment + right.comment,
                        key.importDate,
                        left.updateTime,
                        setOf(),
                        confirmedDelta = left.confirmedDelta + right.confirmedDelta,
                        suspectedDelta = left.suspectedDelta + right.suspectedDelta,
                        curedDelta = left.curedDelta + right.curedDelta,
                        deadDelta = left.deadDelta + right.deadDelta)
            }
            with(aggregate) {
                return@map refle.corov_data_collector.model.CountryData(key.importDate, key.country, confirmedCount, curedCount, suspectedCount, deadCount, suspectedDelta, deadDelta, curedDelta, confirmedDelta)
            }
        }
    }

    fun getStatsByProvince(from: String, to: String, country: String): List<ProvinceData> {
        val dataPoints = loadDataPoints(from, to, country)
        return dataPoints.map {
            ProvinceData(it.importDate, it.country, it.provinceName, it.provinceShortName, it.confirmedCount, it.curedCount, it.suspectedCount, it.deadCount, it.suspectedDelta, it.deadDelta, it.curedDelta, it.confirmedDelta)
        }
    }

    fun getProvinceDetails(country: String, provinceName: String, from: String, to: String): List<CityData> {
        val from = LocalDate.parse(from)
        val to = LocalDate.parse(to)


            // for now I assume there is only one data point
        val result = dataPointRepo.findByImportDateGreaterThanEqualAndImportDateLessThanEqualAndCountryAndProvinceShortName(from, to, country, provinceName)
        return result.map { it.cities.map { city ->
                CityData(it.importDate,
                    it.country,
                    it.provinceName,
                    it.provinceShortName,
                    city.cityName,
                    city.confirmedCount,
                    city.curedCount,
                    city.suspectedCount,
                    city.locationId,
                    city.deadCount,
                    city.suspectedDelta,
                    city.deadDelta,
                    city.curedDelta,
                    city.confirmedDelta)
        } }.flatten()
    }

    private fun loadDataPoints(fromDateString: String, toDateString: String, country: String? = null): List<DataPoint> {
        val from = LocalDate.parse(fromDateString)
        val to = LocalDate.parse(toDateString)


        logger.info("Fetching data points from $from to $to")
        val result = if (country == null) {
            dataPointRepo.findByImportDateGreaterThanEqualAndImportDateLessThanEqual(from, to)
        } else {
            dataPointRepo.findByImportDateGreaterThanEqualAndImportDateLessThanEqualAndCountry(from, to, country)
        }

        logger.info("Found ${result.size} entries for $fromDateString")

        if (result.isNotEmpty()) {
            return result
        }
        return emptyList()
    }

}

private data class CountryAndDateKey(val country: String, val importDate: LocalDate)