/** One audited change: what changed (before → after), when, by whom, and through which operation. */
export interface AuditEntry {
  id: number;
  employeeId: number;
  subject: string;
  operation: string;
  summary: string;
  details: { before: Record<string, string | null>; after: Record<string, string | null> };
  actor: string;
  occurredAt: string;
}
