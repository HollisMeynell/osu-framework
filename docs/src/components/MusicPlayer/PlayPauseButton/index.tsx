import styles from './button.module.scss';
import clsx from "clsx";

type Props = {
    play: boolean,
}

export default function PlayPauseButton({play}: Props) {
    return <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 140 140" width="140" height="140"
                preserveAspectRatio="xMidYMid meet" className={styles.svg}>

        <path strokeWidth="1"
              className={clsx(styles.item, styles.left, play ? styles.show : styles.hide)}
              d="M 20 35 v 70 a 15 15 0 1 0 30 0 v -70 a 15 15 0 1 0 -30 0 z"/>
        <path strokeWidth="1"
              className={clsx(styles.item, styles.right, play ? styles.show : styles.hide)}
              d="M 90 35 v 70 a 15 15 0 1 0 30 0 v -70 a 15 15 0 1 0 -30 0 z"/>

        <path strokeWidth="1"
              className={clsx(styles.item, styles.left, play ? styles.hide : styles.show)}
              d="M 34.5 70 v 29.4 a 15 15 0 0 0 22.5 7.6 l 20.8 -12 v -50 l -20.8 -12 a 15 15 0 0 0 -22.5 7.6 v 29.4 z"/>
        <path strokeWidth="1"
              className={clsx(styles.item, styles.right, play ? styles.hide : styles.show)}
              d="M 77.7 70 v 25 l 20.8 -12 a 15 15 0 0 0 0 -26 l -20.8 -12 v 25 z"/>

    </svg>
}
