package refle.corov_data_collector.model

import org.springframework.data.jpa.repository.Temporal
import java.time.LocalDateTime
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

        @Temporal(TemporalType.TIMESTAMP)
        val updateTime: LocalDateTime,

        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = true)
        val createTime: LocalDateTime?,
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = true)
        val modifyTime: LocalDateTime?,

        @OneToMany(
                cascade = [ CascadeType.ALL ],
                orphanRemoval = true,
                fetch = FetchType.EAGER
        )
        val cities: Set<City>,

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

        @Id @GeneratedValue
        val id: Long? = null
)
