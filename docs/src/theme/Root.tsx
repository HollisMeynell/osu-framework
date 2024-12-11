import React, {useEffect} from 'react';
import {Provider} from "react-redux";
import {store} from "@site/src/store";

export default function Root({children}) {
    return <Provider store={store}>
        {children}
    </Provider>
}
