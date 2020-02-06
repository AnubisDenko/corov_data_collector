package refle.corov_data_collector.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import refle.corov_data_collector.model.CityData
import refle.corov_data_collector.model.CountryData
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.ProvinceData
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.service.Clock
import java.time.LocalDate

@RestController
class LoadDataPointsController(@Autowired private val dataPointRepo: DataPointRepo, @Autowired private val clock: Clock) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping(path = ["/loadLatestDataPoints"])
    fun loadLatestDataPoints(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,@RequestParam country: String?= null): List<DataPoint>{
        var searchDate = date ?: clock.getCurrentDateHK()

        (0..5).forEach {
            searchDate = searchDate.minusDays(it.toLong())

            logger.info("Fetching data points for $searchDate")
            val result = if(country == null) {
                dataPointRepo.findByImportDate(searchDate)
            }else{
                dataPointRepo.findByImportDateAndCountry(searchDate, country)
            }

            logger.info("Found ${result.size} entries for $date")

            if(result.isNotEmpty()){
                return result
            }
        }

        return emptyList()
    }

    @GetMapping(path = ["/loadGlobalStatsByCountry"])
    fun loadStatsByCountry(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?): List<CountryData> {
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
                    null,null, setOf())
            }
            with(aggregate) {
                return@map CountryData(importDate, country, confirmedCount, curedCount, suspectedCount)
            }
        }
    }

    @GetMapping(path = ["/loadCountryDetailsByProvince"])
    fun loadCountryDetailsByProvince(@RequestParam country: String, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?): List<ProvinceData> {
        val dataPoints = loadLatestDataPoints(date, country)
        return dataPoints.map {
            ProvinceData(it.importDate, it.country, it.provinceName, it.provinceShortName, it.confirmedCount, it.curedCount, it.suspectedCount)
        }
    }

    @GetMapping(path = ["/loadProvinceDetails"])
    fun loadProvinceDetails(@RequestParam country: String, @RequestParam provinceName: String, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?): List<CityData> {
        var searchDate = date ?: clock.getCurrentDateHK()

        (0..5).forEach {
            searchDate = searchDate.minusDays(it.toLong())

            // for now I assume there is only one data point
            val result = dataPointRepo.findByImportDateAndCountryAndProvinceName(searchDate, country, provinceName).firstOrNull() ?: return emptyList()
            return result.cities.map { CityData(result.importDate, result.country, result.provinceName, result.provinceShortName, it.cityName, it.confirmedCount, it.curedCount, it.suspectedCount, it.locationId) }
        }

        return listOf()
    }

}