import styles from './style.module.scss';
import Link from '@docusaurus/Link';
import React from "react";
import clsx from "clsx";

type HelloBoxProps = {
    title:string,
}
export default function HelloBox({title}: HelloBoxProps) {
    return <div className={clsx(styles.box)}>
        <div className={clsx(styles.main, "container")}>
            <h1>{title}</h1>
            <p>一个 osu辅助类应用网站</p>
            <div>
                <Link to={"/d/web/README/"} className={clsx("button button--primary button--lg")}>
                    开始使用
                </Link>
                <Link to={"/d/api/README/"} className={clsx("button button--primary button--lg")}>
                    查看API文档
                </Link>
                <Link to={"/d/dev/README/"} className={clsx("button button--primary button--lg")}>
                    查看后端文档
                </Link>
                <Link to={"/blog"} className={clsx("button button--primary button--outline button--lg")}>
                    碎碎念
                </Link>
            </div>
        </div>
    </div>
}
