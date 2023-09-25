import { defineUserConfig } from 'vuepress'
import { defaultTheme } from 'vuepress'
import { sitemapPlugin } from 'vuepress-plugin-sitemap2'

export default defineUserConfig({
  base: '/nextcloud-cookbook/',
  lang: 'en-US',
  title: 'Nextcloud Cookbook Android client',
  description: 'An Android client for Nextcloud Cookbook app.',
  head: [
    ['meta', { name: 'google-site-verification', content: 'aSKiI82xCweR2wlBpOe7lJnKVLLBUVU5ZIRCeRcd0xo' }],
    ['link', { rel: 'icon', href: '/nextcloud-cookbook/images/logo.png' }],
    ['script', { async: '', src: 'https://umami.lukneu.de/script.js', 'data-website-id': '7242c1c7-b33a-4caa-ad52-3c1f461d4e34' }]
  ],
  theme: defaultTheme({
    navbar: [
      {
        text: 'Home',
        link: '/'
      },
      {
        text: 'Contributing',
        link: '/contributing'
      }
    ],
    logo: 'images/logo.png',
    repo: 'lneugebauer/nextcloud-cookbook',
    docsDir: 'docs'
  }),
  plugins: [
    sitemapPlugin({
      hostname: 'https://lneugebauer.github.io/nextcloud-cookbook',
      excludeUrls: ['/404.html']
    })
  ]
})
