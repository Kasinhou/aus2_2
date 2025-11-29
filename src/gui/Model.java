package gui;

import main.Patient;
import structure.HeapFile;

import java.time.LocalDate;

public class Model {
    private HeapFile<Patient> heapFile;

    public Model(HeapFile<Patient> heapFile) {
        this.heapFile = heapFile;
    }

    public String insertPatient(String name, String surname, LocalDate dateOfBirth, String personID) {
        if (personID.isEmpty() || name.isEmpty() || surname.isEmpty()) {
            return "Please fill all info about patient.";
        }
        if (name.length() > 15 || surname.length() > 14 || personID.length() > 10) {
            return "Change according max limit of characters:\nName = 15\nSurname = 14\nID = 10";
        }
        int address = this.heapFile.insert(new Patient(name, surname, dateOfBirth, personID));
        return "Patient with id " + personID + " inserted to block " + address;
    }

    public String getPatient(int blockAddress, String personID) {
        if (personID.length() > 10 || personID.isEmpty()) {
            return "ID should have min 1 and max 10 characters.";
        }
        if (blockAddress >= this.heapFile.getBlockCount()) {
            return "Block address out of range of HeapFile.";
        }
        Patient patient = this.heapFile.get(blockAddress, new Patient("", "", null, personID));
        if (patient == null) {
            return "Patient with id " + personID + " was not found in block " + blockAddress;
        }
        return patient.getOutput();
    }

    public String deletePatient(int blockAddress, String personID) {
        if (personID.length() > 10 || personID.isEmpty()) {
            return "ID should have min 1 and max 10 characters.";
        }
        if (blockAddress >= this.heapFile.getBlockCount()) {
            return "Block address out of range of HeapFile.";
        }
        this.heapFile.delete(blockAddress, new Patient("", "", null, personID));
        return "Patient with id " + personID + " was deleted from block " + blockAddress;
    }

    public String getAllOutput() {
        return this.heapFile.getAllOutput();
    }
}
