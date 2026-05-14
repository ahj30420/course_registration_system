export type Mode = 'student' | 'creator';
export type Tab = 'courses' | 'my-enrollments' | 'creator';

export type ShowMessage = (type: 'success' | 'error', text: string) => void;