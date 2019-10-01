import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
    private Object2DGrid rabbit_space;
    private Object2DGrid grass_space;

    public RabbitsGrassSimulationSpace(int xSize, int ySize) {
        rabbit_space = new Object2DGrid(xSize, ySize);
        grass_space  = new Object2DGrid(xSize, ySize);
    }

    public boolean isCellOccupiedByRabbit(int x, int y){
        boolean retVal = false;
        if(rabbit_space.getObjectAt(x, y)!=null) retVal = true;
        return retVal;
    }

    public boolean isCellOccupiedByGrass(int x, int y){
        boolean retVal = false;
        if(grass_space.getObjectAt(x, y)!=null) retVal = true;
        return retVal;
    }

    private boolean addToSpace(RabbitsGrassSimulationAgent agent, Object2DGrid space, Condition condition){
        boolean retVal = false;
        int count = 0;
        int countLimit = 10 * space.getSizeX() * space.getSizeY();

        while((retVal==false) && (count < countLimit)){
            int x = (int)(Math.random()*(space.getSizeX()));
            int y = (int)(Math.random()*(space.getSizeY()));
            if(condition.apply(x, y)){
                space.putObjectAt(x,y,agent);
                agent.setXY(x,y);
                agent.setSpace(this);
                retVal = true;
            }
            count++;
        }

        return retVal;
    }

    public boolean addRabbit(RabbitsGrassSimulationAgent rabbit){
        return addToSpace(rabbit, rabbit_space, (int x, int y) -> !isCellOccupiedByRabbit(x, y));
    }

    public boolean addGrass(RabbitsGrassSimulationAgent grass){
        return addToSpace(grass, grass_space, (int x, int y) -> !isCellOccupiedByGrass(x, y));
    }

    public Object2DGrid getCurrentRabbitSpace(){
        return rabbit_space;
    }
    public Object2DGrid getCurrentGrassSpace(){
        return grass_space;
    }

    public void removeRabbitAt(int x, int y){
        rabbit_space.putObjectAt(x, y, null);
    }

    public void removeGrassAt(int x, int y){
        grass_space.putObjectAt(x, y, null);
    }


    public boolean moveRabbitAt(int x, int y, int newX, int newY){
        boolean retVal = false;
        if(!isCellOccupiedByRabbit(newX, newY)){
            RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)rabbit_space.getObjectAt(x, y);
            removeRabbitAt(x,y);
            cda.setXY(newX, newY);
            rabbit_space.putObjectAt(newX, newY, cda);
            retVal = true;
        }
        return retVal;
    }
}
