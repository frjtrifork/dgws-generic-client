#dgws-generic-client

Simpel DGWS klient der kan wrappe en XML payload i en korrekt DGWS Soap Envelope og skyde kaldet afsted mod en DGWS.

Der er lavet en WSClient klasse med en main som er rimelig nem at gå til.

##Konfiguration

WSClient indlæser konfiguration fra flere kilder:
 - Benyt -Dpropertynavn=value for at overskrive værdier indlæst fra properties filerne
 - Opret en properties fil og skriv de nøgler du vil overskrive deri, og udpeg filen med -Dconfig=minconfig.properties
 - Opret en config.properties i dit 'working dir' og overskriv f.eks. 'serviceurl' nøglen deri
 - Default konfigurationen findes i src/main/resources/config.properties. Undlad at rette i den fil!, benyt en af de ovenstående muligheder i stedet.
 - For at udpege XML payloaden kan man angive -Dxmlfile=payload.xml

##Kørsel
Klienten kan køres fra kommandolinjen vha Maven:

    mvn exec:java -Dconfig=src/test/resources/getVaccinationCard.properties -Dxmlfile=src/test/resources/getVaccinationCardRequest.xml

eller benyt profilen der svarer til ovenstående:

    mvn -P getVaccinationCard

Man kan desuden bygge en jar med alle dependencies indbygget - som man kan tage med sig på systemer hvor der ikke nødvendigvis er maven eller github adgang:

Kør ```mvn package``` på udviklermaskine og gem dgws-generic-client/target/dgws-generic-client-1.0-SNAPSHOT.one-jar.jar til senere brug.

Kør java -jar med de rette systemproperties:

    java -Dconfig=getVaccinationCard.properties -Dxmlfile=getVaccinationCardRequest.xml -jar dgws-generic-client-1.0-SNAPSHOT.one-jar.jar


Alternativt kan man køre toolet inde fra sin IDE:

- Højreklik på WSClient og vælg run - lav evt. flere konfigurationer til de forskellige sæt af -Dconfig -Dxmlfile systemproperties.

###I produktion

Løb igennem alle parametre og se de står korrekt til produktion, det som minumum skal ændres er:
```
sosi.test.federation=false
sosi.sts.url=korrekt url
sosi.careprovider.name=Skal stemme med dem i certifikatet
sosi.careprovider.cvr=Skal stemme med dem i certifikatet
```

##Eksempler

I src/test/resources findes der konfigurationer der kan kalde DDV getVaccinationCard, Bemyndigelse hentMetadata og Bemyndigelse indlæsMetadata.

For at ændre de endpoints/soapactions der benyttes skal man rette i properties filen.

Resten håndteres af WSClient - (husk selv at whiteliste de CVR der benyttes hvis servicen kræver det - dette er servicespecifikt og kan ikke håndteres af WSClient)
    - Default CVR er angivet i src/main/resources/config.properties i nøglen 'sdsd.org.using.id.value'. Ønsker man at ændre CVR nummeret kan det gøres via ```-Dsdsd.org.using.id.value=25520041``` f.eks. - eller en af de andre metoder beskrevet under Konfiguration.

For at hente vaccinationskort fra DDV specificeres disse VM options til com.trifork.dgws.client.WSClient:
   ```-Dconfig=getVaccinationCard.properties -Dxmlfile=getVaccinationCardRequest.xml```

For at hente metadata fra Bemyndigelse:
   ```-Dxmlfile=hentMetadataRequest.xml -Dconfig=hentmetadata.properties```

For at indlæse metadata til Bemyndigelse:
   ```-Dxmlfile=indlaesmetadata.xml -Dconfig=indlaesmetadata.properties```

##Properties

Man kan overskrive følgene parametre i sin properties fil.
```
# Endpoint
serviceurl=https://udv1.vaccinationsregister.dk/ws/vaccinationsService
soapaction=http://vaccinationsregister.dk/schemas/2010/07/01#GetVaccinationCard

# Keystore and vault config
# Can point to a file using file:/// syntax
keystore.path=classpath:validMocesVault.jks
keystore.password=Test1234
keystore.alias=sosi:alias_system

# Test federation must be false if used in production environtment
sosi.test.federation=true
sosi.sts.url=http://pan.certifikat.dk/sts/services/SecurityTokenService
# Careprovider must match the certificate
sosi.careprovider.name=TRIFORK SERVICES A/S
sosi.careprovider.cvr=25520041

# moces = medarbejder certificat
# voces = virksomheds certificat
sosi.certificate.type=moces
sosi.system.name=SOSITEST

# Whitelisting header values
whitelisting.header=true
sdsd.system.owner.name=Trifork
sdsd.system.name=DGWS Client
sdsd.system.version=1.0
sdsd.org.responsible.name=Trifork
sdsd.org.using.name=Trifork
sdsd.org.using.id.name.format=medcom:cvrnumber
sdsd.org.using.id.value=25520041
sdsd.requested.role=L\u00E6ge
```
