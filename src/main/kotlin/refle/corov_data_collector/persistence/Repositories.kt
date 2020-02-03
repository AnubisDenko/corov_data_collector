package refle.corov_data_collector.persistence

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import refle.corov_data_collector.model.DataPoint

@Repository
interface DataPointRepo: CrudRepository<DataPoint, Long>