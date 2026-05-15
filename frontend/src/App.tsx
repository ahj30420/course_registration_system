import {useState} from 'react'
import Header from "./components/Header.tsx";
import type {Mode, Tab} from "./types/types.ts";
import styles from './App.module.css';
import CreatorForm from "./components/CreatorForm.tsx";
import CourseList from "./components/CourseList.tsx";
import MyEnrollments from "./components/MyEnrollment.tsx";

function App() {
    const [userId, setUserId] = useState('1');
    const [creatorId, setCreatorId] = useState('1');
    const [mode, setMode] = useState<Mode>('student');
    const [tab, setTab] = useState<Tab>('courses');
    const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

    const showMessage = (type: 'success' | 'error', text: string) => {
        setMessage({ type, text });
        setTimeout(() => setMessage(null), 3000);
    };

    const handleModeChange = (next: Mode) => {
        setMode(next);
        setTab(next === 'creator' ? 'creator' : 'courses');
    };

    return (
        <div className={styles.root}>
            <Header
                mode={mode}
                userId={userId}
                creatorId={creatorId}
                onModeChange={handleModeChange}
                onUserIdChange={setUserId}
                onCreatorIdChange={setCreatorId}
            />

            {message && (
                <div className={`${styles.toast} ${message.type === 'success' ? styles.toastSuccess : styles.toastError}`}>
                    {message.text}
                </div>
            )}

            <nav className={styles.nav}>
                <button className={`${styles.navBtn} ${tab === 'courses' ? styles.navBtnActive : ''}`} onClick={() => setTab('courses')}>
                    강의 목록
                </button>
                {mode === 'student' && (
                    <button className={`${styles.navBtn} ${tab === 'my-enrollments' ? styles.navBtnActive : ''}`} onClick={() => setTab('my-enrollments')}>
                        내 수강 목록
                    </button>
                )}
                {mode === 'creator' && (
                    <button className={`${styles.navBtn} ${tab === 'creator' ? styles.navBtnActive : ''}`} onClick={() => setTab('creator')}>
                        강의 관리
                    </button>
                )}
            </nav>

            <main className={styles.main}>
                {tab === 'courses' && (
                    <CourseList mode={mode} userId={userId} creatorId={creatorId} onMessage={showMessage} />
                )}
                {tab === 'my-enrollments' && (
                    <MyEnrollments userId={userId} onMessage={showMessage} />
                )}
                {tab === 'creator' && (
                    <CreatorForm creatorId={creatorId} onMessage={showMessage} />
                )}
            </main>
        </div>
    );
}

export default App
