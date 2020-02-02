package refle.corov_data_collector

import org.springframework.util.ClassUtils

fun loadFixture(fixtureName: String): String {
    val stream = ClassUtils.getDefaultClassLoader()!!.getResourceAsStream(fixtureName)
    return stream.bufferedReader().use { it.readText() }
}