dgws-testclient
===============

Simpel DGWS testklient der kan wrappe en XML payload i en korrekt DGWS Soap Envelope og skyde kaldet afsted mod en DGWS.

Der er lavet en WSClient klasse med en main som er rimelig nem at gå til.
Den bruger konfiguration fra flere kilder - master konfigurationen er i src/main/resources/config.properties. Undlad at rette i den fil. Opret i stedet en anden properties fil og udpeg den vha. -Dconfig=my.properties.
For at udpege XML payloaden kan man angive -Dxmlfile=payload.xml

Usage:
Da OneJar jar'en pt ikke virker med Spring konfigurationen er det nemmeste at køre inde fra sin IDE.
Højreklik på WSClient og vælg run - lav evt. flere konfigurationer. I src/test/resources findes tre sæt konfigurationer der kan kalde DDV getVaccinationCard, Bemyndigelse hentMetadata og Bemyndigelse indlæsMetadata.
For at ændre de endpoints/soapactions der benyttes skal man rette i properties filen, og for at rette i payload retter man i XML filen. Resten klarer WSClient - (husk selv at whiteliste de CVR der benyttes - default står angivet i src/main/resources/config.properties i nøglen 'sdsd.org.using.id.value'. Ønsker man at ændre CVR nummeret skal det gøres i de filer man udpeger med -Dconfig= - man kan også placere en config.properties i 'working dir' og definere en sdsd.org.using.id.value deri som vil slå igennem for alle WSClient kald man udfører i den mappe).

For at hente vaccinationskort fra DDV specificeres disse VM options:
    -Dconfig=getVaccinationCard.properties -Dxmlfile=getVaccinationCardRequest.xml

For at hente metadata fra Bemyndigelse:
    -Dxmlfile=hentMetadataRequest.xml -Dconfig=hentmetadata.properties

For at indlæse metadata til Bemyndigelse:
    -Dxmlfile=indlaesmetadata.xml -Dconfig=indlaesmetadata.properties


