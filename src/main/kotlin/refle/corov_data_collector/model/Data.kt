package refle.corov_data_collector.model

import javax.persistence.*

@Entity
data class Dummy(val value: String, @Id @GeneratedValue val id: Long? = null)

@Entity
data class DataPoint(
        val country: String,
        val provinceName: String,
        val provinceShortName: String,
        val confirmedCount: Int,
        val suspectedCount: Int,
        val curedCount: Int,
        val deadCount: Int,
        val comment: String,
        val updateTime: Long,
        val createTime: Long,
        val modifyTime: Long,

        @OneToMany(mappedBy = "dataPoint", fetch = FetchType.EAGER)
        val cities: Set<City>? = null,

        @Id
        @GeneratedValue
        @Column(name = "data_id")
        val id: Long? = null
)

@Entity
data class City(
        val cityName: String,
        val confirmedCount: Int,
        val suspectedCount: Int,
        val curedCount: Int,
        val deadCount: Int,
        val locationId: Int,

        @ManyToOne
        @JoinColumn(name = "data_id", nullable = false)
        val dataPoint: DataPoint,

        @Id @GeneratedValue
        val id: Long? = null
)
