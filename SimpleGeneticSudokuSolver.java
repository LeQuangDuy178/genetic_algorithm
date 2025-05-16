import java.util.Random;

public class SimpleGeneticSudokuSolver {

    //------------------------------------------------------------------------------------------------
    // Supporting properties
    private static final int generation_display = 1;
    private static final boolean generation_flag = false;

    // Sudoku board-type properties
    private static final int GRID_SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static final Random RANDOM = new Random();

    // Tunable parameters to optimize solving algorithm
    int POPULATION_SIZE = 0; // Number of solving candidates in 1 generation
    double MUTATION_RATE = 0.0; // Lower mutation for easy puzzles
    int MAX_GENERATIONS = 0; // Fewer generations needed for easy puzzles

    //------------------------------------------------------------------------------------------------
    // Data Structure 2: Individual class
    private static class Individual {
        int[][] board;
        int fitness;

        public Individual(int[][] initialBoard) {
            this.board = generateRandomFilledBoard(initialBoard);
            this.fitness = calculateFitness(this.board);
        }
    }

    //--------------------------------------------------------------------
    // Constructor
    public SimpleGeneticSudokuSolver(int POPULATION_SIZE, double MUTATION_RATE, int MAX_GENERATIONS) {
        this.POPULATION_SIZE = POPULATION_SIZE;
        this.MUTATION_RATE = MUTATION_RATE;
        this.MAX_GENERATIONS = MAX_GENERATIONS;
    }

    //--------------------------------------------------------------------

    //------------------------------------------------------------------------------------------------
    // Method 1: solve(int[][] initialBoard)
    public int[][] solve(int[][] initialBoard) {
        List<Individual> population = initializePopulation(initialBoard);

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            bubbleSortPopulation(population); // Use scratch sort

            if (population.get(0).fitness == 0) {
                System.out.println("Solution found at generation: " + generation);
                return population.get(0).board;
            }

            List<Individual> nextGeneration = new ArrayList<>();
            // Keep the fittest (replacement for subList)
            for (int i = 0; i < POPULATION_SIZE / 2; i++) {
                nextGeneration.add(population.get(i));
            }

            while (nextGeneration.size() < POPULATION_SIZE) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);
                int[][] childBoard = crossover(parent1.board, parent2.board, initialBoard);
                mutate(childBoard, initialBoard);
                nextGeneration.add(new Individual(childBoard));
            }

            population = nextGeneration;
            if (generation % generation_display == 0) {
                if (generation_flag) {
                    System.out.println("Generation " + generation + ", Best Fitness: " + population.get(0).fitness);
                }
            }
        }

        System.out.println("Generation number: " + MAX_GENERATIONS);
        System.out.println("Population size: " + POPULATION_SIZE);
        System.out.println("Mutation rate: " + MUTATION_RATE);
        System.out.println("Maximum generations reached. Best fitness: " + population.get(0).fitness);
        return population.get(0).board;
    }

    // Scratch implementation of bubble sort for List<Individual>
    private void bubbleSortPopulation(List<Individual> population) {
        int n = population.size();
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (population.get(j).fitness > population.get(j + 1).fitness) {
                    Individual temp = population.get(j);
                    population.set(j, population.get(j + 1));
                    population.set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    //------------------------------------------------------------------------------------------------
    // Method 2: initializePopulation(int[][] initialBoard)
    private List<Individual> initializePopulation(int[][] initialBoard) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Individual(initialBoard));
        }
        return population;
    }

    //------------------------------------------------------------------------------------------------
    // Method 3: generateRandomFilledBoard(int[][] initialBoard)
    private static int[][] generateRandomFilledBoard(int[][] initialBoard) {
        int[][] board = copyBoard(initialBoard);
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE; i++) {
            numbers.add(i);
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] == 0) {
                    List<Integer> possible = getPossibleValues(board, i, j);
                    if (!possible.isEmpty()) {
                        board[i][j] = possible.get(RANDOM.nextInt(possible.size()));
                    } else {
                        board[i][j] = numbers.get(RANDOM.nextInt(numbers.size())); // Fallback
                    }
                }
            }
        }
        return board;
    }

    //------------------------------------------------------------------------------------------------
    // Method 4: tournamentSelection(List<Individual> population)
    private static Individual tournamentSelection(List<Individual> population) {
        int tournamentSize = 5;
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(RANDOM.nextInt(population.size())));
        }
        Individual fittest = tournament.get(0);
        for (int i = 1; i < tournament.size(); i++) {
            if (tournament.get(i).fitness < fittest.fitness) {
                fittest = tournament.get(i);
            }
        }
        return fittest;
    }

    //------------------------------------------------------------------------------------------------
    // Method 5: crossover(int[][] parent1, int[][] parent2, int[][] initialBoard)
    private static int[][] crossover(int[][] parent1, int[][] parent2, int[][] initialBoard) {
        int[][] child = copyBoard(initialBoard);
        Random random = new Random();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (child[i][j] == 0) {
                    child[i][j] = random.nextBoolean() ? parent1[i][j] : parent2[i][j];
                }
            }
        }
        return child;
    }

    //------------------------------------------------------------------------------------------------
    // Method 6: mutate(int[][] board, int[][] initialBoard)
    private void mutate(int[][] board, int[][] initialBoard) {
        Random random = new Random();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (initialBoard[i][j] == 0 && random.nextDouble() < MUTATION_RATE) {
                    List<Integer> possibleValues = getPossibleValues(board, i, j);
                    if (!possibleValues.isEmpty()) {
                        board[i][j] = possibleValues.get(random.nextInt(possibleValues.size()));
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------
    // Method 7: calculateFitness(int[][] board)
    private static int calculateFitness(int[][] board) {
        int conflicts = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            conflicts += countDuplicates(getRow(board, i));
            conflicts += countDuplicates(getColumn(board, i));
        }
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                conflicts += countDuplicates(getSubgrid(board, i * SUBGRID_SIZE, j * SUBGRID_SIZE));
            }
        }
        return conflicts;
    }

    //------------------------------------------------------------------------------------------------
    // Methods: all helper methods

    // Help Method 1: countDuplicates(int[] arr)
    private static int countDuplicates(int[] arr) {
        List<Integer> seen = new ArrayList<>(); // Use your ArrayList
        int duplicates = 0;
        for (int num : arr) {
            if (num != 0) {
                boolean found = false;
                for (int i = 0; i < seen.size(); i++) {  // Iterate with index
                    if (seen.get(i).equals(num)) { // Use .equals() for comparison
                        found = true;
                        break;
                    }
                }
                if (found) {
                    duplicates++;
                }
                seen.add(num);
            }
        }
        return duplicates;
    }

    // Helper Method 2: getRow(int[][] board, int row)
    private static int[] getRow(int[][] board, int row) {
        int[] r = new int[GRID_SIZE];
        System.arraycopy(board[row], 0, r, 0, GRID_SIZE);
        return r;
    }

    // Helper Method 3: getColumn(int[][] board, int col)
    private static int[] getColumn(int[][] board, int col) {
        int[] column = new int[GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            column[i] = board[i][col];
        }
        return column;
    }

    // Helper Method 4: getSubgrid(int[][] board, int startRow, int startCol)
    private static int[] getSubgrid(int[][] board, int startRow, int startCol) {
        int[] subgrid = new int[GRID_SIZE];
        int index = 0;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                subgrid[index++] = board[startRow + i][startCol + j];
            }
        }
        return subgrid;
    }

    // Helper Method 5: getPossibleValues(int[][] board, int row, int col)
    private static List<Integer> getPossibleValues(int[][] board, int row, int col) {
        List<Integer> possibleValues = new ArrayList<>();
        for (int num = 1; num <= GRID_SIZE; num++) {
            if (isValidPlacement(board, num, row, col)) {
                possibleValues.add(num);
            }
        }
        return possibleValues;
    }

    // Helper Method 6: isValidPlacement(int[][] board, int num, int row, int col)
    private static boolean isValidPlacement(int[][] board, int num, int row, int col) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }
        int startRow = row - row % SUBGRID_SIZE;
        int startCol = col - col % SUBGRID_SIZE;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (board[startRow + i][startCol + j] == num) return false;
            }
        }
        return true;
    }

    // Helper Method 7: copyBoard(int[][] source)
    private static int[][] copyBoard(int[][] source) {
        int[][] destination = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, GRID_SIZE);
        }
        return destination;
    }

    // Helper Method 8: printBoard(int[][] board)
    public static void printBoard(int[][] board) {
        for (int[] row : board) {
            for (int num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }

    // Helper Method 9: printGAConfig(int POPULATION_SIZE, double MUTATION_RATE, int MAX_GENERATIONS)
    public static void printGAConfig(int POPULATION_SIZE, double MUTATION_RATE, int MAX_GENERATIONS) {
        System.out.println("\nPopulation size: " + POPULATION_SIZE);
        System.out.println("Mutation rate: " + MUTATION_RATE);
        System.out.println("Maximum generations: " + MAX_GENERATIONS);
    }

    // Helper Method 10: isValidSet(int[] arr)
    private static boolean isValidSet(int[] arr) {
        List<Integer> seen = new ArrayList<>(); // Using the provided ArrayList implementation
        for (int num : arr) {
            if (num < 1 || num > 9) return false;
            boolean found = false;
            for (int i = 0; i < seen.size(); i++) { // Iterate using index
                if (seen.get(i).equals(num)) { // Use .equals() for Integer comparison
                    found = true;
                    break;
                }
            }
            if (found) return false;
            seen.add(num);
        }
        return seen.size() == GRID_SIZE;
    }

    // Helper Method 11: isCorrectSolved(int[][] board)
    private static boolean isCorrectSolved(int[][] board) {
        // Check rows
        for (int i = 0; i < GRID_SIZE; i++) {
            if (!isValidSet(getRow(board, i))) return false;
        }

        // Check columns
        for (int i = 0; i < GRID_SIZE; i++) {
            if (!isValidSet(getColumn(board, i))) return false;
        }

        // Check subgrids
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (!isValidSet(getSubgrid(board, i * SUBGRID_SIZE, j * SUBGRID_SIZE))) return false;
            }
        }

        return true;
    }

    // Helper Method 12: SolveAndPrint(String level, int[][] board, int population_size, double mutation_rate, int max_generations, boolean print_board)
    private static void SolveAndPrint(String level, int population_size, double mutation_rate, int max_generations, boolean print_board) {
        List<int[][]> SudokuBoards = new ArrayList<>();

        if (level.equals("Easy")) {
            SudokuBoards = SudokuData.GetEasyBoards();
        } else if (level.equals("Medium")) {
            SudokuBoards = SudokuData.GetMediumBoards();
        } else if (level.equals("Hard")) {
            SudokuBoards = SudokuData.GetHardBoards();
        } else if (level.equals("Very Hard")) {
            SudokuBoards = SudokuData.GetVeryHardBoards();
        }

        double totalTime = 0.0;
        int countSolve = 0;

        SimpleGeneticSudokuSolver SudokuSolver = new SimpleGeneticSudokuSolver(population_size,
                mutation_rate, max_generations);

        System.out.println("Genetic Algorithm Parameters for " + level + " Sudoku Board: ");
        printGAConfig(population_size, mutation_rate, max_generations);

        for (int i = 0; i < SudokuBoards.size(); i++) {
            System.out.println("\nSolving " + level + " Sudoku with Simple Genetic Algorithm:");
            System.out.println("Initial Puzzle " + (i + 1) + ":");
            if (print_board) {
                printBoard(SudokuBoards.get(i));
            }

            long startTime = System.currentTimeMillis();
            int[][] solution = SudokuSolver.solve(SudokuBoards.get(i));
            long endTime = System.currentTimeMillis();
            double solveTime = 0.0;

            System.out.println("\nSolution of Puzzle " + (i + 1) + ":");
            if (solution != null) {
                if (print_board) {
                    printBoard(solution);
                }
                if (isCorrectSolved(solution)) {
                    System.out.println("Board " + (i + 1) + " is solved correctly!");
                    countSolve++;
                } else {
                    System.out.println("Board " + (i + 1) + " has incorrect solution");
                }
            } else {
                System.out.println("Could not find a solution within the given generations.");
            }
            System.out.println("Time taken: " + (endTime - startTime) / 1000.0 + " seconds");

            solveTime = (endTime - startTime) / 1000.0;
                totalTime += solveTime;

            }

            printGAConfig(population_size, mutation_rate, max_generations);
            System.out.println("\nThe algorithm solve correctly " + countSolve + " out of " + SudokuBoards.size() + " " + level + " Sudoku Boards");
            System.out.println("\nTotal time for " + SudokuBoards.size() + " " + level + " Sudoku Boards: " + totalTime + " seconds");
    } 

    //------------------------------------------------------------------------------------------------
    // Main code
    public static void main(String[] args) {

        SolveAndPrint("Very Hard", 1000, 1.6, 50, true);

    }
}