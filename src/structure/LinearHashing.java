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
        System.out.println("Hash code " + hashCode);
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }

        //vlozenie do hlavneho suboru ak nie je plny
        Block<T> block = this.mainFile.getBlock(blockAddress);
        if (!block.isFull()) {
            block.addData(data);
            this.mainFile.writeBlock(blockAddress, block.getBytes());
            this.splitBlock();
            return blockAddress;
        }

        //vlozenie prvykrat do preplnujuceho suboru
        int indexToOverflow = block.getIndexToOverflow();
        if (indexToOverflow == -1) {
            int addressToWrite = this.overflowFile.getBlockCount();
            block.setIndexToOverflow(addressToWrite);
            this.mainFile.writeBlock(blockAddress, block.getBytes());
            this.overflowFile.writeNewBlock(data);
            this.splitBlock();
            return addressToWrite;
        }

        //musim ist zretazenim
        int previousIndex = -1;
        while (indexToOverflow != -1) {
            block = this.overflowFile.getBlock(indexToOverflow);
            if (!block.isFull()) {
                // vlozit na prazdne miesto
                block.addData(data);
                this.overflowFile.writeBlock(indexToOverflow, block.getBytes());
                this.splitBlock();
                return indexToOverflow;
            }
            previousIndex = indexToOverflow;
            indexToOverflow = block.getIndexToOverflow();
        }

        //vsetky preplnujuce su plne, treba zasa novy
        int newAddress = this.overflowFile.getBlockCount();
        block.setIndexToOverflow(newAddress);
        this.overflowFile.writeBlock(previousIndex, block.getBytes());
        this.overflowFile.writeNewBlock(data);

        this.splitBlock();
        return newAddress;
    }

    private void splitBlock() {
        if (this.getDensity() < 0.8) {
            System.out.println("NOT split");
            return;
        }
        Block<T> newBlock = new Block<>(this.mainFile.getBlockFactor(), this.classType);
        Block<T> currentBlock = this.mainFile.readBlock(this.splitPointer);
        ArrayList<T> validBlockData = currentBlock.getValidDataArray();
        ArrayList<T> dataToNewAddress = new ArrayList<>();
        for (T data : validBlockData) {
            int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
            if (newBlockAddress != this.splitPointer) {//treba posunut do noveho bloku
                dataToNewAddress.add(data);//tie ktore sa budu posuvat
                currentBlock.removeData(data);
            }
        }
        this.mainFile.writeBlock(this.splitPointer, currentBlock.getBytes());
        int overflowIndex = currentBlock.getIndexToOverflow();
        while (overflowIndex != -1) {
            currentBlock = this.overflowFile.getBlock(overflowIndex);
            ArrayList<T> validOverflowBlockData = currentBlock.getValidDataArray();
            for (T data : validOverflowBlockData) {
                int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
                if (newBlockAddress != this.splitPointer) {
                    dataToNewAddress.add(data);//tie ktore sa budu posuvat
                    currentBlock.removeData(data);
                }
            }
            this.overflowFile.writeBlock(overflowIndex, currentBlock.getBytes());
            overflowIndex = currentBlock.getIndexToOverflow();
        }


        //todo aj overflowfile
//        this.mainFile.writeBlock(this.mainFile.getBlockCount(), newBlock.getBytes());//block na adrese na konci, alebo S + M*2^u

        ++this.splitPointer;
        if (this.splitPointer >= this.blockGroupSize * (1 << this.level)) { // uplna expanzia
            this.splitPointer = 0;
            ++this.level;
        }
    }

    //TODO navrhnut kedy sa bude rozdelovat, //TODO zmenit, nastavit subor podmienok
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

    public String getOutput() {
        String out = "===============================START MAIN FILE=====================================\n\n" + this.mainFile.getAllOutput() + "\n===============================END MAIN FILE========================================\n\n";
        out += "===============================START OVERFLOW FILE==================================\n\n" + this.overflowFile.getAllOutput() + "\n===============================END OVERFLOW FILE=====================================\n\n";
        return out;
    }

    public ArrayList<T> getAllValidData() {
        ArrayList<T> allData = new ArrayList<>();
        allData.addAll(this.mainFile.getAllValidData());
        allData.addAll(this.overflowFile.getAllValidData());
        return allData;
    }
}
