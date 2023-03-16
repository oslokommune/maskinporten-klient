package no.kommune.oslo.automatiserteprosesser.maskinporten

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.regions.Region

import java.io.FileNotFoundException

class MaskinportenJWTGeneratorTest {

    lateinit var maskinportenJWTGenerator: MaskinportenJWTGenerator

    @BeforeEach
    fun setup() {
        val maskinportenKonfigurasjon = MaskinportenFileKeystoreKonfigurasjon(
            MaskinportenTestUtils.issuer,
            MaskinportenTestUtils.audience,
            MaskinportenTestUtils.tokenEndpoint,
            MaskinportenTestUtils.consumer_org,
            MaskinportenTestUtils.keyId,
            MaskinportenTestUtils.keystoreFilePath,
            MaskinportenTestUtils.keystorePassord,
            MaskinportenTestUtils.keystoreAlias,
            MaskinportenTestUtils.keystoreAliasPassword,
            KeystoreType.JKS
        )
        maskinportenJWTGenerator = MaskinportenJWTGenerator(maskinportenKonfigurasjon)
    }

    @Test
    fun `MaskinportenJWTGenerator genererer signedJWT gitt påkrevde parametre`() {
        val tokenBase64 =
            maskinportenJWTGenerator.genererMaskinportenJWT(setOf("test", "test1", "test2"))
        Assertions.assertNotNull(tokenBase64)
        val parsedToken = JWTParser.parse(tokenBase64)
        Assertions.assertTrue(parsedToken is SignedJWT, "Forventet SignedJWT")

    }

    @Test
    fun `MaskinportenJWTGenerator generer token med angitte verdier`() {
        //TODO: Legg inn verifisering på claims
        val token = maskinportenJWTGenerator.genererMaskinportenJWT(setOf("test", "test1", "test2"))
        val parsed = JWTParser.parse(token)
        val payload = parsed.jwtClaimsSet
        val toJSONObject = payload.toJSONObject()
        println(toJSONObject)

    }

    @Test
    fun `Maskinporten konfigurasjon feiler dersom påkrevde felter mangler - keystore som fil`() {
        Assertions.assertThrows(IllegalStateException::class.java, {
            val mangledeParameter = MaskinportenFileKeystoreKonfigurasjon(
                "",
                "",
                "abbababa",
                "949345323",
                "key123",
                "ingenfilher.txt",
                "password",
                "selfsigned",
                "password",
                KeystoreType.JKS
            )
        }, "Forventet IllegalArgumentException ved ikke oppfylte paramtre i konfigurasjonen")
    }


    @Test
    fun `Maskinporten konfigurasjon feiler dersom påkrevde felter mangler - keystore fra parameter store`() {
        Assertions.assertThrows(IllegalStateException::class.java, {
            val mangledeParameter = MaskinportenAwsSsmKonfigurasjon(
                "",
                "",
                "abbababa",
                "949345323",
                Region.EU_NORTH_1,
            )
        }, "Forventet IllegalArgumentException ved ikke oppfylte paramtre i konfigurasjonen")
    }

    @Test
    fun `Maskinporten konfigurasjon feiler dersom keystore ikke finnes på angitt path`() {
        Assertions.assertThrows(FileNotFoundException::class.java, {
            val feilendeKeystore = MaskinportenFileKeystoreKonfigurasjon(
                "testiss",
                "testaud",
                "abbababa",
                "949345323",
                "key123",
                "ingenfilher.txt",
                "password",
                "selfsigned",
                "password",
                KeystoreType.JKS
            )
        }, "Forventet FileNotFoundException ved manglende Keystore")
    }
}
