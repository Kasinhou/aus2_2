package structure;

import avl.AVLTree;
import avl.MyInteger;

import java.io.*;
import java.util.ArrayList;

public class HeapFile<T extends IData<T>> {
    private String path;
    private String pathToInfo;
    private int clusterSize;
    private int blockFactor;
    private int blockSize;
    private int sizeT;

    private AVLTree<MyInteger> freeBlocks;
    private AVLTree<MyInteger> partiallyFreeBlocks;
    private boolean freeBlocksManagement;

    private RandomAccessFile raf;

    private Class<T> classType;

    public HeapFile(String pathToFile, int clusterSize, String pathToInfo,Class<T> classType, boolean freeBlocksManagement) {
        this.path = pathToFile;
        this.pathToInfo = pathToInfo;
        this.clusterSize = clusterSize;
        this.freeBlocks = new AVLTree<>();
        this.partiallyFreeBlocks = new AVLTree<>();
        this.freeBlocksManagement = freeBlocksManagement;
        this.classType = classType;
        try {
            T dummyData = this.classType.newInstance();
            this.sizeT = dummyData.getSize();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.blockFactor = (this.clusterSize - (2 * Integer.BYTES)) / this.sizeT;
        this.blockSize = 2 * Integer.BYTES + this.sizeT * this.blockFactor;
    }

    public void open() {
        try {
            this.raf = new RandomAccessFile(this.path, "rw");
            this.raf.setLength(0);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBlockFactor() {
        return this.blockFactor;
    }

    public Block<T> getBlock(int blockAddress) {
        return this.readBlock(blockAddress);
    }

    public int getBlockCount() {
        int count;
        try {
            count = (int) (this.raf.length() / this.clusterSize);
        } catch (IOException e) {
            System.out.println("Something is wrong with block count.");
            throw new RuntimeException(e);
        }
        return count;
    }

    /**
     *
     * @param data
     * @return adresa bloku kde to uskaldnil
     */
    public int insert(T data) {
        int blockAddress;
        Block<T> blockToInsert;
        if (this.freeBlocksManagement && !this.partiallyFreeBlocks.isEmpty()) {
            blockAddress = this.partiallyFreeBlocks.findMinimum().getInteger();
            blockToInsert = this.readBlock(blockAddress);
        } else if (this.freeBlocksManagement && !this.freeBlocks.isEmpty()) {
            blockAddress = this.freeBlocks.findMinimum().getInteger();
            blockToInsert = this.readBlock(blockAddress);
            this.freeBlocks.delete(this.freeBlocks.findMinimum());
            if (this.blockFactor != 1) {
                this.partiallyFreeBlocks.insert(new MyInteger(blockAddress));
            }
        } else {
            return this.writeNewBlock(data);
        }
        blockToInsert.addData(data);
        this.writeBlock(blockAddress, blockToInsert);

        return blockAddress;
    }

    public Block<T> readBlock(int address) {
        Block<T> block = new Block<>(this.blockFactor, this.classType);
        byte[] readBytes;
        try {
            this.raf.seek((long) address * this.clusterSize);
            readBytes = new byte[this.blockSize];
            this.raf.readFully(readBytes);
        } catch (IOException e) {
            System.out.println("Problem with readFully in readBlock.");
            System.out.println(address + " adresa");
            throw new RuntimeException(e);
        }
        block.fromBytes(readBytes);
        return block;
    }

    // only for free management blocks
    public int findFreeBlockAddress() {
        if (!this.freeBlocksManagement) {
            return -1;
        }
        if (!this.freeBlocks.isEmpty()) {
            return this.freeBlocks.findMinimum().getInteger();
        }
        return -1;
    }

    public int writeNewBlock(T data) {
        Block<T> block = new Block<>(this.blockFactor, this.classType);
        int blockAddress = this.getBlockCount();
        block.addData(data);
        this.writeBlock(blockAddress, block);
        return blockAddress;
    }

    //upravit management rovno tu? todo upravit na management, bud prvy z volnych alebo vytvorit novy blok
    public void writeBlock(int address, Block<T> block) {
        try {
            this.raf.seek((long) address * this.clusterSize);
            byte[] blockBytes = block.getBytes();
            this.raf.write(blockBytes);

            //mozno skusit priradit hned ked pisem do suboru, nie oddelene
            int empty = this.clusterSize - blockBytes.length;
            if (empty > 0) {
                byte[] emptyBytes = new byte[empty];
                this.raf.write(emptyBytes);
            }
        } catch (IOException e) {
            System.out.println("Problem in writeBlock.");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        if (!this.freeBlocksManagement) {
            return;
        }
        if (block.isFull()) {
            this.setBlockAsFull(address);
        } else if (block.getValidCount() != 0) {
            this.setBlockAsPartiallyFree(address);
        } else {
            this.setBlockAsFree(address);
        }
    }

    /**
     * pristupim na adresu, spristupnim Block a v nom porovnavam T
     * @param address ta adresa bloku ktoru vratil insert
     * @return
     */
    public T get(int address, T dummyData) {
        Block<T> block = this.readBlock(address);
        return block.findData(dummyData);
    }

    /**
     * pristupim na adresu, spristupnim Block a v nom porovnavam T
     * @param address ta adresa bloku ktoru vratil insert
     * @return bud T alebo true false
     */
    public void delete(int address, T dummyData) {
        Block<T> block = this.readBlock(address);
        if (block.removeData(dummyData)) {
            this.writeBlock(address, block);
//            if (!this.freeBlocksManagement) {
//                return;//ak nemam tak uz netreba dalej riesit
//            }
//            if (this.blockFactor == 1) {
////                this.freeBlocks.insert(new MyInteger(address));
//                this.setBlockAsFree(address);
//            } else if (block.getValidCount() == 0) {
////                this.partiallyFreeBlocks.delete(new MyInteger(address));
////                this.freeBlocks.insert(new MyInteger(address));
//                this.setBlockAsFree(address);
//            } else if (block.getValidCount() == this.blockFactor - 1) {
////                this.partiallyFreeBlocks.insert(new MyInteger(address));
//                this.setBlockAsPartiallyFree(address);
//            }
        } else {
            System.out.println("Something is wrong with writing to raf after deletion");
        }
//        if (address == (this.getBlockCount() - 1) && block.getValidCount() == 0) {
//            this.handleFreeBlocks(address);
//        }
    }

    // vyriesenie volnych blokov na konci suboru
    private void handleFreeBlocks(int lastBlock) {
        ArrayList<MyInteger> allFreeBlocks = this.freeBlocks.inOrder();
        if (lastBlock != (allFreeBlocks.get(allFreeBlocks.size() - 1)).getInteger()) {
            System.out.println("Something is wrong with addresses in free blocks array.");
        }
        int freeEndBlocksCount = 1;
        int previousBlock = lastBlock;

        for (int i = allFreeBlocks.size() - 2; i >= 0; --i) {
            int currentBlock = allFreeBlocks.get(i).getInteger();
            if (previousBlock - 1 != currentBlock) {
                break;
            }
            previousBlock = currentBlock;
            ++freeEndBlocksCount;
        }
        try {
            this.raf.setLength(this.raf.length() - ((long) freeEndBlocksCount * this.clusterSize));
            for (int i = 0; i < freeEndBlocksCount; ++i) {
                this.freeBlocks.delete(this.freeBlocks.findMaximum());
            }
        } catch (IOException e) {
            System.out.println("Something wrong with setting new length of raf (removed free blocks).");
            throw new RuntimeException(e);
        }
    }

    public void setBlockAsFree(int blockAddress) {
        if (!this.freeBlocksManagement) {
            return;
        }
        MyInteger mi = new MyInteger(blockAddress);
        this.freeBlocks.insert(mi);
//        if (this.blockFactor != 1) {
        this.partiallyFreeBlocks.delete(mi);
//        }

        //isto tu? lebo v linhash zapisujem blok, postupne by sa toto vykonalo
        if (blockAddress == this.getBlockCount() - 1) {
            this.handleFreeBlocks(blockAddress);
        }
    }

    public void setBlockAsPartiallyFree(int blockAddress) {
        if (!this.freeBlocksManagement) {
            return;
        }
        MyInteger mi = new MyInteger(blockAddress);
        if (this.blockFactor != 1) {
            this.partiallyFreeBlocks.insert(mi);
        }
//        if (this.freeBlocks.find(mi) != null) {
        this.freeBlocks.delete(mi);
//        }
    }

    public void setBlockAsFull(int blockAddress) {
        if (!this.freeBlocksManagement) {
            return;
        }
        MyInteger mi = new MyInteger(blockAddress);
        this.partiallyFreeBlocks.delete(mi);
        this.freeBlocks.delete(mi);
    }

    public void load() {
        try {
            this.raf = new RandomAccessFile(this.path, "rw");
            RandomAccessFile fileInfo = new RandomAccessFile(this.pathToInfo, "rw");
            this.clusterSize = fileInfo.readInt();
            this.blockFactor = fileInfo.readInt();
            this.freeBlocksManagement = fileInfo.readBoolean();
            int partiallyFreeSize = fileInfo.readInt();
            for (int i = 0; i < partiallyFreeSize; ++i) {
                this.partiallyFreeBlocks.insert(new MyInteger(fileInfo.readInt()));
            }
            int freeSize = fileInfo.readInt();
            for (int i = 0; i < freeSize; ++i) {
                this.freeBlocks.insert(new MyInteger(fileInfo.readInt()));
            }
            fileInfo.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
//        System.out.println(this.partiallyFreeBlocks);
//        System.out.println(this.freeBlocks);
//        System.out.println(this.clusterSize);
//        System.out.println(this.blockFactor);
        try {
            RandomAccessFile fileInfo = new RandomAccessFile(this.pathToInfo, "rw");
            byte[] infoBytes = this.getInfoBytes();
            fileInfo.write(infoBytes);
            fileInfo.close();

            this.raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getInfoBytes() throws IOException {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        hlpOutStream.writeInt(this.clusterSize);
        hlpOutStream.writeInt(this.blockFactor);
        hlpOutStream.writeBoolean(this.freeBlocksManagement);
        hlpOutStream.writeInt(this.partiallyFreeBlocks.size());
        ArrayList<MyInteger> allPartiallyFreeBlocks = this.partiallyFreeBlocks.inOrder();
        for (MyInteger i : allPartiallyFreeBlocks) {
            hlpOutStream.writeInt(i.getInteger());
        }
        ArrayList<MyInteger> allFreeBlocks = this.freeBlocks.inOrder();
        hlpOutStream.writeInt(allFreeBlocks.size());
        for (MyInteger i : allFreeBlocks) {
            hlpOutStream.writeInt(i.getInteger());
        }

        return hlpByteArrayOutputStream.toByteArray();
    }

    public String getAllOutput() {
        // block na adrese ..., ... zaznamy platne, validcount ...
        int count;
        try {
            count = (int) (this.raf.length() / this.clusterSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("cluster size:").append(this.clusterSize).append(" | number of blocks: ").append(count);
        sb.append("\nManagement of free blocks: ").append(this.freeBlocksManagement);
        sb.append("\nFree blocks [").append(this.freeBlocks.size()).append("] ");
        ArrayList<MyInteger> allFreeBlocks = this.freeBlocks.inOrder();
        int newLine = 1;
        for (MyInteger i : allFreeBlocks) {
            ++newLine;
            sb.append("->").append(i.getInteger());
            if (newLine % 30 == 0) {
                sb.append("\n");
            }
        }
        sb.append("\nPartially free blocks [").append(this.partiallyFreeBlocks.size()).append("] ");
        ArrayList<MyInteger> allPartiallyFreeBlocks = this.partiallyFreeBlocks.inOrder();
        newLine = 1;
        for (MyInteger i : allPartiallyFreeBlocks) {
            ++newLine;
            sb.append("->").append(i.getInteger());
            if (newLine % 30 == 0) {
                sb.append("\n");
            }
        }
        sb.append("\n");

        for (int i = 0; i < count; ++i) {
            sb.append("\nBLOCK ").append(i).append(", address ").append(i * this.clusterSize).append("\n=================================================================\n");
            try {
                this.raf.seek((long) i * this.clusterSize);
                byte[] bytes = new byte[this.clusterSize];
                this.raf.readFully(bytes);
                Block<T> block = new Block<>(this.blockFactor, this.classType);
                block.fromBytes(bytes);
                sb.append(block.getOutput()).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sb.append("=================================================================\n");

        }
        return sb.toString();
    }

    /**
     * Method used for fetching all valid data in heap file. ONLY for testing.
     */
    public ArrayList<T> getAllValidData() {
        ArrayList<T> data = new ArrayList<>();
        try {
            int count = (int) (this.raf.length() / this.clusterSize);
            for (int i = 0; i < count; ++i) {
                this.raf.seek((long) i * this.clusterSize);
                byte[] bytes = new byte[this.clusterSize];
                this.raf.readFully(bytes);
                Block<T> block = new Block<>(this.blockFactor, this.classType);
                block.fromBytes(bytes);
                data.addAll(block.getValidDataArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
