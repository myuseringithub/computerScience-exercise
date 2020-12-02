
// File Header comes here
import java.util.NoSuchElementException;

/**
 * This class implements a binary search tree (BST) which stores a set of
 * Pokemons. The left subtree contains the Pokemons who are less powerful than
 * the Pokemon stored at a parent node. The right subtree contains the Pokemons
 * who are more powerful than the Pokemon stored at a parent node.
 *
 */
public class PokemonTree {
	private PokemonNode root; // root of this binary search tree
	private int size; // total number of Pokemons stored in this tree.

	/**
	 * Checks whether this binary search tree (BST) is empty
	 * 
	 * @return true if this PokemonTree is empty, false otherwise
	 */
	public boolean isEmpty() {
		// TODO Complete the implementation of this method.
		return false; // remove this statement. A default return statement added to let this code
		              // compile.
	}

	/**
	 * Returns the number of Pokemons stored in this BST.
	 * 
	 * @return the size of this PokemonTree
	 */
	public int size() {
		// TODO Complete the implementation of this method.
		return 0; // remove this statement. A default return statement added to let this code
		          // compile.
	}

	/**
	 * Recursive helper method to add a new Pokemon to a PokemonTree rooted at
	 * current.
	 * 
	 * @param current    The "root" of the subtree we are inserting new pokemon
	 *                   into.
	 * @param newPokemon The Pokemon to be added to a BST rooted at current.
	 * @return true if the newPokemon was successfully added to this PokemonTree,
	 *         false otherwise
	 */
	public static boolean addPokemonHelper(Pokemon newPokemon, PokemonNode current) {
		// TODO Complete the implementation of this method.
		return false; // remove this statement. A default return statement added to let this code
		// compile.
	}

	/**
	 * Adds a new Pokemon to this PokemonTree
	 * 
	 * @param newPokemon a new Pokemon to add to this BST.
	 * @return true if the new was successfully added to this BST, and returns false
	 *         if there is a match with this Pokemon already already stored in this
	 *         BST.
	 */
	public boolean addPokemon(Pokemon newPokemon) {
		// TODO Complete the implementation of this method.
		if (isEmpty()) { // Add new to an empty PokemonTree
		  // TODO /* MISSING CODE */

		} else { // Add new to an non-empty PokemonTree
		  // TODO /* MISSING CODE */
		  // [Hint]: call addPokemonHelper to help implement this behavior.
		}
		return false; // remove this statement. A default return statement added to let this code
		              // compile.
	}

	/**
	 * Recursive helper method which returns a String representation of the BST
	 * rooted at current. An example of the String representation of the contents of
	 * a PokemonTree is provided in the description of the above toString() method.
	 * 
	 * @param current reference to the current PokemonNode within this BST.
	 * @return a String representation of all the Pokemons stored in the sub-tree
	 *         PokemonTree rooted at current in increasing order with respect to the
	 *         CP values. Returns an empty String "" if current is
	 *         null.
	 */
	public static String toStringHelper(PokemonNode current) {
		// TODO Complete the implementation of this method.
		return null; // remove this statement.
	}

	/**
	 * Returns a String representation of all the Pokemons stored within this BST in
	 * the increasing order, separated by a newline "\n". For instance:
	 * "[Pikachu CP:123 (A:1 S:2 D:3)]" + "\n" + "[Eevee CP:224 (A:2 S:2 D:4)]" + "\n" + 
	 * [Lapras CP:735 (A:7 S:3 D:5)] + "\n" + "[Mewtwo CP:999 (A:9 S:9 D:9)]" + "\n"
	 * 
	 * @return a String representation of all the Pokemons stored within this BST
	 *         sorted in an increasing order with respect to the CP values.
	 *         Returns an empty string "" if this BST is empty.
	 */
	public String toString() {
		// TODO Complete the implementation of this method.
		// [Hint]: call toStringHelper() method to help implement this behavior.
		return null; // remove this statement.
	}

	/**
	 * Search for a Pokemon (Pokemon) given the CP value as lookup key.
	 * 
	 * @param cp combat power of a Pokemon
	 * @return the Pokemon whose CP value equals our lookup key.
	 * @throws a NoSuchElementException with a descriptive error message if there is
	 *           no Pokemon found in this BST having the provided CP value
	 */
	public Pokemon lookup(int cp) {
		return lookupHelper(cp, root);
	}

	/**
	 * Recursive helper method to lookup a Pokemon given a reference Pokemon with
	 * the same CP in the subtree rooted at current
	 * 
	 * @param find    a reference to a Pokemon target we are lookup for a match in
	 *                the BST rooted at current.
	 * @param current "root" of the subtree we are looking for a match to find
	 *                within it.
	 * @return reference to the Pokemon stored stored in this BST which matches
	 *         find.
	 * @throws NoSuchElementException with a descriptive error message if there is
	 *                                no Pokemon whose CP value matches target value,
	 *                                stored in this BST.
	 */
	public static Pokemon lookupHelper(int cp, PokemonNode current) {
		// TODO Complete the implementation of this method
		return null; // remove this statement (added to let this code to compile)
	}

	/**
	 * Computes and returns the height of this BST, counting the number of nodes
	 * (PokemonNodes) from root to the deepest leaf.
	 * 
	 * @return the height of this Binary Search Tree
	 */
	public int height() {
		return heightHelper(root);
	}

	/**
	 * Recursive helper method that computes the height of the subtree rooted at
	 * current
	 * 
	 * @param current pointer to the current PokemonNode within a PokemonTree
	 * @return height of the subtree rooted at current, counting the number of
	 *         PokemonNodes
	 */
	public static int heightHelper(PokemonNode current) {
		// TODO Complete this implementation of this method
		return 0; // remove this statement.
	}

	/**
	 * Returns the Pokemon of the least powerful Pokemon in this BST.
	 * 
	 * @return the Pokemon of the least powerful Pokemon in this BST and null if this tree
	 *         is empty.
	 */
	public Pokemon getLeastPowerfulPokemon() {
		// TODO Complete the implementation of this method
	  
		// Feel free to implement either the iterative OR the recursive version of this
		// method.
	  
		// If you choose recursion to implement this behavior, add a private helper
		// method and call it here.
	  
		return null; // added to let this incomplete code compile
	}

	/**
	 * Returns the Pokemon of the most powerful Pokemon in this BST.
	 * 
	 * @return the Pokemon of the most powerful Pokemon in this BST, and null if this tree
	 *         is empty.
	 */
	public Pokemon getMostPowerfulPokemon() {
		// TODO Complete the implementation of this method
	  
		// Feel free to implement either the iterative OR the recursive version of this
		// method.
	  
		// If you choose recursion to implement this behavior, add a private helper
		// method and call it here.
	  
		return null; // added to let this incomplete code compile
	}

}
