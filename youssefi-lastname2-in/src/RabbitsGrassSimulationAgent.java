import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.*;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    enum Direction {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

	private int x;
	private int y;
	private Direction direction;
	private int energy;
	private RabbitsGrassSimulationSpace space;
    private boolean gave_birdth;

    private final int birth_threshold;
    private final int grass_energy;
    private final Color color;



	public RabbitsGrassSimulationAgent(int energy, int birth_threshold, Color color, int grass_energy){
		x = 1;
		y = 1;
        gave_birdth = false;
        updateDirection();
		this.energy = energy;
		this.birth_threshold = birth_threshold;
		this.color = color;
		this.grass_energy = grass_energy;
	}

    private void updateDirection() {
        direction = Direction.values()[(int) (Math.random() * 4)];
    }

    /**
     * Set the position
     * @param newX x
     * @param newY y
     */
    public void setXY(int newX, int newY){
		x = newX;
		y = newY;
	}

    /**
     * Move to the direction
     * @param direction Direction enum
     * @return true on success
     */
    private boolean updateXY(Direction direction){
        int new_x = x;
        int new_y = y;

        switch (direction){
            case LEFT:
                 new_x -=1;
                 break;
            case RIGHT:
                new_x += 1;
                break;
            case TOP:
                new_y -= 1 ;
                break;
            case BOTTOM:
                new_y += 1;
                break;
        }

        Object2DGrid grid = space.getCurrentRabbitSpace();
        new_x = (new_x + grid.getSizeX()) % grid.getSizeX();
        new_y = (new_y + grid.getSizeY()) % grid.getSizeY();

        if(tryMoveRabbit(new_x, new_y)){
            // handle move
            // check if there is grass
            if(space.isCellOccupiedByGrass(x, y)){
                energy += grass_energy;
            }

            return true;
        }

        return false;
    }

	public void draw(SimGraphics G) {
        G.drawFastRoundRect(this.color);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

    public void step(){
        updateDirection();

        if(updateXY(direction)) {
            energy--;
        }

        if(energy >= birth_threshold){
            energy /= 2;
            gave_birdth = true;
        }


    }

    /**
     * Move the rabbit to the destination
     * @param newX x
     * @param newY y
     * @return boolean true if succcess
     */
    private boolean tryMoveRabbit(int newX, int newY){
        return space.moveRabbitAt(x, y, newX, newY);
    }


	public int getEnergy() {
		return energy;
	}

    public RabbitsGrassSimulationSpace getSpace() {
        return space;
    }

    public void setSpace(RabbitsGrassSimulationSpace space) {
        this.space = space;
    }

    public boolean gaveBirdth() {
        return gave_birdth;
    }
    public void setGaveBirdth(boolean bool) {
        gave_birdth = bool;
    }
}
