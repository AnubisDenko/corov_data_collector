package refle.corov_data_collector.service

import com.nhaarman.mockitokotlin2.mock
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.text.RandomStringGenerator
import org.jasypt.util.text.AES256TextEncryptor
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.util.ClassUtils
import refle.corov_data_collector.config.AppConfig
import refle.corov_data_collector.persistence.TranslationRepo
import java.io.File
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.test.assertEquals


class GoogleTranslatorTest{
    private val appConfig = AppConfig("TBC")
    private val translationRepo: TranslationRepo = mock()
    private val translator = GoogleTranslator(appConfig, translationRepo)

    @Test
    @Ignore
    fun `translates Chinese into English`(){
        val text = "中国"
        val englishTranslation = "China"

        assertEquals(englishTranslation,translator.translateChineseToEnglish(text))
    }

    @Test
    @Ignore
    fun `encrypt google credentials file`(){
        val credentialsFile = loadFile("google-translate.json")

        val secretKey = RandomStringUtils.randomAlphanumeric(20);
        println(secretKey.toString())

        val encryptedText = encrypt(secretKey, credentialsFile)

//        writeFile("./src/main/resources/google-translate.enc", encryptedText.toString())

        val decrytedText = translator.decrypt(secretKey, encryptedText)

        assertEquals(credentialsFile, decrytedText)
    }

    private fun encrypt(key: String, text: String): String{
        val encryptor = AES256TextEncryptor()
        encryptor.setPassword(key)
        return encryptor.encrypt(text)
    }



    private fun loadFile(file: String): String{
        val stream = ClassUtils.getDefaultClassLoader()!!.getResourceAsStream(file)
        return stream.bufferedReader().use { it.readText() }
    }

    private fun writeFile(fileName: String, content: String){
        File(fileName).writeText(content)
    }
}