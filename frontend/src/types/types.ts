export type Mode = 'student' | 'creator';
export type Tab = 'courses' | 'my-enrollments' | 'creator';

export interface Course {
    id: number;
    title: string;
    description?: string;
    price: number;
    capacity?: number;
    currentEnrollmentCount?: number;
    startDate: string;
    endDate: string;
    status: 'DRAFT' | 'OPEN' | 'CLOSED';
}

export interface CourseForm {
    title: string;
    description?: string;
    price: number;
    capacity: number;
    startDate: string;
    endDate: string;
}

export interface Enrollment {
    enrollmentId: number;
    courseId: number;
    userId: number;
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
    createdAt: string;
    course?: {
        id: number;
        title: string;
        description?: string;
    };
}

export interface PagingRequest {
    page: number;
    size: number;
    sort?: string;
}

export interface PagingResponse {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export type ShowMessage = (type: 'success' | 'error', text: string) => void;