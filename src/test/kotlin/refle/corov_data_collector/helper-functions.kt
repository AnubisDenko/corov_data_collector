package refle.corov_data_collector

import org.springframework.util.ClassUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun loadFixture(fixtureName: String): String {
    val stream = ClassUtils.getDefaultClassLoader()!!.getResourceAsStream(fixtureName)
    return stream.bufferedReader().use { it.readText() }
}

fun LocalDate.toString(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}