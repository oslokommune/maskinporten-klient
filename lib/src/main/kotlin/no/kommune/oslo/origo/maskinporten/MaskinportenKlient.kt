package no.kommune.oslo.origo.maskinporten

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
            try {
            if (!response.isSuccessful) {
                val resp = response.body?.string()
                log.warn("Feilet mot maskinporten [{}]: {}", response.code, resp)
                val error = jacksonObjectMapper().readValue(resp, MaskinportenError::class.java)
                throw MaskinportenKlientException("Unexpected code $response. $error ")
            }

                val resp = response.body?.string()
                return jacksonObjectMapper().readValue(resp, MaskinportenTokenWrapper::class.java)
            } catch (ex: Exception){
                when(ex) {
                    is JsonProcessingException -> {
                        response.body?.string()
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

data class MaskinportenTokenWrapper(val access_token: String,
                                    val token_type: String,
                                    val expires_in: String,
                                    val scope: String )

data class MaskinportenError(val error: String,
                             val error_description: String)

class MaskinportenKlientException(error: String) : Exception(error)

