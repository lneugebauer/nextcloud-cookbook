import { defineUserConfig } from 'vuepress'
import { defaultTheme } from 'vuepress'

export default defineUserConfig({
  base: '/nextcloud-cookbook/',
  lang: 'en-US',
  title: 'Nextcloud Cookbook Android client',
  description: 'An Android client for Nextcloud Cookbook app.',
  head: [
    ['meta', { name: 'google-site-verification', content: 'aSKiI82xCweR2wlBpOe7lJnKVLLBUVU5ZIRCeRcd0xo' }],
    ['link', { rel: 'icon', href: '/nextcloud-cookbook/images/logo.png' }]
  ],
  theme: defaultTheme({
    navbar: [
      {
        text: 'Home',
        link: '/',
      },
    ],
    logo: 'images/logo.png',
    repo: 'lneugebauer/nextcloud-cookbook',
    docsDir: 'docs'
  })
})
