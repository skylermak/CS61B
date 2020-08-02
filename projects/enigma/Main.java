package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import java.util.Collection;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Skyler
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String settings, line = "";
        boolean setting;
        while (_input.hasNextLine()) {
            if (!_input.hasNext("\\s*[*].*")) {
                throw new EnigmaException("Config beginning does not have *");
            } else {
                settings = "";
                setting = false;

                while (!setting) {
                    line = _input.nextLine();
                    if (line.matches("[*].+")) {
                        setting = true;
                    } else {
                        printMessageLine(line);
                    }
                }
                int i = 0;
                Scanner newLine = new Scanner(line);
                while ((i < (enigma.numRotors() + 2))
                        || newLine.hasNext("[(].+[)]")) {
                    if (!newLine.hasNext()) {
                        throw new EnigmaException("Not enough rotors passed.");
                    }
                    settings += newLine.next().replaceAll("[*]", "* ") + " ";
                    i++;
                }
                setUp(enigma, settings.substring(0, settings.length() - 1));
                while (!_input.hasNext("[*]") && _input.hasNextLine()) {
                    String s = _input.nextLine().replaceAll("\\s+", "")
                            .toUpperCase();
                    printMessageLine(enigma.convert(s));
                }
            }
        }

    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {

            _allrotors = new ArrayList<>();
            String alphabet = _config.next();

            if (alphabet.contains("*")
                    || alphabet.contains("(") || alphabet.contains(")")) {
                throw new EnigmaException("formatting error");
            }

            _alphabet = new Alphabet(alphabet);

            if (_config.hasNextInt()) {
                _rotors = _config.nextInt();
                if (_config.hasNextInt()) {
                    _pawls = _config.nextInt();
                    temp = (_config.next()).toUpperCase();
                    while (_config.hasNext()) {
                        currentRotor = temp;
                        currentNotch = (_config.next()).toUpperCase();
                        _allrotors.add(readRotor());
                    }
                } else {
                    throw new EnigmaException("formatting error");
                }
            } else {
                throw new EnigmaException("formatting error");
            }
            return new Machine(_alphabet, _rotors, _pawls, _allrotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {

        try {
            cycle = "";
            temp = _config.next().toUpperCase();

            while (_config.hasNext() && temp.contains("(")) {
                cycle = cycle.concat(temp + " ");
                temp = (_config.next()).toUpperCase();
            }

            if (!_config.hasNext()) {
                cycle = cycle.concat(temp + " ");
            }

            if (currentNotch.charAt(0) == 'M') {
                return new MovingRotor(currentRotor,
                        new Permutation(cycle, _alphabet),
                            currentNotch.substring(1));
            } else if (currentNotch.charAt(0) == 'N') {
                return new FixedRotor(currentRotor,
                        new Permutation(cycle, _alphabet));
            } else if (currentNotch.charAt(0) == 'R') {
                return new Reflector(currentRotor,
                        new Permutation(cycle, _alphabet));
            } else {
                throw new EnigmaException("wrong rotor");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {

        String steckered = "";
        Scanner setting = new Scanner(settings);
        String[] rotors = new String[M.numRotors()];

        if (!setting.hasNext("[*]")) {
            throw error("setting in wrong format");
        }

        setting.next();
        for (int i = 0; i < M.numRotors(); i++) {
            rotors[i] = setting.next();
        }

        M.insertRotors(rotors);
        if (setting.hasNext("\\w" + "{" + (M.numRotors() - 1) + "}")) {
            M.setRotors(setting.next());
        }
        while (setting.hasNext("[(]\\w+[)]")) {
            steckered = steckered.concat(setting.next() + " ");
        }
        if (steckered.length() > 0) {
            Permutation x = new Permutation(steckered.
                    substring(0, steckered.length() - 1), _alphabet);
            M.setPlugboard(x);
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int msgLength = msg.length() - i;
            if (msgLength <= 5) {
                _output.println(msg.substring(i, i + msgLength));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** rotor. */
    private int _rotors;

    /** pawls. */
    private int _pawls;

    /** allrotors. */
    private Collection<Rotor> _allrotors;

    /** current rotor. */
    private String currentRotor;

    /** current notch. */
    private String currentNotch;

    /** temporary string placeholder. */
    private String temp;

    /** cycle. */
    private String cycle;

}
