package no.kommune.oslo.redusertoppholdsbetaling.integrasjoner.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.Clock
import java.util.*

class MaskinportenJWTGenerator(val konfigurasjon : MaskinportenKonfigurasjon) {


    fun genererMaskinportenJWT(scopes: Set<String>): String {
        val signedJWT = SignedJWT(
            jwsHeader(konfigurasjon.certificate),
            claims(konfigurasjon, scopes)
        )
        signedJWT.sign(jwsSigner(konfigurasjon.privateKey))
        return signedJWT.serialize()
    }

    private fun claims(konfigurasjon: MaskinportenKonfigurasjon, scopes: Set<String>): JWTClaimsSet {
        val issueTime = Clock.systemUTC().millis()
        val expirationTime = issueTime + 120_000
        val claims = JWTClaimsSet.Builder()
            .audience(konfigurasjon.audience)
            .issuer(konfigurasjon.issuer)
            .claim("scope", scopes.joinToString(separator = " "))
            .claim("consumer_org", konfigurasjon.consumerOrganization)
            .jwtID(UUID.randomUUID().toString()) // Must be unique for each grant
            .issueTime(Date(issueTime)) // Use UTC time!
            .expirationTime(Date(expirationTime)) // Expiration time is 120 sec.
            .build();
        log.debug("Creating token with claims: [{}]", claims)
        return claims
    }

    private fun jwsHeader(certificate : X509Certificate): JWSHeader {
        val jwsHeader = JWSHeader.Builder(JWSAlgorithm.RS256)
            //.x509CertChain(listOf(Base64.encode(certificate.encoded))) // For å legge til x5c-element i JWT
            .keyID(konfigurasjon.keyId)
            .build()
        return jwsHeader
    }

    private fun jwsSigner(privateKey : PrivateKey): RSASSASigner {
        return RSASSASigner(privateKey)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenJWTGenerator::class.java)
    }
}