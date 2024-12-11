import CodeBlock from '@theme/CodeBlock';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import React, {useEffect, useState} from "react";

interface JvmCodeProps {
    title: string | undefined;
    showLineNumbers: boolean | undefined;
    children: React.JSX.Element;
}

interface CodeProps {
    kt?: string;
    java?: string;
    title?: string;
    showLineNumbers?: boolean;
}
function getCode({kt, java, title, showLineNumbers}: CodeProps):React.JSX.Element[] {
    const result = []
    if (kt) {
        result[0] = <CodeBlock
            language={"kotlin"}
            title={title + ".kt"}
            showLineNumbers={showLineNumbers}
        >
            {kt}
        </CodeBlock>
    } else {
        result[0] = void 0;
    }
    if (java) {
        result[1] = <CodeBlock
            language={"java"}
            title={title + ".java"}
            showLineNumbers={showLineNumbers}
        >
            {java}
        </CodeBlock>
    } else {
        result[1] = void 0;
    }
    return result;
}
export default function JvmCode({title, showLineNumbers, children}: JvmCodeProps) {
    const code:string = children?.props?.children?.props?.children;
    if (!code) return <></>;
    const [kt, java] = code.split("\n---\n");
    const codeNode = getCode({kt, java, title, showLineNumbers})
    return <Tabs groupId={"jvm-code"}>
        <TabItem value={"kotlin"} label={"kt"} default>
            {codeNode[0]}
        </TabItem>
        <TabItem value={"java"} label={"java"}>
            {codeNode[1]}
        </TabItem>
    </Tabs>
}
