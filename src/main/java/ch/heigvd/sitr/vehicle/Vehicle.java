/*
 * Filename: Vehicle.java
 * Creation date: 23.03.2019
 */

package ch.heigvd.sitr.vehicle;

import ch.heigvd.sitr.gui.simulation.SimulationWindow;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

/**
 * Vehicle class represents the simulation vehicles
 *
 * @author Simon Walther
 */
public class Vehicle implements Renderable {
    // Itinerary of the vehicle, subdivided in multiple paths
    private LinkedList<ItineraryPath> itinerary = new LinkedList<ItineraryPath>();

    // Current path step
    @Getter
    private int pathStep;

    // Position of the vehicle relative to the lane's start [m]
    @Getter
    @Setter
    private double position;

    // Speed in [m/s] of the vehicle
    @Getter
    private double speed;

    // Max speed in [m/s] of the vehicle
    @Getter
    private final double maxSpeed;

    // Length of the vehicle in [m]
    @Getter
    private final double length;

    // Vehicle in front of this vehicle
    @Getter
    @Setter
    private Vehicle frontVehicle;

    // Vehicle controller of this vehicle
    @Getter
    private VehicleController vehicleController;

    /**
     * Constructor
     *
     * @param vehicleController controller of the vehicle
     * @param length            length [m] of the vehicle
     * @param maxSpeed          max speed [m/s] of the vehicle
     * @param firstPath         the first itinerary path of the vehicle
     */
    public Vehicle(VehicleController vehicleController, double length, double maxSpeed, ItineraryPath firstPath) {
        this.vehicleController = vehicleController;
        this.length = length;
        this.maxSpeed = maxSpeed;
        this.addToItinerary(firstPath);
    }

    /**
     * New value for the speed
     * <p>
     * Note: if speed exceed max speed then set speed to max speed
     *
     * @param speed The new speed of the vehicle [m/s]
     */
    public void setSpeed(double speed) {
        if (speed > maxSpeed) {
            speed = maxSpeed;
        } else if (speed < -maxSpeed) {
            speed = -maxSpeed;
        }

        this.speed = speed;
    }

    /**
     * Calculate the speed difference with acceleration and time difference
     *
     * @param acceleration acceleration [m/s^2]
     * @param deltaT       time difference [s]
     * @return speed difference [m/s]
     */
    public static double speedDifference(double acceleration, double deltaT) {
        return acceleration * deltaT;
    }

    /**
     * Calculate the position difference with speed and time difference
     *
     * @param speed  speed [m/s]
     * @param deltaT time difference [s]
     * @return position difference [m]
     */
    public static double positionDifference(double speed, double deltaT) {
        return speed * deltaT;
    }

    /**
     * Front distance [m] between this vehicle and its front vehicle
     * <p>
     * Note: length is calculated between vehicles extremities
     *
     * @return front distance
     */
    public double frontDistance() {
        if (frontVehicle == null) {
            return Double.POSITIVE_INFINITY;
        }

        // Distance between two vehicles is the absolute value of the position difference
        double posDistance = Math.abs(this.getPosition() - frontVehicle.getPosition());

        // We subtract from this distance, the distance from the vehicles center and vehicles extremities
        return posDistance - (this.getLength() / 2 + frontVehicle.getLength() / 2);
    }

    /**
     * Relative speed of this vehicle compared to front Vehicle
     * <p>
     * Note : if there is no front vehicle, relative speed is equal to 0
     *
     * @return the relative speed
     */
    public double relSpeed() {
        return (frontVehicle != null) ? speed - frontVehicle.getSpeed() : 0;
    }

    /**
     * Change speed [m/s] of the vehicle according to its acceleration and a time difference
     *
     * @param deltaT time difference [s]
     */
    void updateSpeed(double deltaT) {
        setSpeed(getSpeed() + speedDifference(getVehicleController().acceleration(this), deltaT));
    }

    /**
     * Change position [m] of this vehicle according to its acceleration, speed and a time difference
     *
     * @param deltaT the time difference [s]
     */
    void updatePosition(double deltaT) {
        setPosition(getPosition() + positionDifference(getSpeed(), deltaT));
    }

    /**
     * Update vehicle speed and position
     *
     * @param deltaT the time difference [s]
     */
    public void update(double deltaT) {
        // First update speed according to the vehicle acceleration
        updateSpeed(deltaT);
        // Then update position, taking into account the new speed
        updatePosition(deltaT);
    }

    /**
     * Method that calls the renderer in order to draw the Vehicle on the simulation pane
     */
    @Override
    public void draw() {
        VehicleRenderer.getInstance().display(SimulationWindow.getInstance().getSimulationPane(), this);
    }

    /**
     * Debug toString
     *
     * @return A debug representation of a Vehicle
     */
    @Override
    public String toString() {
        String ret = "";
        ret += "Pos: " + position;
        ret += " a: " + ((vehicleController != null) ? vehicleController.acceleration(this) : "");
        ret += " v: " + speed;
        ret += " frontDistance: " + frontDistance();

        return ret;
    }

    /**
     * Get the current path of the vehicle
     * @return the current path
     */
    public ItineraryPath currentPath() {
        return this.itinerary.get(pathStep);
    }

    /**
     * Add itinerary path to the itinerary
     *
     * Note: does not add it if null
     *
     * @param itineraryPath the itinerary path
     */
    public void addToItinerary(ItineraryPath itineraryPath) {
        if(itineraryPath != null) {
            this.itinerary.add(itineraryPath);
        }
    }
    /**
     * Get the itinerary size of the vehicle
     * @return the itinerary size
     */
    public int itinerarySize() {
        return this.itinerary.size();
    }

    /**
     * Move vehicle to the next path of its itinerary
     *
     * Note: if exceed max path step, path step does not change
     */
    public void nextPath() {
        if((pathStep + 1) < this.itinerarySize()) {
            this.pathStep++;
        }
    }
}
