package avl;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class for structure BST with operations for using this structure.
 * @param <T> data
 */
public class BinarySearchTree<T extends IBSTData<T>> {
    private BSTNode<T> root;
    private int size = 0;

    public BinarySearchTree() {
    }

    protected BSTNode<T> getRoot() {
        return this.root;
    }

    protected void setRoot(BSTNode<T> newRoot) {
        this.root = newRoot;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public int size() {
        return this.size;
    }

    /**
     * Find data in tree.
     * @return data if exists, else null
     */
    public T find(T findData) {
        BSTNode<T> findNode = this.findNode(findData);
        if (findNode == null) {
            return null;
        }
        return findNode.getData();
    }

    /**
     * Help method to find node not data.
     * @return node with found data
     */
    protected BSTNode<T> findNode(T findData) {
        if (this.size == 0) {
            return null;
        }
        BSTNode<T> currentNode = root;
        boolean isNotFound = true;

        while (isNotFound) {
            int result = findData.compareTo(currentNode.getData());

            if (result < 0) {
                if (currentNode.getLeftSon() != null) {
                    currentNode = currentNode.getLeftSon();
                } else {
                    isNotFound = false;
                }
            } else if (result > 0) {
                if (currentNode.getRightSon() != null) {
                    currentNode = currentNode.getRightSon();
                } else {
                    isNotFound = false;
                }
            } else {
                return currentNode;
            }
        }
        return null;
    }

    // creating new node
    protected BSTNode<T> newNode(T newData) {
        return new BSTNode<>(newData);
    }

    /**
     * Insert node to BST if possible (duplicity is not available).
     * @param insertedData data which is going to be inserted
     * @return true if insert is successfully, false if there is duplicity or insertedData is null
     */
    public boolean insert(T insertedData) {
        if (insertedData == null) {
            return false;
        }
        BSTNode<T> insertedNode = this.newNode(insertedData);
        if (this.size == 0) {
            this.setRoot(insertedNode);
            ++this.size;
            return true;
        }
        BSTNode<T> currentNode = root;
        boolean isNotInserted = true;

        while (isNotInserted) {
            int result = insertedNode.getData().compareTo(currentNode.getData());

            if (result < 0) {
                if (currentNode.getLeftSon() != null) {
                    currentNode = currentNode.getLeftSon();
                } else {
                    currentNode.setLeftSon(insertedNode);
                    insertedNode.setParent(currentNode);
                    insertedNode.setIsLeftSon(true);
                    ++this.size;
                    return true;
                }
            } else if (result > 0) {
                if (currentNode.getRightSon() != null) {
                    currentNode = currentNode.getRightSon();
                } else {
                    currentNode.setRightSon(insertedNode);
                    insertedNode.setParent(currentNode);
                    insertedNode.setIsLeftSon(false);
                    ++this.size;
                    return true;
                }
            } else {
                isNotInserted = false;
            }
        }
        return false;
    }

    /**
     * Deleting node with data given as argument
     * @param deletedData data which I am trying to remove
     * @return true if node was removed, false if not (data was not found)
     */
    public boolean delete(T deletedData) {
        BSTNode<T> deletedNode = this.findNode(deletedData);
        if (deletedNode == null) {
            return false;
        }
        if (this.size == 1) {
            this.root = null;
            --this.size;
            return true;
        }
        BSTNode<T> deletedNodeParent = deletedNode.getParent();
        BSTNode<T> deletedNodeLeftSon = deletedNode.getLeftSon();
        BSTNode<T> deletedNodeRightSon = deletedNode.getRightSon();
        boolean hasLeft = deletedNodeLeftSon != null;
        boolean hasRight = deletedNodeRightSon != null;
        boolean deletedIsLeftSon = false;
        if (this.root != deletedNode) {
            deletedIsLeftSon = deletedNodeParent.getLeftSon() == deletedNode;//root
        }

        // node is leaf, has no sons
        if (!hasLeft && !hasRight) {
            if (deletedIsLeftSon) {
                deletedNodeParent.setLeftSon(null);
            } else {
                deletedNodeParent.setRightSon(null);
            }
        // node has only left son
        } else if (hasLeft && !hasRight) {
            if (this.root == deletedNode) {
                this.root = deletedNodeLeftSon;
            } else {
                if (deletedIsLeftSon) {
                    deletedNodeParent.setLeftSon(deletedNodeLeftSon);
                    deletedNodeLeftSon.setIsLeftSon(true);
                } else {
                    deletedNodeParent.setRightSon(deletedNodeLeftSon);
                    deletedNodeLeftSon.setIsLeftSon(false);
                }
            }
            deletedNodeLeftSon.setParent(deletedNodeParent);
        // node has only right son
        } else if (!hasLeft && hasRight) {
            if (this.root == deletedNode) {
                this.root = deletedNodeRightSon;
            } else {
                if (deletedIsLeftSon) {
                    deletedNodeParent.setLeftSon(deletedNodeRightSon);
                    deletedNodeRightSon.setIsLeftSon(true);
                } else {
                    deletedNodeParent.setRightSon(deletedNodeRightSon);
                    deletedNodeRightSon.setIsLeftSon(false);
                }
            }
            deletedNodeRightSon.setParent(deletedNodeParent);
        // node has both left and right sons
        } else {
            // the most left son from right subtree
            BSTNode<T> nextInOrder = this.nextInOrder(deletedNode);
            BSTNode<T> nextInOrderRightSon = nextInOrder.getRightSon();
            BSTNode<T> nextInOrderParent = nextInOrder.getParent();

            // if successor is not right son
            if (nextInOrderParent != deletedNode) {
                nextInOrderParent.setLeftSon(nextInOrderRightSon);
                nextInOrder.setRightSon(deletedNodeRightSon);
                deletedNodeRightSon.setParent(nextInOrder);
                if (nextInOrderRightSon != null) {
                    nextInOrderRightSon.setParent(nextInOrderParent);
                    nextInOrderRightSon.setIsLeftSon(true);
                }
            }
            // if successor is right son, just move
            nextInOrder.setParent(deletedNodeParent);
            nextInOrder.setLeftSon(deletedNodeLeftSon);
            nextInOrder.setIsLeftSon(deletedIsLeftSon);
            deletedNodeLeftSon.setParent(nextInOrder);
            if (this.root == deletedNode) {
                this.root = nextInOrder;
            } else {
                if (deletedIsLeftSon) {
                    deletedNodeParent.setLeftSon(nextInOrder);
                } else {
                    deletedNodeParent.setRightSon(nextInOrder);
                }
            }
        }
        // removing references to deleted node
        deletedNode.setLeftSon(null);
        deletedNode.setRightSon(null);
        deletedNode.setParent(null);
        deletedNode = null;
        --this.size;
        return true;
    }

    /**
     * Find successor in tree to node given as argument
     * @return successor
     */
    protected BSTNode<T> nextInOrder(BSTNode<T> currentNode) {
        if (currentNode == null) {
            return null;
        }
        BSTNode<T> next;
        if (currentNode.getRightSon() != null) {
            next = currentNode.getRightSon();
            while (next.getLeftSon() != null) {
                next = next.getLeftSon();
            }
        } else {
            next = currentNode;
            while (!next.isLeftSon() && next != this.root) {
                next = next.getParent();
            }
            next = next.getParent();
        }
        return next;
    }

    /**
     * Finding all data withing interval from min to max
     * @return list of all found data
     */
    public ArrayList<T> findInterval(T min, T max) {
        ArrayList<T> interval = new ArrayList<>();
        if (this.size == 0 || min == null || max == null) {
            return interval;
        }
        BSTNode<T> currentNode = root;
        boolean isNotFound = true;

        while (isNotFound) {
            int result = min.compareTo(currentNode.getData());

            if (result < 0) {
                if (currentNode.getLeftSon() != null) {
                    currentNode = currentNode.getLeftSon();
                } else {
                    isNotFound = false;
                }
            } else if (result > 0) {
                if (currentNode.getRightSon() != null) {
                    currentNode = currentNode.getRightSon();
                } else {
                    // need to find successor
                    currentNode = this.nextInOrder(currentNode);
                    isNotFound = false;
                }
            } else {
                isNotFound = false;
            }
        }

        if (currentNode == null || max.compareTo(currentNode.getData()) < 0) {
            return interval;
        }
        interval.add(currentNode.getData());
        BSTNode<T> next = this.nextInOrder(currentNode);
        // adding to list till maximum
        while (next != null && max.compareTo(next.getData()) >= 0) {
            interval.add(next.getData());
            next = this.nextInOrder(next);
        }
        return interval;
    }

    /**
     * Level order of tree
     * @return list of nodes in level order
     */
    public ArrayList<T> levelOrder() {
        ArrayList<T> levelOrderList = new ArrayList<>();
        if (this.size == 0) {
            System.out.println("Strom je prazdny.");
            return levelOrderList;
        }
        BSTNode<T> startingNode = this.root;
        LinkedList<BSTNode<T>> sons = new LinkedList<>();
        sons.addLast(startingNode);
        while (!sons.isEmpty()) {
            BSTNode<T> first = sons.getFirst();
            levelOrderList.add(first.getData());
            BSTNode<T> leftSon = first.getLeftSon();
            BSTNode<T> rightSon = first.getRightSon();
            if (leftSon != null) { sons.addLast(leftSon); }
            if (rightSon != null) { sons.addLast(rightSon); }
            sons.removeFirst();
        }
        return levelOrderList;
    }

    /**
     * In order traverse of tree
     * @return list with data in order from minimum to maximum
     */
    public ArrayList<T> inOrder() {
        ArrayList<T> inOrderList = new ArrayList<>();
        BSTNode<T> current = this.findNode(this.findMinimum());
        while (current != null) {
            inOrderList.add(current.getData());
            current = this.nextInOrder(current);
        }
        return inOrderList;
    }

    /**
     * Find minimal data in tree
     * @return data with the lowest key in tree
     */
    public T findMinimum() {
        BSTNode<T> current = this.root;
        if (current == null) {
            return null;
        }
        while (current.getLeftSon() != null) {
            current = current.getLeftSon();
        }
        return current.getData();
    }

    /**
     * Find maximal data in tree
     * @return data with the highest key in tree
     */
    public T findMaximum() {
        BSTNode<T> current = this.root;
        if (current == null) {
            return null;
        }
        while (current.getRightSon() != null) {
            current = current.getRightSon();
        }
        return current.getData();
    }
}
