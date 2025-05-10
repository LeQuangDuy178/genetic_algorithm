package genetic_algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleGeneticSudokuSolver {

    //------------------------------------------------------------------------------------------------
    // Supporting properties
    private static final int generation_display = 1;

    // Sudoku board-type properties
    // The GRID_SIZE and SUBGRID_SIZE is constant for all Sudoku board (9x9 and 3x3)
    // The complexity of this Genetic Algorithm is defined by 3 key manually-tunable parameters
    // Which are POPULATION_SIZE, MUTATION_RATE and MAX_GENERATIONS
    // Lets call their size are the notations P, M and G, respectively
    private static final int GRID_SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static final Random RANDOM = new Random();

    // Tunable parameters to optimize solving algorithm
    private static int POPULATION_SIZE = 0; // Number of solving candidates in 1 generation
    private static double MUTATION_RATE = 0.0; // Lower mutation for easy puzzles
    private static int MAX_GENERATIONS = 0; // Fewer generations needed for easy puzzles

    //------------------------------------------------------------------------------------------------
    // Data Structure 1: 2D Integer Array 
    // Main data structure representing the Sudoku board elements, 1st & 2nd dimension is row & column
    // The shape of main Sudoku board is of shape (GRID_SIZE, GRIZ_SIZE) where GRID_SIZE is fixed dim value
    // The shape of 9 subgrids within Sudoku board is of shape (SUBGRID_SIZE, SUBGRID_SIZE)
    // Accessing each element in the Sudoku board by accessing its index in 1st and 2nd axis of the tensor
    // E.g.: Retrieve tensor[2][3] will access element at row 2, column 3 of 2D tensor of shape (9, 9) 

    //------------------------------------------------------------------------------------------------
    // Data Structure 2: Individual class
    // Consist of 2 properties: 2D Integer Array dtype - Sudoku board and Integer dtype - Fitness calculation 
    // Representing single Sudoku solver with appropriate solved element filled into the initial board
    // Fitness is added to indicate the potential colision number of error in the solving Sudoku board

    private static class Individual {
        int[][] board;
        int fitness;

        public Individual(int[][] initialBoard) {
            this.board = generateRandomFilledBoard(initialBoard);
            this.fitness = calculateFitness(this.board);
        }

        // public Individual(int[][] board) {
        //     this.board = copyBoard(board);
        //     this.fitness = calculateFitness(this.board);
        // }

    }

    //------------------------------------------------------------------------------------------------
    // Data Structure 3: List<Individual>
    // List data structure where elements are of dtype Individual, declared from Individual class
    // Access the Individual dtype object and its properties by Object.properties

    //------------------------------------------------------------------------------------------------
    // Method 1: solveSudokuGA(int[][] initialBoard)
    // Data Structure 1: dtype - 2D integer array
    // Time Complexity: O(G * P(log(P)))
    // Space Complexity: O(P)
    // Algorithm Analysis: solveSudokuGA(int[][] initialBoard)
    // Key Idea: Solve a Sudoku board GAs, use tunable params to control the solving strategy Genetic Algorithms
    // Step 1: Initialize the population list with all possible solving Individual candidates
    // Step 2: In current generation, Sort within the list any candidates and try to compare their fitness value
    // Step 3: If the fitness of any solving candidate within the population list is 0 -> Return solving board
    // Step 4: At next generation, scale half population size and split the sublist of population correspondingly
    // Step 5: In each iteration of generation in range(MAX_GENERATION), if sublist is still halfly scalable
    // Step 6: Try tournament selection strategy and cross over strategy to optimize the solving algorithms
    // Step 7: Mutate the resulted childBoard with respect to the initialBoard with the mutation rate
    // Step 8: Display the informations at each generation, if number of generation reach till end -> No Solution
    public static int[][] solveSudokuGA(int[][] initialBoard) {
        List<Individual> population = initializePopulation(initialBoard);
        
        
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            population.sort((a, b) -> Integer.compare(a.fitness, b.fitness));

            if (population.get(0).fitness == 0) {
                System.out.println("Solution found at generation: " + generation);
                
                return population.get(0).board;
            }

            List<Individual> nextGeneration = new ArrayList<>();
            nextGeneration.addAll(population.subList(0, POPULATION_SIZE / 2)); // Keep the fittest

            while (nextGeneration.size() < POPULATION_SIZE) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);
                int[][] childBoard = crossover(parent1.board, parent2.board, initialBoard);
                mutate(childBoard, initialBoard);
                nextGeneration.add(new Individual(childBoard));
            }

            population = nextGeneration;
            if (generation % generation_display == 0) {
                System.out.println("Generation " + generation + ", Best Fitness: " + population.get(0).fitness);
            }
        }

        System.out.println("Generation number: " + MAX_GENERATIONS);
        System.out.println("Population size: " + POPULATION_SIZE);
        System.out.println("Mutation rate: " + MUTATION_RATE);
        System.out.println("Maximum generations reached. Best fitness: " + population.get(0).fitness);
        return population.get(0).board;
    }

    //------------------------------------------------------------------------------------------------
    // Method 2: initializePopulation(int[][] initialBoard)
    // Data Structure 3: dtype - List<Individual>
    // Time Complexity: O(P)
    // Space Complexity O(P)
    // Algorithm analysis: initializePopulation(int[][] initialBoard)
    // Key Idea: Initialize the population where each Individual is a candidate for the Sudoku solver with fitness 
    // Step 1: Create new object population as a List of Individual dtype, List is from Java library
    // Step 2: Add new object Individual of Sudoku initial board to the population List based on POPULATION_SIZE
    // Step 3: Return that List of Individual for further use
    private static List<Individual> initializePopulation(int[][] initialBoard) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Individual(initialBoard));
        }
        return population;
    }

    //------------------------------------------------------------------------------------------------
    // Method 3: generateRandomFilledBoard(int[][] initialBoard)
    // Data Strucutre 1: dtype - 2D integer array
    // Time Complexity: O(1)
    // Space Complexity: O(1)
    // Algorithm Analysis: generateRandomFilledBoard(int[][] initialBoard)
    // Key Idea: Get the random solving filled Sudoku board, which may be correct or incorrect
    // Step 1: Copy the initialBoard to the 2D array board and generate list of integers 1 -> 9
    // Step 2: Iterate through the Sudoku board in both row and column axis by nested loop
    // Step 3: If there is any cell in board is zero, retreive the list of all possible values to be filled in spot
    // Step 4: Fill any random in the possible list to the spot where board[i][j] == 0
    // Step 5: If no possible values in possible list, use 1->9 list instead (not recommended for easy board)
    private static int[][] generateRandomFilledBoard(int[][] initialBoard) {
        int[][] board = copyBoard(initialBoard);
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] == 0) {
                    List<Integer> possible = getPossibleValues(board, i, j);
                    if (!possible.isEmpty()) {
                        board[i][j] = possible.get(RANDOM.nextInt(possible.size()));
                    } else {
                        // This should ideally not happen for easy puzzles if initial is valid
                        board[i][j] = numbers.get(RANDOM.nextInt(numbers.size())); // Fallback
                    }
                }
            }
        }
        return board;
    }

    //------------------------------------------------------------------------------------------------
    // Method 4: tournamentSelection(List<Individual> population)
    // Data Structure 2: dtype - Individual
    // Time Complexity: O(1)
    // Space Complexity: O(1)
    // Algorithm Analysis: tournamentSelection(List<Individual> population)
    // Key Idea: Selection strategy by Genetic Algorithm to choose best candidate in Individual population list
    // Step 1: Tournament size indicates number of candidates competing together for final selection
    // Step 2: Array List tournament is to store the list of candidates compete together 
    // Step 3: Iterate through the tounament size bounding range, randomly append the candidate from population
    // Step 4: Tournament - implement Java stream() and compare() method to find the candidate with minimum fitness
    private static Individual tournamentSelection(List<Individual> population) {
        int tournamentSize = 5;
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(RANDOM.nextInt(population.size())));
        }
        return tournament.stream().min((a, b) -> Integer.compare(a.fitness, b.fitness)).orElse(null);
    }

    //------------------------------------------------------------------------------------------------
    // Method 5: crossover(int[][] parent1, int[][] parent2, int[][] initialBoard)
    // Data Structure 1: dtype - 2D integer array
    // Time Complexity: O(1)
    // Space Complexity: O(1)
    // Algorithm Analysis: crossover(int[][] parent1, int[][] parent2, int[][] initialBoard)
    // Key Idea: Cross over operation of GAs, combine the potential great solving traits of 2 parents to child solution 
    // Step 1: Generate new 2D child array by copy all its cell in initialBoard and new Random object
    // Step 2: Iterate through column and row of Sudoku child board, if not-filled cell ->
    // Step 3: Copy the element of corresponding spot of parent1 if random.boolean == True, else that of parent2
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
    // Data Structure: dtype - void method, no dtype return
    // Time Complexity: O(1)
    // Space Complexity: O(1)
    // Algorithm Analysis: mutate(int[][] board, int[][] initialBoard)
    // Key idea: Given tunable parameter MUTATION_RATE, randomly fill possible value if variation of mutation is low
    // Step 1: Iterate through the 2D Sudoku unsolved board in row and column axis
    // Step 2: if the randomized float is lower than MUTATION_RATE and there is empty cell in board ->
    // Step 3: Solve the board cell in that position by randomly fill the possible value into it
    // Note: Higher MUTATION_RATE allows more solving variation to the board, lower limits it (for easier board)
    private static void mutate(int[][] board, int[][] initialBoard) {
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
    // Data Structure: dtype - integer
    // Time Complexity: O(1)
    // Space Complexity: O(1)
    // Algorithm Analysis: calculateFitness(int[][] board)
    // Key Idea: Calculate the fitness of current board solution - the violation of errors based on Sudoku rules 
    // Step 1: Generate number of conflicts of the board with the Sudoku rules
    // Step 2: If there is duplicate element in any of the 1x9 row or 9x1 column or 3x3 subgrid via iteration ->
    // Step 3: Increment the number of conflicts by number of violations and return the conflicts number
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
        //System.out.println("Fitness: " + conflicts);
        return conflicts;
    }

    //------------------------------------------------------------------------------------------------
    // Methods: all helper methods
    // Time Complexity: O(1)
    // Space Complexity: O(1)

    // Help Method 1: countDuplicates(int[] arr)
    // Data Structure: Integer
    // Algorithm Analysis: countDuplicates(int[] arr)
    // Key Idea: Count any duplicate cell in the given array
    // Step 1: The method gets 1D array of elements 1 -> 9
    // Step 2: New object seen as an zeros Array List with capable of 10
    // Step 3: Whenever num interated through array is contained in seen list, return True and increment duplicates
    // Step 4: Add num to that list and return that duplicates number
    private static int countDuplicates(int[] arr) {
        List<Integer> seen = new ArrayList<>();
        int duplicates = 0;
        for (int num : arr) {
            if (num != 0 && seen.contains(num)) {
                duplicates++;
            }
            seen.add(num);
        }
        return duplicates;
    }

    // Helper Method 2: getRow(int[][] board, int row)
    // Data Structure: 1D Integer Array
    // Algorithm Analysis: getRow(int[][] board, int row)
    // Key Idea: Get all the elements within the row of the board of shape (1x9, )
    // Step 1: Get the Sudoku board and indexing row number as input arguments
    // Step 2: Return the 1D Array of all elements within that row of input Sudoku board
    private static int[] getRow(int[][] board, int row) {
        return board[row];
    }

    // Helper Method 3: getColumn(int[][] board, int col)
    // Data Structure: 1D Integer Array
    // Algorithm Analysis: getColumn(int[][] board, int col)
    // Key Idea: Get all the elemtns within the column of the board of shape (9x1, )
    // Step 1: Get the Sudoku board and indexing column number as input arguments
    // Step 2: Return the 1D Array of all elements within that column of input Sudoku board
    private static int[] getColumn(int[][] board, int col) {
        int[] column = new int[GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            column[i] = board[i][col];
        }
        return column;
    }

    // Helper Method 4: getSubgrid(int[][] board, int startRow, int startCol)
    // Data Structure: 1D Integer Array
    // Algorithm Analysis: getSubgrid(int[][] board, int startRow, int startCol)
    // Key Idea: Get all elements within the 3x3 Subgrid of the board, order is all column 1st -> all row 2nd
    // Step 1: Get the Sudoku board, starting indexing row and column as input arguments
    // Step 2: Generate zeros subgrid 1D integer array of shape (3x3, ) == (9, ) or (GRIZ_SIZE, )
    // Step 3: Iterate i and j through range of SUBGRID_SIZE for elements in board in both row and column axis
    // Step 4: Retrieve the value at startRow and startCol with iterator i and j and return that subgrid
    // Note: The order of 1D array representation of subgrid is all elements of columns in row 1 -> those of row 2,3
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
    // Data Structure: List of Integer 
    // Algorithm Analysis: getPossibleValues(int[][] board, int row, int col)
    // Step 1: Create new object array list of zeros integer
    // Step 2: Iterate i from 1 -> 9, call method isValidPlacement to check if existing any violation Sudoku board
    // Step 3: If no violation -> Append the num to possibleValues array list, should be valid value of the board
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
    // Data Structure: Boolean (True or False)
    // Algorithm Analysis: isValidPlacement(int[][] board, int num, int row, int col)
    // Step 1: Receiving num as input, determine if any specific number at row and col is violate any of Sudoku rule
    // Step 2: Iterating through all cells in both row and column axis
    // Step 3: If one of any cell in the iterating row and column already contains num -> violate Sudoku rule
    // Step 4: Iterate through 3x3 subgrid also, check singly availablity of the number in subgrid
    // Step 5: If step 3/4 has violation -> return False for incorrect board, else -> return True for correct board 
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
    // Data Structure 1: 2D Integer Array
    // Algorithm Analysis: copyBoard(int[][] source)
    // Step 1: Generate the zeros 2D array of shape (GRID_SIZE, GRID_SIZE)
    // Step 2: Deep copy the input 2D array source to the generated array and return that array
    private static int[][] copyBoard(int[][] source) {
        int[][] destination = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, GRID_SIZE);
        }
        return destination;
    }

    // Helper Method 8: printBoard(int[][] board)
    // Data Structure: Void
    // Algorithm Analysis: printBoard(int[][] board)
    // Step 1: Print the input Sudoku board in pattern of GRID_SIZExGRID_SIZE
    public static void printBoard(int[][] board) {
        for (int[] row : board) {
            for (int num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }

    // Helper Method 9: printGAConfig(int POPULATION_SIZE, double MUTATION_RATE, int MAX_GENERATIONS)
    // Data Structure: Void
    // Algorithm Analysis: printGAConfig(int POPULATION_SIZE, double MUTATION_RATE, int MAX_GENERATIONS)
    // Step 1: Print the parameters implemented to solve the Sudoku boards
    public static void printGAConfig(int POPULATION_SIZE, double MUTATION_RATE, int MAX_GENERATIONS) {
        System.out.println("\nPopulation size: " + POPULATION_SIZE);
        System.out.println("Mutation rate: " + MUTATION_RATE);
        System.out.println("Maximum generations: " + MAX_GENERATIONS);
    }

    //------------------------------------------------------------------------------------------------
    // Main code
    public static void main(String[] args) {
        
        // Init all boards
        List<int[][]> EasySudokuBoards = SudokuData.GetEasyBoards();
        List<int[][]> MediumSudokuBoards = SudokuData.GetMediumBoard();
        List<int[][]> HardSudokuBoards = SudokuData.GetHardBoards();

        // All flags for solving indicator
        boolean SolveEasy = false;
        boolean SolveMedium = false;
        boolean SolveHard = true;

        if (SolveEasy) {
            double totalTime = 0.0;

            POPULATION_SIZE = 10;
            MUTATION_RATE = 0.2;
            MAX_GENERATIONS = 10;

            System.out.println("Genetic Algorithm Parameters for Easy Sudoku Board: ");

            for (int i = 0; i < EasySudokuBoards.size(); i++) {
                System.out.println("\nSolving Easy Sudoku with Simple Genetic Algorithm:");
                System.out.println("Initial Puzzle " + (i + 1) + ":");
                printBoard(EasySudokuBoards.get(i));

                long startTime = System.currentTimeMillis();
                int[][] solution = solveSudokuGA(EasySudokuBoards.get(i));
                long endTime = System.currentTimeMillis();
                double solveTime = 0.0;
                
                System.out.println("\nSolution of Puzzle " + (i + 1) + ":");
                if (solution != null) {
                    printBoard(solution);
                } else {
                    System.out.println("Could not find a solution within the given generations.");
                }
                System.out.println("Time taken: " + (endTime - startTime) / 1000.0 + " seconds");

                solveTime = (endTime - startTime) / 1000.0;
                totalTime += solveTime;

            }

            printGAConfig(POPULATION_SIZE, totalTime, MAX_GENERATIONS);
            System.out.println("\nTotal time for " + EasySudokuBoards.size() + " Easy Sudoku Boards: " + totalTime + " seconds");
        }

        if (SolveMedium) {
            double totalTime = 0.0;

            POPULATION_SIZE = 100;
            MUTATION_RATE = 0.5;
            MAX_GENERATIONS = 20;
            
            for (int i = 0; i < MediumSudokuBoards.size(); i++) {
                System.out.println("\nSolving Medium Sudoku with Simple Genetic Algorithm:");
                System.out.println("Initial Puzzle " + (i + 1) + ":");
                printBoard(MediumSudokuBoards.get(i));

                long startTime = System.currentTimeMillis();
                int[][] solution = solveSudokuGA(MediumSudokuBoards.get(i));
                long endTime = System.currentTimeMillis();
                double solveTime = 0.0;

                System.out.println("\nSolution of Puzzle " + (i + 1) + ":");
                if (solution != null) {
                    printBoard(solution);
                } else {
                    System.out.println("Could not find a solution within the given generations.");
                }
                System.out.println("Time taken: " + (endTime - startTime) / 1000.0 + " seconds");

                solveTime = (endTime - startTime) / 1000.0;
                totalTime += solveTime;
            }

            printGAConfig(POPULATION_SIZE, totalTime, MAX_GENERATIONS);
            System.out.println("\nTotal time for " + MediumSudokuBoards.size() + " Medium Sudoku Boards: " + totalTime + " seconds");
        }

        if (SolveHard) {
            double totalTime = 0.0;

            POPULATION_SIZE = 1000;
            MUTATION_RATE = 0.8;
            MAX_GENERATIONS = 50;

            for (int i = 0; i < HardSudokuBoards.size(); i++) {
                System.out.println("\nSolving Medium Sudoku with Simple Genetic Algorithm:");
                System.out.println("Initial Puzzle " + (i + 1) + ":");
                printBoard(HardSudokuBoards.get(i));

                long startTime = System.currentTimeMillis();
                int[][] solution = solveSudokuGA(HardSudokuBoards.get(i));
                long endTime = System.currentTimeMillis();
                double solveTime = 0.0;

                System.out.println("\nSolution of Puzzle " + (i + 1) + ":");
                if (solution != null) {
                    printBoard(solution);
                } else {
                    System.out.println("Could not find a solution within the given generations.");
                }
                System.out.println("Time taken: " + (endTime - startTime) / 1000.0 + " seconds");

                solveTime = (endTime - startTime) / 1000.0;
                totalTime += solveTime;
            }

            printGAConfig(POPULATION_SIZE, totalTime, MAX_GENERATIONS);
            System.out.println("\nTotal time for " + HardSudokuBoards.size() + " Hard Sudoku Boards: " + totalTime + " seconds");
        }
        
    }
}