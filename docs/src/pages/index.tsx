import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';

import React from "react";
import HelloBox from "@site/src/components/HelloBox";


export default function Home(): React.JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout>
      <main>
          <HelloBox title={siteConfig.tagline}/>
      </main>
    </Layout>
  );
}
