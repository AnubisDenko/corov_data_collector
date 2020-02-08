package refle.corov_data_collector.graphql

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import refle.corov_data_collector.service.DataLoader

@Component
class VirusStatsMutation(@Autowired private val dataLoader: DataLoader): GraphQLMutationResolver {
    fun loadLatestVirusData(): Boolean {
        GlobalScope.launch {
            dataLoader.loadData()
        }
        return true
    }
}