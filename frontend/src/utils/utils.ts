export const STATUS_LABEL: Record<string, string> = {
    DRAFT: '준비중',
    OPEN: '수강 가능',
    CLOSED: '마감',
    PENDING: '결제 대기',
    CONFIRMED: '수강 확정',
    CANCELLED: '취소됨',
    WAITLISTED: '대기 중',
};

export const STATUS_COLOR: Record<string, string> = {
    DRAFT: '#6b7280',
    OPEN: '#059669',
    CLOSED: '#dc2626',
    PENDING: '#d97706',
    CONFIRMED: '#2563eb',
    CANCELLED: '#9ca3af',
    WAITLISTED: '#7c3aed',
};

export function formatPrice(price: number): string {
    if (price === 0) return '무료';
    return price.toLocaleString('ko-KR') + '원';
}

export function formatDate(date: string): string {
    if (!date) return '-';
    return new Date(date).toLocaleDateString('ko-KR');
}
