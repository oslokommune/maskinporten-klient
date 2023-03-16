package no.kommune.oslo.automatiserteprosesser.maskinporten

import com.fasterxml.jackson.annotation.JsonProperty
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import java.io.Closeable
import java.lang.IllegalArgumentException

class MaskinportenParameterStoreKlient(region: Region): Closeable {
    private val client = SsmClient.builder()
        .region(region)
        .build()

    fun getValue(parameterName: String): String {
        val valueRequest = GetParameterRequest.builder()
            .name(parameterName)
            .withDecryption(true)
            .build()

        val respose = client.getParameter(valueRequest)
        return respose.parameter()?.value() ?: throw IllegalArgumentException("Parameter $parameterName not found in parameter store")
    }

    override fun close() {
        client.close()
    }
}

data class OkdataKeyProperties(@JsonProperty("key_id") val keyId: String,
                               @JsonProperty("keystore") val keystoreAsByte64: String,
                               @JsonProperty("key_alias") val keyAlias: String,
                               @JsonProperty("key_password") val keyPassword: String)