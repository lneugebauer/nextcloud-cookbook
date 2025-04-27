---
layout: home

hero:
  name: Nextcloud Cookbook
  text: A native Android client
  tagline: Browse your recipes with ease.
  image:
    src: /images/logo.png
  actions:
    - theme: brand
      text: Help translating
      link: /contributing#translations
    - theme: alt
      text: View on GitHub
      link: https://github.com/lneugebauer/nextcloud-cookbook
---

<script setup lang="ts">
import NCBadge from './components/NCBadge.vue';
import NCDonationBadges from './components/NCDonationBadges.vue';
import NCStoreBadges from './components/NCStoreBadges.vue';

import gitHubBadge from './assets/images/get_it_on_github.png';
import payPalBadge from './assets/images/donate_with_paypal.svg';
</script>

## Download

<NCStoreBadges>
    <NCBadge
        alt="Get it on Play Store"
        event-name="get-it-on-play-store-button"
        link="https://play.google.com/store/apps/details?id=de.lukasneugebauer.nextcloudcookbook"
        size="medium"
        src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
    />
    <NCBadge
        alt="Get it on GitHub"
        event-name="get-it-on-github-button"
        link="https://github.com/lneugebauer/nextcloud-cookbook/releases"
        size="medium"
        :src="gitHubBadge"
    />
    <NCBadge
        alt="Get it on F-Droid"
        event-name="get-it-on-f-droid-button"
        link="https://f-droid.org/packages/de.lukasneugebauer.nextcloudcookbook/"
        size="medium"
        src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    />
</NCStoreBadges>

### Obtainium

To install through [Obtainium](https://github.com/ImranR98/Obtainium/tree/main?tab=readme-ov-file#installation), simply go to "Add App" and paste the URL of this repository as the source URL: `https://github.com/lneugebauer/nextcloud-cookbook`.

## Donate

<NCDonationBadges>
    <NCBadge
        alt="Donate using Liberapay"
        event-name="donate-using-liberapay-button"
        link="https://liberapay.com/lneugebauer/donate"
        size="small"
        src="https://liberapay.com/assets/widgets/donate.svg"
    />
    <NCBadge
        alt="Donate using PayPal"
        event-name="donate-using-paypal-button"
        link="https://www.paypal.com/donate/?hosted_button_id=ECDNN8PS3SSMQ"
        size="small"
        :src="payPalBadge"
    />
    <iframe src="https://github.com/sponsors/lneugebauer/button" title="Sponsor lneugebauer" height="32" width="114" style="border: 0; border-radius: 6px;"></iframe>
</NCDonationBadges>

## Screenshots

<table>
    <tr>
        <td>Phone</td>
        <td><img src="./assets/images/phoneScreenshots/1.png" alt="Phone screenshot 1"></td>
        <td><img src="./assets/images/phoneScreenshots/2.png" alt="Phone screenshot 2"></td>
        <td><img src="./assets/images/phoneScreenshots/3.png" alt="Phone screenshot 3"></td>
        <td><img src="./assets/images/phoneScreenshots/4.png" alt="Phone screenshot 4"></td>
    </tr>
    <tr>
        <td>Tablet</td>
        <td><img src="./assets/images/tenInchScreenshots/1.png" alt="Tablet screenshot 1"></td>
        <td><img src="./assets/images/tenInchScreenshots/2.png" alt="Tablet screenshot 2"></td>
        <td><img src="./assets/images/tenInchScreenshots/3.png" alt="Tablet screenshot 3"></td>
        <td><img src="./assets/images/tenInchScreenshots/4.png" alt="Tablet screenshot 4"></td>
    </tr>
</table>

## Features :rocket:

- List all recipes
- List all recipes by category
- Create recipe
- View recipe
- Edit recipe
- Stay awake on recipe screen
- Settings
- Import recipe via url

## Planned features :checkered_flag:

- Offline usage
- Single Sign On through Nextcloud Files app
- Login via QR-Code


## Translations :earth_africa:

[![Translation status](https://hosted.weblate.org/widget/nextcloud-cookbook/287x66-grey.png)](https://hosted.weblate.org/engage/nextcloud-cookbook/)

## Requirements :link:

* [Nextcloud](https://nextcloud.com/) instance running
* [Nextcloud Cookbook](https://github.com/nextcloud/cookbook) app enabled
