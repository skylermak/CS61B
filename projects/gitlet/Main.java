package gitlet;
import java.io.File;
import java.util.Arrays;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author sky
 */

public class Main {

    /**
     * The main program.
     * @param args the inputs from the user in the command system
     */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                Utils.message("Please enter a command.");
                throw new GitletException();
            }
            if (checkCommand(args[0])) {
                String[] operands = Arrays.copyOfRange(args, 1, args.length);
                if (checkInit()) {
                    File file =  new File(REPOPATH);
                    repo = Utils.readObject(file, Git.class);
                    run(args, operands);
                    File myRepo = new File(REPOPATH);
                    Utils.writeObject(myRepo, repo);
                } else {
                    if (args[0].equals("init")) {
                        repo = new Git();
                        File myRepo = new File(REPOPATH);
                        Utils.writeObject(myRepo, repo);
                    } else {
                        Utils.message("Not in an initialized "
                                + "Gitlet directory.");
                        throw new GitletException();
                    }
                }
            } else {
                Utils.message("No command with that name exists.");
                throw new GitletException();
            }
        } catch (GitletException msg) {
            System.exit(0);
        }
    }

    /**
     * runs the command specified where.
     * @param args is the command user is trying to run
     * @param command is rest of msg to be used in their respected methods.
     */
    private static void run(String[] args, String[] command) {
        String check = "A Gitlet version-control system already exists "
                + "in the current directory.";
        switch (args[0]) {
        case "init": Utils.message(check);
                throw new GitletException();
        case "add": repo.add(command[0]);
                break;
        case "commit": repo.commit(command[0]);
                break;
        case "rm": repo.rm(command[0]);
                break;
        case "log": repo.log();
                break;
        case "global-log": repo.globalLog();
                break;
        case "find": repo.find(command[0]);
                break;
        case "status": repo.status();
                break;
        case "checkout":
            if (command.length != 1) {
                repo.checkout(command);
            } else {
                repo.checkout(command[0]);
            }
            break;
        case "branch": repo.branch(command[0]);
                break;
        case "rm-branch": repo.rmBranch(command[0]);
                break;
        case "reset": repo.reset(command[0]);
                break;
        case "merge": repo.merge(command[0]);
                break;
        default: Utils.message("no remote yet");
        }
    }

    /** a list of strings that holds all commands that can be inputted. */
    private static String[] commands = new String[] {
        "init",
        "add",
        "commit",
        "rm",
        "log",
        "global-log",
        "find",
        "status",
        "checkout",
        "branch",
        "rm-branch",
        "reset",
        "merge"
    };

    /**
     * @return a boolean to check whether gitlet is initialized
     */
    public static boolean checkInit() {
        String dir = System.getProperty("user.dir");
        File temp = new File(dir + "/.gitlet");
        if (temp.exists()) {
            return true;
        }
        return false;
    }

    /**
     * @param input the inputted git command
     * @return whether the input is a valid command
     */
    private static boolean checkCommand(String input) {
        for (String command: commands) {
            if (input.equals(command)) {
                return true;
            }
        }
        return false;
    }

    /** basically the command center of git. */
    private static Git repo;

    /** stores path to our repo. */
    private static final String REPOPATH = ".gitlet/myrepo";

}
