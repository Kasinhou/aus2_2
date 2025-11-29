package main;

import structure.IData;

import java.io.*;
import java.time.LocalDateTime;

public class PCRTest implements IData<PCRTest> {
    private LocalDateTime testDateTime;
    private String testPersonID;// max 10 chars
    private int testCode;
    private boolean testResult;
    private double testValue;
    private String note;//max 11 chars

    public PCRTest(LocalDateTime testDateTime, String testPersonID, int testCode, boolean testResult, double testValue, String note) {
        this.testDateTime = testDateTime;
        this.testPersonID = testPersonID;
        this.testCode = testCode;
        this.testResult = testResult;
        this.testValue = testValue;
        this.note = note;
    }

    @Override
    public boolean equalsTo(PCRTest comparedData) {
        return false;
    }

    @Override
    public PCRTest createClass() {
        return null;
    }

    @Override
    public int getHashCode() {
        return 0;//TODO hashfortest
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
//        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
//        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
//        try {
//            // stores valid chars in strings as ints + strings fill till max limit and date as 3 ints
//            hlpOutStream.writeInt(Math.min(this.name.length(), NAME_LIMIT));
//            hlpOutStream.writeUTF(this.getFullString(this.name, NAME_LIMIT));
//            hlpOutStream.writeInt(Math.min(this.surname.length(), SURNAME_LIMIT));
//            hlpOutStream.writeUTF(this.getFullString(this.surname, SURNAME_LIMIT));
//            hlpOutStream.writeInt(this.dateOfBirth.getDayOfMonth());
//            hlpOutStream.writeInt(this.dateOfBirth.getMonthValue());
//            hlpOutStream.writeInt(this.dateOfBirth.getYear());
//            hlpOutStream.writeInt(Math.min(this.personID.length(), ID_LIMIT));
//            hlpOutStream.writeUTF(this.getFullString(this.personID, ID_LIMIT));
//
//            return hlpByteArrayOutputStream.toByteArray();
//
//        } catch (IOException e){
//            throw new IllegalStateException("Error during conversion to byte array.");
//        }
        return null;
    }

    @Override
    public void fromBytes(byte[] from) {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(from);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);
//        try {
//            int nameLen = hlpInStream.readInt();
//            String nameStr = hlpInStream.readUTF();
//            nameLen = Math.min(nameLen, nameStr.length());
//            this.name = nameStr.substring(0, nameLen);
//
//            int surnameLen = hlpInStream.readInt();
//            String surnameStr = hlpInStream.readUTF();
//            surnameLen = Math.min(surnameLen, surnameStr.length());
//            this.surname = surnameStr.substring(0, surnameLen);
//
//            int day = hlpInStream.readInt();
//            int month = hlpInStream.readInt();
//            int year = hlpInStream.readInt();
//            this.dateOfBirth = LocalDate.of(year, month, day);
//
//            int idLen = hlpInStream.readInt();
//            String idStr = hlpInStream.readUTF();
//            idLen = Math.min(idLen, idStr.length());
//            this.personID = idStr.substring(0, idLen);
//
//        } catch (IOException e) {
//            throw new IllegalStateException("Error during conversion from byte array.");
//        }
    }

    @Override
    public String getOutput() {
        return "Test code: " + this.testCode + "  |  Date and time: " + this.testDateTime + "  |  Result: " + this.testResult + "  |  Value: " + this.testValue + "  |  Patient ID: " + this.testPersonID + "  |  Note: " + this.note;
    }
}
