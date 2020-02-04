package refle.corov_data_collector.model

import org.springframework.data.jpa.repository.Temporal
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
data class ScheduledJobRun(
    val name: String,
    val durationMs: Long,

    @Temporal(TemporalType.TIMESTAMP)
    val ranAt: ZonedDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)