package refle.corov_data_collector.service

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import refle.corov_data_collector.Mappers
import refle.corov_data_collector.config.SourceConfigParams
import refle.corov_data_collector.loadFixture
import refle.corov_data_collector.persistence.DataPointRepo
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail


@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class DataLoaderTest{
    @Autowired private lateinit var restTemplate: RestTemplate
    @Autowired private lateinit var dataLoader: DataLoader
    @Autowired private lateinit var sourceConfigParams: SourceConfigParams

    @Autowired private lateinit var dataPointRepo: DataPointRepo

    private val mapper = Mappers.DEFAULT
    private lateinit var mockServer: MockRestServiceServer

    @Before
    fun init(){
        mockServer = MockRestServiceServer.createServer(restTemplate)
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

    private fun expectSuccessfulCallAndReply(url: String,responseBody: String?){
        if(responseBody == null ){
            mockServer.expect(ExpectedCount.once(),
                    requestTo(URI(url)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK))
        }else{
            mockServer.expect(ExpectedCount.once(),
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