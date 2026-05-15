import { useState } from 'react';
import client from '../api/client';
import styles from './CreatorForm.module.css';
import type {CourseForm, ShowMessage} from "../types/types.ts";

interface Props {
    creatorId: string;
    onMessage: ShowMessage;
}

const EMPTY_FORM: CourseForm = { title: '', description: '', price: 0, capacity: 30, startDate: '', endDate: '' };

export default function CreatorForm({ creatorId, onMessage }: Props) {
    const [form, setForm] = useState<CourseForm>(EMPTY_FORM);
    const [loading, setLoading] = useState(false);

    const set = <K extends keyof CourseForm>(key: K, value: CourseForm[K]) =>
        setForm(prev => ({ ...prev, [key]: value }));

    const submit = async () => {
        setLoading(true);
        try {
            await client.post('/courses', form, { headers: { 'CREATOR-ID': creatorId } });
            onMessage('success', '강의가 등록되었습니다.');
            setForm(EMPTY_FORM);
        } catch (err: unknown) {
            const e = err as { response?: { data?: { body?: { message?: string } } } };
            onMessage('error', e.response?.data?.body?.message ?? '강의 등록에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.form}>
            <h2 className={styles.title}>새 강의 등록</h2>
            <div className={styles.grid}>
                <div className={`${styles.field} ${styles.fieldFull}`}>
                    <label>강의명 *</label>
                    <input type="text" value={form.title} placeholder="강의명을 입력하세요" onChange={e => set('title', e.target.value)} />
                </div>
                <div className={styles.field}>
                    <label>수강료 (원)</label>
                    <input type="number" min={0} value={form.price} onChange={e => set('price', Number(e.target.value))} />
                </div>
                <div className={styles.field}>
                    <label>정원</label>
                    <input type="number" min={1} value={form.capacity} onChange={e => set('capacity', Number(e.target.value))} />
                </div>
                <div className={styles.field}>
                    <label>시작일</label>
                    <input type="date" value={form.startDate} onChange={e => set('startDate', e.target.value)} />
                </div>
                <div className={styles.field}>
                    <label>종료일</label>
                    <input type="date" value={form.endDate} onChange={e => set('endDate', e.target.value)} />
                </div>
                <div className={`${styles.field} ${styles.fieldFull}`}>
                    <label>강의 설명</label>
                    <textarea rows={3} value={form.description} placeholder="강의 설명을 입력하세요" onChange={e => set('description', e.target.value)} />
                </div>
            </div>
            <button className={styles.submitBtn} disabled={loading || !form.title} onClick={submit}>
                {loading ? '등록 중...' : '강의 등록'}
            </button>
        </div>
    );
}