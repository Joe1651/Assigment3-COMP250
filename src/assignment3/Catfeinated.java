package assignment3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Catfeinated implements Iterable<Cat> {
    public CatNode root;

    public Catfeinated() {
    }

    public Catfeinated(CatNode dNode) {
        this.root = dNode;
    }

    // Constructor that makes a shallow copy of a Catfeinated cafe
    // New CatNode objects, but same Cat objects
    public Catfeinated(Catfeinated cafe) {
        if (cafe == null || cafe.root == null) {
            this.root = null;
        } else {
            this.root = new CatNode(cafe.root.catEmployee);
            createCopy(cafe.root, this.root);
        }
    }

    private void createCopy(CatNode currNode, CatNode newRoot) {
        if (currNode != null) {
            if (currNode.junior != null) {
                newRoot.junior = new CatNode(currNode.junior.catEmployee);
                newRoot.junior.parent = newRoot;
                createCopy(currNode.junior, newRoot.junior);
            }
            if (currNode.senior != null) {
                newRoot.senior = new CatNode(currNode.senior.catEmployee);
                newRoot.senior.parent = newRoot;
                createCopy(currNode.senior, newRoot.senior);
            }
        }
    }

    // add a cat to the cafe database
    public void hire(Cat c) {
        if (root == null)
            root = new CatNode(c);
        else
            root = root.hire(c);
    }

    // removes a specific cat from the cafe database
    public void retire(Cat c) {
        if (root != null)
            root = root.retire(c);
    }

    // get the oldest hire in the cafe
    public Cat findMostSenior() {
        if (root == null)
            return null;
        return root.findMostSenior();
    }

    // get the newest hire in the cafe
    public Cat findMostJunior() {
        if (root == null)
            return null;
        return root.findMostJunior();
    }

    // returns a list of cats containing the top numOfCatsToHonor cats
    // in the cafe with the thickest fur. Cats are sorted in descending
    // order based on their fur thickness.
    public ArrayList<Cat> buildHallOfFame(int numOfCatsToHonor) {
        ArrayList<Cat> honorList = new ArrayList<>(numOfCatsToHonor);

        Catfeinated copy = new Catfeinated(this);
        addToList(honorList, copy, numOfCatsToHonor);
        return honorList;
    }

    private void addToList(ArrayList<Cat> list, Catfeinated cafe, int maxSize) {
        if (list.size() == maxSize || cafe.root == null)
            return;

        Cat best = cafe.root.catEmployee;
        list.add(best);

        cafe.retire(best);
        addToList(list, cafe, maxSize);
    }

    // Returns the expected grooming cost the cafe has to incur in the next numDays days
    public double budgetGroomingExpenses(int numDays) {
        Iterator<Cat> iter = this.iterator();
        double total = 0.0;
        while (iter.hasNext()) {
            Cat curCat = iter.next();
            if (curCat.getDaysToNextGrooming() <= numDays) {
                total += curCat.getExpectedGroomingCost();
            }
        }
        return total;
    }

    // returns a list of list of Cats.
    // The cats in the list at index 0 need be groomed in the next week.
    // The cats in the list at index i need to be groomed in i weeks.
    // Cats in each sublist are listed from most junior to most senior
    // (ascending order of seniority overall).
    public ArrayList<ArrayList<Cat>> getGroomingSchedule() {
        ArrayList<ArrayList<Cat>> groomList = new ArrayList<>();

        for (Cat c : this) {
            int numWeeks = c.getDaysToNextGrooming() / 7;

            while (groomList.size() <= numWeeks) {
                groomList.add(new ArrayList<Cat>());
            }
            groomList.get(numWeeks).add(c);
        }

        return groomList;
    }

    public Iterator<Cat> iterator() {
        return new CatfeinatedIterator(root);
    }

    public static class CatNode {
        public Cat catEmployee;
        public CatNode junior;
        public CatNode senior;
        public CatNode parent;

        public CatNode(Cat c) {
            this.catEmployee = c;
            this.junior = null;
            this.senior = null;
            this.parent = null;
        }

        // add the c to the tree rooted at this and returns the root of the resulting tree
        public CatNode hire(Cat c) {
            CatNode root = addCat(this, c, null);

            CatNode addedCat = findCat(root, c);

            while (addedCat.parent != null &&
                    addedCat.catEmployee.getFurThickness() >
                            addedCat.parent.catEmployee.getFurThickness()) {

                if (addedCat == addedCat.parent.junior) {
                    rightRotation(addedCat);
                } else {
                    leftRotation(addedCat);
                }
            }

            while (addedCat.parent != null) {
                addedCat = addedCat.parent;
            }
            return addedCat;
        }

        private void leftRotation(CatNode child) {
            CatNode parent = child.parent;
            CatNode grand = parent.parent;
            CatNode A = child.junior;

            child.parent = grand;
            if (grand != null) {
                if (grand.junior == parent) {
                    grand.junior = child;
                } else if (grand.senior == parent) {
                    grand.senior = child;
                }
            }

            child.junior = parent;
            parent.parent = child;

            parent.senior = A;
            if (A != null) {
                A.parent = parent;
            }
        }

        private void rightRotation(CatNode child) {
            CatNode parent = child.parent;
            CatNode grand = parent.parent;
            CatNode B = child.senior;

            child.parent = grand;
            if (grand != null) {
                if (grand.junior == parent) {
                    grand.junior = child;
                } else if (grand.senior == parent) {
                    grand.senior = child;
                }
            }

            child.senior = parent;
            parent.parent = child;

            parent.junior = B;
            if (B != null) {
                B.parent = parent;
            }
        }

        private CatNode addCat(CatNode node, Cat c, CatNode parent) {
            if (node == null) {
                CatNode newNode = new CatNode(c);
                newNode.parent = parent;
                return newNode;
            }

            if (c.getMonthHired() < node.catEmployee.getMonthHired()) {
                node.senior = addCat(node.senior, c, node);
            } else {
                node.junior = addCat(node.junior, c, node);
            }

            return node;
        }

        private CatNode findCat(CatNode node, Cat c) {
            if (node == null)
                return null;
            if (node.catEmployee.getMonthHired() == c.getMonthHired())
                return node;
            if (c.getMonthHired() < node.catEmployee.getMonthHired()) {
                return findCat(node.senior, c);
            } else {
                return findCat(node.junior, c);
            }
        }

        // remove c from the tree rooted at this and returns the root of the resulting tree
        public CatNode retire(Cat c) {
            CatNode toRetire = findCat(this, c);
            if (toRetire == null) {
                return goBackToRoot(this);
            }

            // no children
            if (toRetire.junior == null && toRetire.senior == null) {
                if (toRetire.parent == null) {
                    return null; // tree becomes empty
                } else {
                    if (toRetire.parent.junior == toRetire) {
                        toRetire.parent.junior = null;
                    } else {
                        toRetire.parent.senior = null;
                    }
                    toRetire.parent = null;
                    return goBackToRoot(this);
                }
            }

            // only junior child
            if (toRetire.junior != null && toRetire.senior == null) {
                CatNode child = toRetire.junior;
                if (toRetire.parent == null) {
                    child.parent = null;
                    return child;
                } else {
                    if (toRetire.parent.junior == toRetire) {
                        toRetire.parent.junior = child;
                    } else {
                        toRetire.parent.senior = child;
                    }
                    child.parent = toRetire.parent;
                    toRetire.parent = null;
                    toRetire.junior = null;
                    return goBackToRoot(this);
                }
            }

            // only senior child
            if (toRetire.junior == null && toRetire.senior != null) {
                CatNode child = toRetire.senior;
                if (toRetire.parent == null) {
                    child.parent = null;
                    return child;
                } else {
                    if (toRetire.parent.junior == toRetire) {
                        toRetire.parent.junior = child;
                    } else {
                        toRetire.parent.senior = child;
                    }
                    child.parent = toRetire.parent;
                    toRetire.parent = null;
                    toRetire.senior = null;
                    return goBackToRoot(this);
                }
            }

            // two children: use most senior in left (junior) subtree
            CatNode pred = toRetire.junior.findMostSeniorNode();
            toRetire.catEmployee = pred.catEmployee;
            removeNodeWithAtMostOneChild(pred);
            restoreHeap(toRetire);
            return goBackToRoot(toRetire);
        }

        private CatNode goBackToRoot(CatNode node) {
            while (node.parent != null) {
                node = node.parent;
            }
            return node;
        }

        // node has at most one child
        private void removeNodeWithAtMostOneChild(CatNode node) {
            CatNode child = (node.junior != null) ? node.junior : node.senior;

            if (node.parent != null) {
                if (node.parent.junior == node) {
                    node.parent.junior = child;
                } else {
                    node.parent.senior = child;
                }
            }
            if (child != null) {
                child.parent = node.parent;
            }

            node.parent = null;
            node.junior = null;
            node.senior = null;
        }

        private void restoreHeap(CatNode node) {
            if (node == null)
                return;

            while (true) {
                CatNode largest = node;

                if (node.junior != null &&
                        node.junior.catEmployee.getFurThickness() >
                                largest.catEmployee.getFurThickness()) {
                    largest = node.junior;
                }

                if (node.senior != null &&
                        node.senior.catEmployee.getFurThickness() >
                                largest.catEmployee.getFurThickness()) {
                    largest = node.senior;
                }

                if (largest == node)
                    break;

                if (largest == node.junior) {
                    rightRotation(largest);
                } else {
                    leftRotation(largest);
                }
            }
        }

        // returns the node (not cat) with highest seniority in this subtree
        private CatNode findMostSeniorNode() {
            CatNode cur = this;
            while (cur.senior != null) {
                cur = cur.senior;
            }
            return cur;
        }

        // find the cat with highest seniority in the tree rooted at this
        public Cat findMostSenior() {
            if (this.senior == null)
                return this.catEmployee;
            return this.senior.findMostSenior();
        }

        // find the cat with lowest seniority in the tree rooted at this
        public Cat findMostJunior() {
            if (this.junior == null)
                return this.catEmployee;
            return this.junior.findMostJunior();
        }

        public String toString() {
            String result = this.catEmployee.toString() + "\n";
            if (this.junior != null) {
                result += "junior than " + this.catEmployee.toString() + " :\n";
                result += this.junior.toString();
            }
            if (this.senior != null) {
                result += "senior than " + this.catEmployee.toString() + " :\n";
                result += this.senior.toString();
            }
            return result;
        }
    }

    public class CatfeinatedIterator implements Iterator<Cat> {
        ArrayList<Cat> cats;
        int currCat = -1;

        public CatfeinatedIterator(CatNode root) {
            cats = new ArrayList<Cat>();
            buildCatList(root);
        }

        private void buildCatList(CatNode root) {
            if (root != null) {
                buildCatList(root.junior);
                this.cats.add(root.catEmployee);
                buildCatList(root.senior);
            }
        }

        public Cat next() {
            if (this.hasNext()) {
                this.currCat += 1;
                return cats.get(currCat);
            }
            throw new NoSuchElementException("No element to iterate on.");
        }

        public boolean hasNext() {
            return currCat < (cats.size() - 1);
        }
    }
}