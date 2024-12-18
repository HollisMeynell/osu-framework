import React, {useEffect} from 'react';
import {Provider} from "react-redux";
import {store} from "@site/src/store";
import setEasterEggTriggered from '@site/src/components/other/egg';
import MusicPlayer from "@site/src/components/MusicPlayer";
import clickBoomEffect from "@site/src/components/other/boom";



export default function Root({children}) {
    useEffect(() => {
        const easterEgg = setEasterEggTriggered();
        const boom = clickBoomEffect();
        return () => {
            easterEgg();
            boom.then((remove) => remove());
        };
    }, []);

    return <Provider store={store}>
        <MusicPlayer/>
        {children}
    </Provider>
}
