import styles from "./loading1.module.scss"
import {useLayoutEffect, useRef} from "react";

type Props = {
    color?: string,
}
export default function Loading1({color}: Props) {
    const spinner = useRef<HTMLDivElement>(null);
    useLayoutEffect(() => {
        if (color && spinner.current) {
            spinner.current.style.setProperty('--color--main', color);
        }
    }, []);
    return <div className={styles.spinner} ref={spinner}>
        <div className={styles.doubleBounce1}/>
        <div className={styles.doubleBounce2}/>
    </div>
}
