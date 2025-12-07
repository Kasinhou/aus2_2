package main;

import gui.Controller;
import gui.Model;
import gui.View;
import structure.HeapFile;
import structure.LinearHashing;
import test.Tester;

import javax.swing.*;
import java.time.LocalDate;

/**
 * Main invokes GUI where user can use the system.
 */
public class Main {
    public static void main(String[] args) {
//        HeapFile<Patient> heapFile = new HeapFile<>("patients.bin", 300, "info.bin", Patient.class, true);
//        heapFile.open();
//        int i = heapFile.insert(new Patient("kndsak", "asdjkn", LocalDate.of(2000, 5, 8), "anso"));
//        heapFile.delete(i, new Patient("", "", null, "anso"));
//        heapFile.insert(new Patient("kndsak", "asdjkn", LocalDate.of(2000, 5, 8), "anso"));
        SwingUtilities.invokeLater(() -> {
            LinearHashing<Patient> lhPatients = new LinearHashing<>("mainPatients.bin", 400, "mainPatientsInfo.bin", "overflowPatients.bin", 200, "overflowPatientsInfo.bin", Patient.class);
            LinearHashing<PCRTest> lhTests = new LinearHashing<>("mainTests.bin", 800, "mainTestsInfo.bin", "overflowTests.bin", 400, "overflowTestsInfo.bin", PCRTest.class);
//            HeapFile<Patient> heapFile = new HeapFile<>("patients.bin", 300, Patient.class, true);
//            HeapFile<PCRTest> hf = new HeapFile<>("tests.bin", 300, PCRTest.class, true);

            View view = new View();
            Model model = new Model(lhPatients, lhTests);
            Controller controller = new Controller(model, view, lhPatients, lhTests, new Tester());
            view.setVisible(true);
        });
    }
}