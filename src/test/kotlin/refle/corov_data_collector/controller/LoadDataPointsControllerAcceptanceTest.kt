package refle.corov_data_collector.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import refle.corov_data_collector.BaseSpringAcceptanceTest
import refle.corov_data_collector.model.City
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.service.TestClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class LoadDataPointsControllerAcceptanceTest: BaseSpringAcceptanceTest(){
    @Autowired private lateinit var clock: TestClock
    @Autowired private lateinit var dataPointRepo: DataPointRepo

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var mapper: ObjectMapper

    @Before
    fun setup(){
        dataPointRepo.deleteAll()
    }

    @Test
    fun `can load latest datapoints`(){
        setupDataPointsFor(clock.getCurrentDateHK(), 100)
        val body = fetchFromController()
        val dataPoints = mapper.readValue<List<DataPoint>>(body)
        assertEquals(2, dataPoints.size)
    }

    @Test
    fun `only returns datapoints on the correct date`(){
        setupDataPointsFor(clock.getCurrentDateHK(), 100)
        setupDataPointsFor(clock.getCurrentDateHK().minusDays(1), 300)
        val body = fetchFromController()
        val dataPoints = mapper.readValue<List<DataPoint>>(body)

        assertEquals(2, dataPoints.size)
        assertEquals(100, dataPoints.first().confirmedCount)
    }

    @Test
    fun `loads the date that we request if provided`(){
        setupDataPointsFor(clock.getCurrentDateHK(), 100)
        setupDataPointsFor(clock.getCurrentDateHK().minusDays(1), 300)

        val body = fetchFromController("/loadLatestDataPoints?date=${clock.getCurrentDateHK().minusDays(1).toString("yyyy-MM-dd")}")
        val dataPoints = mapper.readValue<List<DataPoint>>(body)

        assertEquals(2, dataPoints.size)
        assertEquals(300, dataPoints.first().confirmedCount)
    }

    @Test
    fun `searches back in time to find the latest dataset if no date provided`(){
        setupDataPointsFor(clock.getCurrentDateHK().minusDays(1), 300)
        val body = fetchFromController()
        val dataPoints = mapper.readValue<List<DataPoint>>(body)

        assertEquals(2, dataPoints.size)
        assertEquals(300, dataPoints.first().confirmedCount)
    }

    @Test
    fun `can send back a datapoint with underlying cities`(){
        setupDataPointWithCity(clock.getCurrentDateHK(), 200)
        val body = fetchFromController()
        val dataPoints = mapper.readValue<List<DataPoint>>(body)

        assertEquals(1, dataPoints.size)
        assertEquals(2, dataPoints.first().cities.size)
    }

    private fun fetchFromController(url: String = "/loadLatestDataPoints"): String{
        val result = mockMvc.get(url).andReturn()
        return result.response.contentAsString
    }

    private fun setupDataPointWithCity(date:LocalDate, confirmedCount: Int): Long? {
        val cities = setOf(
                City("City 1", confirmedCount / 2, 0,0,0,0,date),
                City("City 2", confirmedCount / 2, 0,0,0,0,date)
        )

        val dataPoint = DataPoint(
                "OtherDummy",
                "OtherProvince",
                "AlsoShortname",
                confirmedCount,
                0,0,0,"",date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, cities
        )

        cities.forEach { it.dataPoint = dataPoint }
        return dataPointRepo.save(dataPoint).id
    }

    private fun setupDataPointsFor(date:LocalDate, confirmedCount: Int): List<Long?> {
        val dataPoint1 = DataPoint(
                "Dummy",
                "DummyProvince",
                "Shortname",
                confirmedCount,
                0,0,0,"",date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint2 = DataPoint(
                "OtherDummy",
                "OtherProvince",
                "AlsoShortname",
                confirmedCount,
                0,0,0,"",date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val savedPoint1 = dataPointRepo.save(dataPoint1)
        val savedPoint2 = dataPointRepo.save(dataPoint2)
        return listOf(savedPoint1.id, savedPoint2.id)
    }

    private fun LocalDate.toString(pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return this.format(formatter)
    }
}