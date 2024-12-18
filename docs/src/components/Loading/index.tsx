import React, {useLayoutEffect, useState} from "react";
import Loading1 from "@site/src/components/Loading/loading1";
import Loading2 from "@site/src/components/Loading/loading2";
import Loading3 from "@site/src/components/Loading/loading3";


type Props = {
    index?: number,
    color?: string,
}

export default function Loading({index, color}: Props) {
    const [loading, setLoading] = useState<React.JSX.Element>(null)

    useLayoutEffect(() => {
        let i: number;
        if (index) {
            i = 1 + (Math.round(index) % 3);
        } else {
            i = Math.floor(Math.random() * 3) + 1;
        }

        switch (i) {
            case 1: setLoading(<Loading1 color={color}/>);break
            case 2: setLoading(<Loading2 color={color}/>);break
            case 3: setLoading(<Loading3 color={color}/>);break
        }
    }, [color]);

    return loading;
}
