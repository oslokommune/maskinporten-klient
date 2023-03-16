# maskinporten-klient

Maskinportenklient er en klient for å hente ut token fra Maskinporten. 
For å kunne hente ut token fra maskinporten, må  man ha nøkler for en klient av Maskinporten. 

Man kan bruke dataplatformen for å kunne sette opp klient og opprette nøkler. 

## Konfigurasjon
For å kunne bruke maskinporten-klient, må følgende oppgis: 
* Issuer: Issuer er klient-id for maskinporten-klienten.
* Audience: Audience er url til maskinporten (f.eks https://test.maskinporten.no).
* Token-endpoint: Token-endpoint er url til token-endpointet i maskinporten (f.eks https://test.maskinporten.no/token).
* Consumerorganisasjon: Consumerorganisasjon er organisasjonsnummeret til organisasjonen som skal bruke maskinporten-klienten. Det er typisk: 991825827 (Oslo kommune).

Maskinporten-klienten kan konfigureres på to måter:

### Nøkler i keystore-fil
Dersom du har nøkler til maskinporten lagret i en fil, bruk MaskinportenFileKeyStoreKonfiguration for å sette opp maskinporten-klient. 

Denne varianten forutsetter at følgende oppgis: 
* keyId: keyId er id-en til nøkkelen som skal brukes til å signere token-forespørselen.
* keystoreFilepath: keystoreFilepath er filstien til keystore-filen.
* keystorePassword: keystorePassword er passordet til keystore-filen.
* keystoreAlias: keystoreAlias er aliaset til nøkkelen som skal brukes til å signere token-forespørselen. Typisk er dette "client-key".
* keystoreAliasPassword: keystoreAliasPassword er passordet til nøkkelen som skal brukes til å signere token-forespørselen. I dataplatformen er denne lik keystorePassword.
* keyStoreType: keyStoreType er typen på keystore-filen. Typisk er dette "JKS" eller "P12". 

### Nøkler i parameter store
Dersom du har satt dataplatformen til å automatisk rotere nøkler og legge disse inn i parameter store, kan du bruke MaskinportenAwsSsmKonfiguration for å sette opp maskinporten-klient.

Denne varianten forutsetter at følgende oppgis:
* awsRegion: awsRegion er regionen til parameter store.

I tillegg må miljøet settes opp for å kunne koble seg til parameter store. Se https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html for detaljer. 

NB: Dataplatformen støtter fem minutter overlapp mellom gammel og ny nøkkel. Denne konfigurasjonen har per d.d ikke caching av nøkler fra parameter store, så ny klient må opprettes per forespørsel eller minst hver 5 minutt. Nøkler roteres hver ukedag. 