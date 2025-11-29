package structure;

public class LinearHashing<T extends IData<T>> {
    private HeapFile<T> mainFile;
    private HeapFile<T> overflowFile;


    public LinearHashing(String mainFilePath, int mainClusterSize, String overflowFilePath, int overflowClusterSize, Class<T> classType) {
        this.mainFile = new HeapFile<>(mainFilePath, mainClusterSize, classType);
        this.overflowFile = new HeapFile<>(overflowFilePath, overflowClusterSize, classType);
    }

    //TODO isto int?
    public int insert(T data) {
        return 0;
    }
}
