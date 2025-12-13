package gui;

import main.Generator;
import main.PCRTest;
import main.Patient;
import structure.LinearHashing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Class which handles functions on gui (returns messages to controller after doing queries)
 */
public class WHOSystem {
    private LinearHashing<Patient> lhPatients;
    private LinearHashing<PCRTest> lhTests;
    private Generator generator;
    private String configPath;
    private String lhPatientsPath;
    private String lhTestsPath;
    private String mainPatientsPath;
    private String mainPatientsInfoPath;
    private String overflowPatientsPath;
    private String overflowPatientsInfoPath;
    private String mainTestsPath;
    private String mainTestsInfoPath;
    private String overflowTestsPath;
    private String overflowTestsInfoPath;

    public WHOSystem() {
    }

    // Insert patient with user inputs to LH if everything is fine
    public String insertPatient(String name, String surname, LocalDate dateOfBirth, String personID) {
        if (personID.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            return "Please fill all info about patient.";
        }
        if (name.length() > 15 || surname.length() > 14 || personID.length() > 10) {
            return "Change according max limit of characters:\nName = 15\nSurname = 14\nID = 10";
        }
        int address = this.lhPatients.insert(new Patient(name, surname, dateOfBirth, personID));
        return "Patient with id " + personID + " inserted to block " + address;
    }

    // Find patient based on id from user, together with all his tests
    public String getPatient(String personID) {
        if (personID.length() > 10 || personID.isEmpty()) {
            return "ID should have min 1 and max 10 characters.";
        }
        Patient patient = this.lhPatients.get(new Patient("", "", null, personID));
        if (patient == null) {
            return "Patient with id " + personID + " was not found";
        }
        int[] tests = patient.getValidTests();
        StringBuilder testsOutput = new StringBuilder();
        for (int i : tests) {
            PCRTest test = this.lhTests.get(new PCRTest(null, "", i, false, 0.0, ""));
            if (test != null) {
                testsOutput.append("\n").append(test.getOutput());
            } else {
                System.out.println("Something is wrong, test of patient not found!");
            }
        }
        return patient.getOutput() + "\nTests of patient [" + tests.length + "]" + testsOutput;
    }

    // Edit non key attributes of patients which user defined based on patient id
    public String editPatient(String name, String surname, LocalDate dateOfBirth, String personID) {
        if (personID.length() > 10 || personID.isEmpty()) {
            return "ID should have min 1 and max 10 characters.";
        }
        Patient patient = this.lhPatients.get(new Patient("", "", null, personID));
        if (patient == null) {
            return "Patient with id " + personID + " was not found. Not possible to edit.";
        }
        if (name.isEmpty()) {
            name = patient.getName();
        } else if (name.length() > 15) {
            return "Name should have 15 characters max.";
        }
        if (surname.isEmpty()) {
            surname = patient.getSurname();
        } else if (surname.length() > 15) {
            return "Name should have 15 characters max.";
        }
        if (dateOfBirth == null) {
            dateOfBirth = patient.getDateOfBirth();
        }
        Patient editPatient = new Patient(name, surname, dateOfBirth, personID);
        int[] tests = patient.getValidTests();
        for (int i : tests) {
            editPatient.addTest(i);
        }
        this.lhPatients.edit(editPatient);
        return "Edit patient with id " + personID;
    }

    // Insert test with user inputs to LH if everything is fine
    public String insertTest(LocalDateTime dateTime, String personID, int testCode, boolean testResult, double testValue, String note) {
        if (personID.isEmpty()) {
            return "Please fill patient ID.";
        }
        if (personID.length() > 10 || note.length() > 11) {
            return "Change according max limit of characters:\nID = 10\nNote = 11";
        }
        Patient patient = this.lhPatients.get(new Patient("", "", null, personID));
        if (patient == null) {
            return "Not possible to connect test to patient. Patient with id " + personID + " was not found.";
        }
        if (!patient.addTest(testCode)) {
            return "Not possible to add test to patient. Patient with id " + personID + " has already 6 tests.";
        }
        this.lhPatients.edit(patient);
        int address = this.lhTests.insert(new PCRTest(dateTime, personID, testCode, testResult, testValue, note));
        return "Inserted test with code " + testCode + " for patient " + personID + " on address " + address;
    }

    // Find test based on test code from user, with its patient info
    public String getTest(int testCode) {
        PCRTest test = this.lhTests.get(new PCRTest(null, "", testCode, false, 0.0, ""));
        if (test == null) {
            return "Test with code " + testCode + " was not found";
        }
        Patient patient = this.lhPatients.get(new Patient("", "", null, test.getPersonID()));
        return test.getOutput() + "\n" + patient.getOutput();
    }

    // Edit non key attributes of test which user defined based on test code
    public String editTest(LocalDateTime dateTime, int testCode, String testResult, String testValue, String note) {
        PCRTest test = lhTests.get(new PCRTest(null, "", testCode, false, 0.0, ""));
        if (test == null) {
            return "Test with code " + testCode + " is not found. Not possible edit.";
        }
        if (dateTime == null) {
            dateTime = test.getDateTime();
        }
        if (note.isEmpty()) {
            note = test.getNote();
        } else if (note.length() > 11) {
            return "Note should have 11 characters max.";
        }
        boolean result;
        if (testResult.isEmpty()) {
            result = test.getTestResult();
        } else {
            result = Boolean.parseBoolean(testResult);
        }
        double value;
        if (testValue.isEmpty()) {
            value = test.getTestValue();
        } else {
            try {
                value = Double.parseDouble(testValue);
            } catch (NumberFormatException e) {
                return "Use double for test value.";
            }
        }
        this.lhTests.edit(new PCRTest(dateTime, test.getPersonID(), testCode, result, value, note));
        return "Edit test with code " + testCode;
    }

    public String deleteTest(int testCode) {
        return "NOT IMPLEMENTED.";
    }

    public String deletePatient(String personID) {
        return "NOT IMPLEMENTED.";
    }

    // Output of LH patients, used for debug and for user
    public String getAllOutputPatients() {
        return lhPatients.getOutput();
    }

    // Output of LH tests, used for debug and for user
    public String getAllOutputTests() {
        return lhTests.getOutput();
    }

    // Generates fake data and fills LH files
    public String generate() {
        this.generator.generatePeople();
        this.generator.generateTests();
        return "Generated patients and tests.";
    }

    // Opening new binary file based on config file todo pozor na otvorenie zleho, popripade otvorenie
    public String open(String configPath, int clusterMP, int clusterOP, int clusterMT, int clusterOT) {
        if (configPath.isEmpty()) {
            return "You have to fill input path to config with files paths and information what you want to open.";
        }
        try {
            this.loadConfig(configPath);

            this.lhPatients = new LinearHashing<>(this.mainPatientsPath, clusterMP,
                    this.mainPatientsInfoPath, this.overflowPatientsPath, clusterOP,
                    this.overflowPatientsInfoPath, Patient.class, this.lhPatientsPath);
            this.lhTests = new LinearHashing<>(this.mainTestsPath, clusterMT,
                    this.mainTestsInfoPath, this.overflowTestsPath, clusterOT,
                    this.overflowTestsInfoPath, PCRTest.class, this.lhTestsPath);
            this.generator = new Generator(this.lhPatients, this.lhTests);

            // open files, new ones
            this.lhPatients.open();
            this.lhTests.open();

            return "Opened new files from config: " + configPath;
        } catch (NumberFormatException e) {
            return "Error parsing config file: " + e.getMessage();
        }
    }

    // Loading paths and info from config file
    public String load(String configPath) {
        if (configPath.isEmpty()) {
            return "You have to fill input path to config with files paths and information what you want to load.";
        }
        try {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                return "Config file not found: " + configPath;
            }
            
            this.loadConfig(configPath);

            // check if info files exist
            if (!new File(mainPatientsInfoPath).exists() || !new File(overflowPatientsInfoPath).exists() ||
                !new File(mainTestsInfoPath).exists() || !new File(overflowTestsInfoPath).exists()) {
                return "Info files not found. You have to open new files first.\n" +
                       "Required info files: " + mainPatientsInfoPath + ", " + overflowPatientsInfoPath +
                       ", " + mainTestsInfoPath + ", " + overflowTestsInfoPath;
            }

            this.lhPatients = new LinearHashing<>(this.mainPatientsPath, 0, this.mainPatientsInfoPath,
                    this.overflowPatientsPath, 0, this.overflowPatientsInfoPath, Patient.class, this.lhPatientsPath);
            this.lhTests = new LinearHashing<>(this.mainTestsPath, 0, this.mainTestsInfoPath,
                    this.overflowTestsPath, 0, this.overflowTestsInfoPath, PCRTest.class, this.lhTestsPath);
            this.generator = new Generator(this.lhPatients, this.lhTests);

            this.lhPatients.load();
            this.lhTests.load();

            return "Loaded paths and info from config: " + configPath;
        } catch (NumberFormatException e) {
            return "Error parsing config file: " + e.getMessage();
        }
    }

    private String loadConfig(String configPath) {
        try {
            this.configPath = configPath;
            BufferedReader configReader = new BufferedReader(new FileReader(this.configPath));
            this.lhPatientsPath = configReader.readLine();
            this.lhTestsPath = configReader.readLine();

            // patients -> main, cluster size, main info, overflow, cluster size, overflow info
            this.mainPatientsPath = configReader.readLine();
            this.mainPatientsInfoPath = configReader.readLine();
            this.overflowPatientsPath = configReader.readLine();
            this.overflowPatientsInfoPath = configReader.readLine();

            // tests -> main, cluster size, main info, overflow, cluster size, overflow info
            this.mainTestsPath = configReader.readLine();
            this.mainTestsInfoPath = configReader.readLine();
            this.overflowTestsPath = configReader.readLine();
            this.overflowTestsInfoPath = configReader.readLine();

            return "Loaded paths and info from config: " + configPath;
        } catch (IOException e) {
            return "Error opening files from config: " + e.getMessage();
        } catch (NumberFormatException e) {
            return "Error parsing config file: " + e.getMessage();
        }
    }

    public String closeFile() {
        if (this.lhPatients == null || this.lhTests == null) {
            return "No LHs are currently open.";
        }
        this.lhPatients.close();
        this.lhTests.close();
        this.lhPatients = null;
        this.lhTests = null;
        String configInfo = this.configPath != null ? " (" + this.configPath + ")" : "";
        return "Closed both Linear Hashing files" + configInfo;
    }
}
