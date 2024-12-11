import { configureStore } from '@reduxjs/toolkit'
import MusicReducer from './music'
import {useDispatch, useSelector} from "react-redux";

export const store = configureStore({
    reducer: {
        music: MusicReducer
    }
})

export type RootState = ReturnType<typeof store.getState>;

export type AppDispatch = typeof store.dispatch;

export const useAppSelector = useSelector.withTypes<RootState>()

export const useAppDispatch = useDispatch.withTypes<AppDispatch>()
