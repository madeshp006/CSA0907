import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * TASK B3: Complete the Code - Batch Pass Loader with Transaction Management
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO5: Data Persistence Engineering)
 * 
 * Completed TODO 1, TODO 2, and TODO 3:
 * - TODO 1: Parameter binding, batch queuing, periodic execution every 1,000 rows.
 * - TODO 2: con.commit() to make all batch inserts permanent; return count n.
 * - TODO 3: con.rollback() to undo all inserts upon error; re-throws exception.
 */
public class PassLoader {

    /** Loads all passes in one transaction, in batches of 1,000 rows.
     * Returns the number of rows loaded. */
    public static int loadAll(Connection con, List<String[]> passes)
            throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) "
                + "VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            for (String[] p : passes) {
                // TODO 1: Set the five parameters from p (route as int, amount as
                // double), add the row to the batch, and execute the batch after every
                // 1,000 rows
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3].trim()));
                ps.setDouble(5, Double.parseDouble(p[4].trim()));
                ps.addBatch();
                n++;

                if (n % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();

            // TODO 2: Make all inserted rows permanent and return the number of rows
            // loaded
            con.commit();
            return n;

        } catch (SQLException | RuntimeException e) {
            // TODO 3: Undo every insert made in this transaction, then re-throw the
            // exception
            con.rollback();
            throw e;

        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
