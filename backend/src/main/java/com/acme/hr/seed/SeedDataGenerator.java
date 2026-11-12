package com.acme.hr.seed;

import com.acme.hr.common.Country;
import com.acme.hr.compensation.PayType;
import com.acme.hr.employee.Department;
import com.acme.hr.employee.EmployeeProfile;
import com.acme.hr.employee.EmploymentType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Deterministic, realistic-looking org data: country/department/level mixes, local-currency pay
 * bands, stipends for interns and incentives for sales. Pure (no I/O) so it is unit-tested.
 */
final class SeedDataGenerator {

    private static final String[] FIRST_NAMES = {
        "Aarav", "Priya", "Rohan", "Ananya", "Vikram", "Meera", "Arjun", "Kavya", "Ishaan", "Diya",
        "James", "Olivia", "Liam", "Emma", "Noah", "Ava", "Ethan", "Sophia", "Mason", "Mia",
        "Oliver", "Amelia", "Harry", "Isla", "George", "Lukas", "Hannah", "Felix", "Lena", "Jonas",
        "Wei", "Mei", "Jun", "Hui", "Ravi", "Siti", "Daniel", "Grace", "Samuel", "Chloe"};
    private static final String[] LAST_NAMES = {
        "Sharma", "Patel", "Iyer", "Reddy", "Singh", "Gupta", "Nair", "Menon", "Kapoor", "Das",
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Taylor", "Clark",
        "Evans", "Walker", "Wright", "Hughes", "Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Wagner",
        "Tan", "Lim", "Lee", "Ng", "Wong", "Chua", "Goh", "Koh", "Teo", "Ong"};

    private static final String[] LEVELS = {"Associate", "", "Senior", "Lead", "Principal"};
    private static final double[] LEVEL_WEIGHTS = {30, 30, 22, 10, 8};
    private static final double[] LEVEL_PAY_MULTIPLIER = {1.0, 1.4, 1.9, 2.6, 3.2};

    private static final Map<Country, Double> COUNTRY_WEIGHTS = new EnumMap<>(Map.of(
            Country.IN, 40.0, Country.US, 25.0, Country.GB, 15.0, Country.DE, 10.0, Country.SG, 10.0));
    /** Monthly entry-level pay in local currency, and the rounding step for that currency. */
    private static final Map<Country, int[]> ENTRY_PAY_AND_STEP = new EnumMap<>(Map.of(
            Country.IN, new int[] {60_000, 500}, Country.US, new int[] {6_500, 50}, Country.GB, new int[] {4_000, 50},
            Country.DE, new int[] {4_200, 50}, Country.SG, new int[] {5_000, 50}));

    private static final Map<Department, Double> DEPARTMENT_WEIGHTS = new EnumMap<>(Map.of(
            Department.ENGINEERING, 30.0, Department.SALES, 20.0, Department.CUSTOMER_SUPPORT, 12.0,
            Department.OPERATIONS, 10.0, Department.PRODUCT, 8.0, Department.MARKETING, 7.0,
            Department.FINANCE, 5.0, Department.HUMAN_RESOURCES, 4.0, Department.DESIGN, 4.0));
    private static final Map<Department, Double> DEPARTMENT_PAY_MULTIPLIER = new EnumMap<>(Map.of(
            Department.ENGINEERING, 1.15, Department.PRODUCT, 1.15, Department.DESIGN, 1.0,
            Department.SALES, 0.9, Department.MARKETING, 0.95, Department.FINANCE, 1.0,
            Department.HUMAN_RESOURCES, 0.9, Department.OPERATIONS, 0.85, Department.CUSTOMER_SUPPORT, 0.75));
    private static final Map<Department, String> ROLE = new EnumMap<>(Map.of(
            Department.ENGINEERING, "Software Engineer", Department.PRODUCT, "Product Manager",
            Department.DESIGN, "Product Designer", Department.SALES, "Account Executive",
            Department.MARKETING, "Marketing Specialist", Department.FINANCE, "Financial Analyst",
            Department.HUMAN_RESOURCES, "HR Business Partner", Department.OPERATIONS, "Operations Analyst",
            Department.CUSTOMER_SUPPORT, "Support Specialist"));

    private static final Map<EmploymentType, Double> EMPLOYMENT_WEIGHTS = new EnumMap<>(Map.of(
            EmploymentType.FTE, 80.0, EmploymentType.CONTRACTUAL, 13.0, EmploymentType.INTERN, 7.0));

    static final double ACTIVE_SHARE = 0.94;
    static final double CONTRACTOR_PREMIUM = 1.10;
    static final double STIPEND_SHARE_OF_ENTRY_PAY = 0.35;
    static final double MIN_INCENTIVE_SHARE = 0.10;
    static final double MAX_INCENTIVE_SHARE = 0.40;

    private final Random random;
    private final LocalDate today;

    SeedDataGenerator(long seed, LocalDate today) {
        this.random = new Random(seed);
        this.today = today;
    }

    List<SeedEmployee> generate(int count) {
        List<SeedEmployee> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(next());
        }
        for (Department department : Department.values()) {
            assignManagers(result, department);
        }
        return result;
    }

    /**
     * Builds a department's tree with the same constraints the app enforces: managers are active FTEs
     * in the same department. The most senior one heads the department; other principals report to the
     * head, leads report to a principal, and everyone else reports to a lead.
     */
    private void assignManagers(List<SeedEmployee> employees, Department department) {
        List<Integer> members = new ArrayList<>();
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).profile().department() == department) {
                members.add(i);
            }
        }
        List<Integer> eligible = members.stream().filter(i -> employees.get(i).canManage()).toList();
        if (eligible.isEmpty()) {
            return;
        }
        int head = eligible.stream().max(Comparator.comparingInt(i -> employees.get(i).level())).orElseThrow();
        List<Integer> principals = atLevel(employees, eligible, 4, head);
        List<Integer> leads = atLevel(employees, eligible, 3, head);
        List<Integer> principalsAndHead = principals.isEmpty() ? List.of(head) : principals;
        List<Integer> teamManagers = leads.isEmpty() ? principalsAndHead : leads;

        for (int member : members) {
            Integer manager;
            if (member == head) {
                manager = null;
            } else if (principals.contains(member)) {
                manager = head;
            } else if (leads.contains(member)) {
                manager = pickOne(principalsAndHead);
            } else {
                manager = pickOne(teamManagers);
            }
            employees.set(member, employees.get(member).withManager(manager));
        }
    }

    private static List<Integer> atLevel(List<SeedEmployee> employees, List<Integer> eligible, int level, int except) {
        return eligible.stream().filter(i -> i != except && employees.get(i).level() == level).toList();
    }

    private int pickOne(List<Integer> candidates) {
        return candidates.get(random.nextInt(candidates.size()));
    }

    private SeedEmployee next() {
        Country country = pick(COUNTRY_WEIGHTS);
        Department department = pick(DEPARTMENT_WEIGHTS);
        EmploymentType type = pick(EMPLOYMENT_WEIGHTS);
        int level = type == EmploymentType.INTERN ? 0 : pickIndex(LEVEL_WEIGHTS);
        String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)];

        EmployeeProfile profile = new EmployeeProfile(name, title(department, type, level), department, type, country);
        boolean active = random.nextDouble() < ACTIVE_SHARE;
        LocalDate joinedOn = today.minusDays(30 + random.nextInt(5 * 365));
        return new SeedEmployee(profile, active, joinedOn, pay(country, department, type, level), level, null);
    }

    private Map<PayType, BigDecimal> pay(Country country, Department department, EmploymentType type, int level) {
        int[] entryAndStep = ENTRY_PAY_AND_STEP.get(country);
        double base = entryAndStep[0] * DEPARTMENT_PAY_MULTIPLIER.get(department);
        Map<PayType, BigDecimal> pay = new EnumMap<>(PayType.class);
        if (type == EmploymentType.INTERN) {
            pay.put(PayType.STIPEND, roundTo(base * STIPEND_SHARE_OF_ENTRY_PAY * jitter(), entryAndStep[1]));
            return pay;
        }
        double regular = base * LEVEL_PAY_MULTIPLIER[level] * jitter()
                * (type == EmploymentType.CONTRACTUAL ? CONTRACTOR_PREMIUM : 1.0);
        pay.put(PayType.REGULAR, roundTo(regular, entryAndStep[1]));
        if (department == Department.SALES) {
            double share = MIN_INCENTIVE_SHARE + random.nextDouble() * (MAX_INCENTIVE_SHARE - MIN_INCENTIVE_SHARE);
            pay.put(PayType.INCENTIVE, roundTo(regular * share, entryAndStep[1]));
        }
        return pay;
    }

    private static String title(Department department, EmploymentType type, int level) {
        String role = ROLE.get(department);
        if (type == EmploymentType.INTERN) {
            return role + " Intern";
        }
        return LEVELS[level].isEmpty() ? role : LEVELS[level] + " " + role;
    }

    /** ±10% so pay within a band is not identical. */
    private double jitter() {
        return 0.9 + random.nextDouble() * 0.2;
    }

    private static BigDecimal roundTo(double amount, int step) {
        return BigDecimal.valueOf(Math.max(step, Math.round(amount / step) * (long) step)).setScale(2, RoundingMode.UNNECESSARY);
    }

    private <T> T pick(Map<T, Double> weights) {
        double roll = random.nextDouble() * weights.values().stream().mapToDouble(Double::doubleValue).sum();
        T last = null;
        for (Map.Entry<T, Double> entry : weights.entrySet()) {
            last = entry.getKey();
            roll -= entry.getValue();
            if (roll < 0) {
                return last;
            }
        }
        return last;
    }

    private int pickIndex(double[] weights) {
        double roll = random.nextDouble() * java.util.Arrays.stream(weights).sum();
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
