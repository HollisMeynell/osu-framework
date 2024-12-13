import {useAppSelector} from "@site/src/store";
import styles from "./styles.module.scss"

export default function MusicPlayer() {
    const show = useAppSelector(s => s.music.show);
    return <div className={styles.music} data-show={show}>
    </div>
}
