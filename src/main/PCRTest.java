package main;

import structure.IData;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Class Test with attributes and methods to work with binary file.
 */
public class PCRTest implements IData<PCRTest> {
    private static final int ID_LIMIT = 10;
    private static final int NOTE_LIMIT = 11;
    private LocalDateTime testDateTime;
    private String testPersonID;// max 10 chars
    private int testCode;
    private boolean testResult;
    private double testValue;
    private String note;//max 11 chars

    public PCRTest() {
        this.testDateTime = LocalDateTime.now();
        this.testPersonID = "000000";
        this.testCode = -1;
        this.testResult = false;
        this.testValue = -1;
        this.note = "Default";
    }

    public PCRTest(LocalDateTime testDateTime, String testPersonID, int testCode, boolean testResult, double testValue, String note) {
        this.testDateTime = testDateTime;
        this.testPersonID = testPersonID;
        this.testCode = testCode;
        this.testResult = testResult;
        this.testValue = testValue;
        this.note = note;
    }

    /**
     * Compare two tests based on their code.
     */
    @Override
    public boolean equalsTo(PCRTest comparedData) {
        return this.testCode == comparedData.getTestCode();
    }

    public int getTestCode() {
        return this.testCode;
    }

    public String getPersonID() {
        return this.testPersonID;
    }

    @Override
    public PCRTest createClass() {
        return new PCRTest();
    }

    @Override
    public int getHashCode() {
        return Integer.hashCode(this.testCode);
    }

    /**
     * Size of stored Test in bytes.
     */
    @Override
    public int getSize() {
        return Long.BYTES +
                Integer.BYTES + 2 + ID_LIMIT +
                Integer.BYTES + 1 + Double.BYTES +
                Integer.BYTES + 2 + NOTE_LIMIT;
    }

    public LocalDateTime getDateTime() {
        return this.testDateTime;
    }

    public boolean getTestResult() {
        return this.testResult;
    }

    public double getTestValue() {
        return this.testValue;
    }

    public String getNote() {
        return this.note;
    }

    private String getFullString(String str, int maxLimit) {
        if (str.length() >= maxLimit) {
            return str.substring(0, maxLimit);
        }
        return str + "o".repeat(maxLimit - str.length());
    }

    /**
     * Create byte array from Test (all necessary info)
     */
    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        try {
            long dateTimeLong = this.testDateTime.toEpochSecond(ZoneOffset.UTC);
            hlpOutStream.writeLong(dateTimeLong);
            hlpOutStream.writeInt(Math.min(this.testPersonID.length(), ID_LIMIT));
            hlpOutStream.writeUTF(this.getFullString(this.testPersonID, ID_LIMIT));
            hlpOutStream.writeInt(this.testCode);
            hlpOutStream.writeBoolean(this.testResult);
            hlpOutStream.writeDouble(this.testValue);
            hlpOutStream.writeInt(Math.min(this.note.length(), NOTE_LIMIT));
            hlpOutStream.writeUTF(this.getFullString(this.note, NOTE_LIMIT));

            return hlpByteArrayOutputStream.toByteArray();
        } catch (IOException e){
            throw new IllegalStateException("Error during conversion to byte array.");
        }
    }

    /**
     * Define/create Test from byte array.
     */
    @Override
    public void fromBytes(byte[] from) {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(from);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);
        try {
            long dateTimeLong = hlpInStream.readLong();
            this.testDateTime = LocalDateTime.ofEpochSecond(dateTimeLong, 0, ZoneOffset.UTC);

            int idLen = hlpInStream.readInt();
            String idStr = hlpInStream.readUTF();
            idLen = Math.min(idLen, idStr.length());
            this.testPersonID = idStr.substring(0, idLen);

            this.testCode = hlpInStream.readInt();
            this.testResult = hlpInStream.readBoolean();
            this.testValue = hlpInStream.readDouble();

            int noteLen = hlpInStream.readInt();
            String noteStr = hlpInStream.readUTF();
            noteLen = Math.min(noteLen, noteStr.length());
            this.note = noteStr.substring(0, noteLen);

        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public String getOutput() {
        return "Test code: " + this.testCode + "  |  Date and time: " + this.testDateTime + "  |  Result: " + this.testResult + "  |  Value: " + this.testValue + "  |  Patient ID: " + this.testPersonID + "  |  Note: " + this.note;
    }
}
