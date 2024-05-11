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
import NCDonationBadges from './components/NCDonationBadges.vue';
import NCStoreBadges from './components/NCStoreBadges.vue';

import gitHubBadge from './assets/images/get_it_on_github.png';
import payPalBadge from './assets/images/donate_with_paypal.svg';

const storeBadges = [
    {
        alt: 'Get it on Play Store',
        link: 'https://play.google.com/store/apps/details?id=de.lukasneugebauer.nextcloudcookbook',
        src: 'https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'
    }, {
        alt: 'Get it on GitHub',
        link: 'https://github.com/lneugebauer/nextcloud-cookbook/releases',
        src: gitHubBadge
    }, {
        alt: 'Get it on F-Droid',
        link: 'https://f-droid.org/packages/de.lukasneugebauer.nextcloudcookbook/',
        src: 'https://fdroid.gitlab.io/artwork/badge/get-it-on.png'
    }
];

const donationBadges = [
    {
        alt: 'Donate using Liberapay',
        link: 'https://liberapay.com/lneugebauer/donate',
        src: 'https://liberapay.com/assets/widgets/donate.svg'
    }, {
        alt: 'Donate using PayPal',
        link: 'https://www.paypal.com/donate/?hosted_button_id=ECDNN8PS3SSMQ',
        src: payPalBadge
    }
];
</script>

## Download

<NCStoreBadges :badges="storeBadges" />

## Features :rocket:

- List all recipes
- List all recipes by category
- Create recipe
- View recipe
- Edit recipe
- Stay awake on recipe screen
- Settings

## Planned features :checkered_flag:

- Offline usage
- Single Sign On through Nextcloud Files app
- Login via QR-Code
- Import recipe via url

## Donate

<NCDonationBadges :badges="donationBadges" />

## Translations :earth_africa:

[![Translation status](https://hosted.weblate.org/widget/nextcloud-cookbook/287x66-grey.png)](https://hosted.weblate.org/engage/nextcloud-cookbook/)

## Requirements :link:

* [Nextcloud](https://nextcloud.com/) instance running
* [Nextcloud Cookbook](https://github.com/nextcloud/cookbook) app enabled
