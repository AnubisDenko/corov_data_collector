package refle.corov_data_collector.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class TestTranslator: Translator {
    private val dictionary = mapOf(
            "澳大利亚" to "Australia",
            "新加坡" to "Singapore",
            "中国" to "China",
            "湖南省" to "Hunan Province",
            "湖南" to "Hunan",
            "长沙" to "Changsha",
            "香港" to "Hong Kong",
            "澳门" to "Macao"
    )
    override fun translateChineseToEnglish(chinese: String): String {
        return if(dictionary.containsKey(chinese)){
            dictionary[chinese] ?: error("This should never happen as we check if key exists")
        }else{
            chinese
        }
    }
}