package refle.corov_data_collector.model

import org.springframework.data.jpa.repository.Temporal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.persistence.*

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

        @Temporal(TemporalType.DATE)
        val importDate: LocalDate,

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

        @Temporal(TemporalType.DATE)
        val importDate: LocalDate,

        @Id @GeneratedValue
        val id: Long? = null
){
        @ManyToOne
        lateinit var dataPoint: DataPoint
}

@Entity
data class Translation(
        val chinese: String,
        val english: String,
        @Id @GeneratedValue
        val id: Long? = null
)