package refle.corov_data_collector

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import refle.corov_data_collector.model.City
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.service.TestClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun loadFixture(fixtureName: String): String {
    val stream = ClassUtils.getDefaultClassLoader()!!.getResourceAsStream(fixtureName)
    return stream.bufferedReader().use { it.readText() }
}

fun LocalDate.toString(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}

fun setupDataPointWithCity(country: String, province: String, date: LocalDate, confirmedCount: Int): DataPoint {
    val cities = setOf(
            City("${country}_${province}_City1", confirmedCount / 2, 0, 0, 0, 0, date),
            City("${country}_${province}_City2", confirmedCount / 2, 0, 0, 0, 0, date)
    )

    val dataPoint = DataPoint(
            country,
            "${province}_long",
            province,
            confirmedCount,
            0, 0, 0, "",
            date,
            LocalDateTime.now(),
            null,
            null, cities
    )

    cities.forEach { it.dataPoint = dataPoint }
    return dataPoint
}
