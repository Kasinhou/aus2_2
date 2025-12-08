package gui;

import test.Tester;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controller which actions from user is going to happen.
 */
public class Controller {
    private Model model;
    private View view;
    private Tester tester;

    public Controller(Model model, View view, Tester tester) {
        this.model = model;
        this.view = view;
        this.tester = tester;
        this.createMainWindow();
    }

    private void createMainWindow() {
        this.view.getGenerateButton().addActionListener(e -> this.handleGenerateButton());
        this.view.getClearButton().addActionListener(e -> this.handleClearButton());
        this.view.getOutputPatientsButton().addActionListener(e -> this.handleOutputPatientsButton());
        this.view.getOutputTestsButton().addActionListener(e -> this.handleOutputTestsButton());
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
        String message = this.model.generate();
        this.view.getOutputArea().setText(message);
    }

    private void handleClearButton() {
        this.view.getOutputArea().setText("");
        this.view.clearInputFields();
    }

    private void handleOutputPatientsButton() {
        String message = this.model.getAllOutputPatients();
        this.view.getOutputArea().setText(message);
    }

    private void handleOutputTestsButton() {
        String message = this.model.getAllOutputTests();
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
        this.view.getOutputArea().setText(message);
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
        String message = this.model.getPatient(this.view.getPersonID());
        this.view.getOutputArea().setText(message);
    }

    private void handleEditPersonButton() {
        LocalDate date = null;
        String inputDate = this.view.getPersonBirthday().trim();
        if (!inputDate.isEmpty()) {
            try {
                date = LocalDate.parse(inputDate);
            } catch (Exception e) {
                this.view.getOutputArea().setText("Date of birth is in wrong format. Use YYYY-MM-DD");
                return;
            }
        }
        String message = this.model.editPatient(this.view.getPersonName(), this.view.getPersonSurname(), date, this.view.getPersonID());
        this.view.getOutputArea().setText(message);
    }

    private void handleInsertTestButton() {
        LocalDateTime date;
        int code;
        boolean result;
        double value;
        try {
            code = Integer.parseInt(this.view.getTestCode());
            value = Double.parseDouble(this.view.getTestValue());
        } catch (NumberFormatException e) {
            this.view.getOutputArea().setText("Use integer for test code and double for test value.");
            return;
        }
        try {
            date = LocalDateTime.parse(this.view.getTestDate().trim());
        } catch (Exception e) {
            this.view.getOutputArea().setText("Date of birth is in wrong format. Use YYYY-MM-DDTHH:MM:SS");
            return;
        }
        result = Boolean.parseBoolean(this.view.getTestResult());
        String message = this.model.insertTest(date, this.view.getTestPersonID(), code, result, value, this.view.getNote());
        this.view.getOutputArea().setText(message);
    }

    private void handleGetTestButton() {
        int code;
        try {
            code = Integer.parseInt(this.view.getTestCode());
        } catch (NumberFormatException e) {
            this.view.getOutputArea().setText("Use integer for test code.");
            return;
        }
        String message = this.model.getTest(code);
        this.view.getOutputArea().setText(message);
    }

    private void handleEditTestButton() {
        LocalDateTime date = null;
        int code;
        try {
            code = Integer.parseInt(this.view.getTestCode());
        } catch (NumberFormatException e) {
            this.view.getOutputArea().setText("Use integer for test code.");
            return;
        }
        String inputDate = this.view.getTestDate().trim();
        if (!inputDate.isEmpty()) {
            try {
                date = LocalDateTime.parse(inputDate);
            } catch (Exception e) {
                this.view.getOutputArea().setText("Date of birth is in wrong format. Use YYYY-MM-DDTHH:MM:SS");
                return;
            }
        }
        String message = this.model.editTest(date, code, this.view.getTestResult(), this.view.getTestValue(), this.view.getNote());
        this.view.getOutputArea().setText(message);
    }

    private void handleDeletePersonButton() {
        String message = this.model.deletePatient(this.view.getPersonID());
        this.view.getOutputArea().setText(message);
    }

    private void handleDeleteTestButton() {
        String message = this.model.deleteTest(Integer.parseInt(this.view.getTestCode()));
        this.view.getOutputArea().setText(message);
    }
}
