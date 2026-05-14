import styles from './Header.module.css'
import type {Mode} from "../types/types.ts";

interface Props {
    mode: Mode;
    userId: string;
    creatorId: string;
    onModeChange: (mode: Mode) => void;
    onUserIdChange: (id: string) => void;
    onCreatorChange: (id: string) => void;
}

export default function Header({mode, userId, creatorId, onModeChange, onUserIdChange, onCreatorChange}: Props) {
    return (
        <header className={styles.header}>
            <div className={styles.left}>
                <span className={styles.logo}>🎓</span>
                <span className={styles.title}>수강신청 시스템</span>
            </div>
            <div className={styles.right}>
                <div className={styles.modeToggle}>
                    <button
                        className={`${styles.modeBtn} ${mode == 'student' ? styles.modeBtnActive : ''}`}
                        onClick={() => onModeChange('student')}
                    >
                        학생
                    </button>
                    <button
                        className={`${styles.modeBtn} ${mode == 'creator' ? styles.modeBtnActive : ''}`}
                        onClick={() => onModeChange('creator')}
                    >
                        강사
                    </button>
                </div>
                <div className={styles.divider}/>
                <div className={styles.idField}>
                    <label className={styles.idLabel}>USER-ID</label>
                    <input
                        type="number"
                        className={styles.idInput}
                        value={userId}
                        onChange={e => onUserIdChange(e.target.value)}
                    />
                </div>
                <div className={styles.idField}>
                    <label className={styles.idLabel}>CREATOR-ID</label>
                    <input
                        type="number"
                        className={styles.idInput}
                        value={creatorId}
                        onChange={e => onCreatorChange(e.target.value)}
                    />
                </div>
            </div>
        </header>
    );
};