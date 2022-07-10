import { defineUserConfig } from 'vuepress'
import { defaultTheme } from 'vuepress'

export default defineUserConfig({
  base: '/nextcloud-cookbook/',
  lang: 'en-US',
  title: 'Nextcloud Cookbook Android client',
  description: 'An Android client for Nextcloud Cookbook app.',
  head: [
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
