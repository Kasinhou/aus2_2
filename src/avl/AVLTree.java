package avl;

/**
 * Class for structure AVL Tree with operations for using it.
 * @param <T> data stored in tree
 */
public class AVLTree<T extends IBSTData<T>> extends BinarySearchTree<T> {

    public AVLTree() {}

    @Override
    protected BSTNode<T> newNode(T data) {
        return new AVLNode<>(data);
    }

    /**
     * Insertion of node which contains data passed as argument
     * Rebalancing tree after insert with rules for AVL insert
     * @return if insertion was successfully
     */
    @Override
    public boolean insert(T insertedData) {
        boolean isInserted = super.insert(insertedData);
        if (!isInserted) {
            return false;
        }
        AVLNode<T> insertedNode = (AVLNode<T>) this.findNode(insertedData);
        // changing BF after insert
        AVLNode<T> currentNode = insertedNode.getParent();
        AVLNode<T> previousNode = insertedNode;
        while (currentNode != null) {
            if (previousNode.isLeftSon()) {
                currentNode.decreaseBalanceFactor();
            } else {
                currentNode.increaseBalanceFactor();
            }

            if (currentNode.getBalanceFactor() == 0) {
                return true;
            }
            if (currentNode.getBalanceFactor() == -2) {
                if (previousNode.getBalanceFactor() == -1) { // R rotation - R(current)
                    this.rightRotation(currentNode);
                    currentNode.setBalanceFactor(0);
                    previousNode.setBalanceFactor(0);
                } else { // LR rotation - L(previous), R(current)
                    AVLNode<T> previousRightSon = previousNode.getRightSon();
                    int previousRightSonBF = previousRightSon.getBalanceFactor();
                    this.leftRotation(previousNode);
                    this.rightRotation(currentNode);
                    if (previousRightSonBF == 0) {
                        currentNode.setBalanceFactor(0);
                        previousNode.setBalanceFactor(0);
                    } else if (previousRightSonBF == 1) {
                        currentNode.setBalanceFactor(0);
                        previousNode.setBalanceFactor(-1);
                        previousRightSon.setBalanceFactor(0);
                    } else {
                        currentNode.setBalanceFactor(1);
                        previousNode.setBalanceFactor(0);
                        previousRightSon.setBalanceFactor(0);
                    }
                }
                return true;
            } else if (currentNode.getBalanceFactor() == 2) {
                if (previousNode.getBalanceFactor() == 1) { // L rotation - L(current)
                    this.leftRotation(currentNode);
                    currentNode.setBalanceFactor(0);
                    previousNode.setBalanceFactor(0);
                } else { // RL rotation - R(previous), L(current)
                    AVLNode<T> previousLeftSon = previousNode.getLeftSon();
                    int previousLeftSonBF = previousLeftSon.getBalanceFactor();
                    this.rightRotation(previousNode);
                    this.leftRotation(currentNode);
                    if (previousLeftSonBF == 0) {
                        currentNode.setBalanceFactor(0);
                        previousNode.setBalanceFactor(0);
                    } else if (previousLeftSonBF == 1) {
                        currentNode.setBalanceFactor(-1);
                        previousNode.setBalanceFactor(0);
                        previousLeftSon.setBalanceFactor(0);
                    } else {
                        currentNode.setBalanceFactor(0);
                        previousNode.setBalanceFactor(1);
                        previousLeftSon.setBalanceFactor(0);
                    }
                }
                return true;
            }
            previousNode = currentNode;
            currentNode = currentNode.getParent();
        }
        return true;
    }

    /**
     * Deletion of node which contains data passed as argument
     * Rebalancing tree after deletion with rules for AVL delete
     * @return if deletion was successfully
     */
    @Override
    public boolean delete(T deletedData) {
        AVLNode<T> deletedNode = (AVLNode<T>) this.findNode(deletedData);
        if (deletedNode == null) {
            return false;
        }
        int deletedNodeBF = deletedNode.getBalanceFactor();
        // starting node to rebalancing after delete
        AVLNode<T> startingNodeToBalance = deletedNode.getParent();
        boolean startingToBalanceFromLeft = deletedNode.isLeftSon();
        if (deletedNode.getLeftSon() != null && deletedNode.getRightSon() != null) {
            AVLNode<T> nextInOrder = (AVLNode<T>) super.nextInOrder(deletedNode);
            if (nextInOrder.getParent() != deletedNode) {
                startingNodeToBalance = nextInOrder.getParent();
                startingToBalanceFromLeft = true;
            } else {
                startingNodeToBalance = nextInOrder;
                startingToBalanceFromLeft = false;
            }
            nextInOrder.setBalanceFactor(deletedNodeBF);
        }
        boolean isDeleted = super.delete(deletedData);
        if (!isDeleted) {
            return false;
        }

        // BF check and rebalancing
        AVLNode<T> currentNode = startingNodeToBalance;
        boolean currentFromLeft = startingToBalanceFromLeft;
        while (currentNode != null) {
            int currentBF = currentNode.getBalanceFactor();
            if (currentFromLeft) {
                currentNode.increaseBalanceFactor();
            } else {
                currentNode.decreaseBalanceFactor();
            }
            if (currentBF == 0) {
                return true;
            }
            // define variables to change bf after rotations and to go up after
            AVLNode<T> rotated = currentNode;
            int rotatedBF = rotated.getBalanceFactor();
            currentFromLeft = currentNode.isLeftSon();
            currentNode = currentNode.getParent();
            if (rotatedBF == -2) {
                AVLNode<T> rotatedLeftSon = rotated.getLeftSon();
                int rotatedLeftSonBF = rotatedLeftSon.getBalanceFactor();
                if (rotatedLeftSonBF <= 0) {// R(rotated)
                    this.rightRotation(rotated);
                    if (rotatedLeftSonBF == -1) {
                        rotated.setBalanceFactor(0);
                        rotatedLeftSon.setBalanceFactor(0);
                    } else {
                        rotated.setBalanceFactor(-1);
                        rotatedLeftSon.setBalanceFactor(1);
                        return true;
                    }
                } else {//L(rotatedLeftSon), R(rotated)
                    AVLNode<T> rotatedLeftSonRightSon = rotatedLeftSon.getRightSon();
                    int rotatedLeftSonRightSonBF = rotatedLeftSonRightSon.getBalanceFactor();
                    this.leftRotation(rotatedLeftSon);
                    this.rightRotation(rotated);
                    rotated.setBalanceFactor(0);
                    rotatedLeftSon.setBalanceFactor(0);
                    rotatedLeftSonRightSon.setBalanceFactor(0);
                    if (rotatedLeftSonRightSonBF == -1) {
                        rotated.setBalanceFactor(1);
                    } else if (rotatedLeftSonRightSonBF == 1) {
                        rotatedLeftSon.setBalanceFactor(-1);
                    }
                }
            } else if (rotatedBF == 2) {
                AVLNode<T> rotatedRightSon = rotated.getRightSon();
                int rotatedRightSonBF = rotatedRightSon.getBalanceFactor();
                if (rotatedRightSonBF >= 0) {// L(rotated)
                    this.leftRotation(rotated);
                    if (rotatedRightSonBF == 1) {
                        rotated.setBalanceFactor(0);
                        rotatedRightSon.setBalanceFactor(0);
                    } else {
                        rotated.setBalanceFactor(1);
                        rotatedRightSon.setBalanceFactor(-1);
                        return true;
                    }
                } else {//R(rotatedRightSon) a L(rotated)
                    AVLNode<T> rotatedRightSonLeftSon = rotatedRightSon.getLeftSon();
                    int rotatedRightSonLeftSonBF = rotatedRightSonLeftSon.getBalanceFactor();
                    this.rightRotation(rotatedRightSon);
                    this.leftRotation(rotated);
                    rotated.setBalanceFactor(0);
                    rotatedRightSon.setBalanceFactor(0);
                    rotatedRightSonLeftSon.setBalanceFactor(0);
                    if (rotatedRightSonLeftSonBF == 1) {
                        rotated.setBalanceFactor(-1);
                    } else if (rotatedRightSonLeftSonBF == -1) {
                        rotatedRightSon.setBalanceFactor(1);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Left rotation around node passed as argument
     */
    private void leftRotation(AVLNode<T> rotatedNode) {
        if (rotatedNode == null || rotatedNode.getRightSon() == null) {
            System.out.println("Left rotation unsuccessfully");
            return;
        }
        AVLNode<T> rotatedNodeRightSon = rotatedNode.getRightSon();
        AVLNode<T> rotatedNodeParent = rotatedNode.getParent();
        boolean isRotatedLeftSon = rotatedNode.isLeftSon();
        if (rotatedNodeRightSon.getLeftSon() != null) {
            AVLNode<T> rotatedNodeRightSonLeftSubtree = rotatedNodeRightSon.getLeftSon();
            rotatedNodeRightSonLeftSubtree.setParent(rotatedNode);
            rotatedNodeRightSonLeftSubtree.setIsLeftSon(false);
            rotatedNode.setRightSon(rotatedNodeRightSonLeftSubtree);
        } else {
            rotatedNode.setRightSon(null);
        }
        if (rotatedNodeParent != null) {
            if (isRotatedLeftSon) {
                rotatedNodeParent.setLeftSon(rotatedNodeRightSon);
            } else {
                rotatedNodeParent.setRightSon(rotatedNodeRightSon);
            }
        } else {
            super.setRoot(rotatedNodeRightSon);
        }
        rotatedNodeRightSon.setParent(rotatedNodeParent);
        rotatedNode.setParent(rotatedNodeRightSon);
        rotatedNodeRightSon.setLeftSon(rotatedNode);
        rotatedNode.setIsLeftSon(true);
        if (rotatedNodeParent != null) {
            rotatedNodeRightSon.setIsLeftSon(isRotatedLeftSon);
        } else {
            rotatedNodeRightSon.setIsLeftSon(false);
        }
    }

    /**
     * Right rotation around node passed as argument
     */
    private void rightRotation(AVLNode<T> rotatedNode) {
        if (rotatedNode == null || rotatedNode.getLeftSon() == null) {
            System.out.println("Right rotation unsuccessfully");
            return;
        }
        AVLNode<T> rotatedNodeLeftSon = rotatedNode.getLeftSon();
        AVLNode<T> rotatedNodeParent = rotatedNode.getParent();
        boolean isRotatedLeftSon = rotatedNode.isLeftSon();
        if (rotatedNodeLeftSon.getRightSon() != null) {
            AVLNode<T> rotatedNodeLeftSonRightSubtree = rotatedNodeLeftSon.getRightSon();
            rotatedNodeLeftSonRightSubtree.setParent(rotatedNode);
            rotatedNodeLeftSonRightSubtree.setIsLeftSon(true);
            rotatedNode.setLeftSon(rotatedNodeLeftSonRightSubtree);
        } else {
            rotatedNode.setLeftSon(null);
        }
        if (rotatedNodeParent != null) {
            if (isRotatedLeftSon) {
                rotatedNodeParent.setLeftSon(rotatedNodeLeftSon);
            } else {
                rotatedNodeParent.setRightSon(rotatedNodeLeftSon);
            }
        } else {
            super.setRoot(rotatedNodeLeftSon);
        }
        rotatedNodeLeftSon.setParent(rotatedNodeParent);
        rotatedNode.setParent(rotatedNodeLeftSon);
        rotatedNodeLeftSon.setRightSon(rotatedNode);
        rotatedNode.setIsLeftSon(false);
        if (rotatedNodeParent != null) {
            rotatedNodeLeftSon.setIsLeftSon(isRotatedLeftSon);
        } else {
            rotatedNodeLeftSon.setIsLeftSon(false);
        }
    }

    /**
     * This method is only to validate real balance of AVL nodes, used only in tester.
     * Used recursion and post order for computing height of node sons before checking node.
     */
    public boolean isNotAVL() {
        return this.getHeightOfNode((AVLNode<T>) super.getRoot()) == -1;
    }

    // returns either diff of heights of right and left subtree or -1 if unbalanced
    private int getHeightOfNode(AVLNode<T> currentNode) {
        if (currentNode == null) {
            return 0;
        }
        int leftSonHeight = this.getHeightOfNode(currentNode.getLeftSon());
        if (leftSonHeight == -1) {// not AVL
            return -1;
        }
        int rightSonHeight = this.getHeightOfNode(currentNode.getRightSon());
        if (rightSonHeight == -1) {// not AVL
            return -1;
        }

        // |BF| >= 2
        if (Math.abs(leftSonHeight - rightSonHeight) >= 2) {
            return -1;
        } else {
            return Math.max(leftSonHeight, rightSonHeight);
        }
    }
}
