package no.kommune.oslo.redusertoppholdsbetaling.integrasjoner.maskinporten

data class MaskinportenTokenWrapper(val access_token: String,
                                    val token_type: String,
                                    val expires_in: String,
                                    val scope: String )