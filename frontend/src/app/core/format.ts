import { Pipe, PipeTransform } from '@angular/core';

const ACRONYMS = new Set(['FTE']);

/** "HUMAN_RESOURCES" → "Human resources"; known acronyms stay upper-case. */
export function humanize(value: string | null | undefined): string {
  if (!value) {
    return '';
  }
  if (ACRONYMS.has(value)) {
    return value;
  }
  const words = value.toLowerCase().split('_');
  return [words[0].charAt(0).toUpperCase() + words[0].slice(1), ...words.slice(1)].join(' ');
}

const formatters = new Map<string, Intl.NumberFormat>();

/** Formats an amount in its own currency, e.g. 95000 INR → "₹95,000". */
export function formatMoney(amount: number | null | undefined, currency: string): string {
  if (amount === null || amount === undefined) {
    return '—';
  }
  let formatter = formatters.get(currency);
  if (!formatter) {
    formatter = new Intl.NumberFormat('en', { style: 'currency', currency, maximumFractionDigits: 2 });
    formatters.set(currency, formatter);
  }
  return formatter.format(amount);
}

@Pipe({ name: 'humanize' })
export class HumanizePipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return humanize(value);
  }
}

@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(amount: number | null | undefined, currency: string): string {
    return formatMoney(amount, currency);
  }
}
