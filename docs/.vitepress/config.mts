import {defineConfig} from 'vitepress'

export default defineConfig({
    base: '/nextcloud-cookbook/',
    title: 'Nextcloud Cookbook',
    description: 'An Android client for Nextcloud Cookbook app.',
    head: [
        ['meta', {name: 'google-site-verification', content: 'aSKiI82xCweR2wlBpOe7lJnKVLLBUVU5ZIRCeRcd0xo'}],
        ['link', {rel: 'icon', href: '/nextcloud-cookbook/images/logo.png'}],
        ['script', {
            async: '',
            src: 'https://umami.lukneu.de/script.js',
            'data-website-id': '7242c1c7-b33a-4caa-ad52-3c1f461d4e34'
        }]
    ],
    sitemap: {
        hostname: 'https://lneugebauer.github.io/nextcloud-cookbook/'
    },
    lastUpdated: true,
    themeConfig: {
        nav: [
            {text: 'Home', link: '/'},
            {text: 'Contributing', link: '/contributing'},
            {text: 'FAQs', link: '/faqs'}
        ],
        logo: '/images/logo.png',
        socialLinks: [
            {icon: 'github', link: 'https://github.com/lneugebauer/nextcloud-cookbook'}
        ],
        footer: {
            message: 'Released under the <a href="https://github.com/lneugebauer/nextcloud-cookbook/blob/main/LICENSE">MIT License</a>.'
        },
        editLink: {
            pattern: 'https://github.com/lneugebauer/nextcloud-cookbook/edit/main/docs/:path'
        }
    }
})
