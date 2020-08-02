package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Arrays;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author sky
 * */

public class Git implements Serializable {

    /**
     *
     */
    public Git() {
        Commit init = Commit.initialCommit();
        new File(".gitlet").mkdir();
        new File(".gitlet/commits").mkdir();
        new File(".gitlet/staging").mkdir();

        File initialFile = new File(".gitlet/commits/" + init.uID());
        Utils.writeContents(initialFile, Utils.serialize(init));

        _head = "master";
        _branches = new HashMap<String, String>();
        _branches.put("master", init.uID());

        _staging = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
    }

    /**
     * adds file to staging.
     * @param f name of file
     */
    public void add(String f) {
        File file = new File(f);
        if (!file.exists()) {
            Utils.message("File does not exist.");
            throw new GitletException();
        }
        String fileHash = Utils.sha1(Utils.readContentsAsString(file));
        Commit head = convertUIDToCommit(head());
        HashMap<String, String> files = head.files();

        File blobFile = new File(".gitlet/staging/" + fileHash);
        boolean check = (files == null);
        if (check || !files.containsKey(f) || !files.get(f).equals(fileHash)) {
            _staging.put(f, fileHash);
            String contents = Utils.readContentsAsString(file);
            Utils.writeContents(blobFile, contents);
        } else {
            if (blobFile.exists()) {
                _staging.remove(f);
            }
        }
        if (_untrackedFiles.contains(f)) {
            _untrackedFiles.remove(f);
        }
    }

    /**
     * creates a new commit. saves a snapshot of certain files in the
     * current commit and staging area so they can be restored at a later time.
     * @param msg commit message
     */
    public void commit(String msg) {
        if (msg.trim().equals("")) {
            Utils.message("Please enter a commit message.");
            throw new GitletException();
        }

        Commit head = convertUIDToCommit(head());
        HashMap<String, String> tracked = head.files();

        if (tracked == null) {
            tracked = new HashMap<String, String>();
        }

        if (_staging.size() != 0 || _untrackedFiles.size() != 0) {
            for (String fileName : _staging.keySet()) {
                tracked.put(fileName, _staging.get(fileName));
            }
            for (String fileName : _untrackedFiles) {
                tracked.remove(fileName);
            }
        } else {
            Utils.message("No changes added to the commit.");
            throw new GitletException();
        }

        String[] parent = new String[]{head.uID()};
        Commit newCommit = new Commit(msg, tracked, parent, true);
        File newCommFile = new File(".gitlet/commits/" + newCommit.uID());
        Utils.writeObject(newCommFile, newCommit);

        _staging = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
        _branches.put(_head, newCommit.uID());
    }

    /**
     * non-default commit. otherwise, same as commit above.
     * @param msg commit message
     * @param parents parent commits
     */
    public void commit(String msg, String[] parents) {
        if (msg.trim().equals("")) {
            Utils.message("Please enter a commit message.");
            throw new GitletException();
        }
        Commit head = convertUIDToCommit(head());
        HashMap<String, String> tracked = head.files();

        if (tracked == null) {
            tracked = new HashMap<String, String>();
        }

        if (_staging.size() != 0 || _untrackedFiles.size() != 0) {
            for (String fileName : _staging.keySet()) {
                tracked.put(fileName, _staging.get(fileName));
            }
            for (String fileName : _untrackedFiles) {
                tracked.remove(fileName);
            }
        } else {
            Utils.message("No changes added to the commit.");
            throw new GitletException();
        }

        Commit newCommit = new Commit(msg, tracked, parents, true);
        File newCommFile = new File(".gitlet/commits/" + newCommit.uID());
        Utils.writeObject(newCommFile, newCommit);

        _staging = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
        _branches.put(_head, newCommit.uID());
    }

    /**
     * prints info on commit in format below.
     *    ===
     *    commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
     *    Date: Thu Nov 9 20:00:05 2017 -0800
     *    A commit message.
     *
     *    ===
     *    commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     *    Merge: 4975af1 2c1ead1
     *    Date: Sat Nov 11 12:30:00 2017 -0800
     *    Merged development into master.
     * @param uid universal id of the commit
     */
    public void print(String uid) {
        Commit commit = convertUIDToCommit(uid);
        if (commit.parents() != null && commit.parents().length > 1) {
            System.out.println("===");
            System.out.println("commit " + uid);
            System.out.format("Merge: %s %s\n",
                    commit.parents()[0].substring(0, 7),
                    commit.parents()[0].substring(0, 7));
            System.out.println("Date: " + commit.date());
            System.out.println(commit.msg());
        } else {
            System.out.println("===");
            System.out.println("commit " + uid);
            System.out.println("Date: " + commit.date());
            System.out.println(commit.msg());
        }
        System.out.println();
    }

    /**
     * Unstage the file if it is currently staged.
     * @param f name of file
     */
    public void rm(String f) {
        File file = new File(f);
        Commit head = convertUIDToCommit(head());
        HashMap<String, String> tracked = head.files();
        if (!file.exists() && !tracked.containsKey(f)) {
            Utils.message("File does not exist.");
            throw new GitletException();
        }
        boolean changed = false;
        if (_staging.containsKey(f)) {
            _staging.remove(f);
            changed = true;
        }
        if (tracked != null && tracked.containsKey(f)) {
            _untrackedFiles.add(f);
            File toRemove = new File(f);
            Utils.restrictedDelete(toRemove);
            changed = true;
        }
        if (!changed) {
            Utils.message("No reason to remove the file.");
            throw new GitletException();
        }
    }

    /**
     * display information about each commit backwards
     * along the commit tree until the initial commit.
     */
    public void log() {
        String head = head();
        while (head != null) {
            Commit first = convertUIDToCommit(head);
            print(head);
            head = first.pID();
        }
    }

    /**
     * displays information about all commits ever made.
     */
    public void globalLog() {
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();

        for (File file : commits) {
            print(file.getName());
        }
    }

    /**
     * takes in commit msg and prints id of all commits with that msg.
     * @param msg the msg of the commit
     */
    public void find(String msg) {
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();
        boolean found = false;

        for (File file : commits) {
            Commit commit = convertUIDToCommit(file.getName());
            if (commit.msg().equals(msg)) {
                System.out.println(file.getName());
                found = true;
            }
        }
        if (!found) {
            Utils.message("Found no commit with that message.");
            throw new GitletException();
        }
    }

    /**
     * displays branches, staged files, and those marked for tracking.
     *=== Branches ===
     * *master
     * other-branch
     *
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     *
     * === Removed Files ===
     * goodbye.txt
     *
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     *
     * === Untracked Files ===
     * random.stuff
     */
    public void status() {
        System.out.println("=== Branches ===");
        Object[] b = _branches.keySet().toArray();
        Arrays.sort(b);
        for (Object branch : b) {
            if (branch.equals(_head)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Object[] stages = _staging.keySet().toArray();
        Arrays.sort(stages);
        for (Object staged : stages) {
            System.out.println(staged);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Object[] untracked = _untrackedFiles.toArray();
        Arrays.sort(untracked);
        for (Object untrackedFiles : untracked) {
            System.out.println(untrackedFiles);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /**
     * covers first 2 checkout methods
     * Takes the version of the file as it exists in the head commit,
     * the front of the current branch
     * or
     * Takes the version of the file as it exists in the commit with given id,
     * and
     * puts it in the working directory, overwriting the version of the file
     * that's already there if there is one.
     * The new version of the file is not staged.
     * @param input user input
     */
    public void checkout(String[] input) {
        String cID;
        String fileName;
        if (input.length == 2 && input[0].equals("--")) {
            fileName = input[1];
            cID = head();
        } else if (input.length == 3 && input[1].equals("--")) {
            cID = input[0];
            fileName = input[2];
        } else {
            Utils.message("Incorrect operands");
            throw new GitletException();
        }
        cID = convertID(cID);
        Commit comm = convertUIDToCommit(cID);
        HashMap<String, String> tracked = comm.files();
        if (tracked.containsKey(fileName)) {
            File f = new File(fileName);
            String blobFileName = ".gitlet/staging/" + tracked.get(fileName);
            File g = new File(blobFileName);
            String contents = Utils.readContentsAsString(g);
            Utils.writeContents(f, contents);
        } else {
            Utils.message("File does not exist in that commit.");
            throw new GitletException();
        }
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist.
     * @param branch name of branch
     */
    public void checkout(String branch) {
        if (!_branches.containsKey(branch)) {
            Utils.message("No such branch exists.");
            throw new GitletException();
        }
        if (_head.equals(branch)) {
            String s = "No need to checkout the current branch.";
            Utils.message(s);
            throw new GitletException();
        }
        String cid = _branches.get(branch);
        HashMap<String, String> files = convertUIDToCommit(cid).files();
        String currString = System.getProperty("user.dir");
        File curr = new File(currString);
        checkUntracked(curr);
        for (File file : curr.listFiles()) {
            if (files == null) {
                Utils.restrictedDelete(file);
            } else {
                if (!file.getName().equals(".gitlet")
                        && !files.containsKey(file.getName())) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        if (files != null) {
            for (String file : files.keySet()) {
                String s = ".gitlet/staging/" + files.get(file);
                File fs = new File(s);
                String contents = Utils.readContentsAsString(fs);
                Utils.writeContents(new File(file), contents);
            }
        }
        _staging = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
        _head = branch;
    }

    /**
     * takes shortened id and returns the full id.
     * @param id shortened id
     * @return the
     */
    private String convertID(String id) {
        if (id.length() == Utils.UID_LENGTH) {
            return id;
        }
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();

        for (File file : commits) {
            if (file.getName().contains(id)) {
                return file.getName();
            }
        }
        Utils.message("No commit with that id exists.");
        throw new GitletException();
    }

    /**
     * checked whether the inputted file is untracked.
     * @param curr (ent) file
     */
    private void checkUntracked(File curr) {
        Commit head = convertUIDToCommit(head());
        HashMap<String, String> tracked = head.files();
        for (File file : curr.listFiles()) {
            if (tracked == null) {
                if (curr.listFiles().length > 1) {
                    Utils.message("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    throw new GitletException();
                }
            } else {
                if (!file.getName().equals(".gitlet")
                        && !tracked.containsKey(file.getName())
                        && !_staging.containsKey(file.getName())) {
                    Utils.message("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    throw new GitletException();
                }
            }
        }
    }

    /**
     * creates a branch with the inputted name.
     * @param branch name of branch
     */
    public void branch(String branch) {
        if (!_branches.containsKey(branch)) {
            _branches.put(branch, head());
        } else {
            Utils.message("A branch with that name already exists.");
            throw new GitletException();
        }
    }

    /**
     * removes the specified branch.
     * @param branch name of branch
     */
    public void rmBranch(String branch) {
        if (_head.equals(branch)) {
            Utils.message("Cannot remove the current branch.");
            throw new GitletException();
        }
        if (_branches.containsKey(branch)) {
            _branches.remove(branch);
        } else {
            Utils.message("A branch with that name does not exist.");
            throw new GitletException();
        }
    }

    /**
     *Checks out all the files tracked by the given commit.
     * @param cid the ID of the commit
     */
    public void reset(String cid) {
        cid = convertID(cid);
        HashMap<String, String> files = convertUIDToCommit(cid).files();

        String currString = System.getProperty("user.dir");
        File curr = new File(currString);
        checkUntracked(curr);

        for (File file : curr.listFiles()) {
            if (!files.containsKey(file.getName())) {
                Utils.restrictedDelete(file);
            }
        }
        for (String file : files.keySet()) {
            File f = new File(".gitlet/staging/" + files.get(file));
            Utils.writeContents(new File(file), Utils.readContentsAsString(f));
        }
        _staging = new HashMap<String, String>();
        _branches.put(_head, cid);
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param branch name of branch
     */
    public void merge(String branch) {
        if (_staging.size() != 0 || _untrackedFiles.size() != 0) {
            Utils.message("You have uncommitted changes.");
            throw new GitletException();
        }
        if (!_branches.containsKey(branch)) {
            Utils.message("A branch with that name does not exist.");
            throw new GitletException();
        }
        if (branch.equals(_head)) {
            Utils.message("Cannot merge a branch with itself.");
            throw new GitletException();
        }
        String split = split(branch, _head);
        if (split.equals(_branches.get(branch))) {
            Utils.message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.equals(_branches.get(_head))) {
            _branches.put(_head, _branches.get(branch));
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        HashMap<String, String> splitF = convertUIDToCommit(split).files();
        mergeHelper(branch);
        HashMap<String, String> curr = convertUIDToCommit(head()).files();
        HashMap<String, String> given = convertUIDToCommit(_branches
                .get(branch)).files();

        for (String fileName : given.keySet()) {
            if (!splitF.containsKey(fileName)) {
                if (!curr.containsKey(fileName)) {
                    String b = _branches.get(branch);
                    checkout(new String[] {b, "--", fileName});
                    _staging.put(fileName, given.get(fileName));
                } else if (!given.containsKey(fileName)) {
                    continue;
                } else if (checkMod(fileName, given, curr)) {
                    String p = ".gitlet/staging/";
                    File c = new File(p + curr.get(fileName));
                    File g = new File(p + given.get(fileName));
                    String contents = "<<<<<<< HEAD\n";
                    contents += Utils.readContentsAsString(c);
                    contents += "=======\n";
                    contents += Utils.readContentsAsString(g) + ">>>>>>>";
                    Utils.writeContents(new File(fileName), contents);
                    add(fileName);
                    Utils.message("Encountered a merge conflict.");
                }
            }
        }
        String[] parents = new String[]{head(), _branches.get(branch)};
        commit("Merged " + branch + " into " + _head + ".", parents);
    }

    /**
     * merge helper to shorten merge method.
     * @param branch name of branch
     */
    private void mergeHelper(String branch) {
        String split = split(branch, _head);
        HashMap<String, String> splitF = convertUIDToCommit(split).files();
        HashMap<String, String> curr = convertUIDToCommit(head()).files();
        HashMap<String, String> given = convertUIDToCommit(_branches
                .get(branch)).files();

        File current = new File(System.getProperty("user.dir"));
        checkUntracked(current);

        for (String fileName : splitF.keySet()) {
            boolean checkGiven = given.containsKey(fileName);
            boolean checkModCurr = checkMod(fileName, splitF, curr);
            boolean checkModGiven = checkMod(fileName, splitF, given);
            if (!checkModCurr) {
                if (!checkGiven) {
                    Utils.restrictedDelete(new File(fileName));
                    rm(fileName);
                    continue;
                }
                if (checkModGiven) {
                    String b = _branches.get(branch);
                    checkout(new String[]{b, "--", fileName});
                    add(fileName);
                }
            }
            if (checkModCurr && checkModGiven) {
                if (checkMod(fileName, given, curr)) {
                    mergeConflict(branch, fileName);
                }
            }
        }
    }

    /**
     * checks whether there is a merge conflict.
     * @param branch name of branch
     * @param file name of file
     */
    private void mergeConflict(String branch, String file) {
        HashMap<String, String> cr = convertUIDToCommit(head()).files();
        HashMap<String, String> given = convertUIDToCommit(_branches
                .get(branch)).files();
        String dir = ".gitlet/staging/";
        File curr;
        String c;
        File give;
        String g;
        if (cr.containsKey(file)) {
            curr = new File(dir + cr.get(file));
            c = Utils.readContentsAsString(curr);
        } else {
            curr = null;
            c = "";
        }
        if (given.containsKey(file)) {
            give = new File(dir + given.get(file));
            g = Utils.readContentsAsString(give);
        } else {
            give = null;
            g = "";
        }
        String x = "<<<<<<< HEAD\n" + c + "=======\n" + g + ">>>>>>>\n";
        Utils.writeContents(new File(file), x);
        add(file);
        Utils.message("Encountered a merge conflict.");
    }

    /**
     * finds the split point of b1 and b2.
     * @param b1 branch 1
     * @param b2 branch 2
     * @return the commit of the split
     */
    private String split(String b1, String b2) {
        ArrayList<String> bCommits1 = new ArrayList<String>();
        ArrayList<String> bCommits2 = new ArrayList<String>();
        String parent1 = _branches.get(b1);
        String parent2 = _branches.get(b2);

        while (parent1 != null) {
            bCommits1.add(parent1);
            parent1 = convertUIDToCommit(parent1).pID();
        }
        while (parent2 != null) {
            bCommits2.add(parent2);
            parent2 = convertUIDToCommit(parent2).pID();
        }
        for (String commit : bCommits1) {
            if (bCommits2.contains(commit)) {
                return commit;
            }
        }
        return "";
    }

    /**
     * returns a boolean to check whether a file has been modified.
     * @param file we want to check
     * @param i from
     * @param j to
     * @return a boolean
     */
    boolean checkMod(String file, HashMap<String, String> i,
                     HashMap<String, String> j) {
        if (i.containsKey(file) && j.containsKey(file)) {
            String hash1 = i.get(file);
            String hash2 = j.get(file);
            if (!hash1.equals(hash2)) {
                return true;
            }
        }
        return (i.containsKey(file) || j.containsKey(file));
    }

    /**
     * converts the inputting UID to the respected commit.
     * @param uid the Universal ID of the commit
     * @return the respected commit
     */
    public Commit convertUIDToCommit(String uid) {
        File file = new File(".gitlet/commits/" + uid);
        if (file.exists()) {
            return Utils.readObject(file, Commit.class);
        } else {
            Utils.message("No commit with that id exists.");
            throw new GitletException();
        }
    }

    /** @return the head of the branch. */
    public String head() {
        return _branches.get(_head);
    }

    /** stores the branches of the tree. */
    private HashMap<String, String> _branches;

    /** stores the head of the tree. */
    private String _head;

    /** stores the files that are in staging. */
    private HashMap<String, String> _staging;

    /** stores the untracked files of the repo. */
    private ArrayList<String> _untrackedFiles;
}
