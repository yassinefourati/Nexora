export interface Employee {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  department: 'Engineering' | 'Sales' | 'Marketing' | 'Support' | 'HR' | 'Finance';
  role: string;
  salary: number;
  startDate: string;
  status: 'Active' | 'On Leave' | 'Terminated';
  location: string;
}

const FIRST = ['Alice', 'Bob', 'Claire', 'David', 'Eva', 'Frank', 'Grace', 'Hassan', 'Ines', 'Jack', 'Kira', 'Liam', 'Mona', 'Noah', 'Omar', 'Priya', 'Quinn', 'Rosa', 'Sam', 'Tina'];
const LAST = ['Martin', 'Tremblay', 'Dubois', 'Lavoie', 'Bergeron', 'Chen', 'Okafor', 'Nguyen', 'Rossi', 'Novak', 'Kim', 'Silva', 'Haddad', 'Petrov', 'Diallo'];
const DEPARTMENTS: Employee['department'][] = ['Engineering', 'Sales', 'Marketing', 'Support', 'HR', 'Finance'];
const ROLES: Record<Employee['department'], string[]> = {
  Engineering: ['Frontend Engineer', 'Backend Engineer', 'DevOps Engineer', 'QA Engineer', 'Engineering Manager'],
  Sales: ['Account Executive', 'Sales Manager', 'SDR', 'Sales Ops'],
  Marketing: ['Content Strategist', 'Growth Marketer', 'Brand Designer', 'Marketing Manager'],
  Support: ['Support Specialist', 'Support Lead', 'Customer Success Manager'],
  HR: ['HR Generalist', 'Recruiter', 'HR Business Partner'],
  Finance: ['Accountant', 'Financial Analyst', 'Controller'],
};
const LOCATIONS = ['Montreal', 'Toronto', 'New York', 'Austin', 'London', 'Berlin', 'Remote'];
const STATUSES: Employee['status'][] = ['Active', 'Active', 'Active', 'Active', 'On Leave', 'Terminated'];

function seededRandom(seed: number) {
  let s = seed;
  return () => { s = (s * 16807) % 2147483647; return (s - 1) / 2147483646; };
}

export function generateEmployees(count = 200): Employee[] {
  const rand = seededRandom(42);
  const pick = <T,>(arr: T[]) => arr[Math.floor(rand() * arr.length)];
  return Array.from({ length: count }, (_, i) => {
    const department = pick(DEPARTMENTS);
    const first = pick(FIRST);
    const last = pick(LAST);
    const start = new Date(2018, Math.floor(rand() * 12), Math.floor(rand() * 28) + 1);
    start.setFullYear(start.getFullYear() + Math.floor(rand() * 7));
    return {
      id: `emp-${i + 1}`,
      firstName: first,
      lastName: last,
      email: `${first.toLowerCase()}.${last.toLowerCase()}${i}@acme.com`,
      department,
      role: pick(ROLES[department]),
      salary: Math.round((45000 + rand() * 130000) / 500) * 500,
      startDate: start.toISOString().slice(0, 10),
      status: pick(STATUSES),
      location: pick(LOCATIONS),
    };
  });
}
