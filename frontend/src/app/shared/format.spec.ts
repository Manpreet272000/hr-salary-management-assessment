import { formatMoney, humanize } from './format';

describe('humanize', () => {
  it('turns enum constants into sentence case', () => {
    expect(humanize('HUMAN_RESOURCES')).toBe('Human resources');
    expect(humanize('SALES')).toBe('Sales');
  });

  it('keeps known acronyms and tolerates empty values', () => {
    expect(humanize('FTE')).toBe('FTE');
    expect(humanize(undefined)).toBe('');
  });
});

describe('formatMoney', () => {
  it('formats in the given currency', () => {
    expect(formatMoney(95000, 'INR')).toBe('₹95,000.00');
    expect(formatMoney(1234.5, 'USD')).toBe('$1,234.50');
  });

  it('shows a dash for missing amounts', () => {
    expect(formatMoney(undefined, 'EUR')).toBe('—');
  });
});
