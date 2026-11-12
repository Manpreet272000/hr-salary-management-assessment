/** "2026-11" for the given date (local time), matching the backend's YearMonth format. */
export function toMonth(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}
