---
lang: en-US
title: FAQs
---

# FAQs

## Can I connect via http? <Badge type="tip" text="^0.21.0" />

Yes.

## Is the app available in my language?

If the app isn't available in your language yet, please have a look at the [contributing](./contributing#translations) page.
The easiest way to contribute is to create a free Weblate account.

## Why are my ingredients not calculated? <Badge type="tip" text="^0.18.0" />

Ingredients must follow the following specific format to be calculated.

`<amount> <optional_unit> <name>`

## How can I share recipes?

**Sharing with another Nextcloud User**

You can share recipes by sharing the Nextcloud folder from the Nextcloud Files app, which contains all recipes, with another Nextcloud user.

**Public sharing**

Currently it's not possible to share a public link to a recipe.
However, you can share recipes as text using the share button on the details screen.

## How can I update the image of a recipe?

You can update the image of a recipe by editing the recipe and selecting a new image from your gallery or by taking a new photo. The app will upload the image to your Nextcloud and update the recipe accordingly.

## Why do I get a "409 Conflict" error when saving a recipe?

A 409 error typically indicates that a recipe with the exact same name already exists in your library. Since the Nextcloud Cookbook app stores recipes as files based on their title, a naming collision prevents the creation of a new recipe. Try using a unique name for your recipe.
