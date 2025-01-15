import {useAppDispatch, useAppSelector} from "@site/src/store";
import styles from "./styles.module.scss"
import PlayPauseButton from "@site/src/components/MusicPlayer/PlayPauseButton";
import {useEffect, useRef, useState} from "react";

export default function MusicPlayer() {
    const url = "https://disk.365246692.xyz/d/public/mapping/Illumi.mp3";
    const show = useAppSelector(s => s.music.show);
    const dispatch = useAppDispatch();
    const dom = useRef<HTMLDivElement>(null);
    const audio = useRef<HTMLAudioElement>(null);
    const [play, setPlay] = useState(false);
    const onClick = () => {
        if (!play) {
            audio.current.play().then(() => {
                setPlay(true);
            });
        } else {
            audio.current.pause();
            setPlay(false);
        }
    }

    useEffect(() => {
        if (!show) {
            return () => {}
        }
        const close = (event: MouseEvent) => {
            if (dom.current.contains(event.target as Node)) {
                return;
            }
            dispatch({type: 'music/change'});
            remove();
        }
        window.addEventListener('click', close);
        function remove() {
            window.removeEventListener('click', close);
        }
        return () => {
            remove();
        }
    }, [show]);

    return <div className={styles.music} data-show={show} onClick={onClick} ref={dom}>
        <PlayPauseButton play={play}/>
        <audio src={url} ref={audio}/>
    </div>
}

interface PlayList {
    list: number[],
    default?: number,
    nowPlaying?: number,
    onChange?: (id: number) => undefined,
    onPlay?: () => undefined,
}

