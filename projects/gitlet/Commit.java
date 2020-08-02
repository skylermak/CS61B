package gitlet;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * @author sky
 * */

public class Commit implements Serializable {

    /**  */
    private String _msg;

    /**  */
    private HashMap<String, String> _files;

    /**  */
    private String[] _parents;

    /**  */
    private String _date;

    /**  */
    private String uID;

    /**  */
    public static final SimpleDateFormat SDF =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /**
     * Creates a commit object initialized by the following.
     * @param msg a message describing the commit
     * @param files the file that is being committed
     * @param parents the prior commits
     * @param check a boolean checking whether the commit exists already
     */
    public Commit(String msg, HashMap files, String[] parents, boolean check) {

        _msg = msg;
        _files = files;
        _parents = parents;
        Date date;

        if (check) {
            date = new Date();
            _date = SDF.format(date);
        } else {
            _date = "Wed Dec 31 16:00:00 1969 -0800";
        }

        String filesStr;
        if (_files != null) {
            filesStr = _files.toString();
        } else {
            filesStr = "";
        }
        uID = Utils.sha1(_date, Arrays.toString(_parents), _msg, filesStr);
    }

    /** @return creates the initial commit that
     * contains nothing with msg "initial commit" */
    public static Commit initialCommit() {
        return new Commit("initial commit", null, null, false);
    }

    /** @return the message of the commit */
    public String msg() {
        return _msg;
    }

    /** @return a hashmap of the blobs being tracked */
    public HashMap<String, String> files() {
        return _files;
    }

    /** @return a list of the parent commits */
    public String[] parents() {
        return _parents;
    }

    /** @return the date of the commit in the SDF format */
    public String date() {
        return _date;
    }

    /** @return returns the ID of the parent commit */
    public String pID() {
        if (_parents != null) {
            return _parents[0];
        }
        return null;
    }

    /** @return the universal ID of commit */
    public String uID() {
        return uID;
    }

}
