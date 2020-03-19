package refle.corov_data_collector.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.ColumnDefault
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

        @OneToMany(
                cascade = [ CascadeType.ALL ],
                orphanRemoval = true,
                fetch = FetchType.EAGER

        )
        val cities: Set<City>,

        @Id
        @GeneratedValue
        @Column(name = "data_id")
        val id: Long? = null,

        @ColumnDefault("0")
        val confirmedDelta: Int = 0,
        @ColumnDefault("0")
        val suspectedDelta: Int = 0,
        @ColumnDefault("0")
        val curedDelta: Int = 0,
        @ColumnDefault("0")
        val deadDelta: Int = 0
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
        val id: Long? = null,

        @ColumnDefault("0")
        val confirmedDelta: Int = 0,
        @ColumnDefault("0")
        val suspectedDelta: Int = 0,
        @ColumnDefault("0")
        val curedDelta: Int = 0,
        @ColumnDefault("0")
        val deadDelta: Int = 0
){
        @ManyToOne
        @JsonIgnore
        lateinit var dataPoint: DataPoint

}

@Entity
data class Translation(
        @Column(length = 2048)
        val chinese: String,

        @Column(length = 2048)
        val english: String,

        @Id @GeneratedValue
        val id: Long? = null
)