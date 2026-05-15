import { useState, useEffect, useCallback } from 'react';
import client from '../api/client';
import type { Course, Mode, ShowMessage } from '../types/types.ts';
import { STATUS_LABEL, STATUS_COLOR, formatPrice, formatDate } from '../utils/utils.ts';
import styles from './CourseList.module.css';

interface Props {
    mode: Mode;
    userId: string;
    creatorId: string;
    onMessage: ShowMessage;
}

export default function CourseList({ mode, userId, creatorId, onMessage }: Props) {
    const [courses, setCourses] = useState<Course[]>([]);
    const [statusFilter, setStatusFilter] = useState('OPEN');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const fetchCourses = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const params: Record<string, string> = {};
            if (statusFilter) params.status = statusFilter;
            const res = await client.get('/courses', { params });
            setCourses(res.data.body ?? []);
        } catch {
            setError('강의 목록을 불러오는 데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    }, [statusFilter]);

    useEffect(() => { fetchCourses(); }, [fetchCourses]);

    const enroll = async (courseId: number) => {
        try {
            await client.post(`/courses/${courseId}/enrollments`, {}, { headers: { 'USER-ID': userId } });
            onMessage('success', '수강 신청이 완료되었습니다.');
        } catch (err: unknown) {
            const e = err as { response?: { data?: { body?: { message?: string } } } };
            onMessage('error', e.response?.data?.message ?? '수강 신청에 실패했습니다.');
        }
    };

    const openCourse = async (courseId: number) => {
        try {
            console.log(creatorId);
            await client.patch(`/courses/${courseId}/open`, {}, { headers: { 'CREATOR-ID': creatorId } });
            onMessage('success', '강의가 오픈되었습니다.');
            fetchCourses();
        } catch (err: unknown) {
            const e = err as { response?: { data?: { body?: { message?: string } } } };
            onMessage('error', e.response?.data?.message ?? '강의 오픈에 실패했습니다.');
        }
    };

    const closeCourse = async (courseId: number) => {
        try {
            await client.patch(`/courses/${courseId}/close`, {}, { headers: { 'CREATOR-ID': creatorId } });
            onMessage('success', '강의 모집이 마감되었습니다.');
            fetchCourses();
        } catch (err: unknown) {
            const e = err as { response?: { data?: { body?: { message?: string } } } };
            onMessage('error', e.response?.data?.message ?? '강의 마감에 실패했습니다.');
        }
    };

    return (
        <div>
            <div className={styles.filterBar}>
                <span className={styles.filterLabel}>상태</span>
                {(['', 'OPEN', 'CLOSED', 'DRAFT'] as const).map(s => (
                    <button
                        key={s}
                        className={`${styles.filterBtn} ${statusFilter === s ? styles.filterBtnActive : ''}`}
                        onClick={() => setStatusFilter(s)}
                    >
                        {s === '' ? '전체' : STATUS_LABEL[s]}
                    </button>
                ))}
                <button className={styles.refreshBtn} onClick={fetchCourses}>↻ 새로고침</button>
            </div>

            {loading && <div className={styles.loading}>강의 목록을 불러오는 중...</div>}
            {error && <div className={styles.errorMsg}>{error}</div>}
            {!loading && !error && courses.length === 0 && (
                <div className={styles.empty}>표시할 강의가 없습니다.</div>
            )}

            {!loading && courses.length > 0 && (
                <div className={styles.courseGrid}>
                    {courses.map(course => (
                        <div key={course.id} className={styles.courseCard}>
                            <div className={styles.cardTop}>
                <span
                    className={styles.statusBadge}
                    style={{ background: STATUS_COLOR[course.status] + '18', color: STATUS_COLOR[course.status] }}
                >
                  {STATUS_LABEL[course.status]}
                </span>
                                <span className={styles.courseIdLabel}>#{course.id}</span>
                            </div>
                            <h3 className={styles.courseTitle}>{course.title}</h3>
                            {course.description && <p className={styles.courseDesc}>{course.description}</p>}
                            <div className={styles.courseMeta}>
                                <span>💰 {formatPrice(course.price)}</span>
                                <span>👥 정원 {course.capacity}명</span>
                            </div>
                            {(course.startDate || course.endDate) && (
                                <div className={styles.courseDates}>
                                    📅 {formatDate(course.startDate)} ~ {formatDate(course.endDate)}
                                </div>
                            )}
                            {mode === 'student' && (
                                <button
                                    className={`${styles.enrollBtn} ${course.status !== 'OPEN' ? styles.enrollBtnDisabled : ''}`}
                                    disabled={course.status !== 'OPEN'}
                                    onClick={() => enroll(course.id)}
                                >
                                    {course.status === 'OPEN' ? '수강 신청' : course.status === 'CLOSED' ? '마감됨' : '준비 중'}
                                </button>
                            )}
                            {mode === 'creator' && (
                                <div className={styles.creatorActions}>
                                    {course.status === 'DRAFT' && (
                                        <button className={styles.openBtn} onClick={() => openCourse(course.id)}>수강 오픈</button>
                                    )}
                                    {course.status === 'OPEN' && (
                                        <button className={styles.closeBtn} onClick={() => closeCourse(course.id)}>마감 처리</button>
                                    )}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}