package main;

import gui.Controller;
import gui.Model;
import gui.View;
import structure.Block;
import structure.HeapFile;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Main invokes GUI where user can use the system.
 */
public class Main {
    public static void main(String[] args) {
//        String name = "Matus";
//        System.out.println(name);
//        name = name + "a".repeat(15 - name.length());
//        System.out.println(name);
//        name = name.substring(0, 5);
//        System.out.println(name);
//
//        Patient ja = new Patient("Matúš", "Kasák", LocalDate.of(2002, 7, 30), "564331");
//        byte[] array = ja.getBytes();
//        System.out.println(Arrays.toString(array));
//        ja.fromBytes(array);
//        System.out.println(ja.getOutput());
//
//        Block<Patient> block = new Block<>(3, Patient.class);
//        block.getDataArray();



        SwingUtilities.invokeLater(() -> {
            HeapFile<Patient> heapFile = new HeapFile<>("patients.bin", 300, Patient.class);
//            int a = heapFile.insert(new Patient("Matus", "Kasak", LocalDate.of(2002, 7, 30), "564331"));
//            int b = heapFile.insert(new Patient("Eva", "Nizna", LocalDate.of(2001, 2, 21), "56111"));
////            System.out.println(heapFile.get(a, new Patient("", "", null, "564331")).getOutput());
////            heapFile.delete(a, new Patient("", "", null, "564331"));
//            int a1 = heapFile.insert(new Patient("Matus", "Kasak", LocalDate.of(2002, 7, 30), "564331"));
////            heapFile.delete(a1, new Patient("", "", null, "564331"));
//            int g = heapFile.insert(new Patient("Ondro", "afkojo", LocalDate.of(2011, 7, 24), "wef1"));
//            heapFile.insert(new Patient("Dalsi", "afkojo", LocalDate.of(2011, 7, 24), "wef2"));
//            heapFile.insert(new Patient("NAmiesto mna v 0 blocku a 4", "afkojo", LocalDate.of(2011, 7, 24), "wef3"));
//            heapFile.insert(new Patient("NAmiesto mna v 0 blocku a 4", "afkojo", LocalDate.of(2011, 7, 24), "wef4"));
//            int p = heapFile.insert(new Patient("NAmiesto mna v 0 blocku a 4", "afkojo", LocalDate.of(2011, 7, 24), "wef5"));
//            int c = heapFile.insert(new Patient("Jan", "ehwfugfu", LocalDate.of(2011, 7, 26), "56901"));
//            int d = heapFile.insert(new Patient("Juro", "whefuw", LocalDate.of(2021, 7, 25), "ifuwgfui"));
//            int e = heapFile.insert(new Patient("Michal", "shfus", LocalDate.of(2021, 7, 23), "5432"));
//            int f = heapFile.insert(new Patient("Ondro", "afkojo", LocalDate.of(2011, 7, 24), "wef"));
//            int r = heapFile.insert(new Patient("Ondro", "afkojo", LocalDate.of(2011, 7, 24), "sgagafd"));
//            heapFile.delete(c, new Patient("", "", null, "56901"));
//            heapFile.delete(d, new Patient("", "", null, "ifuwgfui"));
//            heapFile.delete(e, new Patient("", "", null, "5432"));
//            heapFile.delete(f, new Patient("", "", null, "wef"));
//            heapFile.delete(r, new Patient("", "", null, "sgagafd"));
//                System.out.println(heapFile.get(a, new Patient("", "", null, "564331")).getOutput());

            //            HeapFile<Patient> heapFile = new HeapFile<>("binFile.bin", 110, Patient.class);
            View view = new View();
            Model model = new Model(heapFile);
            Controller controller = new Controller(model, view, heapFile);
            view.setVisible(true);
//            heapFile.close();
        });
    }
}