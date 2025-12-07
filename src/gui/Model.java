package gui;

import main.PCRTest;
import main.Patient;
import structure.HeapFile;
import structure.LinearHashing;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Model {
//    private HeapFile<Patient> hfPatients;
//    private HeapFile<PCRTest> hfTests;
    private LinearHashing<Patient> lhPatients;
    private LinearHashing<PCRTest> lhTests;

    public Model(LinearHashing<Patient> lhPatients, LinearHashing<PCRTest> lhTests) {
//        this.hfPatients = hfPatients;
//        this.hfTests = hfTests;
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
        //do coho vkladat, asi linhash
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
        return patient.getOutput();
    }

    //todo osetrit vstupy a co editovat
    public String editPatient(String name, String surname, LocalDate dateOfBirth, String personID) {
        this.lhPatients.edit(new Patient(name, surname, dateOfBirth, personID));
        return "Not implemented yet";
    }

    public String deletePatient(String personID) {
//        if (personID.length() > 10 || personID.isEmpty()) {
//            return "ID should have min 1 and max 10 characters.";
//        }
//        if (blockAddress >= this.hfPatients.getBlockCount()) {
//            return "Block address out of range of HeapFile.";
//        }
//        this.hfPatients.delete(blockAddress, new Patient("", "", null, personID));
//        return "Patient with id " + personID + " was deleted from";
        return "NOT IMPLEMENTED.";
    }

    //TODO osetrit vstupy, pridat aj pacienta
    public String insertTest(LocalDateTime dateTime, String personID, int testCode, boolean testResult, double testValue, String note) {
//        this.lhPatients.get()
        this.lhTests.insert(new PCRTest(dateTime, personID, testCode, testResult, testValue, note));
        return "Insertion test not implemented yet.";
    }

    public String getTest(int testCode) {
        PCRTest test = this.lhTests.get(new PCRTest(null, "", testCode, false, 0.0, ""));
        Patient patient = this.lhPatients.get(new Patient("", "", null, test.getPersonID()));
        System.out.println(patient.getOutput());
        return test.getOutput() + "\n" + patient.getOutput();
    }

    //TODO
    public String editTest(LocalDateTime dateTime, String personID, int testCode, boolean testResult, double testValue, String note) {
        this.lhTests.edit(new PCRTest(dateTime, personID, testCode, testResult, testValue, note));//todo naozaj aj person id?
        return "Edit test not implemented yet.";
    }

    public String deleteTest(int testCode) {
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
