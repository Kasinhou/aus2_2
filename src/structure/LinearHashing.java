package structure;

import java.util.ArrayList;

public class LinearHashing<T extends IData<T>> {
    private HeapFile<T> mainFile;//nie je management volnych blokov
    private HeapFile<T> overflowFile;

    private int splitPointer;
    private int blockGroupSize;
    private int level;
    private int insertedMainCount;
    private int insertedOverflowCount;
    private int splitCount;

    Class<T> classType;

    public LinearHashing(String mainFilePath, int mainClusterSize, String pathToInfoMain, String overflowFilePath, int overflowClusterSize, String pathToInfoOverflow, Class<T> classType) {
        this.mainFile = new HeapFile<>(mainFilePath, mainClusterSize, pathToInfoMain, classType, false);
        this.overflowFile = new HeapFile<>(overflowFilePath, overflowClusterSize, pathToInfoOverflow, classType, true);
        this.splitPointer = 0;
        this.blockGroupSize = 2;
        this.level = 0;
        this.insertedMainCount = 0;
        this.insertedOverflowCount = 0;
        this.splitCount = 0;
        this.classType = classType;
    }

    public void open() {
        this.mainFile.open();
        this.overflowFile.open();
        for (int i = 0; i < this.blockGroupSize; ++i) {
            this.mainFile.writeBlock(i, new Block<>(this.mainFile.getBlockFactor(), this.classType));
        }
    }

    // split, hesovacka, level, ostatne?
    public void load() {
        this.mainFile.load();
        this.overflowFile.load();
    }

    public void close() {
        this.mainFile.close();
        this.overflowFile.close();
    }

    /**
     * Insert data to LH according to rules, does split if conditions are true
     */
    public int insert(T data) {
        // calculate address of block based on hash
        int hashCode = data.getHashCode();
        int blockAddress = this.calculateAddress(hashCode);

        // insert data to main block if it is not full
        Block<T> block = this.mainFile.readBlock(blockAddress);
        if (!block.isFull()) {
            block.addData(data);
            this.mainFile.writeBlock(blockAddress, block);
            ++insertedMainCount;
            this.splitBlock();
            return blockAddress;
        }

        // main block is full but it has no next overflow
        int indexToOverflow = block.getIndexToOverflow();
        if (indexToOverflow == -1) {
            int addressToWrite = this.overflowFile.findFreeBlockAddress();
            // inserted to first free block in overflow
            if (addressToWrite != -1) {
                block.setIndexToOverflow(addressToWrite);
                this.mainFile.writeBlock(blockAddress, block);
                Block<T> overflowBlock = this.overflowFile.readBlock(addressToWrite);
                overflowBlock.addData(data);
                this.overflowFile.writeBlock(addressToWrite, overflowBlock);
            // there is no free overflow
            } else {
                addressToWrite = this.overflowFile.writeNewBlock(data);
                block.setIndexToOverflow(addressToWrite);
                this.mainFile.writeBlock(blockAddress, block);
            }
            ++insertedOverflowCount;
            this.splitBlock();
            return addressToWrite;
        }

        // main block has next overflow, find first free space for insert data
        int previousIndex = -1;
        while (indexToOverflow != -1) {
            block = this.overflowFile.readBlock(indexToOverflow);
            if (!block.isFull()) {
                block.addData(data);
                this.overflowFile.writeBlock(indexToOverflow, block);
                ++insertedOverflowCount;
                this.splitBlock();
                return indexToOverflow;
            }
            previousIndex = indexToOverflow;
            indexToOverflow = block.getIndexToOverflow();
        }

        // all overflow blocks in chain are full
        int newAddress = this.overflowFile.findFreeBlockAddress();
        // there is some free block
        if (newAddress != -1) {
            block.setIndexToOverflow(newAddress);
            this.overflowFile.writeBlock(previousIndex, block);
            Block<T> overflowBlock = this.overflowFile.readBlock(newAddress);
            overflowBlock.addData(data);
            this.overflowFile.writeBlock(newAddress, overflowBlock);
        // there is no other free block
        } else {
            newAddress = this.overflowFile.writeNewBlock(data);
            block.setIndexToOverflow(newAddress);
            this.overflowFile.writeBlock(previousIndex, block);
        }
        ++insertedOverflowCount;
        this.splitBlock();
        return newAddress;
    }

    /**
     * Find data in LH. Returns either found data or null.
     */
    public T get(T data) {
        int hashCode = data.getHashCode();
        int blockAddress = this.calculateAddress(hashCode);

        Block<T> block = this.mainFile.readBlock(blockAddress);
        T foundData = this.mainFile.get(blockAddress, data);
        if (foundData != null) {
            return foundData;
        }
        int indexToOverflow = block.getIndexToOverflow();
        while (indexToOverflow != -1) {
            foundData = this.overflowFile.get(indexToOverflow, data);
            if (foundData != null) {
                return foundData;
            }
            indexToOverflow = this.overflowFile.readBlock(indexToOverflow).getIndexToOverflow();
        }
        return null;
    }

    /**
     * Edit data in LH to data passed as argument.
     */
    public boolean edit(T data) {
        int hashCode = data.getHashCode();
        int blockAddress = this.calculateAddress(hashCode);

        Block<T> block = this.mainFile.readBlock(blockAddress);
        if (block.editData(data)) {
            this.mainFile.writeBlock(blockAddress, block);
            return true;
        }
        int indexToOverflow = block.getIndexToOverflow();
        while (indexToOverflow != -1) {
            Block<T> overflowBlock = this.overflowFile.readBlock(indexToOverflow);
            if (overflowBlock.editData(data)) {
                this.overflowFile.writeBlock(indexToOverflow, overflowBlock);
                return true;
            }
            indexToOverflow = overflowBlock.getIndexToOverflow();
        }
        return false;
    }

    // calculate block address based on hash
    private int calculateAddress(int hashCode) {
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }
        return blockAddress;
    }

    // method which check if the split should happen based on several conditions
    private boolean shouldSplit() {
        int mainBC = this.mainFile.getBlockCount();
        int overflowBC = this.overflowFile.getBlockCount();
        int mainBF = this.mainFile.getBlockFactor();
        int overflowBF = this.overflowFile.getBlockFactor();

        double density = (double) (insertedMainCount + insertedOverflowCount) / ((mainBC * mainBF) + (overflowBC * overflowBF));
        double densityMain = (double) insertedMainCount / (mainBC * mainBF);
        double densityOverflow = 0.0;
        if (overflowBC != 0) {
            densityOverflow = (double) insertedOverflowCount / (overflowBC * overflowBF);
        }

        return (density > 0.8 && densityMain > 0.85) || (densityOverflow > 0.5 && densityMain > 0.8) || (insertedOverflowCount > (0.3 * mainBC * mainBF));
    }

    // splitting block in case if the conditions are true
    private void splitBlock() {
        if (!this.shouldSplit()) {
            return;
        }
        ++splitCount;
        Block<T> mainSplitBlock = this.mainFile.readBlock(this.splitPointer);
        ArrayList<T> validBlockData = mainSplitBlock.getValidDataArray();
        ArrayList<T> lowerHash = new ArrayList<>();//data ktore ostavaju na tej istej adrese
        ArrayList<T> higherHash = new ArrayList<>();
        int mainFileBF = this.mainFile.getBlockFactor();
        int overflowFileBF = this.overflowFile.getBlockFactor();

        // data in split pointer block in main file divided into two groups (which hash is used)
        for (T data : validBlockData) {
            int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
            if (newBlockAddress != this.splitPointer) {
                higherHash.add(data);
            } else {
                lowerHash.add(data);
            }
            --insertedMainCount;
        }
        mainSplitBlock.setBlockToEmpty();

        // same principle as above, traverse withing overflow blocks chain from split pointer block in main file and divided data into two groups
        ArrayList<Block<T>> overflowBlocksChain = new ArrayList<>();
        ArrayList<Integer> overflowAddresses = new ArrayList<>();// overflow addresses in chain
        int overflowIndex = mainSplitBlock.getIndexToOverflow();
        while (overflowIndex != -1) {
            Block<T> overflowBlock = this.overflowFile.readBlock(overflowIndex);
            ArrayList<T> validOverflowBlockData = overflowBlock.getValidDataArray();
            for (T data : validOverflowBlockData) {
                int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
                if (newBlockAddress != this.splitPointer) {
                    higherHash.add(data);
                } else {
                    lowerHash.add(data);
                }
                --insertedOverflowCount;
            }
            overflowBlock.setBlockToEmpty();
            overflowBlocksChain.add(overflowBlock);
            overflowAddresses.add(overflowIndex);
            overflowIndex = overflowBlock.getIndexToOverflow();
        }

        // splitting, writing data back based on hash
        int lowerHashTCount = lowerHash.size();
        int lowerToMainCount = Math.min(lowerHashTCount, mainFileBF);
        int higherIndexToOverflow;
        // adding data to main block (split pointer)
        for (int i = 0; i < lowerToMainCount; ++i) {
            mainSplitBlock.addData(lowerHash.get(i));
            ++insertedMainCount;
        }
        // all data from lower hash are in main block
        if (mainFileBF >= lowerHashTCount) {
            higherIndexToOverflow = mainSplitBlock.getIndexToOverflow();
            mainSplitBlock.setIndexToOverflow(-1);
            this.mainFile.writeBlock(this.splitPointer, mainSplitBlock);
        // remaining data is written to overflow blocks chain
        } else {
            mainSplitBlock.setIndexToOverflow(overflowAddresses.get(0));
            this.mainFile.writeBlock(this.splitPointer, mainSplitBlock);

            int blockChainIndex = 0;
            Block<T> overflowBlockFromLowerHash = overflowBlocksChain.get(blockChainIndex);
            int currentAddress = overflowAddresses.get(blockChainIndex);
            int indexInBlock = 0;

            for (int i = mainFileBF; i < lowerHashTCount; ++i) {
                ++indexInBlock;
                overflowBlockFromLowerHash.addData(lowerHash.get(i));
                ++insertedOverflowCount;
                if (indexInBlock == overflowFileBF && (i + 1) < lowerHashTCount) {
                    // if not last data, writes overflow block if it is already full
                    if (blockChainIndex + 1 < overflowBlocksChain.size()) {//nemalo by nastat nikdy ze by sa nesplnilo
                        overflowBlockFromLowerHash.setIndexToOverflow(overflowAddresses.get(blockChainIndex + 1));
                    }
                    this.overflowFile.writeBlock(currentAddress, overflowBlockFromLowerHash);

                    ++blockChainIndex;
                    overflowBlockFromLowerHash = overflowBlocksChain.get(blockChainIndex);
                    currentAddress = overflowAddresses.get(blockChainIndex);
                    indexInBlock = 0;
                }
            }

            if (blockChainIndex + 1 < overflowBlocksChain.size()) {
                higherIndexToOverflow = overflowAddresses.get(blockChainIndex + 1);
            } else {
                higherIndexToOverflow = -1;
            }
            overflowBlockFromLowerHash.setIndexToOverflow(-1);
            this.overflowFile.writeBlock(currentAddress, overflowBlockFromLowerHash);

            // removing used blocks used for data in lower hash
            int usedBlocks = blockChainIndex + 1;
            for (int i = 0; i < usedBlocks; ++i) {
                overflowBlocksChain.remove(0);
                overflowAddresses.remove(0);
            }
        }

        // all data from lower hash are done, now do data from higher hash
        int higherHashTCount = higherHash.size();
        Block<T> newMainBlock = new Block<>(mainFileBF, this.classType);
        int higherToMainCount = Math.min(higherHashTCount, mainFileBF);
        // fill firstly new block in main file
        for (int i = 0; i < higherToMainCount; ++i) {
            newMainBlock.addData(higherHash.get(i));
            ++insertedMainCount;
        }
        // all data from higher hash are in main file
        if (mainFileBF >= higherHashTCount) {
            newMainBlock.setIndexToOverflow(-1);
            this.mainFile.writeBlock(this.splitPointer + this.blockGroupSize * (1 << this.level), newMainBlock);
        // remaining data from higher hash add to overflow blocks chain
        } else {
            // set new index to overflow
            if (higherIndexToOverflow == -1 && !overflowBlocksChain.isEmpty()) {
                higherIndexToOverflow = overflowAddresses.get(0);
            }
            newMainBlock.setIndexToOverflow(higherIndexToOverflow);
            this.mainFile.writeBlock(this.splitPointer + this.blockGroupSize * (1 << this.level), newMainBlock);

            if (higherIndexToOverflow != -1) {
                int blockChainIndex = 0;
                Block<T> overflowBlockFromHigherHash = overflowBlocksChain.get(blockChainIndex);
                int currentAddress = overflowAddresses.get(blockChainIndex);
                int indexInBlock = 0;
                for (int i = mainFileBF; i < higherHashTCount; ++i) {
                    ++indexInBlock;
                    overflowBlockFromHigherHash.addData(higherHash.get(i));
                    ++insertedOverflowCount;
                    if (indexInBlock == overflowFileBF && (i + 1) < higherHashTCount) {
                        // if not last data, writes overflow block if it is already full
                        if (blockChainIndex + 1 < overflowBlocksChain.size()) {
                            overflowBlockFromHigherHash.setIndexToOverflow(overflowAddresses.get(blockChainIndex + 1));
                        }
                        this.overflowFile.writeBlock(currentAddress, overflowBlockFromHigherHash);

                        ++blockChainIndex;
                        overflowBlockFromHigherHash = overflowBlocksChain.get(blockChainIndex);
                        currentAddress = overflowAddresses.get(blockChainIndex);
                        indexInBlock = 0;
                    }
                }

                // writes last block
                overflowBlockFromHigherHash.setIndexToOverflow(-1);
                this.overflowFile.writeBlock(currentAddress, overflowBlockFromHigherHash);

                // removing used blocks used for data in higher hash
                int usedBlocks = blockChainIndex + 1;
                for (int i = 0; i < usedBlocks; ++i) {
                    overflowBlocksChain.remove(0);
                    overflowAddresses.remove(0);
                }
            }
        }

        // remaining blocks are written and set as free blocks
        for (int i = 0; i < overflowBlocksChain.size(); ++i) {
            Block<T> block = overflowBlocksChain.get(i);
            int address = overflowAddresses.get(i);
            block.setIndexToOverflow(-1);
            this.overflowFile.writeBlock(address, block);
        }

        ++this.splitPointer;
        if (this.splitPointer >= this.blockGroupSize * (1 << this.level)) { // uplna expanzia
            this.splitPointer = 0;
            ++this.level;
        }
    }

    public String getOutput() {
        String out = "===============================START MAIN FILE=====================================\n\n" + this.mainFile.getAllOutput() + "\n===============================END MAIN FILE=========================================\n\n";
        out += "===============================START OVERFLOW FILE==================================\n\n" + this.overflowFile.getAllOutput() + "\n===============================END OVERFLOW FILE=====================================\n\n";
        return out;
    }

    // Method used in tester to verify if there is no data missing
    public ArrayList<T> getAllValidData() {
        ArrayList<T> allData = new ArrayList<>();
        allData.addAll(this.mainFile.getAllValidData());
        allData.addAll(this.overflowFile.getAllValidData());
        return allData;
    }
}
