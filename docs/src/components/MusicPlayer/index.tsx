import {useAppDispatch, useAppSelector} from "@site/src/store";
import styles from "./styles.module.scss"

export default function MusicPlayer() {
    const show = useAppSelector(s => s.music.show);
    const dispatch = useAppDispatch();
    const close = () => {
        dispatch({type: 'music/change'});
    }

    return <div className={styles.music} data-show={show} onClick={close}>
    </div>
}
