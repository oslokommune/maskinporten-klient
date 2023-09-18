package no.kommune.oslo.automatiserteprosesser.maskinporten

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MaskinportenKlient(
    val maskinportenKonfigurasjon: MaskinportenKonfigurasjon
    ) {

    private val jwtGenerator = MaskinportenJWTGenerator(maskinportenKonfigurasjon)

    private val client = OkHttpClient.Builder().build()

    fun hentAccessTokenFraMaskinporten(scopes: Set<String>): MaskinportenTokenWrapper {

        val formBody = FormBody.Builder()
            .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            .add("assertion", jwtGenerator.genererMaskinportenJWT(scopes))
            .build()

        val request = Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "*/*")
            .url(maskinportenKonfigurasjon.tokenEndpoint)
            .post(formBody)
            .build()

        log.debug("Henter token fra maskinporten..")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val resp = response.body?.string()
                log.warn("Feilet mot maskinporten [{}]: {}", response.code, resp)
                throw MaskinportenKlientException("Unexpected code $response")
            }
            try {
                val resp = response.body?.string()
                return jacksonObjectMapper().readValue(resp, MaskinportenTokenWrapper::class.java)
            } catch (ex: Exception){
                when(ex) {
                    is JsonProcessingException -> {
                        log.error("Kunne ikke prosessere response {}: {}", ex.message, ex.stackTrace)
                        throw ex
                    }
                    else -> {
                        log.error("Ukjent feil {}: {}", ex.message, ex.stackTrace)
                        throw ex
                    }
                }
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenKlient::class.java)
    }

}
