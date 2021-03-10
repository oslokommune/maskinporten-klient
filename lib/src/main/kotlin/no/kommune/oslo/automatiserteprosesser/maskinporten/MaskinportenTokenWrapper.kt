package no.kommune.oslo.automatiserteprosesser.maskinporten

data class MaskinportenTokenWrapper(val access_token: String,
                                    val token_type: String,
                                    val expires_in: String,
                                    val scope: String )