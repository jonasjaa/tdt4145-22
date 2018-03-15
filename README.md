# Gruppe 22 TDT4145 - Databaseprosjekt

Github: git@github.com:jonasjaa/tdt4145-22.git
MySQLdatabase: jonasjaa_prosjektdb

# Beskrivelse av de fem kravene

## Registrere apparater, øvelser og treningsøkter med tilhørende data. 

Forklaring: Apparater, øvelser og treningsøkter er alle i hver sin egen tabell, slik at de er uavhengige av hverandre. Dette gjør at apparater, øvelser og treningsøkter kan legges til uten noe særlig redundans. 

## Få opp informasjon om et antall n sist gjennomførte treningsøkter med notater, der n spesifiseres av brukeren. 

Forklaring: Hver treningsøkt har et øktnummer som vil stigende, slik at det enkelt kan hentes ut den siste øktene.

## For hver enkelt øvelse skal det være mulig å se en resultatlogg i et gitt tidsintervall spesifisert av brukeren. 

Forklaring: Her hentes det ut alle treningsøkter i det gitte tidsintervallet gitt av brukeren. I dette tidsintervallet vil brukeren få opp hvilke øvelser som har blitt utført, hvor mange sett som ble gjort og hvor mange kilo som ble løftet for hver øvelse.

## Lage øvelsegrupper og finne øvelser som er i samme gruppe. 

Forklaring: Når man oppretter øvelsesgruppene vil man i tabellen “ØvelseIØvelsegruppe” få en oversikt over hvilke øvelser som hører til hvilke øvelsesgrupper. Ved å hente ut øvelser med samme gruppenavn eller gruppenummer fra “Øvelsesgruppe”-tabellen , vil brukeren kunne se hvilke øvelser som er i samme gruppe.

## Et valgfritt use case som dere selv bestemmer - Personlig highscore for apparatøvelser.

Forklaring: For å hente ut personlig highscore på en apparatøvelse så kan man hente ut alle treningsøktene, og for hver øvelse så henter man ut maksimalt antall kilo løftet.