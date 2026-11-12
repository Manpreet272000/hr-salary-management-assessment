import { PayStatsRow } from '../core/models';
import { groupByCurrency, percentOf } from './pay-stats';

function row(group: string, currency: string, headcount: number, max: number, total: number): PayStatsRow {
  return { group, currency, headcount, min: 1, median: 2, average: 2, max, totalMonthlyPayroll: total };
}

describe('groupByCurrency', () => {
  it('never mixes currencies and orders sections by headcount', () => {
    const sections = groupByCurrency([
      row('SALES', 'USD', 10, 9000, 50000),
      row('SALES', 'INR', 30, 200000, 3000000),
      row('DESIGN', 'INR', 5, 150000, 600000),
    ]);

    expect(sections.map((s) => s.currency)).toEqual(['INR', 'USD']);
    expect(sections[0]).toMatchObject({ headcount: 35, totalMonthlyPayroll: 3600000, scaleMax: 200000 });
    expect(sections[0].rows.map((r) => r.group)).toEqual(['SALES', 'DESIGN']);
  });
});

describe('percentOf', () => {
  it('scales to a percentage and guards against zero', () => {
    expect(percentOf(50, 200)).toBe(25);
    expect(percentOf(5, 0)).toBe(0);
  });
});
