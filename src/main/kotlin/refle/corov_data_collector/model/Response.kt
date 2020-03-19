package refle.corov_data_collector.model


data class Response(val results:List<Result>, val success:Boolean)
data class Result(val countryName: String, val provinceName: String, val provinceShortName: String, val confirmedCount: Int,
                  val suspectedCount: Int, val curedCount: Int, val deadCount: Int, val comment: String? = null,
                  val updateTime: Long, val cities: List<RawCity>?,
                  val continentName: String, val continentEnglishName: String?, val countryEnglishName: String?, val provinceEnglishName: String?)
data class RawCity(val cityName: String, val confirmedCount: Int, val suspectedCount: Int, val curedCount: Int, val deadCount: Int, val locationId: Int, val cityEnglishName: String?)
