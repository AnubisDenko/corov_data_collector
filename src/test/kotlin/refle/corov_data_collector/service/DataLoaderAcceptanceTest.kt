package refle.corov_data_collector.service

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import refle.corov_data_collector.BaseSpringAcceptanceTest
import refle.corov_data_collector.Mappers
import refle.corov_data_collector.config.SourceConfigParams
import refle.corov_data_collector.loadFixture
import refle.corov_data_collector.persistence.DataPointRepo
import refle.corov_data_collector.setupDataPointWithCity
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail


class DataLoaderAcceptanceTest: BaseSpringAcceptanceTest(){
    @Autowired private lateinit var restTemplate: RestTemplate
    @Autowired private lateinit var dataLoader: DataLoader
    @Autowired private lateinit var sourceConfigParams: SourceConfigParams

    @Autowired private lateinit var dataPointRepo: DataPointRepo
    @Autowired private lateinit var clock: TestClock

    private val mapper = Mappers.DEFAULT
    private lateinit var mockServer: MockRestServiceServer

    @Before
    fun init(){
        mockServer = MockRestServiceServer.createServer(restTemplate)
        clock.current = ZonedDateTime.of(2020, 1,10,10,10,10,0, ZoneId.of("Asia/Hong_Kong"))
        dataPointRepo.deleteAll()
    }



    @Test
    fun `does try to load data`(){
        val response = loadFixture("fixtures/sampleAreaResponse.json")
        expectSuccessfulCallAndReply("${sourceConfigParams.baseUrl}area", response)
        dataLoader.loadData()

        mockServer.verify()
    }

    @Test
    fun `does persist the loaded data in database`(){
        val response = loadFixture("fixtures/sampleAreaResponse.json")
        expectSuccessfulCallAndReply("${sourceConfigParams.baseUrl}area", response)
        dataLoader.loadData()

        val dataPoints = dataPointRepo.findAll().toList()
        assertEquals(3, dataPoints.size)

        val china = dataPoints.find { it.country == "China" } ?: fail("No data for China found")

        val expectedUpdateTime = convertToDateTime(1580618843298L)
        assertEquals(expectedUpdateTime, china.updateTime)
        assertNull(china.createTime)
        assertNull(china.modifyTime)
        assertEquals("Hunan Province", china.provinceName)
        assertEquals("Hunan", china.provinceShortName)

        val cnCities = china.cities ?: fail("No cities saved for China data")
        assertEquals(14, cnCities.size)

        assertNotNull(cnCities.find { it.cityName == "Changsha" })

        val australia = dataPoints.find { it.country == "Australia" } ?: fail("No data for Australia")
        val ausCities = australia.cities
        assertTrue{ ausCities.isEmpty() }
    }

    @Test
    fun `does clean data for current day before refetching`(){
        val response = loadFixture("fixtures/sampleAreaResponse.json")
        expectSuccessfulCallAndReply("${sourceConfigParams.baseUrl}area", response, ExpectedCount.twice())
        dataLoader.loadData()
        dataLoader.loadData()

        val dataPoints = dataPointRepo.findAll().toList()
        assertEquals(3, dataPoints.size)
    }

    @Test
    fun `maps country for Hong Kong and Macao from China to City`(){
        val response = loadFixture("fixtures/hong_kong_and_macau_response.json")
        expectSuccessfulCallAndReply("${sourceConfigParams.baseUrl}area", response)
        dataLoader.loadData()

        val dataPoints = dataPointRepo.findAll().toList()
        val hongkong = dataPoints.find { it.provinceName == "Hong Kong" } ?: fail("Hong Kong not found")
        assertEquals("Hong Kong", hongkong.country)

        val macao = dataPoints.find { it.provinceName == "Macao" } ?: fail("Macao not found")
        assertEquals("Macao", macao.country)
    }

    @Test
    fun `calculates the delta for loaded datapoint compared to previous day`(){
        setupDataPointWithCityAndSave("China", "Hunan",clock.getCurrentDateHK().minusDays(1),363, 0,6,5)
        val response = loadFixture("fixtures/sampleForDeltaCalc.json")
        expectSuccessfulCallAndReply("${sourceConfigParams.baseUrl}area", response)

        dataLoader.loadData()

        val hunanToday = dataPointRepo.findByImportDate(clock.getCurrentDateHK()).first()

        with(hunanToday) {
            assertEquals(100, confirmedDelta)
            assertEquals(5, suspectedDelta)
            assertEquals(5, curedDelta)
            assertEquals(15, deadDelta)
        }
    }

    private val setupDataPointWithCityAndSave = { country: String, province: String, date: LocalDate, confirmedCount: Int, suspectedCount: Int , curedCount: Int, deadCount: Int ->
        val dataPoint = setupDataPointWithCity(country, province, date, confirmedCount, suspectedCount, curedCount, deadCount)
        dataPointRepo.save(dataPoint).id
    }

    private fun expectSuccessfulCallAndReply(url: String,responseBody: String?, times: ExpectedCount = ExpectedCount.once()){
        if(responseBody == null ){
            mockServer.expect(times,
                    requestTo(URI(url)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK))
        }else{
            mockServer.expect(times,
                    requestTo(URI(url)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody)
                    )
        }
    }

    private val convertToDateTime = { milli:Long -> Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault()).toLocalDateTime() }
}