import React, {useEffect} from 'react';
import {Provider} from "react-redux";
import {store} from "@site/src/store";
import setEasterEggTriggered from '@site/src/components/other/egg';
import clickBoomEffect from '@site/src/components/other/boom';
import MusicPlayer from "@site/src/components/MusicPlayer";



export default function Root({children}) {
    useEffect(() => {
        const boom = clickBoomEffect();
        const easterEgg = setEasterEggTriggered();
        return () => {
            boom();
            easterEgg();
        };
    }, []);

    return <Provider store={store}>
        <MusicPlayer/>
        {children}
    </Provider>
}
