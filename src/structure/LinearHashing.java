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

    public LinearHashing(String mainFilePath, int mainClusterSize, String overflowFilePath, int overflowClusterSize, Class<T> classType) {
        this.mainFile = new HeapFile<>(mainFilePath, mainClusterSize, classType, false);
        this.overflowFile = new HeapFile<>(overflowFilePath, overflowClusterSize, classType, true);
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
            this.mainFile.writeBlock(i, new Block<>(this.mainFile.getBlockFactor(), this.classType).getBytes());
        }
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
            this.mainFile.writeBlock(blockAddress, block.getBytes());
            ++insertedMainCount;
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
            ++insertedOverflowCount;
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
                ++insertedOverflowCount;
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
        System.out.println(density + " " + densityMain + " " + densityOverflow);

        return (density > 0.8 && densityMain > 0.85) || (densityOverflow > 0.5 && densityMain > 0.8) || (insertedOverflowCount > (0.3 * mainBC * mainBF));
    }

    //todo spravit to ze ak je niekde volny blok tak pridam tam to co chcem zretazovat pri splite
    private void splitBlock() {
        if (!this.shouldSplit()) {
            return;
        }
        System.out.println("SPLIT");
        ++splitCount;
        Block<T> currentBlock = this.mainFile.readBlock(this.splitPointer);
        ArrayList<T> validBlockData = currentBlock.getValidDataArray();
        ArrayList<T> dataToNewAddress = new ArrayList<>();
        for (T data : validBlockData) {//z hlavneho suboru vsetky
            int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
            if (newBlockAddress != this.splitPointer) {//treba posunut do noveho bloku
                dataToNewAddress.add(data);//tie ktore sa budu posuvat
                currentBlock.removeData(data);
                --insertedMainCount;
            }
        }
        this.mainFile.writeBlock(this.splitPointer, currentBlock.getBytes());

        // prejst zretazenim a vsetky zaznamy prezeniem novou vyssou hesovackou
        int overflowIndex = currentBlock.getIndexToOverflow();
        while (overflowIndex != -1) {
            currentBlock = this.overflowFile.getBlock(overflowIndex);
            ArrayList<T> validOverflowBlockData = currentBlock.getValidDataArray();
            for (T data : validOverflowBlockData) {
                int newBlockAddress = Math.floorMod(data.getHashCode(), this.blockGroupSize * (1 << (this.level + 1)));
                if (newBlockAddress != this.splitPointer) {
                    dataToNewAddress.add(data);//tie ktore sa budu posuvat
                    currentBlock.removeData(data);// pridanie do volnych blokov ak je volny, aby sa nasledne mohli vlozit nejake ine zaznamy a zretazili sa
                    if (currentBlock.getValidCount() == 0) {
                        this.overflowFile.setBlockAsFree(overflowIndex);
                    }//aj ciastocne vyriesit
                    --insertedOverflowCount;
                }
            }
            this.overflowFile.writeBlock(overflowIndex, currentBlock.getBytes());
            overflowIndex = currentBlock.getIndexToOverflow();
        }

        //mam zoznam vsetkych ktore treba zapisat na novy blok, teraz musim cyklom prejst a pridat ich tam, pokial bude mainfile plny idem na overflow, ten moze byt tiez plny
        int moveCount = dataToNewAddress.size();
        int mainFileBF = this.mainFile.getBlockFactor();
        int toMainCount = Math.min(moveCount, mainFileBF);
        Block<T> newMainBlock = new Block<>(mainFileBF, this.classType);
        for (int i = 0; i < toMainCount; ++i) {
            newMainBlock.addData(dataToNewAddress.get(i));
            ++insertedMainCount;
        }
        if (mainFileBF >= moveCount) {
            this.mainFile.writeBlock(this.mainFile.getBlockCount(), newMainBlock.getBytes());//dalo by sa aj podla vzorca s+m*2^u
        } else {
            newMainBlock.setIndexToOverflow(this.overflowFile.getBlockCount());
            this.mainFile.writeBlock(this.mainFile.getBlockCount(), newMainBlock.getBytes());

            int overflowFileBF = this.overflowFile.getBlockFactor();
            int indexInBlock = 0;
            Block<T> newOverflowBlock = new Block<>(overflowFileBF, this.classType);
            for (int i = mainFileBF; i < moveCount; ++i) {
                ++indexInBlock;
                newOverflowBlock.addData(dataToNewAddress.get(i));
                ++insertedOverflowCount;
                if (indexInBlock == overflowFileBF && (i + 1) < moveCount) {
                    //zapisat block ked sa naplnit a pokial nie je posledny zaznam zo vsetkych
                    newOverflowBlock.setIndexToOverflow(this.overflowFile.getBlockCount() + 1);
                    this.overflowFile.writeBlock(this.overflowFile.getBlockCount(), newOverflowBlock.getBytes());

                    // vytvorit dalsi blok
                    newOverflowBlock = new Block<>(overflowFileBF, this.classType);
                    indexInBlock = 0;
                }
            }
            //posledny vzdy zapisem
            this.overflowFile.writeBlock(this.overflowFile.getBlockCount(), newOverflowBlock.getBytes());
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
        T foundData = this.mainFile.get(blockAddress, data);
        if (foundData != null) {
            block.removeData(foundData);
            block.addData(data);
            this.mainFile.writeBlock(blockAddress, block.getBytes());
            return true;
        }
        int indexToOverflow = block.getIndexToOverflow();
        while (indexToOverflow != -1) {
            foundData = this.overflowFile.get(indexToOverflow, data);
            if (foundData != null) {
                block.removeData(foundData);
                block.addData(data);
                this.overflowFile.writeBlock(indexToOverflow, block.getBytes());
                return true;
            }
            indexToOverflow = this.overflowFile.getBlock(indexToOverflow).getIndexToOverflow();
        }
        return false;
    }

    public void load() {

    }

    public void close() {
        this.mainFile.close();
        this.overflowFile.close();
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
