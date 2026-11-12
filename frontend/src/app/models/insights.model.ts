export interface HeadcountRow {
  group: string;
  active: number;
  inactive: number;
}

export interface PayStatsRow {
  group: string;
  currency: string;
  headcount: number;
  min: number;
  median: number;
  average: number;
  max: number;
  totalMonthlyPayroll: number;
}
