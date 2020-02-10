package refle.corov_data_collector.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import refle.corov_data_collector.BaseSpringAcceptanceTest
import refle.corov_data_collector.setupDataPointWithCity
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.service.TestClock
import java.time.LocalDate
import kotlin.test.assertNotNull
import refle.corov_data_collector.toString
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * TODO at the moment this test ignores the graphql layer as I didn't get it to work. This needs to be fixed!!
 */
class LoadByCountryAcceptanceTest() : BaseSpringAcceptanceTest() {
    @Autowired
    private lateinit var dataPointRepo: DataPointRepo
    @Autowired
    private lateinit var clock: TestClock

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var virusStatsQuery: VirusStatsQuery

    @Before
    fun setup() {
        dataPointRepo.deleteAll()
    }

    @Test
    fun `service aggregates counts by country`() {
        setupDataPointsForTwoCountries(clock.getCurrentDateHK(), "China", 600, "Germany", 100)

        val result = virusStatsQuery.getStatsByCountry(getTodayAsString(), getTodayAsString())
        assertNotNull(result)

        val china = result.find { it.country == "China" } ?: fail("China not found")
        assertEquals(1200, china.confirmedCount)

        val germany = result.find { it.country == "Germany" } ?: fail("Germany not found")
        assertEquals(200, germany.confirmedCount)
    }

    @Test
    fun `can load country data for multiple days`(){
        (0..4).forEach {
            setupDataPointsForTwoCountries(clock.getCurrentDateHK().plusDays(it.toLong() - 1), "China", 600, "Germany", 100)
        }

        val result = virusStatsQuery.getStatsByCountry(getYesterdayAsString(), getTodayAsString())
        assertNotNull(result)
        assertEquals(4, result.size)
        assertNotNull( result.find { it.country == "China" && it.importDate == clock.getCurrentDateHK() })
        assertNotNull( result.find { it.country == "China" && it.importDate == clock.getCurrentDateHK().minusDays(1) })
        assertNotNull( result.find { it.country == "Germany" && it.importDate == clock.getCurrentDateHK() })
        assertNotNull( result.find { it.country == "Germany" && it.importDate == clock.getCurrentDateHK().minusDays(1) })
    }

    @Test
    fun `provides provinces details for country`() {
        setupDataPointsForTwoCountries(clock.getCurrentDateHK(), "China", 600, "Germany", 100)
        val result = virusStatsQuery.getStatsByProvince(getTodayAsString(), getTodayAsString(),"China")
        assertEquals(2, result.size)
        assertNotNull(result.find { it.provinceName == "China1" })
        assertNotNull(result.find { it.provinceName == "China2" })
    }

    @Test
    fun `provides provinces details for country for multiple days`() {
        (0..4).forEach {
            setupDataPointsForTwoCountries(clock.getCurrentDateHK().plusDays(it.toLong() - 1), "China", 600, "Germany", 100)
        }

        val result = virusStatsQuery.getStatsByProvince(getYesterdayAsString(), getTodayAsString(),"China")
        assertEquals(4, result.size)
        assertNotNull( result.find { it.country == "China" && it.provinceName == "China1" && it.importDate == clock.getCurrentDateHK() } )
        assertNotNull( result.find { it.country == "China" && it.provinceName == "China1" && it.importDate == clock.getCurrentDateHK().minusDays(1) } )
        assertNotNull( result.find { it.country == "China" && it.provinceName == "China2" && it.importDate == clock.getCurrentDateHK() } )
        assertNotNull( result.find { it.country == "China" && it.provinceName == "China2" && it.importDate == clock.getCurrentDateHK().minusDays(1) } )
    }

    @Test
    fun `provides city details for country and province`() {
        val dataPoints = listOf(
                setupDataPointWithCity("China", "Shandong", clock.getCurrentDateHK(), 100),
                setupDataPointWithCity("China", "Hunan", clock.getCurrentDateHK(), 300),
                setupDataPointWithCity("Germany", "Bavaria", clock.getCurrentDateHK(), 400)
        )
        dataPointRepo.saveAll(dataPoints)

        val result = virusStatsQuery.getProvinceDetails("China", "Shandong", getTodayAsString(),getTodayAsString())
        assertNotNull(result.find { it.city == "China_Shandong_City1" })
        assertNotNull(result.find { it.city == "China_Shandong_City2" })
    }

    @Test
    fun `provides city details for country and province for multiple days`() {
        (0..4).forEach {
            val dataPoints = listOf(
                    setupDataPointWithCity("China", "Shandong", clock.getCurrentDateHK().plusDays(it.toLong() - 1), 100),
                    setupDataPointWithCity("China", "Hunan", clock.getCurrentDateHK().plusDays(it.toLong() - 1), 300),
                    setupDataPointWithCity("Germany", "Bavaria", clock.getCurrentDateHK().plusDays(it.toLong() - 1), 400)
            )
            dataPointRepo.saveAll(dataPoints)
        }

        val result = virusStatsQuery.getProvinceDetails("China", "Shandong", getYesterdayAsString(),getTodayAsString())
        assertNotNull(result.find { it.city == "China_Shandong_City1" && it.importDate == clock.getCurrentDateHK()})
        assertNotNull(result.find { it.city == "China_Shandong_City1" && it.importDate == clock.getCurrentDateHK().minusDays(1)})
        assertNotNull(result.find { it.city == "China_Shandong_City2" && it.importDate == clock.getCurrentDateHK() })
        assertNotNull(result.find { it.city == "China_Shandong_City2" && it.importDate == clock.getCurrentDateHK().minusDays(1) })
    }

    private val getTodayAsString = { clock.getCurrentDateHK().toString("yyyy-MM-dd") }
    private val getYesterdayAsString = { clock.getCurrentDateHK().minusDays(1).toString("yyyy-MM-dd") }

    private fun setupDataPointsForTwoCountries(date: LocalDate, country1Name: String, country1ConfirmedCount: Int, country2Name: String, country2ConfirmedCount: Int) {
        val dataPoint1 = DataPoint(
                country1Name,
                "${country1Name}1",
                "Shortname",
                country1ConfirmedCount,
                0, 0, 0,
                "",
                date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint2 = DataPoint(
                country1Name,
                "${country1Name}2",
                "AlsoShortname",
                country1ConfirmedCount,
                0, 0, 0,
                "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint3 = DataPoint(
                country2Name,
                "${country2Name}1",
                "Shortname",
                country2ConfirmedCount,
                0, 0, 0,
                "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        val dataPoint4 = DataPoint(
                country2Name,
                "${country2Name}2",
                "AlsoShortname",
                country2ConfirmedCount,
                0, 0, 0,
                "", date,
                clock.getCurrentDatetimeHK().toLocalDateTime(),
                null,
                null, setOf()
        )

        dataPointRepo.saveAll(listOf(dataPoint1, dataPoint2, dataPoint3, dataPoint4))


    }
}