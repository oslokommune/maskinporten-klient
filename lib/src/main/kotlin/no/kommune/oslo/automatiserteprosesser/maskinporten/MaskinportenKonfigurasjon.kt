package no.kommune.oslo.automatiserteprosesser.maskinporten

import java.security.PrivateKey

interface MaskinportenKonfigurasjon {
    val issuer: String
    val audience: String
    val tokenEndpoint: String
    val consumerOrganization: String
    val privateKey: PrivateKey
    val keyId: String
}

enum class KeystoreType(val storeValue: String) {
    JKS("JKS"),
    P12("pkcs12")
}
