package main;

import gui.Controller;
import gui.Model;
import gui.View;
import test.Tester;

import javax.swing.*;

/**
 * Main invokes GUI where user can use the system.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View view = new View();
            Model model = new Model();
            Controller controller = new Controller(model, view, new Tester());
            view.setVisible(true);
        });
    }
}