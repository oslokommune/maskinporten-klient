package no.kommune.oslo.redusertoppholdsbetaling.integrasjoner.maskinporten

import no.kommune.oslo.redusertoppholdsbetaling.integrasjoner.maskinportenHttpClient.MaskinportenKonfigurasjonLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class MaskinportenKonfigurasjon(val issuer : String,
                                val audience : String,
                                val tokenEndpoint : String,
                                val consumerOrganization : String,
                                val keyId : String,
                                val keystoreFilepath : String,
                                val keystorePassword : String,
                                val keystoreAlias : String,
                                val keystoreAliasPassword : String

) {

    init {
        if(issuer.isNullOrEmpty() || audience.isNullOrEmpty() || tokenEndpoint.isNullOrEmpty()
            || consumerOrganization.isNullOrEmpty() || keystoreFilepath.isNullOrEmpty()
            || keystorePassword.isNullOrEmpty() ||keystoreAlias.isNullOrEmpty()
            ||keystoreAliasPassword.isNullOrEmpty()){
            throw IllegalArgumentException("MaskinportenKonfig mangler påkrevde konfigurasjonsverdier")
        }
        if(!File(keystoreFilepath).exists()){
            throw FileNotFoundException("Keystore ikke tilgjengelig på angitt filbane: ${keystoreFilepath}")
        }
    }

    val certificate: X509Certificate by lazy { loadCertificate() }
    val privateKey: PrivateKey by lazy { loadPrivateKey() }

    private fun loadPrivateKey(): PrivateKey {
        val keyStore = getLoadedKeystore()
        return keyStore.getKey(keystoreAlias, keystoreAliasPassword.toCharArray()) as PrivateKey
    }

    private fun loadCertificate(): X509Certificate {
        val keyStore = getLoadedKeystore()
        return keyStore.getCertificate(keystoreAlias) as X509Certificate
    }

    private fun getLoadedKeystore(): KeyStore {
        try {
            val inputStream: InputStream = File(keystoreFilepath).inputStream()
            val keyStore = KeyStore.getInstance("JKS")
            keyStore.load(inputStream, keystorePassword.toCharArray())
            return keyStore
        } catch (ex: Exception){
            when(ex) {
                is IOException, is NoSuchAlgorithmException, is CertificateException -> {
                    log.error("Kunne ikke håndtere keystore for maskinporten: {}", ex.stackTrace)
                    throw ex
                }
                is FileNotFoundException, is SecurityException -> {
                    log.error("Kunne ikke finne eller lese keystore: {}", ex.stackTrace)
                    throw ex
                }
                else -> throw ex
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenKonfigurasjonLoader::class.java)
    }

}

