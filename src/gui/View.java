package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Defines the structure of GUI and includes methods, getters for Controller.
 */
public class View extends JFrame {
    private JTextField personName;
    private JTextField personSurname;
    private JTextField personBirthday;
    private JTextField personID;

    private JTextField testDate;
    private JTextField testPersonID;
    private JTextField testCode;
    private JTextField testResult;
    private JTextField testValue;
    private JTextField note;

    private JTextField file;

    private JButton generateButton;
    private JButton clearButton;
    private JButton outputPatientsButton;
    private JButton outputTestsButton;
    private JButton openButton;
    private JButton loadButton;
    private JButton closeButton;
    private JButton testLinHashButton;
    private JButton testHeapFileButton;


    private JButton insertPersonButton;
    private JButton getPersonButton;
    private JButton editPersonButton;
    private JButton deletePersonButton;
    private JButton insertTestButton;
    private JButton getTestButton;
    private JButton editTestButton;
    private JButton deleteTestButton;

    private JTextArea outputArea;

    public View() {
        setTitle("Heap File");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 600);
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel basicButtons = new JPanel();
        basicButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.generateButton = new JButton("Generate");
        this.clearButton = new JButton("Clear");
        basicButtons.add(new JLabel("BASIC"));
        basicButtons.add(this.generateButton);
        basicButtons.add(this.clearButton);

        JPanel fileButtons = new JPanel();
        fileButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.openButton = new JButton("Open");
        this.loadButton = new JButton("Load");
        this.closeButton = new JButton("Close");
        fileButtons.add(new JLabel("FILE"));
        fileButtons.add(this.openButton);
        fileButtons.add(this.loadButton);
        fileButtons.add(this.closeButton);

        JPanel outputButtons = new JPanel();
        outputButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.outputPatientsButton = new JButton("Patients");
        this.outputTestsButton = new JButton("Tests");
        outputButtons.add(new JLabel("OUTPUT"));
        outputButtons.add(this.outputPatientsButton);
        outputButtons.add(this.outputTestsButton);

        JPanel testButtons = new JPanel();
        testButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.testLinHashButton = new JButton("Lin Hash File");
        this.testHeapFileButton = new JButton("Heap File");
        testButtons.add(new JLabel("TESTING"));
        testButtons.add(this.testLinHashButton);
        testButtons.add(this.testHeapFileButton);

        JPanel personInputs = new JPanel(new GridLayout(4, 2, 5, 5));
        personInputs.setBorder(BorderFactory.createTitledBorder("PERSON inputs"));
        this.personName = new JTextField(10);
        this.personSurname = new JTextField(10);
        this.personBirthday = new JTextField(10);
        this.personID = new JTextField(10);
        personInputs.add(new JLabel("First name"));
        personInputs.add(this.personName);
        personInputs.add(new JLabel("Last name"));
        personInputs.add(this.personSurname);
        personInputs.add(new JLabel("Date of Birth"));
        personInputs.add(this.personBirthday);
        personInputs.add(new JLabel("Person ID"));
        personInputs.add(this.personID);

        JPanel actionPersonButtons = new JPanel();
        actionPersonButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.insertPersonButton = new JButton("Insert");
        this.getPersonButton = new JButton("Get");
        this.editPersonButton = new JButton("Edit");
        this.deletePersonButton = new JButton("Delete");
        actionPersonButtons.add(this.insertPersonButton);
        actionPersonButtons.add(this.getPersonButton);
        actionPersonButtons.add(this.editPersonButton);
        actionPersonButtons.add(this.deletePersonButton);

        JPanel testInputs = new JPanel(new GridLayout(6, 2, 5, 5));
        testInputs.setBorder(BorderFactory.createTitledBorder("PCR TEST inputs"));
        this.testDate = new JTextField(10);
        this.testPersonID = new JTextField(10);
        this.testCode = new JTextField(10);
        this.testResult = new JTextField(10);
        this.testValue = new JTextField(10);
        this.note = new JTextField(10);
        testInputs.add(new JLabel("Date and time of test"));
        testInputs.add(this.testDate);
        testInputs.add(new JLabel("Person ID"));
        testInputs.add(this.testPersonID);
        testInputs.add(new JLabel("Test Code"));
        testInputs.add(this.testCode);
        testInputs.add(new JLabel("Test result"));
        testInputs.add(this.testResult);
        testInputs.add(new JLabel("Test value"));
        testInputs.add(this.testValue);
        testInputs.add(new JLabel("Note"));
        testInputs.add(this.note);

        JPanel actionTestButtons = new JPanel();
        actionTestButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.insertTestButton = new JButton("Insert");
        this.getTestButton = new JButton("Get");
        this.editTestButton = new JButton("Edit");
        this.deleteTestButton = new JButton("Delete");
        actionTestButtons.add(this.insertTestButton);
        actionTestButtons.add(this.getTestButton);
        actionTestButtons.add(this.editTestButton);
        actionTestButtons.add(this.deleteTestButton);

        this.outputArea = new JTextArea();
        this.outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(this.outputArea);

        leftPanel.add(basicButtons);
        leftPanel.add(fileButtons);
        leftPanel.add(outputButtons);
        leftPanel.add(testButtons);
        leftPanel.add(personInputs);
        leftPanel.add(actionPersonButtons);
        leftPanel.add(testInputs);
        leftPanel.add(actionTestButtons);

        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    public String getPersonName() {
        return this.personName.getText();
    }

    public String getPersonSurname() {
        return this.personSurname.getText();
    }

    public String getPersonBirthday() {
        return this.personBirthday.getText();
    }

    public String getPersonID() {
        return this.personID.getText();
    }

    public String getTestDate() {
        return this.testDate.getText();
    }

    public String getTestPersonID() {
        return this.testPersonID.getText();
    }

    public String getTestCode() {
        return this.testCode.getText();
    }

    public String getTestResult() {
        return this.testResult.getText();
    }

    public String getTestValue() {
        return this.testValue.getText();
    }

    public String getNote() {
        return this.note.getText();
    }

    public String getFile() {
        return this.file.getText();
    }

    public void clearInputFields() {
        this.personName.setText("");
        this.personSurname.setText("");
        this.personBirthday.setText("");
        this.personID.setText("");
        this.testDate.setText("");
        this.testPersonID.setText("");
        this.testCode.setText("");
        this.testResult.setText("");
        this.testValue.setText("");
        this.note.setText("");
    }

    public JButton getGenerateButton() {
        return this.generateButton;
    }

    public JButton getClearButton() {
        return this.clearButton;
    }

    public JButton getOutputPatientsButton() {
        return this.outputPatientsButton;
    }

    public JButton getOutputTestsButton() {
        return this.outputTestsButton;
    }

    public JButton getOpenButton() {
        return this.openButton;
    }

    public JButton getLoadButton() {
        return this.loadButton;
    }

    public JButton getCloseButton() {
        return this.closeButton;
    }

    public JButton getTestLinHashButton() {
        return this.testLinHashButton;
    }

    public JButton getTestHeapFileButton() {
        return this.testHeapFileButton;
    }

    public JButton getInsertPersonButton() {
        return this.insertPersonButton;
    }

    public JButton getGetPersonButton() {
        return this.getPersonButton;
    }

    public JButton getEditPersonButton() {
        return this.editPersonButton;
    }

    public JButton getDeletePersonButton() {
        return this.deletePersonButton;
    }

    public JButton getInsertTestButton() {
        return this.insertTestButton;
    }

    public JButton getGetTestButton() {
        return this.getTestButton;
    }

    public JButton getEditTestButton() {
        return this.editTestButton;
    }

    public JButton getDeleteTestButton() {
        return this.deleteTestButton;
    }

    public JTextArea getOutputArea() {
        return this.outputArea;
    }
}
