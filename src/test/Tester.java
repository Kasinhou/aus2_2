package test;

import main.Generator;
import main.Patient;
import structure.HeapFile;
import structure.LinearHashing;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tester {
    public Tester() {

    }

    public String testHeapFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("TESTING HEAP FILE ON RANDOM OPERATIONS\nIf you see some error or warning messages, something is wrong.\n");
        HeapFile<Patient> heapFile = new HeapFile<>("testHF.bin", 300, "src/testHFI.bin", Patient.class, true);
        heapFile.open();
        Generator generator = new Generator(null, null);
        ArrayList<Patient> patients = new ArrayList<>();
        ArrayList<Integer> addresses = new ArrayList<>();
        int insertCount = 0, deleteCount = 0, getCount = 0;
        Random random = new Random();
        for (int s = 0; s < 10; ++s) {
            random.setSeed(s);
                System.out.println(s);
            for (int i = 0; i < 1500; ++i) {
                double r = random.nextDouble();
                if (r < 0.2) {//GET
                    if (patients.isEmpty()) {
                        continue;
                    }
                    int getIndex = random.nextInt(patients.size());
                    Patient findPatient = heapFile.get(addresses.get(getIndex),
                            new Patient("", "", null, patients.get(getIndex).getPersonID()));
                    if (findPatient == null || !findPatient.getPersonID().equals(patients.get(getIndex).getPersonID())
                            || !findPatient.getOutput().equals(patients.get(getIndex).getOutput())) {
                        sb.append("Index = ").append(i).append("\nGet method in heapfile did not found the right Patient compared with ids.");
                    }
                    ++getCount;
                } else if (r < 0.55) {//DELETE
                    if (patients.isEmpty()) {
                        continue;
                    }
                    int deleteIndex = random.nextInt(patients.size());
                    heapFile.delete(addresses.get(deleteIndex), new Patient("", "", null, patients.get(deleteIndex).getPersonID()));
                    Patient deleted = null;
                    if (patients.size() > 1 && addresses.get(deleteIndex) < heapFile.getBlockCount()) {//potrebujem vediet ci vobec mozem zavolat get aby to nspadlo na eofexception
                        deleted = heapFile.get(addresses.get(deleteIndex), new Patient("", "", null, patients.get(deleteIndex).getPersonID()));
                    }
                    if (deleted != null) {
                        sb.append("Index = ").append(i).append("\nDeleted patient in heapfile is not actually deleted, get did found deleted on the address still valid.");
                    }
                    patients.remove(deleteIndex);
                    addresses.remove(deleteIndex);
                    ++deleteCount;
                } else {//INSERT
                    Patient patient = generator.generatePatient();
                    int address = heapFile.insert(patient);
                    Patient inserted = heapFile.get(address, new Patient("", "", null, patient.getPersonID()));
                    if (inserted == null || !patient.getOutput().equals(inserted.getOutput())) {
                        sb.append("Index = ").append(i).append("\nInserted patient in heapfile is not actually inserted, get did not found inserted on the good address.");
                    }
                    patients.add(patient);
                    addresses.add(address);
                    ++insertCount;
                }
                if (i % 900 == 0) {
                    ArrayList<Patient> validData = heapFile.getAllValidData();
                    if (validData.size() != patients.size() || validData.size() != addresses.size()) {
                        sb.append("\nSize of valid data in heapfile is not equal the size of patients in tester!");
                    }

                    List<String> insertedPatients = patients.stream().map(Patient::getOutput).toList();
                    List<String> validPatients = validData.stream().map(Patient::getOutput).toList();
                    for (String patient : insertedPatients) {
                        if (!validPatients.contains(patient)) {
                            sb.append("\n").append(patient).append(" is not in list of patients in tester. Something is wrong.");
                        }
                    }
                }
            }
        }

        sb.append("\nNumber of insertion: ").append(insertCount);
        sb.append("\nNumber of deletion: ").append(deleteCount);
        sb.append("\nNumber of get: ").append(getCount);
        sb.append("\nNumber of valid data: ").append(heapFile.getAllValidData().size());
        sb.append("\nNumber of addresses: ").append(addresses.size());
        sb.append("\nNumber of patients: ").append(patients.size());
        sb.append("\n\nOutput\n").append(heapFile.getAllOutput());
        heapFile.close();
        return sb.toString();
    }

    public String testLinearHashing() {
        StringBuilder sb = new StringBuilder();
        sb.append("TESTING LINEAR HASHING ON RANDOM OPERATIONS\nIf you see some error or warning messages, something is wrong.\n");
        LinearHashing<Patient> linHash = new LinearHashing<>("testLHM.bin", 1000, "testLHMI.bin", "testLHO.bin", 500, "testLHOI.bin", Patient.class);
        linHash.open();
        ArrayList<Patient> patients = new ArrayList<>();
        Generator generator = new Generator(null, null);
        int insertCount = 0, deleteCount = 0, getCount = 0, editCount = 0;
        Random random = new Random();
        for (int s = 0; s < 10; ++s) {
            random.setSeed(s);
            System.out.println(s);
            for (int i = 0; i < 1000; ++i) {
//                System.out.println(i);
                double r = random.nextDouble();
                if (r < 0.15) {//GET
                    if (patients.isEmpty()) {
                        continue;
                    }
                    int getIndex = random.nextInt(patients.size());
                    Patient findPatient = linHash.get(new Patient("", "", null, patients.get(getIndex).getPersonID()));
                    if (findPatient == null || !findPatient.getPersonID().equals(patients.get(getIndex).getPersonID())
                            || !findPatient.getOutput().equals(patients.get(getIndex).getOutput())) {
                        sb.append("\nIndex = ").append(i).append(", Get method in linear hashing did not found the right Patient compared with ids.");
                        sb.append("\nPatient with this id was not found: ").append(patients.get(getIndex).getPersonID()).append(" find: ").append(findPatient);
                    }
                    ++getCount;

                } else if (r < 0.35) {//EDIT
                    if (patients.isEmpty()) {
                        continue;
                    }
                    Patient patient = generator.generatePatient();
                    int getIndex = random.nextInt(patients.size());
                    if (linHash.edit(new Patient(patient.getName(), patient.getSurname(), patient.getDateOfBirth(), patients.get(getIndex).getPersonID()))) {
                        Patient edited = linHash.get(new Patient("", "", null, patients.get(getIndex).getPersonID()));
                        if (edited == null) {
                            sb.append("\nIndex = ").append(i).append(", Edited patient in linear hashing is not found after edit data.");
                        } else {
                            if (!patient.getName().equals(edited.getName()) || !patient.getSurname().equals(edited.getSurname()) || !patient.getDateOfBirth().equals(edited.getDateOfBirth())) {
                                sb.append("\nIndex = ").append(i).append(", Edited patient in linear hashing is not actually edited, or is edited incorrect.");
                                sb.append("\nPatient with this id was not found: ").append(patient.getPersonID()).append("\nedited: ").append(edited.getOutput()).append("\ngenerated: ").append(patient.getOutput());
                            } else {
                                patients.add(new Patient(edited.getName(), edited.getSurname(), edited.getDateOfBirth(), patients.get(getIndex).getPersonID()));
                                patients.remove(getIndex);
                                ++editCount;
                            }
                        }
                    } else {
                        System.out.println("Edit return false even though the patient should be edited.");
                    }
                } else if (r < 0.35) { //DELETE - zatial neimplementovany
                } else {//INSERT
                    Patient patient = generator.generatePatient();
                    linHash.insert(patient);//treba vytahovat adresu odtialto?, moze sa to menit pri splite
                    Patient inserted = linHash.get(new Patient("", "", null, patient.getPersonID()));
                    if (inserted == null || !patient.getOutput().equals(inserted.getOutput())) {
                        sb.append("\nIndex = ").append(i).append(", Inserted patient in linear hashing is not actually inserted, or is inserted incorrect, get did not found inserted data.");
                        sb.append("\nPatient with this id was not found: ").append(patient.getPersonID()).append(" inserted: ").append(inserted);
                    }
                    patients.add(patient);
                    ++insertCount;
                }
                if (i % 900 == 0) {
                    ArrayList<Patient> validData = linHash.getAllValidData();
                    if (validData.size() != patients.size()) {
                        sb.append("\nIndex = ").append(i).append(", Size of valid data in linear hashing is not equal the size of patients in tester!");
                    }

                    List<String> insertedPatients = patients.stream().map(Patient::getOutput).toList();
                    List<String> validPatients = validData.stream().map(Patient::getOutput).toList();
                    for (String patient : insertedPatients) {
                        if (!validPatients.contains(patient)) {
                            sb.append("Index = ").append(i).append("\n").append(patient).append(" is not in list of patients in tester. Something is wrong.");
                        }
                    }
                }
            }
        }
        sb.append("\nNumber of insertion: ").append(insertCount);
        sb.append("\nNumber of deletion: ").append(deleteCount);
        sb.append("\nNumber of get: ").append(getCount);
        sb.append("\nNumber of edit: ").append(editCount);
        sb.append("\nNumber of valid data: ").append(linHash.getAllValidData().size());
        sb.append("\nNumber of patients: ").append(patients.size());
        sb.append("\n\nOutput\n").append(linHash.getOutput());
        linHash.close();
        return sb.toString();
    }
}
