package main;

import gui.Controller;
import gui.Model;
import gui.View;
import structure.HeapFile;
import structure.LinearHashing;
import test.Tester;

import javax.swing.*;

/**
 * Main invokes GUI where user can use the system.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LinearHashing<Patient> lhPatients = new LinearHashing<>("mainPatients.bin", 200, "overflowPatients.bin", 100, Patient.class);
            LinearHashing<PCRTest> lhTests = new LinearHashing<>("mainTests.bin", 200, "overflowTests.bin", 100, PCRTest.class);
//            HeapFile<Patient> heapFile = new HeapFile<>("patients.bin", 300, Patient.class, true);
//            HeapFile<PCRTest> hf = new HeapFile<>("tests.bin", 300, PCRTest.class, true);

            View view = new View();
            Model model = new Model(lhPatients, lhTests);
            Controller controller = new Controller(model, view, lhPatients, lhTests, new Tester());
            view.setVisible(true);
        });
    }
}