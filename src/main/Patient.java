package main;

import structure.IData;

import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;

public class Patient implements IData<Patient> {
    private static final int NAME_LIMIT = 15;
    private static final int SURNAME_LIMIT = 14;
    private static final int ID_LIMIT = 10;
    private String name;
    private String surname;
    private LocalDate dateOfBirth;
    private String personID;
    private int[] tests;//TODO testy max 6, ulozit do suboru, -1 neplatne?

    public Patient() {
        this.name = "Default";
        this.surname = "Default";
        this.dateOfBirth = LocalDate.now();
        this.personID = "000000";
        this.tests = new int[6];
    }

    public Patient(String name, String surname, LocalDate dateOfBirth, String personID) {
        this.name = name;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.personID = personID;
        this.tests = new int[6];
//        System.out.println(Arrays.toString(this.tests));
        //todo pridat aj sem testy?
    }

    public String getPersonID() {
        return this.personID;
    }

    public boolean addTest(int code) {
        for (int i = 0; i < this.tests.length; i++) {
            if (this.tests[i] == 0) {
                this.tests[i] = code;
                return true;
            }
        }
        System.out.println("The person has already maximum number of tests.");
        return false;
    }

    @Override
    public boolean equalsTo(Patient comparedData) {
        return this.personID.compareTo(comparedData.getPersonID()) == 0;
    }

    @Override
    public Patient createClass() {
        return new Patient();
//        return new Patient(this.name, this.surname, this.dateOfBirth, this.personID);
    }

    @Override
    public int getHashCode() {
        return this.personID.hashCode();
    }

    @Override
    public int getSize() {
        return Integer.BYTES + 2 + NAME_LIMIT +
                Integer.BYTES + 2 + SURNAME_LIMIT +
                Integer.BYTES * 3 +
                Integer.BYTES + 2 + ID_LIMIT +
                Integer.BYTES * 6;
    }

    private String getFullString(String str, int maxLimit) {
        if (str.length() >= maxLimit) {
            return str.substring(0, maxLimit);
        }
        // doplnenie znakov, moze byt hocico do max poctu podla zadania
        return str + "o".repeat(maxLimit - str.length());
    }

    // dlzka poli bude rovnaka, musi sa to rovnat getSize T
    // string doplnime do max nejakymi znakmi, a za tym hned int pocet platnych znakov v stringu
    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        try {
            // stores valid chars in strings as ints + strings fill till max limit and date as 3 ints
            hlpOutStream.writeInt(Math.min(this.name.length(), NAME_LIMIT));
            hlpOutStream.writeUTF(this.getFullString(this.name, NAME_LIMIT));
            hlpOutStream.writeInt(Math.min(this.surname.length(), SURNAME_LIMIT));
            hlpOutStream.writeUTF(this.getFullString(this.surname, SURNAME_LIMIT));
            hlpOutStream.writeInt(this.dateOfBirth.getDayOfMonth());
            hlpOutStream.writeInt(this.dateOfBirth.getMonthValue());
            hlpOutStream.writeInt(this.dateOfBirth.getYear());
            hlpOutStream.writeInt(Math.min(this.personID.length(), ID_LIMIT));
            hlpOutStream.writeUTF(this.getFullString(this.personID, ID_LIMIT));
            for (int test : this.tests) {
                hlpOutStream.writeInt(test);
            }

            return hlpByteArrayOutputStream.toByteArray();

        } catch (IOException e){
            throw new IllegalStateException("Error during conversion to byte array.");
        }
    }

    @Override
    public void fromBytes(byte[] array) {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(array);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);
        try {
            int nameLen = hlpInStream.readInt();
            String nameStr = hlpInStream.readUTF();
            nameLen = Math.min(nameLen, nameStr.length());
            this.name = nameStr.substring(0, nameLen);

            int surnameLen = hlpInStream.readInt();
            String surnameStr = hlpInStream.readUTF();
            surnameLen = Math.min(surnameLen, surnameStr.length());
            this.surname = surnameStr.substring(0, surnameLen);

            int day = hlpInStream.readInt();
            int month = hlpInStream.readInt();
            int year = hlpInStream.readInt();
            if (year == 0 || month == 0 || day == 0) {
                this.dateOfBirth = LocalDate.now();
            } else {
                this.dateOfBirth = LocalDate.of(year, month, day);
            }

            int idLen = hlpInStream.readInt();
            String idStr = hlpInStream.readUTF();
            idLen = Math.min(idLen, idStr.length());
            this.personID = idStr.substring(0, idLen);

            for (int i = 0; i < this.tests.length; ++i) {
                this.tests[i] = hlpInStream.readInt();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        for (int test : this.tests) {
            sb.append(test).append("  ");
        }
        return "Patient name: " + this.name + " " + this.surname + "  |  Date of birth: " + this.dateOfBirth + "  |  ID: " + this.personID + "\nTests: " + sb;
    }
}
