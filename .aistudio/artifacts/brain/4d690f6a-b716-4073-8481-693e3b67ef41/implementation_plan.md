# Urban Soltech: Community, Live Solar Rates & Sizing System

A clean, modern Android application for **Urban Soltech** combining a community platform for solar enthusiasts, an hourly live solar panel rate index, an advanced solar sizing and quotation engine with instant PDF export, an emergency direct helpline, and secure cloud authentication.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been confirmed and will guide the build:

- **Quotation Delivery**: Interactive visual quotation card within the app with instant one-tap PDF export using Android's native `PdfDocument` engine and file share sheet.
- **Community Media**: Support for both high-resolution photos and video clips with smooth in-app media playback and real-time community comment threads.
- **Visual Theme & Identity**: Deep industrial slate (`#0E131F`, `#1A2333`) accented with electric solar yellow (`#FFD700`, `#F59E0B`), featuring a custom lightning bolt emblem topped with "Urban Soltech".
- **Backend & Persistence**: Firebase Firestore integration for cloud-synchronized posts, comments, live market rate updates, and user quotation history, paired with an emergency dialer directly connected to `+92 333 6919922`.

---

## 1. Overview & Core Concept

**Urban Soltech** delivers a comprehensive solar energy platform for Pakistani homeowners, commercial clients, and solar installers.

- **Community Hub**: Solar owners and installers share photos and video walkthroughs of rooftop installations, net metering milestones, and inverter setups, engaging through comments and likes.
- **Live Solar Rate Index**: Real-time per-Watt market rates for Tier-1 solar panels (Longi Hi-MO, Jinko Tiger Neo, JA Solar, Canadian Solar) with hourly automated refresh, trend indicators, and market insights.
- **Solar Sizing & Quotation Calculator**: Takes monthly electricity units (kWh), inverter preference (On-grid, Off-grid, Hybrid), heavy loads (1.0, 1.5, 2.0 ton ACs), and household appliances (fans, TVs, refrigerators). Performs detailed load and irradiation modeling, yielding recommended system kW, panel count, battery storage (for off-grid/hybrid), itemized equipment pricing in PKR, and financial payback period (ROI in years).
- **Official PDF Quotation**: Generates a professional branded Urban Soltech quotation document exportable directly to device storage or messaging apps.
- **One-Tap Emergency Support**: Prominent emergency assistance button immediately invoking the Android dialer with `+92 333 6919922` to reach the Urban Soltech engineering helpdesk.
- **Cloud Security**: Authenticated user accounts safeguarding saved quotations, profile preferences, and community posts.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Onboarding & Auth Flow**:
   - Splash screen displaying the Urban Soltech lightning bolt emblem.
   - Clean authentication screen with Google Sign-In via Jetpack Credential Manager, ensuring secure account setup and cloud data persistence.
2. **Dashboard / Navigation**:
   - Bottom navigation bar with 4 core destinations: **Live Rates**, **Calculator**, **Community**, and **Support / Profile**.
3. **Live Solar Rates Screen**:
   - Current market benchmark (PKR/Watt) with a countdown timer to next hourly refresh and manual pull-to-refresh.
   - Detailed price breakdown across leading Tier-1 panel manufacturers (Longi N-Type, Jinko Tiger, JA Solar, Canadian Solar).
   - Market commentary and historical 24-hour rate trend chart.
4. **Solar Calculator Flow**:
   - **Step 1 - Energy Profile**: Monthly units consumed (kWh), grid connection type (On-grid, Off-grid, Hybrid).
   - **Step 2 - Appliance Inventory**: Air conditioners with tonnage selection (1.0, 1.5, 2.0 ton), fans, refrigerators, and TVs.
   - **Step 3 - Sizing Simulation**: Realistic calculation progress bar (1–2 seconds smooth animation) detailing system sizing calculations.
   - **Step 4 - Visual Quotation Card**: Recommended system capacity (e.g., 10.4 kW), panel count (e.g., 18x 585W bifacial), battery capacity (kWh Tubular/Lithium if Hybrid/Off-grid), itemized bills (panels, inverter, batteries, structure, AC/DC cabling, net metering setup, installation), total PKR cost, and estimated ROI payback period in years.
   - **Step 5 - PDF Export**: Generates a formatted PDF quotation with Urban Soltech branding for download or sharing.
5. **Community Feed Screen**:
   - Chronological and trending feed of solar installation photos and video clips.
   - Post creation modal with media picker, title, tags (e.g., "10kW Hybrid", "Net Metering Approved"), and system specs.
   - Inline video playback, post reactions, and nested comment thread sheets.
6. **Emergency Helpline**:
   - High-visibility action button accessible from the app bar and support tab.
   - Launches Android `ACTION_DIAL` with `+92 333 6919922` pre-filled for immediate customer assistance.

### Visual Identity & Theme

- **Aesthetic Direction**: High-tech Industrial Modern — dark slate surfaces conveying engineering precision, with electric yellow and warm solar amber accents representing clean solar energy.
- **Color Palette**:
  - `Background`: Deep Midnight Slate (`#0B0F19`)
  - `Surface / Card`: Dark Industrial Slate (`#141B2D`, `#1F293D`)
  - `Primary / Accent`: Electric Solar Yellow (`#FFD700`, `#FACC15`)
  - `Secondary Accent`: Solar Amber / Cyan (`#F59E0B`, `#06B6D4`)
  - `Text Primary`: Crisp White (`#F8FAFC`)
  - `Text Muted`: Slate Gray (`#94A3B8`)
  - `Emergency Crimson`: Vivid Red (`#EF4444`)
- **Typography & Hierarchy**: Clean, technical typography with bold numeric callouts for system kilowatt ratings, payback years, and market rates.

---

## 3. Key Product Decisions & Trade-Offs

- **Native Android PDF Generation (`android.graphics.pdf.PdfDocument`)**:
  - *Chosen Approach*: Native vector canvas drawing of the quotation onto a standard A4 page format, saved to app cache and shared via `FileProvider`.
  - *Why*: Instant generation with zero external heavy dependencies, zero network latency, and 100% offline reliability.
- **Accurate Solar Engineering Math**:
  - *Chosen Approach*: Solar sizing formulas standard to the Pakistani solar industry (daily solar insolation average 4.8–5.2 PSH, seasonal diversity factor 1.25x for peak summer AC loads, standard 580W–600W Tier-1 bifacial panels, battery sizing based on nighttime critical load backup).
  - *Why*: Provides practical, realistic quotations rather than arbitrary placeholders.
- **Live Hourly Rate Index Engine**:
  - *Chosen Approach*: Combines live Firestore rate stream with a timed hourly refresh scheduler and local fallback cache to ensure rates display seamlessly regardless of network connectivity.
- **Unified Media Playback**:
  - *Chosen Approach*: Smooth Compose media cards with video playback controls and photo pinch/zoom preview.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Urban Soltech Architecture                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                         Presentation Layer                          │   │
│   │                                                                     │   │
│   │  ┌────────────────┐  ┌────────────────┐  ┌───────────────────────┐  │   │
│   │  │   LiveRates    │  │ SolarCalc &    │  │ Community & Media     │  │   │
│   │  │   Dashboard    │  │ Quotation Card │  │ Feed + Comments       │  │   │
│   │  └───────┬────────┘  └───────┬────────┘  └───────────┬───────────┘  │   │
│   │          │                   │                       │              │   │
│   │          ▼                   ▼                       ▼              │   │
│   │  ┌────────────────┐  ┌────────────────┐  ┌───────────────────────┐  │   │
│   │  │ RatesViewModel │  │ CalcViewModel  │  │  CommunityViewModel   │  │   │
│   │  └───────┬────────┘  └───────┬────────┘  └───────────┬───────────┘  │   │
│   └──────────┼───────────────────┼───────────────────────┼──────────────┘   │
│              ▼                   ▼                       ▼                  │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                         Domain / Services                           │   │
│   │                                                                     │   │
│   │  ┌────────────────┐  ┌────────────────┐  ┌───────────────────────┐  │   │
│   │  │ SolarSizing    │  │ PdfQuotation   │  │ Emergency Dialer      │  │   │
│   │  │ Engine         │  │ Generator      │  │ Manager               │  │   │
│   │  └────────────────┘  └────────────────┘  └───────────────────────┘  │   │
│   └──────────────────────────────┬──────────────────────────────────────┘   │
│                                  ▼                                          │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                         Data / Repository Layer                     │   │
│   │                                                                     │   │
│   │  ┌────────────────┐  ┌────────────────┐  ┌───────────────────────┐  │   │
│   │  │ RatesRepo      │  │ QuotationRepo  │  │ CommunityRepo         │  │   │
│   │  └───────┬────────┘  └───────┬────────┘  └───────────┬───────────┘  │   │
│   └──────────┼───────────────────┼───────────────────────┼──────────────┘   │
│              ▼                   ▼                       ▼                  │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                   Cloud Firestore & Jetpack Auth                    │   │
│   │                                                                     │   │
│   │  • /market_rates/{rateId}     • /users/{uid}/quotes/{quoteId}       │   │
│   │  • /community_posts/{postId}  • /community_posts/{id}/comments/{cid}│   │
│   └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Models

1. **MarketRate**: `brand: String`, `modelType: String`, `pricePerWatt: Double`, `minWattage: Int`, `maxWattage: Int`, `trendDelta: Double`, `lastUpdated: Timestamp`
2. **SolarQuotation**: `id: String`, `userId: String`, `monthlyUnits: Int`, `inverterType: String` (On-grid/Off-grid/Hybrid), `systemKw: Double`, `panelCount: Int`, `panelWattage: Int`, `batterySpecs: String?`, `itemizedCosts: Map<String, Double>`, `totalCostPkr: Double`, `paybackYears: Double`, `annualGenerationKwh: Double`, `createdAt: Timestamp`
3. **CommunityPost**: `id: String`, `authorId: String`, `authorName: String`, `caption: String`, `mediaType: String` (IMAGE / VIDEO), `mediaUri: String`, `likesCount: Int`, `commentsCount: Int`, `systemTag: String`, `createdAt: Timestamp`
4. **PostComment**: `id: String`, `authorId: String`, `authorName: String`, `text: String`, `createdAt: Timestamp`

### Emergency Dialing Integration
- Android `Intent(Intent.ACTION_DIAL, Uri.parse("tel:+923336919922"))` launched safely with context handling, accessible from anywhere in the app with high visual priority.
