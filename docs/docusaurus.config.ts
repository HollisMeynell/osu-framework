import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// css 指南 https://infima.dev/docs/getting-started/introduction
// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'spring osu 框架',
  tagline: 'spring!',
  favicon: 'img/osu.svg',
  trailingSlash: true,

  plugins: ['docusaurus-plugin-sass'],

  // Set the production url of your site here
  url: 'https://spring.365246692.xyz',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  // organizationName: 'facebook', // Usually your GitHub org/user name.
  // projectName: 'docusaurus', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'zh-Hans',
    locales: ['zh-Hans'],
  },

  markdown: {
    mermaid: true,
  },

  themes: ['@docusaurus/theme-mermaid'],

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          routeBasePath: '/d',
          editUrl:
            'https://github.com/HollisMeynell/osu-framework/tree/master/',
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          editUrl:
            'https://github.com/HollisMeynell/osu-framework/tree/master/',
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    docs: {
      sidebar: {
        hideable: true,
        autoCollapseCategories: true,
      },
    },
    navbar: {
      title: 'spring!',
      logo: {
        alt: 'osu',
        src: 'img/osu.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'applicationSidebar',
          position: 'left',
          label: '网站文档',
        },
        {
          type: 'docSidebar',
          sidebarId: 'apiSidebar',
          position: 'left',
          label: '应用API文档',
        },
        {
          type: 'docSidebar',
          sidebarId: 'devSidebar',
          position: 'left',
          label: '框架开发',
        },
        {to: '/blog', label: 'Blog', position: 'left'},
        {
          href: 'https://github.com/HollisMeynell/osu-framework',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'light',
      links: [
        {
          title: '站点',
          items: [
            {
              label: '文档',
              to: '/d/web/README/',
            },
            {
              label: '日志',
              to: '/blog/',
            },
          ],
        },
        {
          title: '相关网站',
          items: [
            {
              label: '选图网站',
              href: 'https://sp.365246692.xyz',
            },
          ],
        },
        {
          title: '其他',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/HollisMeynell/osu-framework',
            },
          ],
        },
      ],
      copyright: `暂时还木有备案 | Built with <a target="_blank" href="https://docusaurus.io/">Docusaurus</a>.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'javadoc', 'kotlin', 'rust', 'csharp', 'sql', 'ini', 'json', 'toml', 'yaml']
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
