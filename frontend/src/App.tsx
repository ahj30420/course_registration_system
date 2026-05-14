import {useState} from 'react'
import Header from "./components/Header.tsx";
import type {Mode} from "./types/types.ts";
import styles from './App.module.css';

function App() {
    const [userId, setUserId] = useState<string>('1');
    const [creatorId, setCreatorId] = useState<string>('1');
    const [mode, setMode] = useState<Mode>('student');

    const handelModeChange = (next: Mode) => {
        setMode(next);
    }

    return (
        <div className={styles.root}>
            <Header
                mode = {mode}
                userId = {userId}
                creatorId = {creatorId}
                onModeChange = {handelModeChange}
                onUserIdChange = {setUserId}
                onCreatorChange = {setCreatorId}
            />
        </div>
    );
}

export default App
