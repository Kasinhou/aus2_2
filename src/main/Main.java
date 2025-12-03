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
            LinearHashing<Patient> lhPatients = new LinearHashing<>("mainPatients.bin", 300, "overflowPatients.bin", 200, Patient.class);
            LinearHashing<PCRTest> lhTests = new LinearHashing<>("mainTests.bin", 300, "overflowTests.bin", 200, PCRTest.class);
            HeapFile<Patient> heapFile = new HeapFile<>("patients.bin", 300, Patient.class, true);
            HeapFile<PCRTest> hf = new HeapFile<>("tests.bin", 300, PCRTest.class, true);

            View view = new View();
            Model model = new Model(heapFile, hf, lhPatients, lhTests);
            Controller controller = new Controller(model, view, heapFile, hf, new Tester());
            view.setVisible(true);
        });
    }
}