package refle.corov_data_collector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

object Mappers{
    val DEFAULT: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
}