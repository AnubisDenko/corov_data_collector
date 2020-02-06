package refle.corov_data_collector.persistence

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import refle.corov_data_collector.model.CountryData
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.ScheduledJobRun
import refle.corov_data_collector.model.Translation
import java.time.LocalDate

@Repository
interface DataPointRepo: CrudRepository<DataPoint, Long>{
    fun deleteByImportDate(date: LocalDate): Long
    fun findByImportDate(date:LocalDate): List<DataPoint>
    fun findByImportDateAndCountry(date:LocalDate, country:String): List<DataPoint>
    fun findByImportDateAndCountryAndProvinceName(date:LocalDate, country:String, provinceName: String): List<DataPoint>
}

@Repository
interface TranslationRepo: CrudRepository<Translation, Long>{
    fun findByChinese(chinese: String): Translation?
}

@Repository
interface ScheduledJobRepo: CrudRepository<ScheduledJobRun, Long>