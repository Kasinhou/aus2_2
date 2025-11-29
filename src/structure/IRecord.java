package structure;

public interface IRecord {
    int getSize();
    byte[] getBytes();
    void fromBytes(byte[] from);
    String getOutput();
}
