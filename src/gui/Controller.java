package gui;
import main.Generator;
import main.PCRTest;
import main.Patient;
import structure.HeapFile;
import test.Tester;

import java.time.LocalDate;

/**
 * Controller which actions from user.
 */
public class Controller {
    private Model model;
    private View view;
    private HeapFile<Patient> hfPatients;
    private HeapFile<PCRTest> hfTests;
    private Generator generator;
    private Tester tester;

    public Controller(Model model, View view, HeapFile<Patient> hfPatients, HeapFile<PCRTest> hfTests, Tester tester) {
        this.model = model;
        this.view = view;
        this.hfPatients = hfPatients;
        this.hfTests = hfTests;
        this.generator = new Generator(this.hfPatients, this.hfTests);
        this.tester = tester;
        this.createMainWindow();
    }

    private void createMainWindow() {
        this.view.getGenerateButton().addActionListener(e -> this.handleGenerateButton());
        this.view.getClearButton().addActionListener(e -> this.handleClearButton());
        this.view.getOutputButton().addActionListener(e -> this.handleOutputButton());
        this.view.getOpenButton().addActionListener(e -> this.handleOpenButton());
        this.view.getLoadButton().addActionListener(e -> this.handleLoadButton());
        this.view.getCloseButton().addActionListener(e -> this.handleCloseButton());
        this.view.getTestLinHashButton().addActionListener(e -> this.handleTestLHButton());
        this.view.getTestHeapFileButton().addActionListener(e -> this.handleTestHFButton());
        this.view.getInsertPersonButton().addActionListener(e -> this.handleInsertPersonButton());
        this.view.getGetPersonButton().addActionListener(e -> this.handleGetPersonButton());
        this.view.getEditPersonButton().addActionListener(e -> this.handleEditPersonButton());
        this.view.getDeletePersonButton().addActionListener(e -> this.handleDeletePersonButton());
        this.view.getInsertTestButton().addActionListener(e -> this.handleInsertTestButton());
        this.view.getGetTestButton().addActionListener(e -> this.handleGetTestButton());
        this.view.getEditTestButton().addActionListener(e -> this.handleEditTestButton());
        this.view.getDeleteTestButton().addActionListener(e -> this.handleDeleteTestButton());
    }

    private void handleGenerateButton() {
        this.generator.generatePeople();
        this.generator.generateTests();
    }

    private void handleClearButton() {
        this.view.getOutputArea().setText("");
        this.view.clearInputFields();
    }

    private void handleOutputButton() {
//        String message = this.model.getAllTestsOutput();
        String message = this.model.getAllOutput();
        this.view.getOutputArea().setText(message);
    }

    private void handleOpenButton() {
        String message = this.model.openNewFile();
        this.view.getOutputArea().setText(message);
    }

    private void handleLoadButton() {
        String message = this.model.loadFile();
        this.view.getOutputArea().setText(message);
    }

    private void handleCloseButton() {
        String message = this.model.closeFile();
        this.view.getOutputArea().setText("Loading file not implemented. Add load method.");
    }


    private void handleTestLHButton() {
        String message = this.tester.testLinearHashing();
        this.view.getOutputArea().setText(message);
    }

    private void handleTestHFButton() {
        String message = this.tester.testHeapFile();
        this.view.getOutputArea().setText(message);
    }

    private void handleInsertPersonButton() {
        LocalDate date;
        try {
            date = LocalDate.parse(this.view.getPersonBirthday().trim());
        } catch (Exception e) {
            this.view.getOutputArea().setText("Date of birth is in wrong format. Use YYYY-MM-DD");
            return;
        }
        String message = this.model.insertPatient(this.view.getPersonName(), this.view.getPersonSurname(), date, this.view.getPersonID());
        this.view.getOutputArea().setText(message);
    }

    private void handleGetPersonButton() {
        int address;
        try {
            address = Integer.parseInt(this.view.getPersonAddress());
        } catch (NumberFormatException e) {
            this.view.getOutputArea().setText("Use integer for address.");
            return;
        }
        String message = this.model.getPatient(address, this.view.getPersonID());
        this.view.getOutputArea().setText(message);
    }

    private void handleEditPersonButton() {
        String message = "Edit patient is not implemented yet";
        this.view.getOutputArea().setText(message);
    }

    private void handleDeletePersonButton() {
        int address;
        try {
            address = Integer.parseInt(this.view.getPersonAddress());
        } catch (NumberFormatException e) {
            this.view.getOutputArea().setText("Use integer for address.");
            return;
        }
        String message = this.model.deletePatient(address, this.view.getPersonID());
        this.view.getOutputArea().setText(message);
    }

    private void handleInsertTestButton() {
        String message = "Insert test is not implemented yet";
        this.view.getOutputArea().setText(message);
    }

    private void handleGetTestButton() {
        String message = "Get test is not implemented yet";
        this.view.getOutputArea().setText(message);
    }

    private void handleEditTestButton() {
        String message = "Edit test is not implemented yet";
        this.view.getOutputArea().setText(message);
    }

    private void handleDeleteTestButton() {
        String message = "Delete test is not implemented yet";
        this.view.getOutputArea().setText(message);
    }
}
