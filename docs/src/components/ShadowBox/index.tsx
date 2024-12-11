import React from 'react';
import styles from './styles.module.scss';
import {useAppDispatch} from "@site/src/store";

export default function ShadowBox({}) {
    const dispatch = useAppDispatch();

    const showMusic = () => {
        dispatch({type: 'music/change'})
    }

    return <div className={styles.box}>
        <div onClick={showMusic}>来点音乐</div>
    </div>
}
