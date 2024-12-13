import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/**
 * 通过创建侧边栏，您可以：
 - 创建有序的文档组
 - 为该组的每个文档呈现一个侧边栏
 - 提供下一个/上一个导航

 侧边栏可以从文件系统生成，也可以在此处显式定义。

根据需要创建任意数量的侧边栏。
 */
export default {
  // 默认情况下，Docusaurus 会从 docs 文件夹结构生成侧边栏
  applicationSidebar: [
    {type: 'autogenerated', dirName: '01-web'},
  ],
  apiSidebar: [
    {type: 'autogenerated', dirName: '02-api'},
  ],
  devSidebar: [
    {type: 'autogenerated', dirName: '03-dev'},
  ],

  // But you can create a sidebar manually
  /*
  tutorialSidebar: [
    'intro',
    'hello',
    {
      type: 'category',
      label: 'Tutorial',
      items: ['tutorial-basics/create-a-document'],
    },
  ],
   */
};
