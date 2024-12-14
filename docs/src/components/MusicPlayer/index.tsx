import {useAppDispatch, useAppSelector} from "@site/src/store";
import styles from "./styles.module.scss"

export default function MusicPlayer() {
    const show = useAppSelector(s => s.music.show);
    const dispatch = useAppDispatch();
    const close = () => {
        dispatch({type: 'music/change'});
    }

    return <div className={styles.music} data-show={show} onClick={close}>
        音乐
    </div>
}

interface PlayList {
    list: number[],
    default?: number,
    nowPlaying?: number,
    onChange?: (id: number) => undefined,
    onPlay?:() => undefined,
}

