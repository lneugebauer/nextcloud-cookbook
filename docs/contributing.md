---
lang: en-US
title: Contributing
---

# Contributing

## Translate

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