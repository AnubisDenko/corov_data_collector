package refle.corov_data_collector.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import org.jasypt.util.text.AES256TextEncryptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import refle.corov_data_collector.config.AppConfig
import refle.corov_data_collector.model.Translation
import refle.corov_data_collector.persistence.TranslationRepo

@Component
@Profile("!test")
class GoogleTranslator(@Autowired private val appConfig: AppConfig, @Autowired private val translationRepo: TranslationRepo):Translator {
    private val translate: Translate

    init {
        val googleCredentialsFile = decrypt(appConfig.passphrase, loadFile("google-translate.enc"))

        val credentials = GoogleCredentials.fromStream(googleCredentialsFile.byteInputStream())
                .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"));
        translate = TranslateOptions.newBuilder().setCredentials(credentials).build().service
    }


    override fun translateChineseToEnglish(chinese: String): String{
        val cached = translationRepo.findByChinese(chinese)

        return if(cached != null){
            cached.english
        }else{
            val translatedText = translate.translate(chinese).translatedText
            translationRepo.save(Translation(chinese, translatedText))
            translatedText
        }
    }

    fun decrypt(key: String, encryptedText:String): String{
        val encryptor = AES256TextEncryptor()
        encryptor.setPassword(key)
        return encryptor.decrypt(encryptedText)
    }

    private fun loadFile(file: String): String{
        val stream = ClassUtils.getDefaultClassLoader()!!.getResourceAsStream(file)
        return stream.bufferedReader().use { it.readText() }
    }
}

interface Translator{
    fun translateChineseToEnglish(chinese:String): String
}