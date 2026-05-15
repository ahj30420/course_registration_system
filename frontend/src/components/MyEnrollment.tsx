import {useState, useEffect, useCallback} from 'react';
import client from '../api/client';
import type {PagingRequest, PagingResponse, Enrollment, ShowMessage} from '../types/types.ts';
import {STATUS_LABEL, STATUS_COLOR} from '../utils/utils.ts';
import styles from './MyEnrollments.module.css';

interface Props {
    userId: string;
    onMessage: ShowMessage;
}

const EMPTY_PAGE_REQUEST: PagingRequest = {page: 0, size: 1};

export default function MyEnrollments({userId, onMessage}: Props) {
    const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
    const [pageRequest, setPageRequest] = useState<PagingRequest>(EMPTY_PAGE_REQUEST);
    const [pageResponse, setPageResponse] = useState<PagingResponse>();
    const [loading, setLoading] = useState(false);

    const fetchEnrollments = useCallback(async () => {
        setLoading(true);
        try {
            const res = await client.get('/enrollments/my', {
                params: pageRequest,
                headers: { 'USER-ID': userId }
            });

            const body = res.data.body;
            setEnrollments(body?.content ?? []);

            if (body) {
                setPageResponse({
                    page: body.page,
                    size: body.size,
                    totalElements: body.totalElements,
                    totalPages: body.totalPages
                });
            }
        } catch {
            // handled silently
        } finally {
            setLoading(false);
        }
    }, [userId, pageRequest]);

    useEffect(() => {
        fetchEnrollments();
    }, [fetchEnrollments]);

    const handlePageChange = (newPage: number) => {
        setPageRequest(prev => ({...prev, page: newPage}));
    }

    const confirmEnrollment = async (enrollmentId: number) => {
        try {
            await client.post(`/${enrollmentId}/confirm`, {}, {headers: {'USER-ID': userId}});
            onMessage('success', '결제가 확정되었습니다.');
            fetchEnrollments();
        } catch (err: unknown) {
            const e = err as { response?: { data?: { body?: { message?: string } } } };
            onMessage('error', e.response?.data?.body?.message ?? '결제 확정에 실패했습니다.');
        }
    };

    const cancelEnrollment = async (enrollmentId: number) => {
        try {
            await client.post(`/${enrollmentId}/cancel`, {}, {headers: {'USER-ID': userId}});
            onMessage('success', '수강이 취소되었습니다.');
            fetchEnrollments();
        } catch (err: unknown) {
            const e = err as { response?: { data?: { body?: { message?: string } } } };
            onMessage('error', e.response?.data?.body?.message ?? '수강 취소에 실패했습니다.');
        }
    };

    return (
        <div>
            <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>내 수강 목록</h2>
                <button className={styles.refreshBtn} onClick={fetchEnrollments}>↻ 새로고침</button>
            </div>

            {loading && <div className={styles.loading}>수강 목록을 불러오는 중...</div>}

            {!loading && enrollments.length === 0 && (
                <div className={styles.empty}>수강 신청 내역이 없습니다.</div>
            )}

            {!loading && enrollments.length > 0 && (
                <div className={styles.enrollmentList}>
                    {enrollments.map(e => (
                        <div key={e.enrollmentId} className={styles.enrollmentCard}>
                            <div className={styles.info}>
                                <span className={styles.enrollmentId}>수강 신청 #{e.enrollmentId}</span>
                                {e.course?.title && <span className={styles.courseTitle}>{e.course.title}</span>}
                                <span
                                    className={styles.statusBadge}
                                    style={{background: STATUS_COLOR[e.status] + '18', color: STATUS_COLOR[e.status]}}
                                >
                                  {STATUS_LABEL[e.status]}
                                </span>
                            </div>
                            <div className={styles.actions}>
                                {e.status === 'PENDING' && (
                                    <button className={styles.confirmBtn}
                                            onClick={() => confirmEnrollment(e.enrollmentId)}>결제 확정</button>
                                )}
                                {(e.status === 'PENDING' || e.status === 'CONFIRMED') && (
                                    <button className={styles.cancelBtn}
                                            onClick={() => cancelEnrollment(e.enrollmentId)}>취소</button>
                                )}
                            </div>
                        </div>
                    ))}

                    {pageResponse && (
                        <div className={styles.pagination}>
                            <button
                                className={styles.pageBtn}
                                disabled={pageResponse.page === 0}
                                onClick={() => handlePageChange(pageResponse.page - 1)}
                            >
                                이전
                            </button>
                            <span className={styles.pageInfo}>
                                <strong>{pageResponse.page + 1}</strong> / {pageResponse.totalPages}
                            </span>
                            <button
                                className={styles.pageBtn}
                                disabled={pageResponse.page >= pageResponse.totalPages - 1}
                                onClick={() => handlePageChange(pageResponse.page + 1)}
                            >
                                다음
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}