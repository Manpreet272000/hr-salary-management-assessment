import { PayStatsRow } from '../../models';

export interface CurrencySection {
  currency: string;
  headcount: number;
  totalMonthlyPayroll: number;
  /** Largest max in the section, used to scale the range bars. */
  scaleMax: number;
  rows: PayStatsRow[];
}

/**
 * Pay can't be added across currencies, so rows are presented as one section per currency
 * (largest workforce first).
 */
export function groupByCurrency(rows: PayStatsRow[]): CurrencySection[] {
  const sections = new Map<string, CurrencySection>();
  for (const row of rows) {
    let section = sections.get(row.currency);
    if (!section) {
      section = { currency: row.currency, headcount: 0, totalMonthlyPayroll: 0, scaleMax: 0, rows: [] };
      sections.set(row.currency, section);
    }
    section.rows.push(row);
    section.headcount += row.headcount;
    section.totalMonthlyPayroll += row.totalMonthlyPayroll;
    section.scaleMax = Math.max(section.scaleMax, row.max);
  }
  return [...sections.values()].sort((a, b) => b.headcount - a.headcount);
}

/** Position of a value on a 0–100% bar. */
export function percentOf(value: number, max: number): number {
  return max > 0 ? Math.round((value / max) * 1000) / 10 : 0;
}
