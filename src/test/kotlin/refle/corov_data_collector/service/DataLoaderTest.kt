package refle.corov_data_collector.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
import refle.corov_data_collector.persistence.CityRepo
import refle.corov_data_collector.persistence.DataPointRepo
import java.net.URI
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
    @Autowired private lateinit var cityRepo: CityRepo

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

        val china = dataPointRepo.findAll().toList().find { it.country == "中国" } ?: fail("No data for China found")
        val cities = china.cities ?: fail("No cities saved for China data")
        assertEquals(10, cities.size)
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
}