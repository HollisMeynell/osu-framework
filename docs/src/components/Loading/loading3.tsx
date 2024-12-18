import styles from "./loading1.module.scss"
import {useLayoutEffect, useRef} from "react";
import clsx from "clsx";

type Props = {
    color?: string,
}
export default function Loading3({color}: Props) {
    const spinner = useRef<HTMLDivElement>(null);
    useLayoutEffect(() => {
        if (color && spinner.current) {
            spinner.current.style.setProperty('--color--main', color);
        }
    }, []);
    return <div className={styles.skCircle} ref={spinner}>
        <div className={styles.skChild}></div>
        <div className={clsx(styles.skChild, styles.skCircle2)}></div>
        <div className={clsx(styles.skChild, styles.skCircle3)}></div>
        <div className={clsx(styles.skChild, styles.skCircle4)}></div>
        <div className={clsx(styles.skChild, styles.skCircle5)}></div>
        <div className={clsx(styles.skChild, styles.skCircle6)}></div>
        <div className={clsx(styles.skChild, styles.skCircle7)}></div>
        <div className={clsx(styles.skChild, styles.skCircle8)}></div>
        <div className={clsx(styles.skChild, styles.skCircle9)}></div>
        <div className={clsx(styles.skChild, styles.skCircle1)}></div>
        <div className={clsx(styles.skChild, styles.skCircle1)}></div>
        <div className={clsx(styles.skChild, styles.skCircle1)}></div>
    </div>
}
