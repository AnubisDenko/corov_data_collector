package refle.corov_data_collector.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import refle.corov_data_collector.service.DataLoader

@RestController
class ManualDataLoaderController(@Autowired private val dataLoader: DataLoader) {

    @PostMapping(path = ["/triggerDataLoad"])
    fun triggerDataLoad(){
        dataLoader.loadData()
    }
}