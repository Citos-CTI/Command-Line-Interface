package engler.de.main;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Random;

public class UserOperations {

    public static void main(String[] args) {
        SqliteUserDatabase sqliteUserDatabase = new SqliteUserDatabase("/Users/johannesengler/jtc_server/users.db");

        if (args.length < 1) {
            unknwonCommand();
        } else if (args[0].equals("adduser") && args.length == 3) {
            String salt = BCrypt.gensalt();
            String password = generateRandomPassword();
            String hashed = BCrypt.hashpw(password, salt);
            if (sqliteUserDatabase.insertUserInDatabase(args[1], hashed, salt, args[2])) {
                System.out.println("User: " + args[1] + " added");
                System.out.println("PW: " + password);
            } else {
                System.out.println("User: " + args[1] + " is already in the database");
            }
        } else if (args[0].equals("deluser") && args.length == 2) {
            sqliteUserDatabase.deleteUser(args[1]);
            System.out.println("User: " + args[1] + " deleted");
        } else if (args[0].equals("newpw") && args.length == 2) {
            String salt = BCrypt.gensalt();
            String password = generateRandomPassword();
            String hashed = BCrypt.hashpw(password, salt);
            sqliteUserDatabase.updateStatementForHash(args[1], hashed, salt);
            System.out.println("New password for user: " + args[1]);
            System.out.println("PW: " + password);
        } else if (args[0].equals("list") && args.length == 1) {
            ArrayList<String> entries = (ArrayList<String>) sqliteUserDatabase.getDatabaseEntries();
            System.out.println("Username\tExtension");
            entries.forEach(str -> System.out.println(str));
        } else {
            unknwonCommand();
        }
    }

    public static void unknwonCommand() {
        System.out.println("Sorry, please execute one of these commands:");
        System.out.println("  1. adduser <username> <extension>     //Creates new user with <username> and <extension>");
        System.out.println("  2. deluser <username>                 // Deletes user with <username>");
        System.out.println("  3. newpw <username>                   // Prints the new password for <username>");
        System.out.println("  4. list                               // Prints out all users with extensions");
    }

    public static String generateRandomPassword() {
        Random r = new Random();
        String g = "";
        for (int i = 0; i <= 7; ++i) {
            int rand = r.nextInt((3 - 1) + 1) + 1;
            int randomNum = 0;
            if (rand == 1) {
                randomNum = r.nextInt((57 - 48) + 1) + 48;
            } else if (rand == 2) {
                randomNum = r.nextInt((90 - 65) + 1) + 65;
            } else {
                randomNum = r.nextInt((122 - 97) + 1) + 97;
            }
            g = g + Character.toString((char) randomNum);
        }
        return g;
    }
}
