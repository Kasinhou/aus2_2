package main;

import com.github.javafaker.Faker;
import structure.LinearHashing;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * Generator to filled db system with fake data.
 */
public class Generator {
    private static final int PEOPLE_COUNT = 50;
    private static final int TESTS_COUNT = 50;

    private LinearHashing<Patient> lhPatients;
    private LinearHashing<PCRTest> lhTests;
    private Faker faker;
    private ArrayList<String> idsList;
    private ArrayList<Integer> testCodes;

    public Generator(LinearHashing<Patient> lhPatients, LinearHashing<PCRTest> lhTests) {
        this.faker = new Faker();
        this.idsList = new ArrayList<>();
        this.testCodes = new ArrayList<>();
        this.lhPatients = lhPatients;
        this.lhTests = lhTests;
    }

    public void generatePeople() {
        int i = 0;
        while (i < PEOPLE_COUNT) {
            Patient patient = this.generatePatient();
            this.lhPatients.insert(patient);
            ++i;
        }
        System.out.println(PEOPLE_COUNT + " Patients generated.");
    }

    /**
     * Generate patient with random attributes using faker and according rules.
     */
    public Patient generatePatient() {
        String name, surname;
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
        String personID = this.getID();

        return new Patient(name, surname, date, personID);
    }

    public void generateTests() {
        int i = 0;
        while (i < TESTS_COUNT) {
            PCRTest test = this.generateTest();
            Patient patient = this.lhPatients.get(new Patient("", "", null, test.getPersonID()));
            if (!patient.addTest(test.getTestCode())) {
                // patient already has max number of tests, generate new
                this.testCodes.remove((Integer)test.getTestCode());
                continue;
            }
            this.lhPatients.edit(patient);
            this.lhTests.insert(test);
            ++i;
        }
        System.out.println(TESTS_COUNT + " Tests generated.");
    }

    /**
     * Generate test with random attributes using faker and according rules.
     */
    public PCRTest generateTest() {
        String personID, note;
        LocalDateTime dateTime;
        boolean testResult;
        double testValue;
        int testCode = this.getCode();
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

    /**
     * Method to get unique patient ID.
     */
    public String getID() {
        String personID;
        do {
            personID = this.faker.lorem().characters(1, 10, true, true);
        } while (!this.addedPersonID(personID));
        return personID;
    }

    /**
     * Method to get unique test code.
     */
    public int getCode() {
        int testCode;
        do {
            testCode = this.faker.number().numberBetween(1, Integer.MAX_VALUE);
        } while (!this.addedTestCode(testCode));
        return testCode;
    }

    /**
     * Method to find out if the test code is already used.
     */
    private boolean addedTestCode(int code) {
        if (this.testCodes.contains(code)) {
            return false;
        }
        this.testCodes.add(code);
        return true;
    }

    /**
     * Method to find out if the patient id is already used.
     */
    private boolean addedPersonID(String id) {
        if (this.idsList.contains(id)) {
            return false;
        }
        this.idsList.add(id);
        return true;
    }
}
