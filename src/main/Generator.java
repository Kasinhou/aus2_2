package main;

import com.github.javafaker.Faker;
import structure.LinearHashing;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Generator {
    private static final int PEOPLE_COUNT = 500;
    private static final int TESTS_COUNT = 2000;

    private LinearHashing<Patient> lhPatients;
    private LinearHashing<PCRTest> lhTests;
    private Faker faker;
    private ArrayList<String> idsList;
//    private ArrayList<Integer> testCount;
    private ArrayList<Integer> testCodes;

    public Generator(LinearHashing<Patient> lhPatients, LinearHashing<PCRTest> lhTests) {
        this.faker = new Faker();
        this.idsList = new ArrayList<>();
//        this.testCount = new ArrayList<>();
        this.testCodes = new ArrayList<>();
        this.lhPatients = lhPatients;
        this.lhTests = lhTests;
    }

    public void generatePeople() {
        int i = 0;
        while (i < PEOPLE_COUNT) {
            Patient patient = this.generatePatient();
//            System.out.println(patient.getPersonID());
            this.lhPatients.insert(patient);
            ++i;
        }
        System.out.println(PEOPLE_COUNT + " Patients generated.");
    }

    public Patient generatePatient() {
        String name, surname, personID;
        LocalDate date;
        do {
            name = faker.name().firstName();
        } while (name.length() > 15);
        do {
            surname = faker.name().lastName();
        } while (surname.length() > 14);
        date = faker.date().between(Date.valueOf("1945-01-01"), Date.valueOf("2025-11-21"))
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
//            date = this.faker.date().birthday(5, 100).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        do {
            personID = this.faker.lorem().characters(1, 10, true, true);
        } while (!this.addedPersonID(personID));

        return new Patient(name, surname, date, personID);
    }

    public void generateTests() {
        int i = 0;
        while (i < TESTS_COUNT) {
            PCRTest test = this.generateTest();
            Patient patient = this.lhPatients.get(new Patient("", "", null, test.getPersonID()));
            if (!patient.addTest(test.getTestCode())) {
//                System.out.println("Already maximum amount of tests.");
                this.testCodes.remove((Integer)test.getTestCode());
                continue;
            }
            this.lhPatients.edit(patient);
            this.lhTests.insert(test);
//            System.out.println(test.getTestCode());
            ++i;
        }
        System.out.println(TESTS_COUNT + " Tests generated.");
    }

    public PCRTest generateTest() {
        String personID, note;
        LocalDateTime dateTime;
        int testCode;
        boolean testResult;
        double testValue;
        do {
            testCode = this.faker.number().numberBetween(1, Integer.MAX_VALUE);
        } while (!this.addedTestCode(testCode));
        dateTime = faker.date().between(Date.valueOf("1945-01-01"), Date.valueOf("2025-11-21"))
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withNano(0);
        int index = this.faker.number().numberBetween(0, this.idsList.size());
        personID = this.idsList.get(index);
        testResult = this.faker.bool().bool();
        testValue = testResult ? this.faker.number().randomDouble(2, 15, 30) : this.faker.number().randomDouble(2, 25, 45);
        note = this.faker.medical().symptoms();
        if (note.length() > 8) {
            note = note.substring(0, 8) + "...";
        }

        return new PCRTest(dateTime, personID, testCode, testResult, testValue, note);
    }

    private boolean addedTestCode(int code) {
        if (this.testCodes.contains(code)) {
            return false;
        }
        this.testCodes.add(code);
        return true;
    }

    private boolean addedPersonID(String id) {
        if (this.idsList.contains(id)) {
            return false;
        }
        this.idsList.add(id);
//        this.testCount.add(0);
        return true;
    }
}
