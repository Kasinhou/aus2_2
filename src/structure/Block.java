package structure;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Block represents one block which stored data according to block factor.
 */
public class Block<T extends IData<T>> implements IRecord {
    private ArrayList<T> dataArray;
    private int validCount;
    private int sizeT;
    private int blockFactor;

    private Class<T> classType;

    private int indexToOverflow;

    public Block(int blockFactor, Class<T> classType) {
        this.blockFactor = blockFactor;
        this.dataArray = new ArrayList<>(this.blockFactor);
        this.validCount = 0;
        this.classType = classType;
        this.indexToOverflow = -1;
        try {
            T dummyData = this.classType.newInstance();// namnozit konkretne instancie triedy
            this.sizeT = dummyData.getSize();
            for (int i = 0; i < this.blockFactor; ++i) {
                this.dataArray.add(this.classType.newInstance().createClass());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFull() {
        return this.validCount == this.blockFactor;
    }

    public void setIndexToOverflow(int address) {
        this.indexToOverflow = address;
    }

    public int getIndexToOverflow() {
        return this.indexToOverflow;
    }

    public int getValidCount() {
        return this.validCount;
    }

    public void setBlockToEmpty() {
        this.validCount = 0;
    }

    /**
     * Adding data to block
     */
    public void addData(T data) {
        this.dataArray.set(this.validCount, data);
        ++this.validCount;
    }

    /**
     * Traversing data array and comparing data using equals.
     * @return find data if not null
     */
    public T findData(T data) {
        for (int i = 0; i < this.validCount; ++i) {
            if (this.dataArray.get(i).equalsTo(data)) {
                return this.dataArray.get(i);
            }
        }
        return null;
    }

    /**
     * Editing data already in block.
     */
    public boolean editData(T data) {
        for (int i = 0; i < this.validCount; ++i) {
            if (this.dataArray.get(i).equalsTo(data)) {
                this.dataArray.set(i, data);
                return true;
            }
        }
        return false;
    }

    /**
     * Removing data from block by swaping with last valid and decremented valid count.
     */
    public boolean removeData(T data) {
        for (int i = 0; i < this.validCount; ++i) {
            if (this.dataArray.get(i).equalsTo(data)) {
                Collections.swap(this.dataArray, i, this.validCount - 1);
                --this.validCount;
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve only valid data in block.
     */
    public ArrayList<T> getValidDataArray() {
        ArrayList<T> valid = new ArrayList<>();
        for (int i = 0; i < this.validCount; ++i) {
            valid.add(this.dataArray.get(i));
        }
        return valid;
    }

    /**
     * How many bytes has block (valid count, index to overflow, all records)
     */
    @Override
    public int getSize() {
        return Integer.BYTES * 2 + this.blockFactor * this.sizeT;
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        try {
            hlpOutStream.writeInt(this.validCount);
            hlpOutStream.writeInt(this.indexToOverflow);
            for (T arrayT : this.dataArray) {
                byte[] bytesT = arrayT.getBytes();
                hlpOutStream.write(bytesT);
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
            this.validCount = hlpInStream.readInt();
            this.indexToOverflow = hlpInStream.readInt();
            for (int i = 0; i < this.blockFactor; ++i) {
                byte[] bytesT = new byte[this.sizeT];
                hlpInStream.readFully(bytesT);
                this.dataArray.get(i).fromBytes(bytesT);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("Valid count: ").append(this.validCount);
        sb.append("    |    Block factor: ").append(this.blockFactor);
        sb.append("    |    Block size: ").append(this.getSize());
        sb.append("    |    Size of record: ").append(this.sizeT);
        sb.append("    |    Index to next overflow: ").append(this.indexToOverflow);
        sb.append("\n---valid data---");
        for (int i = 0; i < this.blockFactor; ++i) {
            if (i == this.validCount) {
                sb.append("\n---invalid data---");
            }
            sb.append("\n[").append(i).append("] -> ");
            sb.append(this.dataArray.get(i).getOutput());
        }
        return sb.toString();
    }
}
