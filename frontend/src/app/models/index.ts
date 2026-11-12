/**
 * The View layer's models: TypeScript shapes of the backend's DTOs (com.acme.hr.model.dto).
 * Enum-like values (department, country, leave type…) are plain strings whose allowed values come
 * from GET /api/meta, so they are defined once, in the backend.
 */
export * from './page.model';
export * from './employee.model';
export * from './compensation.model';
export * from './attendance.model';
export * from './payroll.model';
export * from './insights.model';
export * from './audit.model';
export * from './meta.model';
