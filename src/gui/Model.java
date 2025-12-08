package gui;

import main.PCRTest;
import main.Patient;
import structure.LinearHashing;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Model {
    private LinearHashing<Patient> lhPatients;
    private LinearHashing<PCRTest> lhTests;

    public Model(LinearHashing<Patient> lhPatients, LinearHashing<PCRTest> lhTests) {
        this.lhPatients = lhPatients;
        this.lhTests = lhTests;
    }

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

    public String getTest(int testCode) {
        PCRTest test = this.lhTests.get(new PCRTest(null, "", testCode, false, 0.0, ""));
        Patient patient = this.lhPatients.get(new Patient("", "", null, test.getPersonID()));
        return test.getOutput() + "\n" + patient.getOutput();
    }

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

    public String getAllOutputPatients() {
        return lhPatients.getOutput();
    }

    public String getAllOutputTests() {
        return lhTests.getOutput();
    }

    public String openNewFile() {
        this.lhPatients.open();
        this.lhTests.open();
        return "Opening patients and tests files.";
    }

    public String loadFile() {
        this.lhPatients.load();
        this.lhTests.load();
        return "Loading info file and continue with lin hash files.";
    }

    public String closeFile() {
        this.lhPatients.close();
        this.lhTests.close();
        return "Closing both Linear Hashing files";
    }
}
