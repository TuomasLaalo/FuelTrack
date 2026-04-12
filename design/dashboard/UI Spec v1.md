# FuelTrack UI Spec v1

## 1. Design direction

FuelTrackin tavoitetuntuma:

- soft
- calm
- premium utility
- editorial but practical
- warm and slightly elevated
- not default Material demo
- not dense dashboard grid

Päätös:
- käytetään pehmeää pinnan erottelua
- käytetään hillittyä vihreää korostuksena
- käytetään typografiaa identiteetin rakentamiseen
- vältetään raskaita rajoja, kovia varjoja ja liian teknistä fiilistä

---

## 2. Typography

### Font families

- **Headline:** Manrope
- **Body:** Inter
- **Label:** Inter

### Role mapping

**Manrope**
- screen titles
- hero metric values
- section titles
- important card titles
- key numeric emphasis

**Inter**
- body text
- metadata
- labels
- button text
- input labels
- helper text

### Suggested Compose typography roles

Tämä ei ole vielä pakollinen lopullinen `Typography`, mutta hyvä lähtö:

- `displayLarge` → hero metric
- `headlineLarge` → screen title
- `titleLarge` → section title / large card title
- `titleMedium` → card title
- `bodyLarge` / `bodyMedium` → main content
- `labelLarge` / `labelMedium` / `labelSmall` → buttons, metadata, small labels

### Styling intent

- hero numerot: isot, tiukat, vahvat
- section titles: selkeät, painavat, mutta eivät aggressiiviset
- metadata: pieni, rauhallinen, usein uppercase tai hieman trackingia
- nappiteksti: selkeä, ei liian raskas

### Typography guidance

- älä käytä liikaa all capsia
- käytä all capsia lähinnä pienissä meta-labeleissa kuten:
  - ACTIVE VEHICLE
  - QUANTITY
  - PRICE / UNIT

---

## 3. Color system

Tuosta HTML:stä saadaan hyvä perusrunko.

### Core tokens

- **Background:** `#FCF9F4`
- **Page Background / Warm Surface:** `#F6F3EE`
- **Surface Variant:** `#E5E2DD`
- **Primary:** `#006C4D`
- **Primary Container:** `#58B38D`
- **On Primary:** `#FFFFFF`
- **Text Primary / On Surface:** `#1C1C19`
- **Text Secondary / On Surface Variant:** `#3E4943`
- **Outline Variant:** `#BEC9C1`
- **Soft Mint Highlight:** `#D1FAE5`
- **Soft Green Badge:** `#D0E8DD`

### Practical app-level roles

Suosittelen käyttämään käytännössä näitä rooleja:

- `AppBackground = #F6F3EE`
- `ScreenSurface = #FCF9F4`
- `CardSurface = #FFFFFF` tai hyvin vaalea lämmin pinta
- `CardSurfaceMuted = #F0EDE9`
- `HeroPrimary = #006C4D`
- `HeroSecondary = #58B38D`
- `TextPrimary = #1C1C19`
- `TextSecondary = #3E4943`
- `BorderSoft = #BEC9C1`
- `AccentSoft = #D1FAE5`

### Color rules

- vihreää käytetään vain tärkeissä kohdissa
- kaikki kortit eivät saa olla vihreitä
- tausta pysyy lämpimänä ja rauhallisena
- tekstin kontrasti säilytetään hyvänä
- dividerit ja rajat pidetään hyvin kevyinä

---

## 4. Shape language

HTML viittaa vahvasti pehmeään kulmakieleen.

### Shape rules

- **Hero card:** large rounded, noin `24dp`
- **Primary content cards:** `20dp`
- **Stat cards:** `18–20dp`
- **Icon containers:** `12–16dp`
- **Buttons pill-like:** `999dp` tai hyvin pyöreä shape
- **FAB:** rounded rectangle, ei pakollisesti täysin pyöreä

### Recommended Compose mapping

- `extraLarge` = 24.dp
- `large` = 20.dp
- `medium` = 16.dp
- `small` = 12.dp

### Intent

- vältä kovia suorakulmioita
- vältä liian pieniä radiuksia
- hero-elementit saavat olla selvästi pyöreämpiä kuin utilitaristiset elementit

---

## 5. Depth and elevation

Tämä on yksi tärkeimmistä puuttuvista asioista Android-versiossa.

### Depth principle

- tonal separation first
- shadow second

### Elevation tiers

**Flat**
- section backgrounds
- embedded content
- app background transitions

**Soft**
- quick stat cards
- refuel detail card
- action/info card

**Raised**
- FAB
- hero card, mutta enemmän värin/gradientin kuin varjon kautta
- ehkä primary floating elements

### Rules

- ei tummia, kovia varjoja
- ei voimakasta “drop shadow app” -fiilistä
- käytä mieluummin:
  - vaalea surface
  - kevyt border
  - pieni elevation
  - pehmeä shadow

### Compose guidance

Material-elevation voi riittää, mutta sitä pitää käyttää hillitysti:
- small cards: 1–3dp
- important cards: 4–6dp
- FAB: 6–10dp
- hero: usein gradientti + pieni elevation riittää

---

## 6. Spacing system

HTML:ssä näkyy hyvin rauhallinen, johdonmukainen rytmi.

### Base spacing scale

- 4dp
- 8dp
- 12dp
- 16dp
- 20dp
- 24dp
- 32dp

### Usage guidance

- screen horizontal padding: `20dp`
- section spacing: `20–24dp`
- card inner padding: `16dp` tai `20dp`
- hero card padding: `20dp`
- small gaps inside compact cards: `8dp`
- regular content gaps: `12dp`
- section header to card gap: `12dp`

### Editorial rhythm

- isot blokit saavat hengittää
- metadata ei saa olla liian kaukana pääarvosta
- kaikki ei saa olla tasaisesti ruudukossa ilman painotusta

---

## 7. Shared components

### A. App header

Sisältää:
- vasen icon action
- FuelTrack title
- profile/avatar tai oikean reunan utility action

Tyyli:
- kevyt
- sticky
- hieman blur/pintaa
- ei raskasta toolbar-fiilistä

Compose intent:
- ei perus stock TopAppBar sellaisenaan
- rakennetaan hieman custom header

---

### B. Vehicle context block

Sisältää:
- pieni label: ACTIVE VEHICLE
- auton nimi
- fuel type badge

Tyyli:
- kevyt
- editorial
- toimii dashboardin johdantona

---

### C. Hero metric card

Sisältää:
- pieni label
- iso arvo
- yksikkö
- trendi-pill/badge
- taustalla hienovarainen gradientti / glow

Tyyli:
- dashboardin tärkein visuaalinen elementti
- selvästi isompi kuin muut kortit
- ei liian korkea
- toimii “cover momentina”

Tärkeää:
- yksi selvä pääviesti
- ei liikaa kilpailevaa sisältöä hero-kortissa

---

### D. Primary trip action

Sisältää:
- ikoni
- teksti `Start Trip`

Tyyli:
- pehmeä pill shape
- vihreä, mutta kevyempi kuin hero
- ei liian suuri CTA
- selkeä mutta elegantti

Päätös:
- tämä saa olla kapselimainen
- näyttää enemmän tuotetoiminnolta kuin default buttonilta

---

### E. Quick stat card

Sisältää:
- ikoni
- pieni label
- arvo

Tyyli:
- vaalea pinta
- pehmeä shadow
- kevyt border
- hyvin helposti monistettava

Päätös:
- quick stat cards ovat litteämpiä kuin hero
- näitä voi käyttää myös Stats- ja Trip-näkymissä

---

### F. Refuel detail card

Sisältää:
- aseman nimi
- päivämäärä
- kokonaissumma
- alarivillä quantity ja price/unit

Tyyli:
- tärkeä sisältökortti
- hieman rikkaampi sisäinen rakenne
- yläosa summary, alaosa structured detail

Päätös:
- tästä tulee yksi sovelluksen tärkeimmistä reusable-komponenteista

---

### G. Info / action card

Esim:
- Compare Fuel Prices

Sisältää:
- ikoni
- otsikko
- tuki-/seliteteksti
- trailing chevron

Tyyli:
- kevyt, rauhallinen, hyödyllinen
- ei paina liikaa dashboardia alas

---

### H. FAB

Tyyli:
- pieni
- siisti
- rounded rectangle
- selkeä vihreä
- näkyvä mutta ei massiivinen

Muistutus UX-linjasta:
- ensimmäisellä käynnistyksellä voidaan näyttää kevyt onboarding-hintti FABiin

---

### I. Bottom navigation

Tässä HTML:ssä nav on yksinkertainen ja hyvä lähtökohta.

Päätös:
- nav pidetään suhteellisen simppelinä
- ei liian koristeellinen
- pieni pehmeys sallittu
- active state näkyy selvemmin kuin inactive

Tyyli:
- lämmin vaalea pinta
- kevyt top border/shadow
- aktiivinen ikoni täytetty tai vahvempi
- labelit pienet ja siistit

---

## 8. Dashboard hierarchy

Dashboardin lukujärjestys:

1. app identity / header
2. active vehicle
3. hero consumption metric
4. primary trip action
5. quick stats
6. latest refuel
7. optional utility actions

### Decision

Dashboard ei ole puhdas “control center”, eikä pelkkä “cover page”.
Sen pitää olla:
- **personal dashboard first**
- **daily utility second**

Eli vähän editorial opening + käytännöllinen data.

---

## 9. Screen blueprint: Dashboard

### Recommended section order

1. Header
2. Vehicle context
3. Hero metric card
4. Start Trip action
5. Quick Stats
6. Latest Refuel
7. Utility action card
8. FAB
9. Bottom nav

### Layout notes

- sisältö keskitetään maltilliseen leveyteen
- käytetään paljon täysleveitä kortteja
- quick stats voi olla 2-column grid
- Latest Refuel on full-width detail card
- utility actions tulevat alemmaksi eivätkä kilpaile hero-kortin kanssa

---

## 10. Reuse across next screens

Näistä komponenteista rakennetaan seuraavat ruudut:

### Fuel History

- app header
- section title
- refuel detail card listana
- filter/sort control mahdollisesti myöhemmin

### Add Fuel Entry

- app header
- soft input fields
- primary submit button
- maybe segmented controls / fuel type / quick selectors

### Trip List

- section title
- trip summary cards
- same stat and metadata language

### Trip Summary

- hero-style metric section
- summary stat cards
- structured detail sections

---

## 11. Compose implementation mapping

### Theme

Tee vähintään nämä:

- `Color.kt`
- `Type.kt`
- `Shape.kt`
- `Spacing.kt` tai design constants -tiedosto
- mahdollinen `Elevation.kt` / design tokens object

### Suggested theme pieces

**ColorScheme**
- mapataan ydinvärit
- tarvittaessa custom extension colors additional roleille kuten:
  - `cardSurfaceMuted`
  - `heroGradientStart`
  - `heroGradientEnd`
  - `softAccent`
  - `borderSoft`

**Typography**
- Manrope headline-rooleihin
- Inter body/label-rooleihin

**Shapes**
- medium, large, extraLarge uudelleen määritelty pehmeämmiksi

**Reusable composables**
- `FuelTrackHeader()`
- `VehicleContextBlock()`
- `HeroMetricCard()`
- `PrimaryPillButton()`
- `QuickStatCard()`
- `RefuelSummaryCard()`
- `InfoActionCard()`
- `FuelTrackBottomNav()`

---

## 12. What to preserve from the Stitch reference

Säilytä:
- Manrope + Inter -pari
- lämmin tausta
- vihreän hillitty käyttö
- pehmeät radiukset
- hero-kortin vahva fokus
- quick stat -korttien keveys
- refuel-kortin rakenteellinen selkeys
- yksinkertainen bottom nav
- kevyt, premium utility -fiilis

Älä kopioi mekaanisesti:
- jokaista Tailwind-luokkaa
- jokaista pikselimittaa
- jokaista varjoa 1:1
- jokaista web-layoutin ratkaisua

Androidissa tavoite on:
- **sama tunne**
- ei sama DOM-rakenne

---

## 13. Immediate implementation priorities

1. fontit käyttöön: Manrope + Inter
2. color tokens kuntoon
3. shapes pehmeämmiksi
4. hero card + quick stat card + refuel card reusableksi
5. dashboard päivitetään näillä
6. sitten Fuel History ja Add Fuel Entry

---

## 14. Tiivistetyt päätökset

- Headline font = **Manrope**
- Body/Label font = **Inter**
- Background = **warm off-white**
- Primary accent = **deep green**
- Shape language = **soft rounded**
- Depth = **subtle tonal layering**
- Hero = **main focal point**
- Quick stats = **lighter support cards**
- Refuel card = **primary detail pattern**
- Buttons = **pill-like primary, lighter secondary**
- Nav = **simple, lightly softened**
- Overall feel = **premium automotive utility, calm and buildable**