import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.util.SimUtilities;

import java.awt.*;
import java.util.ArrayList;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {
    private static final int GRID_SIZE = 20;
    private static final int NUM_INIT_RABBITS = 10;
    private static final int NUM_INIT_GRASS = 10;
    private static final int GRASS_GROWTH_RATE = 1;
    private static final int BIRTH_THRESHOLD = 75;
    private static final int INITIAL_ENERGY = 50;
    private static final int INITIAL_GRASS_ENERGY = 10;


    private Schedule schedule;
    private RabbitsGrassSimulationSpace space;
    private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
    private ArrayList<RabbitsGrassSimulationAgent> grassList;
    private DisplaySurface displaySurf;

    private int gridSize = GRID_SIZE;
    private int numInitRabbits = NUM_INIT_RABBITS;
    private int numInitGrass = NUM_INIT_GRASS;
    private int grassGrowthRate = GRASS_GROWTH_RATE;
    private int birthThreshold = BIRTH_THRESHOLD;
    private int initialEnergy = INITIAL_ENERGY;
    private int grassEnergy = INITIAL_GRASS_ENERGY;

    private ArrayList<Integer> evolution_number_rabbits;

    private OpenSequenceGraph number_of_rabbits;


    public void setup() {
        System.out.println("Running setup");
        space = null;
        rabbitList = new ArrayList<>();
        grassList = new ArrayList<>();
        evolution_number_rabbits = new ArrayList<>();
        schedule = new Schedule(1);

        // Init display
        if (displaySurf != null) {
            displaySurf.dispose();
        }
        displaySurf = null;


        if (number_of_rabbits != null){
            number_of_rabbits.dispose();
        }
        number_of_rabbits = null;

        // Create Displays
        displaySurf = new DisplaySurface(this, "Simulation rabbit grass");
        number_of_rabbits = new OpenSequenceGraph("Number of rabbits",this);

        // Register Displays
        registerDisplaySurface("Simulation rabbit grass", displaySurf);
        this.registerMediaProducer("Plot", number_of_rabbits);
    }


    public static void main(String[] args) {

        System.out.println("Rabbit skeleton");

        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        // Do "not" modify the following lines of parsing arguments
        if (args.length == 0) // by default, you don't use parameter file nor batch mode
            init.loadModel(model, "", false);
        else
            init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));

    }

    public void begin() {
        buildModel();
        buildSchedule();
        buildDisplay();

        displaySurf.display();
        number_of_rabbits.display();
    }

    public void buildModel() {
        System.out.println("Running BuildModel");

        space = new RabbitsGrassSimulationSpace(gridSize, gridSize);

        for (int i = 0; i < numInitRabbits; i++) {
            addNewRabbit();
        }

        for (int i = 0; i < numInitGrass; i++) {
            addNewGrass();
        }
    }

    private void addNewGrass() {
        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(initialEnergy, birthThreshold, Color.green, grassEnergy);
        grassList.add(a);
        space.addGrass(a);
    }

    private void addNewRabbit() {
        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(initialEnergy, birthThreshold, Color.red, grassEnergy);
        if (space.addRabbit(a)) {
            rabbitList.add(a);
        }
    }

    public void buildSchedule() {
        System.out.println("Running BuildSchedule");
        class RabbitsGrassStep extends BasicAction {
            public void execute() {
                if(rabbitList.isEmpty()){
                    stop();
                }

                SimUtilities.shuffle(rabbitList);
                for (int i = 0; i < rabbitList.size(); i++) {
                    RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbitList.get(i);

                    rabbit.step();

                    if (rabbit.gaveBirdth()) {
                        // new born
                        addNewRabbit();
                        rabbit.setGaveBirdth(false);
                    }

                    if (space.isCellOccupiedByGrass(rabbit.getX(), rabbit.getY())) {
                        // remove grass
                        removeGrass(rabbit.getX(), rabbit.getY());
                    }
                }

                // add grass
                for (int i = 0; i < grassGrowthRate; i++) {
                    addNewGrass();
                }

                // remove deads
                int deadRabbits = reapDeadRabbits();

                displaySurf.updateDisplay();
            }
        }

        schedule.scheduleActionBeginning(0, new RabbitsGrassStep());

        class NumberOfRabbitsInSpace extends BasicAction {
            public void execute(){
                evolution_number_rabbits.add(rabbitList.size());
                number_of_rabbits.step();
                printStats();
            }

            private void printStats() {
                Integer sum = 0;
                if(!evolution_number_rabbits.isEmpty()) {
                    for (Integer num : evolution_number_rabbits) {
                        sum += num;
                    }
                }

                int mean = sum / evolution_number_rabbits.size();

                int sum_square = 0;
                if(!evolution_number_rabbits.isEmpty()) {
                    for (Integer num : evolution_number_rabbits) {
                        sum_square += Math.pow(num-mean, 2);
                    }
                }
                double std = Math.sqrt(sum_square / evolution_number_rabbits.size());
                System.out.println("Mean : " + mean + " STD : " + std);
            }
        }

        schedule.scheduleActionAtInterval(10, new NumberOfRabbitsInSpace());
    }



    /**
     * Remove the grass at the given coordonate
     *
     * @param x x
     * @param y y
     * @return true if removed
     */
    private boolean removeGrass(int x, int y) {

        int i = 0;
        boolean removed = false;
        while (i < grassList.size() && !removed) {
            RabbitsGrassSimulationAgent grass = grassList.get(i);
            if (grass.getX() == x && grass.getY() == y) {
                space.removeGrassAt(x, y);
                grassList.remove(i);
                removed = true;
            }

            i++;
        }

        return removed;
    }

    public void buildDisplay() {
        System.out.println("Running BuildDisplay");

        ColorMap map = new ColorMap();

        for (int i = 1; i < 16; i++) {
            map.mapColor(i, new Color((int) (i * 8 + 127), 0, 0));
        }
        map.mapColor(0, Color.white);

        Object2DDisplay displayRabbits = new Object2DDisplay(space.getCurrentRabbitSpace());
        displayRabbits.setObjectList(rabbitList);

        Object2DDisplay displayGrass = new Object2DDisplay(space.getCurrentGrassSpace());
        displayGrass.setObjectList(grassList);

        displaySurf.addDisplayable(displayRabbits, "Rabbits");
        displaySurf.addDisplayable(displayGrass, "Grass");


        number_of_rabbits.addSequence("Number of rabbits", new rabbitsInSpace());
    }

    public String[] getInitParam() {
        // Parameters to be set by users via the Repast UI slider bar
        // Do "not" modify the parameters names provided in the skeleton code, you can add more if you want
        String[] params = {"GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "InitialEnergy", "BirthThreshold", "GrassEnergy"};
        return params;
    }

    public String getName() {
        return "Model name...";
    }

    public Schedule getSchedule() {
        return this.schedule;
    }


    /**
     * Remove dead rabbits
     *
     * @return number of removed rabbits
     */
    private int reapDeadRabbits() {
        int count = 0;
        for (int i = (rabbitList.size() - 1); i >= 0; i--) {
            RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent) rabbitList.get(i);
            if (cda.getEnergy() < 1) {
                space.removeRabbitAt(cda.getX(), cda.getY());
                rabbitList.remove(i);
                count++;
            }
        }
        return count;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int getNumInitRabbits() {
        return numInitRabbits;
    }

    public void setNumInitRabbits(int numInitRabbits) {
        this.numInitRabbits = numInitRabbits;
    }

    public int getNumInitGrass() {
        return numInitGrass;
    }

    public void setNumInitGrass(int numInitGrass) {
        this.numInitGrass = numInitGrass;
    }

    public int getGrassGrowthRate() {
        return grassGrowthRate;
    }

    public void setGrassGrowthRate(int grassGrowthRate) {
        this.grassGrowthRate = grassGrowthRate;
    }

    public int getBirthThreshold() {
        return birthThreshold;
    }

    public void setBirthThreshold(int birthThreshold) {
        this.birthThreshold = birthThreshold;
    }

    public int getInitialEnergy() {
        return initialEnergy;
    }

    public void setInitialEnergy(int initialEngergy) {
        this.initialEnergy = initialEngergy;
    }

    public int getGrassEnergy() {
        return grassEnergy;
    }

    public void setGrassEnergy(int grassEnergy) {
        this.grassEnergy = grassEnergy;
    }

    class rabbitsInSpace implements DataSource, Sequence {

        public Object execute() {
            return new Double(getSValue());
        }

        public double getSValue() {
            return (double) rabbitList.size();
        }
    }
}
