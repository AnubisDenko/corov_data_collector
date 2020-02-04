package refle.corov_data_collector.persistence

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.Translation
import java.time.LocalDate

@Repository
interface DataPointRepo: CrudRepository<DataPoint, Long>{
    fun deleteByImportDate(date: LocalDate): Long
}

@Repository
interface TranslationRepo: CrudRepository<Translation, Long>{
    fun findByChinese(chinese: String): Translation?
}