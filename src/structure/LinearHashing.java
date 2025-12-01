package structure;

import java.util.ArrayList;

public class LinearHashing<T extends IData<T>> {
    private HeapFile<T> mainFile;//nie je management volnych blokov
    private HeapFile<T> overflowFile;

    private int splitPointer;
    private int blockGroupSize;
    private int level;
    private int insertedCount;

    Class<T> classType;

    public LinearHashing(String mainFilePath, int mainClusterSize, String overflowFilePath, int overflowClusterSize, Class<T> classType) {
        this.mainFile = new HeapFile<>(mainFilePath, mainClusterSize, classType, false);
        this.overflowFile = new HeapFile<>(overflowFilePath, overflowClusterSize, classType, true);
        this.splitPointer = 0;
        this.blockGroupSize = 4;
        this.level = 0;
        this.insertedCount = 0;
        this.classType = classType;
        for (int i = 0; i < this.blockGroupSize; ++i) {
            this.mainFile.writeBlock(i, new Block<>(this.mainFile.getBlockFactor(), this.classType).getBytes());
        }
    }

    //TODO isto int?
    public int insert(T data) {
        int hashCode = data.getHashCode();
//        int mainAddress = hashCode % (this.blockGroupSize * (1 << this.level));
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }
        //TODO skusit refactoring insertu na heapfile
        Block<T> block = this.mainFile.getBlock(blockAddress);
        if (!block.isFull()) {
            block.addData(data);
            this.mainFile.writeBlock(blockAddress, block.getBytes());
        } else {
            //TODO vlozit do overflowfile
            int overflowAddress = this.overflowFile.insert(data);
            block.setIndexToOverflow(overflowAddress);
        }

        if (this.getDensity() > 0.8) {//TODO zmenit, nastavit subor podmienok
            this.splitBlock();
        }


        return 0;
    }

    private void splitBlock() {
        Block<T> newBlock = new Block<>(this.mainFile.getBlockFactor(), this.classType);
        Block<T> currentBlock = this.mainFile.readBlock(this.splitPointer);
        int overflowIndex;
        do {
            ArrayList<T> validBlockData = currentBlock.getValidDataArray();
            for (T data : validBlockData) {
                int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
                if (newBlockAddress != this.splitPointer) {
                    currentBlock.removeData(data);
                    if (!newBlock.isFull()) {
                        newBlock.addData(data);
                    } else {
                        //TODO vlozit do overflowfile zretazeneho zoznamu
                        if (newBlock.getIndexToOverflow() == -1) {
                            int overflowAddress = this.overflowFile.insert(data);
                            newBlock.setIndexToOverflow(overflowAddress);//isto tot nebude prepisovat ak pride na iny
                            //trackovat poslednu adresu preplnujuceho?
                        } else {

                        }


//                        this.overflowFile.getBlock(overflowAddress).
                    }
                }
            }
            //overflow split
            overflowIndex = currentBlock.getIndexToOverflow();
            if (overflowIndex != -1) {
                currentBlock = this.overflowFile.getBlock(overflowIndex);
            }
        } while (overflowIndex != -1);
        //todo aj overflowfile
        this.mainFile.writeBlock(this.mainFile.getBlockCount(), newBlock.getBytes());//block na adrese na konci, alebo S + M*2^u

        ++this.splitPointer;
        if (this.splitPointer >= this.blockGroupSize * (1 << this.level)) { // uplna expanzia
            this.splitPointer = 0;
            ++this.level;
        }
    }

    //TODO navrhnut kedy sa bude rozdelovat
    private double getDensity() {
//        return insertedCount /
        return 0;
    }

    public T get(T data) {
        int hashCode = data.getHashCode();
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }
        return null;
    }
}
