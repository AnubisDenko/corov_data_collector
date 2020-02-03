package refle.corov_data_collector.persistence

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import refle.corov_data_collector.model.City
import refle.corov_data_collector.model.DataPoint
import refle.corov_data_collector.model.Dummy

@Repository
interface DataPointRepo: CrudRepository<DataPoint, Long>