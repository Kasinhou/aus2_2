package test;

import gui.Controller;
import gui.Model;
import gui.View;
import main.Generator;
import main.Patient;
import structure.HeapFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tester {
    public static void main(String[] args) {
        testRandomOperations();
    }

    private static void testRandomOperations() {
        SwingUtilities.invokeLater(() -> {
            HeapFile<Patient> heapFile = new HeapFile<>("patients.bin", 300, Patient.class);
            Generator generator = new Generator(heapFile);
            ArrayList<Patient> patients = new ArrayList<>();
            ArrayList<Integer> addresses = new ArrayList<>();
            int insertCount = 0, deleteCount = 0, getCount = 0;
            for (int s = 0; s < 1; ++s) {
//                System.out.println(s);
                Random random = new Random(s);//ma zmysel davat rozne seedy?
                for (int i = 0; i < 100000; ++i) {
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
                            System.out.println("Index = " + i);
                            System.out.println("Get method did not found the right Patient compared with ids.");
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
                            System.out.println("Index = " + i);
                            System.out.println("Deleted patient is not actually deleted, get did found deleted on the address still valid.");
                        }
                        patients.remove(deleteIndex);
                        addresses.remove(deleteIndex);
                        ++deleteCount;
                    } else {//INSERT
                        Patient patient = generator.generatePatient();
                        int address = heapFile.insert(patient);
                        Patient inserted = heapFile.get(address, new Patient("", "", null, patient.getPersonID()));
                        if (inserted == null || !patient.getOutput().equals(inserted.getOutput())) {
                            System.out.println("Index = " + i);
                            System.out.println("Inserted patient is not actually inserted, get did not found inserted on the good address.");
                        }
                        patients.add(patient);
                        addresses.add(address);
                        ++insertCount;
                    }
                    if (i % 1000 == 0) {
                        ArrayList<Patient> validData = heapFile.getAllValidData();
                        if (validData.size() != patients.size() || validData.size() != addresses.size()) {
                            System.out.println("Size of valid data in block is not equal the size of patients in tester!");
                        }

                        List<String> insertedPatients = patients.stream().map(Patient::getOutput).toList();
                        List<String> validPatients = validData.stream().map(Patient::getOutput).toList();
                        for (String patient : insertedPatients) {
                            if (!validPatients.contains(patient)) {
                                System.out.println(patient + " is not in list of patients in tester. Something is wrong.");
                            }
                        }
                    }
                }
            }

            System.out.println("Number of insertion: " + insertCount);
            System.out.println("Number of deletion: " + deleteCount);
            System.out.println("Number of get: " + getCount);
            System.out.println("Number of valid data: " + heapFile.getAllValidData().size());
            System.out.println("Number of addresses: " + addresses.size());
            System.out.println("Number of patients: " + patients.size());

            System.out.println();
            View view = new View();
            Model model = new Model(heapFile);
            Controller controller = new Controller(model, view, heapFile);
            view.setVisible(true);
//            heapFile.close();
        });
    }
}
