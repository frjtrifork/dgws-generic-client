dgws-testclient
===============

Simpel DGWS testklient der kan wrappe en XML payload i en korrekt DGWS Soap Envelope og skyde kaldet afsted mod en DGWS.

Der er lavet en WSClient klasse med en main som er rimelig nem at gå til.

Konfiguration
=============
WSClient indlæser konfiguration fra flere kilder:
 - Benyt -Dpropertynavn=value for at overskrive værdier indlæst fra properties filerne
 - Opret en properties fil og skriv de nøgler du vil overskrive deri, og udpeg filen med -Dconfig=minconfig.properties
 - Opret en config.properties i dit 'working dir' og overskriv f.eks. 'serviceurl' nøglen deri
 - Default konfigurationen findes i src/main/resources/config.properties. Undlad at rette i den fil!, benyt en af de ovenstående muligheder i stedet.
 - For at udpege XML payloaden kan man angive -Dxmlfile=payload.xml

Kørsel
======
Det nemmeste er pt. at køre toolet inde fra sin IDE:
    - Højreklik på WSClient og vælg run - lav evt. flere konfigurationer.

    I src/test/resources findes tre sæt konfigurationer der kan kalde DDV getVaccinationCard, Bemyndigelse hentMetadata og Bemyndigelse indlæsMetadata.


For at ændre de endpoints/soapactions der benyttes skal man rette i properties filen, og for at rette i payload retter man i XML filen.
Resten klarer WSClient - (husk dog selv at whiteliste de CVR der benyttes)
    - Default CVR er angivet i src/main/resources/config.properties i nøglen 'sdsd.org.using.id.value'. Ønsker man at ændre CVR nummeret kan det gøres via -Dsdsd.org.using.id.value=25520041 f.eks. - eller en af de andre metoder beskrevet under Konfiguration.

Eksempler
=========
For at hente vaccinationskort fra DDV specificeres disse VM options til com.trifork.dgws.testclient.WSClient:
   -Dconfig=getVaccinationCard.properties -Dxmlfile=getVaccinationCardRequest.xml

For at hente metadata fra Bemyndigelse:
   -Dxmlfile=hentMetadataRequest.xml -Dconfig=hentmetadata.properties

For at indlæse metadata til Bemyndigelse:
   -Dxmlfile=indlaesmetadata.xml -Dconfig=indlaesmetadata.properties


