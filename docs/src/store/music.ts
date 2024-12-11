import {createSlice} from '@reduxjs/toolkit';

interface MusicState {
    show: boolean;
}

const initialState: MusicState = {
    show: false,
}

const store = createSlice({
    name: 'music',
    initialState,
    reducers: {
        change(state) {
            state.show = !state.show;
        },
    },
})

export const {change} = store.actions;
export default store.reducer;
