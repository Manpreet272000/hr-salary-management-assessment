import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · ACME HR',
    loadComponent: () => import('./employees/employee-list').then((m) => m.EmployeeList),
  },
  {
    path: 'employees/:id',
    title: 'Employee · ACME HR',
    loadComponent: () => import('./employees/employee-detail').then((m) => m.EmployeeDetail),
  },
  {
    path: 'insights',
    title: 'Pay insights · ACME HR',
    loadComponent: () => import('./insights/insights-page').then((m) => m.InsightsPage),
  },
  { path: '**', redirectTo: 'employees' },
];
