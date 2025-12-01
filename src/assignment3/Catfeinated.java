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
        CatNode newRoot = new CatNode(cafe.root.catEmployee);
        createCopy(cafe.root, newRoot);
        this.root = newRoot;
	}

    private void createCopy(CatNode currNode, CatNode newRoot) {
        // Goes through the tree with pre-order traversal and creates the tree again
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

        // work on a copy of the whole cafe
        Catfeinated copy = new Catfeinated(this);

        addToList(honorList, copy, numOfCatsToHonor);
        return honorList;
    }

    private void addToList(ArrayList<Cat> list, Catfeinated cafe, int maxSize) {
        if (list.size() == maxSize || cafe.root == null) return;

        // root has thickest fur because of the heap property
        Cat best = cafe.root.catEmployee;
        list.add(best);

        cafe.retire(best);              // modifies only the copy
        System.out.println(cafe.root);
        addToList(list, cafe, maxSize); // recurse on the modified copy
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
	// Cats in each sublist are listed in from most senior to most junior. 
    public ArrayList<ArrayList<Cat>> getGroomingSchedule() {
        ArrayList<ArrayList<Cat>> groomList = new ArrayList<>();

        // iterate over this cafe; your iterator already exists
        for (Cat c : this) {
            int numWeeks = c.getDaysToNextGrooming() / 7;

            // ensure there is a sublist at index numWeeks
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
        // add the c to the tree rooted at this and returns the root of the resulting tree
        public CatNode hire (Cat c) {
            CatNode root = addCat(this, c, null);

            // trouver le noeud inséré
            CatNode addedCat = findCat(root, c);

            // upheap basé sur furThickness
            while (addedCat.parent != null &&
                    addedCat.catEmployee.getFurThickness() >
                            addedCat.parent.catEmployee.getFurThickness()) {

                // fils gauche -> right rotation
                if (addedCat == addedCat.parent.junior) {
                    rightRotation(addedCat);
                } else { // fils droit -> left rotation
                    leftRotation(addedCat);
                }
            }

            // remonter jusqu'à la racine
            while (addedCat.parent != null) {
                addedCat = addedCat.parent;
            }
            return addedCat;
        }


        // child est le fils droit (senior) qui doit monter
        private void leftRotation(CatNode child) {
            CatNode parent = child.parent;    // P
            CatNode grand  = parent.parent;   // peut être null
            CatNode A      = child.junior;    // A

            // rattacher child au grand-parent
            child.parent = grand;
            if (grand != null) {
                if (grand.junior == parent) {
                    grand.junior = child;
                } else if (grand.senior == parent) {
                    grand.senior = child;
                }
            }

            // P devient fils gauche (junior) de child
            child.junior = parent;
            parent.parent = child;

            // A devient fils droit (senior) de P
            parent.senior = A;
            if (A != null) {
                A.parent = parent;
            }
            // L (parent.junior avant rotation) et B (child.senior) ne bougent pas
        }


        // child est le fils gauche (junior) qui doit monter
        private void rightRotation(CatNode child) {
            CatNode parent = child.parent;    // P
            CatNode grand  = parent.parent;   // peut être null
            CatNode B      = child.senior;    // B

            // rattacher child au grand-parent
            child.parent = grand;
            if (grand != null) {
                if (grand.junior == parent) {
                    grand.junior = child;
                } else if (grand.senior == parent) {
                    grand.senior = child;
                }
            }

            // P devient fils droit (senior) de child
            child.senior = parent;
            parent.parent = child;

            // B devient fils gauche (junior) de P
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

        private CatNode findCat (CatNode node, Cat c) {
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
			// 1. Trouver la node avec la key c
            CatNode toRetire = findCat(this, c);

            // 2. Faire remove la key c en switchant le max dans le left subtree avec le root a delete
            if (toRetire.senior == null && toRetire.junior == null) { // Si aucun child
                removeCat(toRetire, c);
            }
            else if (toRetire.senior == null) { // Juste left child
                if (toRetire.parent != null) {
                    if (toRetire.parent.senior == toRetire)
                        toRetire.parent.senior = toRetire.junior;
                    else
                        toRetire.parent.junior = toRetire.junior;
                    toRetire.junior.parent = toRetire.parent;
                } else {
                    toRetire.junior.parent = null;
                    CatNode temp = toRetire.junior;
                    toRetire.junior = null;
                    return temp;
                }
            }
            else if (toRetire.junior == null) { // Juste right child
                if (toRetire.parent != null) {
                    if (toRetire.parent.senior == toRetire)
                        toRetire.parent.senior = toRetire.senior;
                    else
                        toRetire.parent.junior = toRetire.senior;
                    toRetire.senior.parent = toRetire.parent;
                } else {
                    toRetire.senior.parent = null;
                    CatNode temp = toRetire.senior;
                    toRetire.senior = null;
                    return temp;
                }
            }
            else {
                Cat mostSenior = toRetire.junior.findMostSenior();
                toRetire.catEmployee = mostSenior;
                removeCat(toRetire.junior, mostSenior);
                restoreHeap(toRetire);
                return goBackToRoot(toRetire);
            }

            return this;
		}

        private CatNode goBackToRoot(CatNode node) {
            // remonter jusqu'à la racine
            while (node.parent != null) {
                node = node.parent;
            }
            return node;
        }

        private void restoreHeap(CatNode node) {
            if (node == null) return;

            while (true) {
                CatNode largest = node;

                // check junior (left) child
                if (node.junior != null &&
                        node.junior.catEmployee.getFurThickness() >
                                largest.catEmployee.getFurThickness()) {
                    largest = node.junior;
                }

                // check senior (right) child
                if (node.senior != null &&
                        node.senior.catEmployee.getFurThickness() >
                                largest.catEmployee.getFurThickness()) {
                    largest = node.senior;
                }

                // heap property satisfied
                if (largest == node) break;

                // rotate the larger child up
                if (largest == node.junior) {
                    // junior (left) too big -> right rotation on junior
                    rightRotation(largest);
                } else {
                    // senior (right) too big -> left rotation on senior
                    leftRotation(largest);
                }

                // after rotation, 'node' has moved one level down; loop continues
            }
        }


        private void removeCat(CatNode node, Cat c) {
            if (node != null) {
                if (node.parent != null && c.equals(node.catEmployee)) {
                    if (node.parent.junior == node)
                        node.parent.junior = null;
                    else
                        node.parent.senior = null;
                    node.parent = null;
                }
                else if (c.getMonthHired() < node.catEmployee.getMonthHired()) {
                    removeCat(node.senior, c);
                } else {
                    removeCat(node.junior, c);
                }
            }
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

		// Feel free to modify the toString() method if you'd like to see something else displayed.
		public String toString() {
			String result = this.catEmployee.toString() + "\n";
			if (this.junior != null) {
				result += "junior than " + this.catEmployee.toString() + " :\n";
				result += this.junior.toString();
			}
			if (this.senior != null) {
				result += "senior than " + this.catEmployee.toString() + " :\n";
				result += this.senior.toString();
			} /*
			if (this.parent != null) {
				result += "parent of " + this.catEmployee.toString() + " :\n";
				result += this.parent.catEmployee.toString() +"\n";
			}*/
			return result;
		}
	}


	public class CatfeinatedIterator implements Iterator<Cat> {
		// HERE YOU CAN ADD THE FIELDS YOU NEED

        ArrayList<Cat> cats;
        int currCat = -1;

		public CatfeinatedIterator(CatNode root) {
            cats = new ArrayList<Cat>(5);
            this.buildCatList(root);
        }

        private void buildCatList(CatNode root) {
            if (root != null) {     // Change traversal to match ascending order (plus récent au plus vieux)
                buildCatList(root.junior);
                this.cats.add(root.catEmployee);
                buildCatList(root.senior);
            }
        }

		public Cat next(){
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

	public static void main(String[] args) {
		Cat B = new Cat("Buttercup", 45, 53, 5, 85.0);
		Cat C = new Cat("Chessur", 8, 23, 2, 250.0);
		Cat J = new Cat("Jonesy", 0, 21, 12, 30.0);	
		Cat JJ = new Cat("JIJI", 156, 17, 1, 30.0);
		Cat JTO = new Cat("J. Thomas O'Malley", 21, 10, 9, 20.0);
		Cat MrB = new Cat("Mr. Bigglesworth", 71, 0, 31, 55.0);
		Cat MrsN = new Cat("Mrs. Norris", 100, 68, 15, 115.0);
		Cat T = new Cat("Toulouse", 180, 37, 14, 25.0);
		Cat BC = new Cat("Blofeld's cat", 6, 72, 18, 120.0);
		Cat L = new Cat("Lucifer", 10, 44, 20, 50.0);

        Catfeinated cafe = new Catfeinated();
        cafe.hire(B);
        cafe.hire(JTO);
        cafe.hire(C);
        cafe.retire(B);
        cafe.hire(JJ);
        cafe.hire(J);
        cafe.hire(MrsN);
        cafe.retire(MrsN);
        cafe.hire(B);
        cafe.hire(T);
        cafe.hire(MrB);
        cafe.hire(MrsN);
        cafe.hire(new Cat("Blofeld’s cat", 6, 72, 18, 120.0));
        cafe.hire(new Cat("Lucifer", 10, 44, 20, 50.0));

        Catfeinated cafe2 = new Catfeinated(cafe);

//        System.out.println(cafe.root);
        System.out.println(cafe2.root);
//        System.out.println(cafe.findMostSenior()); // displays Jonesy(0 , 21)
//        System.out.println(cafe.findMostJunior()); // displays Toulouse(180 , 37)
//        System.out.println(cafe.buildHallOfFame(3)); // displays [Blofeld’s cat(6 , 72), Mrs. Norris(100 , 68), Buttercup(45 , 53)]
        System.out.println(cafe.budgetGroomingExpenses(13)); // displays 415.0
        System.out.println(cafe.buildHallOfFame(3));
        System.out.println(cafe.getGroomingSchedule());

	}


}


