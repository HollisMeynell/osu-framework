import styles from "./loading1.module.scss"
import {useLayoutEffect, useRef} from "react";
import clsx from "clsx";

type Props = {
    color?: string,
}
export default function Loading2({color}: Props) {
    const spinner = useRef<HTMLDivElement>(null);
    useLayoutEffect(() => {
        if (color && spinner.current) {
            spinner.current.style.setProperty('--color--main', color);
        }
    }, []);
    return <div className={styles.skCubeGrid} ref={spinner}>
        <div className={clsx(styles.skCube, styles.skCube1)}/>
        <div className={clsx(styles.skCube, styles.skCube2)}/>
        <div className={clsx(styles.skCube, styles.skCube3)}/>
        <div className={clsx(styles.skCube, styles.skCube4)}/>
        <div className={clsx(styles.skCube, styles.skCube5)}/>
        <div className={clsx(styles.skCube, styles.skCube6)}/>
        <div className={clsx(styles.skCube, styles.skCube7)}/>
        <div className={clsx(styles.skCube, styles.skCube8)}/>
        <div className={clsx(styles.skCube, styles.skCube9)}/>
    </div>
}
