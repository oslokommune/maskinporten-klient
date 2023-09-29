package no.kommune.oslo.origo.maskinporten

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.cert.CertificateException

class MaskinportenFileKeystoreKonfigurasjon(
    override val issuer: String,
    override val audience: String,
    override val tokenEndpoint: String,
    override val consumerOrganization: String,
    override val keyId: String,
    val keystoreFilepath: String,
    val keystorePassword: String,
    val keystoreAlias: String,
    val keystoreAliasPassword: String,
    val keystoreType: KeystoreType = KeystoreType.JKS,
    ) : MaskinportenKonfigurasjon {

    init {
        check(issuer.isNotEmpty())
        check(audience.isNotEmpty())
        check(tokenEndpoint.isNotEmpty())
        check(consumerOrganization.isNotEmpty())
        check(keystoreFilepath.isNotEmpty())
        check(keystorePassword.isNotEmpty())
        check(keystoreAlias.isNotEmpty())
        check(keystoreAliasPassword.isNotEmpty())
        if (!File(keystoreFilepath).exists()) {
            throw FileNotFoundException("Keystore ikke tilgjengelig på angitt filbane: ${keystoreFilepath}")
        }
    }

    override val privateKey: PrivateKey by lazy { loadPrivateKey() }

    private fun loadPrivateKey(): PrivateKey {
        val keyStore = getLoadedKeystore()
        log.debug("Keystore provider: ${keyStore.provider}")
        log.debug("Keystore type: ${keyStore.type}")
        log.debug("Aliases found in keystore: ${keyStore.aliases().toList()}")
        log.debug("Is $keystoreAlias found in keystore?: ${keyStore.isKeyEntry(keystoreAlias)}")

        return keyStore.getKey(keystoreAlias, keystoreAliasPassword.toCharArray()) as PrivateKey
    }

    private fun getLoadedKeystore(): KeyStore {
        try {
            val inputStream: InputStream = File(keystoreFilepath).inputStream()
            val keyStore = KeyStore.getInstance(keystoreType.storeValue)
            keyStore.load(inputStream, keystorePassword.toCharArray())
            return keyStore
        } catch (ex: Exception) {
            when (ex) {
                is FileNotFoundException, is SecurityException -> {
                    log.error("Kunne ikke finne eller lese keystore: {}", ex.stackTrace)
                    throw ex
                }

                is IOException, is NoSuchAlgorithmException, is CertificateException -> {
                    log.error("Kunne ikke håndtere keystore for maskinporten: {}", ex.stackTrace)
                    throw ex
                }

                else -> throw ex
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenKonfigurasjon::class.java)
    }

}
