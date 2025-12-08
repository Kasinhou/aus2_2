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

    public int insert(T data) {
//        System.out.println("Inserted all = " + (insertedOverflowCount + insertedMainCount));
        int hashCode = data.getHashCode();
//        System.out.println("Hash code " + hashCode);
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }
//        System.out.println("block address " + blockAddress);
//        System.out.println("split " + this.splitPointer);

        //vlozenie do hlavneho suboru ak nie je plny
        Block<T> block = this.mainFile.getBlock(blockAddress);
        if (!block.isFull()) {
            block.addData(data);
            this.mainFile.writeBlock(blockAddress, block);
            ++insertedMainCount;
            this.splitBlock();
            return blockAddress;
        }

        //vlozenie prvykrat do preplnujuceho suboru
        int indexToOverflow = block.getIndexToOverflow();
        if (indexToOverflow == -1) {
            int addressToWrite = this.overflowFile.findFreeBlockAddress();
            if (addressToWrite != -1) {
                block.setIndexToOverflow(addressToWrite);
                this.mainFile.writeBlock(blockAddress, block);
                Block<T> overflowBlock = this.overflowFile.getBlock(addressToWrite);
                overflowBlock.addData(data);
                this.overflowFile.writeBlock(addressToWrite, overflowBlock);

            } else {
                addressToWrite = this.overflowFile.writeNewBlock(data);
                block.setIndexToOverflow(addressToWrite);
                this.mainFile.writeBlock(blockAddress, block);
            }
            ++insertedOverflowCount;
            this.splitBlock();
            return addressToWrite;
        }

        //musim ist zretazenim
        int previousIndex = -1;
        while (indexToOverflow != -1) {
            block = this.overflowFile.getBlock(indexToOverflow);
            if (!block.isFull()) {//todo refactor?
                // vlozit na prazdne miesto
                block.addData(data);
                this.overflowFile.writeBlock(indexToOverflow, block);
                ++insertedOverflowCount;
                this.splitBlock();
                return indexToOverflow;
            }
            previousIndex = indexToOverflow;
            indexToOverflow = block.getIndexToOverflow();
        }

        //vsetky preplnujuce su plne, treba zasa novy
        int newAddress = this.overflowFile.findFreeBlockAddress();
        if (newAddress != -1) {
            block.setIndexToOverflow(newAddress);
            this.overflowFile.writeBlock(previousIndex, block);
            Block<T> overflowBlock = this.overflowFile.getBlock(newAddress);
            overflowBlock.addData(data);
            this.overflowFile.writeBlock(newAddress, overflowBlock);
        } else {
            newAddress = this.overflowFile.writeNewBlock(data);
            block.setIndexToOverflow(newAddress);
            this.overflowFile.writeBlock(previousIndex, block);
        }
        ++insertedOverflowCount;
        this.splitBlock();
        return newAddress;
    }

    // navrhnut kedy sa bude rozdelovat, nastavit subor podmienok
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
//        System.out.println(density + " " + densityMain + " " + densityOverflow);

        return (density > 0.8 && densityMain > 0.85) || (densityOverflow > 0.5 && densityMain > 0.8) || (insertedOverflowCount > (0.3 * mainBC * mainBF));
    }

    private void splitBlock() {
        if (!this.shouldSplit()) {
            return;
        }
//        System.out.println("SPLIT");
        ++splitCount;
        Block<T> mainSplitBlock = this.mainFile.readBlock(this.splitPointer);
        ArrayList<T> validBlockData = mainSplitBlock.getValidDataArray();
        ArrayList<T> lowerHash = new ArrayList<>();//data ktore ostavaju na tej istej adrese
        ArrayList<T> higherHash = new ArrayList<>();
        int mainFileBF = this.mainFile.getBlockFactor();
        int overflowFileBF = this.overflowFile.getBlockFactor();

        for (T data : validBlockData) {//z hlavneho suboru vsetky
            int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
            if (newBlockAddress != this.splitPointer) {
                higherHash.add(data);
            } else {
                lowerHash.add(data);
            }
            --insertedMainCount;
        }
        mainSplitBlock.setBlockToEmpty();

        // prejst zretazenim a vsetky zaznamy prezeniem novou vyssou hesovackou
        ArrayList<Block<T>> overflowBlocksChain = new ArrayList<>();
        ArrayList<Integer> overflowAddresses = new ArrayList<>();//adresy do overflow
        int overflowIndex = mainSplitBlock.getIndexToOverflow();
        while (overflowIndex != -1) {
            Block<T> overflowBlock = this.overflowFile.getBlock(overflowIndex);
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
//        System.out.println("Lower hash records = " + lowerHash);
//        System.out.println("Higher hash records = " + higherHash);

        int higherIndexToOverflow;
        int lowerHashTCount = lowerHash.size();
        int lowerToMainCount = Math.min(lowerHashTCount, mainFileBF);
        for (int i = 0; i < lowerToMainCount; ++i) {
            mainSplitBlock.addData(lowerHash.get(i));
            ++insertedMainCount;
        }
        if (mainFileBF >= lowerHashTCount) {
            higherIndexToOverflow = mainSplitBlock.getIndexToOverflow();
            mainSplitBlock.setIndexToOverflow(-1);
            this.mainFile.writeBlock(this.splitPointer, mainSplitBlock);
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
                    //zapisat block ked sa naplnit a pokial nie je posledny zaznam zo vsetkych
                    if (blockChainIndex + 1 < overflowBlocksChain.size()) {//nemalo by nastat nikdy ze by sa nesplnilo
                        overflowBlockFromLowerHash.setIndexToOverflow(overflowAddresses.get(blockChainIndex + 1));//mozno zbytocne kedze by to tu uz malo byt ale istota
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

            // odstranit vsetky bloky pouzite na lower hash
            int usedBlocks = blockChainIndex + 1;
            for (int i = 0; i < usedBlocks; ++i) {
                overflowBlocksChain.remove(0);
                overflowAddresses.remove(0);
            }
        }

        //new main block a jeho preplnujuce
        int higherHashTCount = higherHash.size();
        Block<T> newMainBlock = new Block<>(mainFileBF, this.classType);
        int higherToMainCount = Math.min(higherHashTCount, mainFileBF);
        for (int i = 0; i < higherToMainCount; ++i) {
            newMainBlock.addData(higherHash.get(i));
            ++insertedMainCount;
        }
        if (mainFileBF >= higherHashTCount) {
            newMainBlock.setIndexToOverflow(-1);
            this.mainFile.writeBlock(this.splitPointer + this.blockGroupSize * (1 << this.level), newMainBlock);
        } else {
            // nastavit novy index to preplnujuceho
            if (higherIndexToOverflow == -1 && overflowBlocksChain.size() > 0) {
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
                        //zapisat block ked sa naplnit a pokial nie je posledny zaznam zo vsetkych
                        if (blockChainIndex + 1 < overflowBlocksChain.size()) {
                            overflowBlockFromHigherHash.setIndexToOverflow(overflowAddresses.get(blockChainIndex + 1));//pre istotu
                        }
                        this.overflowFile.writeBlock(currentAddress, overflowBlockFromHigherHash);

                        ++blockChainIndex;
                        overflowBlockFromHigherHash = overflowBlocksChain.get(blockChainIndex);
                        currentAddress = overflowAddresses.get(blockChainIndex);
                        indexInBlock = 0;
                    }
                }

                //posledny vzdy zapisem
                overflowBlockFromHigherHash.setIndexToOverflow(-1);
                this.overflowFile.writeBlock(currentAddress, overflowBlockFromHigherHash);

                int usedBlocks = blockChainIndex + 1;
                for (int i = 0; i < usedBlocks; ++i) {
                    overflowBlocksChain.remove(0);
                    overflowAddresses.remove(0);
                }
            }
        }

        // zostatkove bloky prdam do volnych blokov
        for (int i = 0; i < overflowBlocksChain.size(); ++i) {
            Block<T> block = overflowBlocksChain.get(i);
            int address = overflowAddresses.get(i);
//            System.out.println("Freeing block at address " + address);
            block.setIndexToOverflow(-1);
            this.overflowFile.writeBlock(address, block);
//            this.overflowFile.setBlockAsFree(address);
        }

        ++this.splitPointer;
        if (this.splitPointer >= this.blockGroupSize * (1 << this.level)) { // uplna expanzia
            this.splitPointer = 0;
            ++this.level;
        }
    }

    public T get(T data) {
        int hashCode = data.getHashCode();
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }

        Block<T> block = this.mainFile.getBlock(blockAddress);
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
            indexToOverflow = this.overflowFile.getBlock(indexToOverflow).getIndexToOverflow();
        }

        return null;
    }

    //todo neprepise to cele T, nahradi sa novym?
    public boolean edit(T data) {
        int hashCode = data.getHashCode();
        int blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << this.level));
        if (blockAddress < this.splitPointer) {
            blockAddress = Math.floorMod(hashCode, this.blockGroupSize * (1 << (this.level + 1)));
        }

        Block<T> block = this.mainFile.getBlock(blockAddress);
        if (block.editData(data)) {
            this.mainFile.writeBlock(blockAddress, block);
            return true;
        }
        int indexToOverflow = block.getIndexToOverflow();
        while (indexToOverflow != -1) {
            Block<T> overflowBlock = this.overflowFile.getBlock(indexToOverflow);
            if (overflowBlock.editData(data)) {
                this.overflowFile.writeBlock(indexToOverflow, overflowBlock);
                return true;
            }
            indexToOverflow = overflowBlock.getIndexToOverflow();
        }
        return false;
    }

    public String getOutput() {
        String out = "===============================START MAIN FILE=====================================\n\n" + this.mainFile.getAllOutput() + "\n===============================END MAIN FILE=========================================\n\n";
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
