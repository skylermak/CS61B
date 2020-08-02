package enigma;

import java.util.ArrayList;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Skyler
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;

        _permutations = new ArrayList<>();
        for (int i = 0; i < _alphabet.size(); i++) {
            _permutations.add(i);
        }

        String[] stringArr;
        String list = cycles.replace(")", "");
        cycles = list.replace("(", "");
        stringArr = cycles.split(" ");
        for (String i : stringArr) {
            addCycle(i);
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        for (int i = 0; i < cycle.length(); i++) {
            char charAtI = cycle.charAt(i);
            if (i != (cycle.length() - 1)) {
                _permutations.set(_alphabet.toInt(charAtI),
                        _alphabet.toInt(cycle.charAt(i + 1)));
            } else {
                _permutations.set(_alphabet.toInt(charAtI),
                        _alphabet.toInt(cycle.charAt(0)));
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _permutations.get(wrap(p));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _permutations.indexOf(wrap(c));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int intP = _alphabet.toInt(p);
        return _alphabet.toChar(permute(intP));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int intC = _alphabet.toInt(c);
        return _alphabet.toChar(invert(intC));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i : _permutations) {
            if (i == _permutations.indexOf(i)) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** permutations. */
    private ArrayList<Integer> _permutations;
}
