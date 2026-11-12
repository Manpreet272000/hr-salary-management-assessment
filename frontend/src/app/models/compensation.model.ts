export interface PayComponent {
  id: number;
  payType: string;
  amount: number;
  currency: string;
  effectiveFrom: string;
  effectiveTo: string | null;
}

export interface Compensation {
  employeeId: number;
  allowedPayTypes: string[];
  components: PayComponent[];
  monthlyGross: number;
}
