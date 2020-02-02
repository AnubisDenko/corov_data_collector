package refle.corov_data_collector.model


data class Response(val results:List<Result>, val success:Boolean)
data class Result(val country: String, val provinceName: String, val provinceShortName: String, val confirmedCount: Int,
                  val suspectedCount: Int, val curedCount: Int, val deadCount: Int, val comment: String,
                  val updateTime: Long, val createTime: Long, val modifyTime: Long, val cities: List<RawCity>?)
data class RawCity(val cityName: String, val confirmedCount: Int, val suspectedCount: Int, val curedCount: Int, val deadCount: Int, val locationId: Int)
