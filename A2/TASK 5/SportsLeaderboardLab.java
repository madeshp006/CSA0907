import java.util.*;

public class SportsLeaderboardLab {
    static class Team {
        final String name;
        final int points;
        final int goalDiff;

        Team(String name, int points, int goalDiff) {
            this.name = name;
            this.points = points;
            this.goalDiff = goalDiff;
        }

        @Override
        public String toString() {
            return String.format("%s [Pts:%d, GD:%+d]", name, points, goalDiff);
        }
    }

    // Generic Player entity to prove reusability of Leaderboard<T>
    static class QuizPlayer {
        final String username;
        final int score;
        final int penaltySeconds;

        QuizPlayer(String username, int score, int penaltySeconds) {
            this.username = username;
            this.score = score;
            this.penaltySeconds = penaltySeconds;
        }

        @Override
        public String toString() {
            return String.format("%s (Score:%d, Pen:%ds)", username, score, penaltySeconds);
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO2 ASSIGNMENT 2: TASK 5 (Q5) EXPERIMENTAL LAB         ");
        System.out.println("   Student: P. Madesh  |  Register No: 192365007  |  Java Version: 25.0.2      ");
        System.out.println("   Topic: Deriving a Generic Ranking Component from the Transitivity Principle ");
        System.out.println("================================================================================\n");

        System.out.println(">>> 1. VERIFICATION OF TRANSITIVITY PRINCIPLE & COMPLETE TIE-BREAKING:");
        System.out.println("   Principle: If A ranks above B and B ranks above C, then A must rank above C.");
        System.out.println("--------------------------------------------------------------------------------");

        Comparator<Team> correctRanker = Comparator
            .comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
            .thenComparing(t -> t.name);

        List<Team> teams = new ArrayList<>(Arrays.asList(
            new Team("Panthers", 9, 4),
            new Team("Eagles", 9, 6),
            new Team("Cobras", 6, 1),
            new Team("Bisons", 9, 4),
            new Team("Hawks", 9, 6), // Ties with Eagles on points and GD!
            new Team("Sharks", 6, 5)
        ));

        List<Team> sortedTeams = new ArrayList<>(teams);
        sortedTeams.sort(correctRanker);

        System.out.println("   Rankings Produced by Multi-Tier Comparator:");
        for (int i = 0; i < sortedTeams.size(); i++) {
            System.out.printf("   #%d: %s%n", (i + 1), sortedTeams.get(i));
        }

        System.out.println("\n>>> 2. BREAKING A FAULTY COMPARATOR (INTEGER SUBTRACTION OVERFLOW BUG):");
        System.out.println("--------------------------------------------------------------------------------");
        // Flawed comparator using subtraction: (t1.points - t2.points)
        Comparator<Team> subtractionComparator = (t1, t2) -> (t2.points - t1.points);

        Team teamA = new Team("TeamExtremeHigh", Integer.MAX_VALUE - 5, 0);
        Team teamB = new Team("TeamExtremeLow", -100, 0);

        int resultSubtract = subtractionComparator.compare(teamA, teamB);
        int resultSafe = Integer.compare(teamB.points, teamA.points);

        System.out.println("   Comparing TeamExtremeHigh (" + teamA.points + ") vs TeamExtremeLow (" + teamB.points + "):");
        System.out.printf("   • (t2.points - t1.points) Subtraction Result = %d (OVERFLOW ERROR -> False Inversion!)%n", resultSubtract);
        System.out.printf("   • Integer.compare() Safe Comparison Result  = %d (CORRECT NEGATIVE VALUE)%n", resultSafe);
        System.out.println("   [CRITICAL]: Never use raw subtraction for numeric comparisons in Java!");

        System.out.println("\n>>> 3. GENERIC REUSABILITY (RANKING QUIZ PLAYERS VIA LEADERBOARD<T>):");
        System.out.println("--------------------------------------------------------------------------------");
        Comparator<QuizPlayer> quizRanker = Comparator
            .comparingInt((QuizPlayer p) -> p.score).reversed()
            .thenComparingInt(p -> p.penaltySeconds)
            .thenComparing(p -> p.username);

        Leaderboard<QuizPlayer> quizBoard = new Leaderboard<>(quizRanker);
        quizBoard.add(new QuizPlayer("Madesh_P", 95, 120));
        quizBoard.add(new QuizPlayer("Arun_K", 95, 90)); // Higher rank due to lower penalty
        quizBoard.add(new QuizPlayer("Divya_S", 100, 150)); // Highest score

        System.out.println("   Quiz Leaderboard Ranking:");
        List<QuizPlayer> quizRanked = quizBoard.ranking();
        for (int i = 0; i < quizRanked.size(); i++) {
            System.out.printf("   Rank %d: %s%n", (i + 1), quizRanked.get(i));
        }

        System.out.println("\n================================================================================");
        System.out.println(" [WHY BISONS OVER PANTHERS]: Points=9, GD=4 tied. Lexicographical tie-breaker  ");
        System.out.println("                             'Bisons'.compareTo('Panthers') < 0 ranks Bisons 1st ");
        System.out.println("================================================================================");
    }
}
