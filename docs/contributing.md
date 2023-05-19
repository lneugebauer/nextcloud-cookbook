---
lang: en-US
title: Contributing
---

# Contributing

Thanks for wanting to contribute source code to Nextcloud Cookbook Android client. That's great!

Please always create an [issue](https://github.com/lneugebauer/nextcloud-cookbook/issues) before
starting a change. This is very helpful to understand what kind of issue the pull request will solve
and if your change will be accepted.

## Bugfix

Please describe the type of bug you want to fix and provide feedback on how to reproduce the
problem. I'll only accept bug fixes if I can reproduce the problem.

## Features/Improvements

Not every feature is relevant to the majority of the users. It helps to have a discussion about a
new feature before opening a pull request.

## Translations

If you want to help translating the Nextcloud Cookbook Android client, I'm happy to accept pull
requests!

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