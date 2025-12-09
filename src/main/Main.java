package main;

import gui.Controller;
import gui.WHOSystem;
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
            WHOSystem who = new WHOSystem();
            Controller controller = new Controller(who, view, new Tester());
            view.setVisible(true);
        });
    }
}