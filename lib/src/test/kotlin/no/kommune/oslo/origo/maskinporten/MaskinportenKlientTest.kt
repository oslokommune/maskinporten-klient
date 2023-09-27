package no.kommune.oslo.origo.maskinporten

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class MaskinportenKlientTest {

    private val mockWebServer = MockWebServer()

    @BeforeEach
    fun setUp() {
        mockWebServer.start(MaskinportenTestUtils.TOKEN_ENDPOINT_PORT)
    }
    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `Skal returnere MaskinportenTokenWrapper dersom Maskinporten returnerer gyldig response`() {
        val token = MaskinportenTokenWrapper("tokentoken", "dummyType", "3600","dummyScope")
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(200)
            setBody(jacksonObjectMapper().writeValueAsString(token))
        })
        val klient = MaskinportenKlient(lagMaskinportenKonfigurasjon())
        val tokenWrapper = klient.hentAccessTokenFraMaskinporten(setOf("dummy:scope"))
        assert(tokenWrapper.access_token.isNotEmpty())
    }

    @Test
    fun `Skal returnere MaskinportenKlientException dersom Maskinporten returnerer feilkode med uhåndtert melding`(){
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            val feilmelding = MaskinportenError("invalid_grant", "Genrisk feilmelding")
            setBody(jacksonObjectMapper().writeValueAsString(feilmelding))
        })
        val klient = MaskinportenKlient(lagMaskinportenKonfigurasjon())
        assertThrows<MaskinportenKlientException> {
            klient.hentAccessTokenFraMaskinporten(setOf("dummy:scope"))
        }

    }

    @Test
    fun `Skal returnere UtgåttNøkkelForKlientException dersom Maskinporten returnerer feilmelding med utgått nøkkel`() {
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            val feilmelding = MaskinportenError("invalid_grant", "Invalid assertion. Client authentication failed. Expired key")
            setBody(jacksonObjectMapper().writeValueAsString(feilmelding))
        })
        val klient = MaskinportenKlient(lagMaskinportenKonfigurasjon())
        assertThrows<UtgåttNøkkelForKlientException> {
            klient.hentAccessTokenFraMaskinporten(setOf("dummy:scope"))
        }

    }


    @Test
    fun `Skal returnere UkjentKidForKlientException dersom Maskinporten returnerer feilmelding med ukjent KID`() {
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(400)
            val feilmelding = MaskinportenError("invalid_grant", "Invalid assertion. Client authentication failed. Unknown key identifier (kid) for client")
            setBody(jacksonObjectMapper().writeValueAsString(feilmelding))
        })
        val klient = MaskinportenKlient(lagMaskinportenKonfigurasjon())
        assertThrows<UkjentKidForKlientException> {
            klient.hentAccessTokenFraMaskinporten(setOf("dummy:scope"))
        }

    }

    fun lagMaskinportenKonfigurasjon() : MaskinportenKonfigurasjon {
        return MaskinportenFileKeystoreKonfigurasjon(
            MaskinportenTestUtils.issuer,
            MaskinportenTestUtils.audience,
            MaskinportenTestUtils.tokenEndpoint,
            MaskinportenTestUtils.consumer_org,
            MaskinportenTestUtils.keyId, MaskinportenTestUtils.keystoreFilePath,
            MaskinportenTestUtils.keystorePassord,
            MaskinportenTestUtils.keystoreAlias,
            MaskinportenTestUtils.keystoreAliasPassword,
            KeystoreType.JKS)
    }

}
