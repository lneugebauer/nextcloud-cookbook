---
lang: en-US
title: Contributing
---

# Contributing

Thanks for wanting to contribute source code to Nextcloud Cookbook Android client.
That's great!

Please always create an [issue](https://github.com/lneugebauer/nextcloud-cookbook/issues) before starting a change.
This is very helpful to understand what kind of issue the pull request will solve and if your change will be accepted.

## Bugfix

Please describe the type of bug you want to fix and provide feedback on how to reproduce the problem.
I'll only accept bug fixes if I can reproduce the problem.

## Features/Improvements

Not every feature is relevant to the majority of the users.
It helps to have a discussion about a new feature before opening a pull request.

## Translations

Thank you for your interest in translating Nextcloud Cookbook Android client!
I appreciate your help in making it accessible to a wider audience.

### Weblate

I have chosen to use Weblate as a platform for translations.
Weblate provides an easy-to-use web-based interface that allows translators to collaborate and work on translations.

To get started with translating the app on Weblate, simply [visit the translation page](https://hosted.weblate.org/engage/nextcloud-cookbook/) and select your preferred language.
You can then start translating the app directly on the platform.

There are two components: [App](https://hosted.weblate.org/projects/nextcloud-cookbook/app/) and [Fastlane](https://hosted.weblate.org/projects/nextcloud-cookbook/fastlane/).

App contains all strings used in the app.

Fastlane is a collection of app store metadata that is used to submit the app to F-Droid and Play Store.

### XML

Please fork the repository, make your changes to the XML files and submit a pull request. Remember
to include information about what language it is.

All strings are located in the `res/values` directory in a file
called [`strings.xml`](https://github.com/lneugebauer/nextcloud-cookbook/blob/main/app/src/main/res/values/strings.xml)
.

For different languages, you create separate directories with the appropriate language code,
e.g. `res/values-de` for German.
Copy the `strings.xml` file into each of the language-specific directories and translate the strings
into the appropriate language.

In Android Studio, you can use
the [translations editor](https://developer.android.com/studio/write/translations-editor), but you
are free to use any text editor you like.

### Translation status

[![Translation status](https://hosted.weblate.org/widget/nextcloud-cookbook/multi-auto.svg)](https://hosted.weblate.org/engage/nextcloud-cookbook/)