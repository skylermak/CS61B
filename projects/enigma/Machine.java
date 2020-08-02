package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Skyler
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _rotors = new Rotor[allRotors.size()];

        int i = 0;
        for (Rotor r: allRotors) {
            _rotors[i] = r;
            i++;
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {

        _allRotors = new ArrayList<>();
        ArrayList<String> checkNames = new ArrayList<>();
        for (String x: rotors) {
            for (Rotor y : _rotors) {
                if (x.toUpperCase().equals(y.name().toUpperCase())) {
                    _allRotors.add(y);
                    if (checkNames.contains(x)) {
                        throw new EnigmaException("same rotor");
                    } else {
                        checkNames.add(x);
                    }
                }
            }
        }

        if (_allRotors.size() != rotors.length) {
            throw new EnigmaException("Rotors not correct");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (!_allRotors.get(0).reflecting()) {
            throw new EnigmaException("no reflector.");
        }
        if (setting.length() != (numRotors() - 1)) {
            throw new EnigmaException("length not equal");
        } else {
            if (setting.length() == (numRotors() - 1)) {
                for (int i = 1; i < _allRotors.size(); i++) {
                    if (i < numRotors() - numPawls()) {
                        if (!(_allRotors.get(i).reflecting())
                                && !(_allRotors.get(i).rotates())) {
                            int x = _alphabet.toInt(setting.charAt(i - 1));
                            _allRotors.get(i).set(x);
                        }
                    } else if (i >= numRotors() - numPawls()) {
                        if (_allRotors.get(i).rotates()) {
                            int x = _alphabet.toInt(setting.charAt(i - 1));
                            _allRotors.get(i).set(x);
                        }
                    }
                }
            } else {
                throw new EnigmaException("incorrect length.");
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. fix this shit */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {

        ArrayList<Rotor> shift = new ArrayList<>();
        c = c % _alphabet.size();

        for (int i = numRotors() - numPawls(); i < numRotors(); i++) {
            Rotor curr = _allRotors.get(i);
            if (i == (numRotors() - 1)) {
                shift.add(curr);
            } else if (_allRotors.get(i + 1).atNotch()
                    || shift.contains(_allRotors.get(i - 1))) {
                if (!shift.contains(curr)) {
                    shift.add(curr);
                }
                if (_allRotors.get(i).atNotch()) {
                    if (!shift.contains(_allRotors.get(i - 1))) {
                        shift.add(_allRotors.get(i - 1));
                    }
                }
            }
        }

        for (Rotor r: shift) {
            r.advance();
        }

        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        for (int i = _allRotors.size() - 1; i >= 0; i--) {
            Rotor forwards = _allRotors.get(i);
            c = forwards.convertForward(c);
        }
        for (int j = 1; j < _allRotors.size(); j++) {
            Rotor backwards = _allRotors.get(j);
            c = backwards.convertBackward(c);
        }
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        return c;
    }


    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String convertString = "";
        for (int i = 0; i < msg.length(); i++) {
            char x = _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
            convertString += x;
        }
        return convertString;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** number of rotors. */
    private final int _numRotors;

    /** number of pawls. */
    private final int _numPawls;

    /** plugboard. */
    private Permutation _plugboard;

    /** rotors. */
    private Rotor[] _rotors;

    /** all rotors. */
    private ArrayList<Rotor> _allRotors;
}
