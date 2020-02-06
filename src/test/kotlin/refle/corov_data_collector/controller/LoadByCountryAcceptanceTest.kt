package refle.corov_data_collector.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import refle.corov_data_collector.BaseSpringAcceptanceTest
import refle.corov_data_collector.model.*
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.service.TestClock
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class LoadByCountryAcceptanceTest() : BaseSpringAcceptanceTest() {
    @Autowired private lateinit var dataPointRepo: DataPointRepo
    @Autowired private lateinit var clock: TestClock

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var mapper: ObjectMapper

    @Before
    fun setup(){
        dataPointRepo.deleteAll()
    }

    @Test
    fun `service aggregates counts by country`() {
        setupDataPointsForTwoCountries(clock.getCurrentDateHK(), "China", 600, "Germany", 100)
        val result = mapper.readValue<List<CountryData>>(fetchFromController())
        assertNotNull(result)
        val china = result.find { it.country == "China" } ?: fail("China not found")
        assertEquals(1200, china.confirmedCount)

        val germany = result.find { it.country == "Germany" } ?: fail("Germany not found")
        assertEquals(200, germany.confirmedCount)
    }

    @Test
    fun `provides provinces details for country`(){
        setupDataPointsForTwoCountries(clock.getCurrentDateHK(), "China", 600, "Germany", 100)
        val result = mapper.readValue<List<ProvinceData>>(fetchFromController("/loadCountryDetailsByProvince?country=China"))
        assertEquals(2, result.size)
        assertNotNull(result.find { it.provinceName == "China1" })
        assertNotNull(result.find { it.provinceName == "China2" })
    }

    @Test
    fun `provides city details for country and province`(){
        setupDataPointWithCity("China","Shandong", clock.getCurrentDateHK(), 100)
        setupDataPointWithCity("China","Hunan", clock.getCurrentDateHK(), 300)
        setupDataPointWithCity("Germany","Bavaria", clock.getCurrentDateHK(), 400)

        val result = mapper.readValue<List<CityData>>(fetchFromController("/loadProvinceDetails?country=China&provinceName=Shandong"))
        assertNotNull(result.find { it.city == "China_Shandong_City1" })
        assertNotNull(result.find { it.city == "China_Shandong_City2" })
    }

    private fun fetchFromController(url: String = "/loadGlobalStatsByCountry"): String{
        val result = mockMvc.get(url).andReturn()
        return result.response.contentAsString
    }

    private fun setupDataPointWithCity(country: String, province: String, date:LocalDate, confirmedCount: Int): Long? {
        val cities = setOf(
                City("${country}_${province}_City1", confirmedCount / 2, 0,0,0,0,date),
                City("${country}_${province}_City2", confirmedCount / 2, 0,0,0,0,date)
        )

        val dataPoint = DataPoint(
                country,
                province,
                "${province}_short",
                confirmedCount,
                0,0,0,"",date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, cities
        )

        cities.forEach { it.dataPoint = dataPoint }
        return dataPointRepo.save(dataPoint).id
    }

    private fun setupDataPointsForTwoCountries(date: LocalDate, country1Name: String, country1ConfirmedCount: Int, country2Name: String, country2ConfirmedCount: Int){
        val dataPoint1 = DataPoint(
                country1Name,
                "${country1Name}1",
                "Shortname",
                country1ConfirmedCount,
                0, 0, 0, "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint2 = DataPoint(
                country1Name,
                "${country1Name}2",
                "AlsoShortname",
                country1ConfirmedCount,
                0, 0, 0, "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint3 = DataPoint(
                country2Name,
                "${country2Name}1",
                "Shortname",
                country2ConfirmedCount,
                0, 0, 0, "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint4 = DataPoint(
                country2Name,
                "${country2Name}2",
                "AlsoShortname",
                country2ConfirmedCount,
                0, 0, 0, "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        dataPointRepo.saveAll(listOf(dataPoint1,dataPoint2,dataPoint3,dataPoint4))



    }
}