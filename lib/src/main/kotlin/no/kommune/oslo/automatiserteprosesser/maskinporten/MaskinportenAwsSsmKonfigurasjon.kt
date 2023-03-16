package no.kommune.oslo.automatiserteprosesser.maskinporten

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.util.Base64
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.cert.CertificateException

class MaskinportenAwsSsmKonfigurasjon(override val issuer : String,
                                override val audience : String,
                                override val tokenEndpoint : String,
                                override val consumerOrganization : String,
                                awsRegion : Region

) : MaskinportenKonfigurasjon {

    private val ssmPathToMaskinportenKey: String = "/okdata/maskinporten/$issuer/key.json"
    private val parameterStoreClient: MaskinportenParameterStoreKlient
    private val objectMapper: ObjectMapper
    private val keyProperties: OkdataKeyProperties

    init {
        check(issuer.isNotEmpty())
        check(audience.isNotEmpty())
        check(tokenEndpoint.isNotEmpty())
        check(consumerOrganization.isNotEmpty())

        parameterStoreClient = MaskinportenParameterStoreKlient(awsRegion)
        objectMapper = ObjectMapper()
        keyProperties = getOkdataKeyProperties()
    }

    override val privateKey: PrivateKey by lazy { loadPrivateKey() }
    override val keyId: String by lazy { keyProperties.keyId }



    private fun loadPrivateKey(): PrivateKey {
        val keyStore = getLoadedKeystore(keyProperties)
        log.debug("Keystore provider: ${keyStore.provider}")
        log.debug("Keystore type: ${keyStore.type}")
        log.debug("Aliases found in keystore: ${keyStore.aliases().toList()}")
        log.debug("Is ${keyProperties.keyAlias} found in keystore?: ${keyStore.isKeyEntry(keyProperties.keyAlias)}")

        return keyStore.getKey(keyProperties.keyAlias, keyProperties.keyPassword.toCharArray()) as PrivateKey
    }

    private fun getLoadedKeystore(keyProperties: OkdataKeyProperties): KeyStore {
        try {
            val inputStream: InputStream = Base64.from(keyProperties.keystoreAsByte64).decode().inputStream()
            val keyStore = KeyStore.getInstance(KeystoreType.P12.storeValue)
            keyStore.load(inputStream, keyProperties.keyPassword.toCharArray())
            return keyStore
        } catch (ex: Exception){
            when(ex) {
                is IOException, is NoSuchAlgorithmException, is CertificateException -> {
                    log.error("Kunne ikke håndtere keystore for maskinporten: {}", ex.stackTrace)
                    throw ex
                }
                is SecurityException -> {
                    log.error("Kunne ikke finne eller lese keystore: {}", ex.stackTrace)
                    throw ex
                }
                else -> throw ex
            }
        }
    }

    private fun getOkdataKeyProperties(): OkdataKeyProperties {
        val keystoreAsByteBase64 = parameterStoreClient.getValue(ssmPathToMaskinportenKey)
        return objectMapper.readValue(keystoreAsByteBase64, OkdataKeyProperties::class.java)

    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenKonfigurasjon::class.java)
    }

}
