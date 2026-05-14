import {useState} from 'react'
import Header from "./components/Header.tsx";
import type {Mode, Tab} from "./types/types.ts";
import styles from './App.module.css';
import CreatorForm from "./components/CreatorForm.tsx";

function App() {
    const [userId, setUserId] = useState<string>('1');
    const [creatorId, setCreatorId] = useState<string>('1');
    const [mode, setMode] = useState<Mode>('student');
    const [tab, setTab] = useState<Tab>('courses');
    const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string} | null>(null);

    const showMessage = (type: 'success' | 'error', text: string) => {
        setMessage({type, text});
        setTimeout(() => setMessage(null), 3000);
    }

    const handelModeChange = (next: Mode) => {
        setMode(next);
        setTab(next === 'creator' ? 'creator' : 'courses');
    }

    return (
        <div className={styles.root}>
            <Header
                mode = {mode}
                userId = {userId}
                creatorId = {creatorId}
                onModeChange = {handelModeChange}
                onUserIdChange = {setUserId}
                onCreatorIdChange = {setCreatorId}
            />

            {message && (
                <div className={`${styles.toast} ${message.type === 'success' ? styles.toastSuccess : styles.toastError}`}>
                    {message.type}
                </div>
            )}

            <main className={styles.main}>
                {tab === 'creator' && (
                  <CreatorForm creatorId={creatorId} onMessage={showMessage} />
                )}
            </main>
        </div>
    );
}

export default App
