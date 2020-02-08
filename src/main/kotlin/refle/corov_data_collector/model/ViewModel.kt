package refle.corov_data_collector.model

import java.time.LocalDate


data class CountryData(
        val importDate: LocalDate,
        val country: String,
        val confirmedCount: Int,
        val curedCount: Int,
        val suspectedCount: Int,
        val deadCount: Int)

data class ProvinceData(
        val importDate: LocalDate,
        val country: String,
        val provinceName: String,
        val provinceShortName: String,
        val confirmedCount: Int,
        val curedCount: Int,
        val suspectedCount: Int,
        val deadCount: Int)

data class CityData(
        val importDate: LocalDate,
        val country: String,
        val provinceName: String,
        val provinceShortName: String,
        val city: String,
        val confirmedCount: Int,
        val curedCount: Int,
        val suspectedCount: Int,
        val locationId: Int,
        val deadCount: Int)
