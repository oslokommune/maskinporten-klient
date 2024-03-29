package no.kommune.oslo.origo.maskinporten

class MaskinportenTestUtils {

    companion object {
        const val TOKEN_ENDPOINT_PORT = 9090
        const val issuer = "testIss"
        const val audience = "testAud"
        const val consumer_org = "949345323"
        const val keyId = "key123"
        const val tokenEndpoint = "http://localhost:${TOKEN_ENDPOINT_PORT}/token"
        const val keystorePassord = "password"
        const val keystoreAlias = "selfsigned"
        val keystoreFilePath = pathFraFil("selfsigned.jks")
        const val keystoreAliasPassword = "password"

        fun pathFraFil(filnavn : String ) = MaskinportenTestUtils::class.java.classLoader.getResource(filnavn).file
    }
}
