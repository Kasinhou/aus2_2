package structure;

import avl.AVLTree;
import avl.MyInteger;

import java.io.*;
import java.util.ArrayList;

public class HeapFile<T extends IData<T>> {
    private String pathToFile;
    private int clusterSize;
    private int blockFactor;
    private int blockSize;
    private int sizeT;

//    private ArrayList<Integer> freeBlocks;
//    private ArrayList<Integer> partiallyFreeBlocks;

    private AVLTree<MyInteger> freeBlocks;
    private AVLTree<MyInteger> partiallyFreeBlocks;

    private RandomAccessFile raf;

    private Class<T> classType;

    public HeapFile(String pathToFile, int clusterSize, Class<T> classType) {
        this.pathToFile = pathToFile;
        this.clusterSize = clusterSize;
//        this.freeBlocks = new ArrayList<>();
//        this.partiallyFreeBlocks = new ArrayList<>();
        this.freeBlocks = new AVLTree<>();
        this.partiallyFreeBlocks = new AVLTree<>();
        this.classType = classType;
        try {
            T dummyData = this.classType.newInstance();
            this.sizeT = dummyData.getSize();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.blockFactor = (this.clusterSize - Integer.BYTES) / this.sizeT;
        this.blockSize = Integer.BYTES + this.sizeT * this.blockFactor;
        //vyratat blokovaci faktor - odpocitam najskor od velkosti clustra informacie o bloku, potom zvysok vydelim (celociselne) velkostou kolko ma jeden zaznam (getsize z T)
//        this.blockingFactor = (this.clusterSize - blockinfo) / getsizeT;
        // urcit cestu k suboru a velkost clustra (kolko zabera blok na disku)

        try {
            this.raf = new RandomAccessFile(pathToFile, "rw");
            this.raf.setLength(0);//TODO ak sa bude nacitavat tak zrusit
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        if (!this.partiallyFreeBlocks.isEmpty()) {
            blockAddress = this.partiallyFreeBlocks.findMinimum().getInteger();
            blockToInsert = this.readBlock(blockAddress);//TODO upravit zoznamy
        } else if (!this.freeBlocks.isEmpty()) {
            blockAddress = this.freeBlocks.findMinimum().getInteger();
            blockToInsert = this.readBlock(blockAddress);
            this.freeBlocks.delete(this.freeBlocks.findMinimum());
            if (this.blockFactor != 1) {
                this.partiallyFreeBlocks.insert(new MyInteger(blockAddress));
            }
        } else {
            try {
                blockAddress = (int) (this.raf.length() / this.clusterSize);
            } catch (IOException e) {
                System.out.println("Problem with raf length in insert.");
                throw new RuntimeException(e);
            }
            blockToInsert = new Block<>(this.blockFactor, this.classType);
            if (this.blockFactor != 1) {
                this.partiallyFreeBlocks.insert(new MyInteger(blockAddress));
            }
        }
        blockToInsert.addData(data);
        this.writeBlock(blockAddress, blockToInsert.getBytes());
//        this.raf.seek(blockAddress);
//        byte[] blockBytes = blockToInsert.getBytes();
//        this.raf.write(blockBytes);
//
//        //mozno skusit priradit hned ked pisem do suboru, nie oddelene
//        int empty = this.clusterSize - blockBytes.length;
//        if (empty > 0) {
//            byte[] emptyBytes = new byte[empty];
//            this.raf.write(emptyBytes);
//        }
        
        // inde???
        if (this.blockFactor != 1 && blockToInsert.getValidCount() == this.blockFactor) {
            this.partiallyFreeBlocks.delete(this.partiallyFreeBlocks.findMinimum());
        }
        return blockAddress;
    }

    private Block<T> readBlock(int address) {
        Block<T> block = new Block<>(this.blockFactor, this.classType);
        try {
            this.raf.seek((long) address * this.clusterSize);
        } catch (IOException e) {
            System.out.println("Problem with seeking in readBlock.");
            throw new RuntimeException(e);
        }
        byte[] readBytes = new byte[this.blockSize];
//        for (int i = 0; i < this.blockSize; ++i) {
//            readBytes[i] = this.raf.readByte();
//        }
        try {
            this.raf.readFully(readBytes);
        } catch (IOException e) {
            System.out.println("Problem with readFully in readBlock.");
            throw new RuntimeException(e);
        }
        block.fromBytes(readBytes);
        return block;
    }

    private void writeBlock(int address, byte[] blockBytes) {
        try {
            this.raf.seek((long) address * this.clusterSize);
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
    }

    /**
     * pristupim na adresu, spristupnim Block a v nom porovnavam T
     * @param address ta adresa bloku ktoru vratil insert
     * @return
     */
    public T get(int address, T dummyData) {
//        this.raf.seek(address * velkostbloku); celkovo velkost bloku zoberiem
        Block<T> block = this.readBlock(address);
        return block.findData(dummyData);
    }

    /**
     * pristupim na adresu, spristupnim Block a v nom porovnavam T
     * @param address ta adresa bloku ktoru vratil insert
     * @return bud T alebo true false
     */
    public void delete(int address, T dummyData) {
        // naseekujem sa na adresu bloku, precitam pole bajtov reprezentujuci ten blok na disku
        // v operacnej pamati Block, poslem tie bajty from bytes, rozkusuje sa mi to na T, pridam/nacitam aj s valid count
        //delete zapise to pole bajtov po zmazani naspat
        Block<T> block = this.readBlock(address);
        if (block.removeData(dummyData)) {
            this.writeBlock(address, block.getBytes());
//            System.out.println("Zmazane data zapisane do suboru");
            if (this.blockFactor == 1) {
                this.freeBlocks.insert(new MyInteger(address));
            } else if (block.getValidCount() == 0) {
                this.partiallyFreeBlocks.delete(new MyInteger(address));
                this.freeBlocks.insert(new MyInteger(address));
            } else if (block.getValidCount() == this.blockFactor - 1) {
                this.partiallyFreeBlocks.insert(new MyInteger(address));
            }
        } else {
            System.out.println("Something is wrong with writing to raf after deletion");
        }
        try {
            if (address == (this.raf.length() / this.clusterSize) - 1 && block.getValidCount() == 0) {
                this.handleFreeBlocks(address);
            }
        } catch (IOException e) {
            System.out.println("Something is wrong with deleting free blocks.");
            throw new RuntimeException(e);
        }
    }

    // vyriesenie volnych blokov na konci suboru
    private void handleFreeBlocks(int lastBlock) {
//        Collections.sort(this.freeBlocks);
        ArrayList<MyInteger> allFreeBlocks = this.freeBlocks.inOrder();
//        if (lastBlock != this.freeBlocks.findMaximum().getInteger()) {
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

    public void close() {
        System.out.println(this.partiallyFreeBlocks);
        System.out.println(this.freeBlocks);
        System.out.println(this.clusterSize);
        System.out.println(this.blockFactor);
        try {
            RandomAccessFile fileInfo = new RandomAccessFile("infoOP.bin", "rw");
            byte[] infoBytes = getInfoBytes();
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
        sb.append("\nFree blocks [").append(this.freeBlocks.size()).append("] ");
        ArrayList<MyInteger> allFreeBlocks = this.freeBlocks.inOrder();
        for (MyInteger i : allFreeBlocks) {
            sb.append("->").append(i.getInteger());
        }
        sb.append("\nPartially free blocks [").append(this.partiallyFreeBlocks.size()).append("] -> ");
        ArrayList<MyInteger> allPartiallyFreeBlocks = this.partiallyFreeBlocks.inOrder();
        for (MyInteger i : allPartiallyFreeBlocks) {
            sb.append("->").append(i.getInteger());
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
