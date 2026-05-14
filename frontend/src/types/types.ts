export type Mode = 'student' | 'creator';
export type Tab = 'courses' | 'my-enrollments' | 'creator';

export interface Course {
    id: number;
    title: string;
    description?: string;
    price: number;
    capacity?: number;
    currentEnrollmentCount?: number;
    startDate?: string;
    endDate?: string;
    status: 'DRAFT' | 'OPEN' | 'CLOSED';
}

export type ShowMessage = (type: 'success' | 'error', text: string) => void;