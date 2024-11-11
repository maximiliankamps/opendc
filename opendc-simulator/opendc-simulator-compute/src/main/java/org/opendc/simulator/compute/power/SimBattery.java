package org.opendc.simulator.compute.power;

import org.opendc.simulator.compute.cpu.SimCpu;
import org.opendc.simulator.engine.FlowConsumer;
import org.opendc.simulator.engine.FlowEdge;
import org.opendc.simulator.engine.FlowGraph;
import org.opendc.simulator.engine.FlowNode;
import org.opendc.simulator.engine.FlowSupplier;

public class SimBattery extends FlowNode implements FlowSupplier, FlowConsumer {
    private long lastUpdate;

    private double powerDemand = 0.0;
    private double powerSupplied = 0.0;
    private double totalEnergyUsage = 0.0;

    private FlowEdge muxEdge;
    private FlowEdge powerSupplyEdge;

    private final double capacity = 1000.0;
    private double charge = 0.0;

    private BatteryState state = BatteryState.IDLE;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Basic Getters and Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Determine whether the InPort is connected to a {@link SimCpu}.
     *
     * @return <code>true</code> if the InPort is connected to an OutPort, <code>false</code> otherwise.
     */
    public boolean isConnected() {
        return muxEdge != null;
    }

    /**
     * Return the power demand of the machine (in W) measured in the PSU.
     * <p>
     * This method provides access to the power consumption of the machine before PSU losses are applied.
     */
    public double getPowerDemand() {
        return this.powerDemand;
    }

    /**
     * Return the instantaneous power usage of the machine (in W) measured at the InPort of the power supply.
     */
    public double getPowerDraw() {
        return this.powerSupplied;
    }

    /**
     * Return the cumulated energy usage of the machine (in J) measured at the InPort of the powers supply.
     */
    public double getEnergyUsage() {
        updateCounters();
        return totalEnergyUsage;
    }

    @Override
    public double getCapacity() {
        return this.capacity;
    }

    public BatteryState getState() {
        return this.state;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public SimBattery(FlowGraph graph) {
        super(graph);
        lastUpdate = this.clock.millis();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FlowNode related functionality
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public long onUpdate(long now) {
        updateCounters();

        if (state == BatteryState.DISCHARGING) {
            double powerSupply = this.powerDemand;

            if (powerSupply != this.powerSupplied) {
                this.pushSupply(this.muxEdge, powerSupply);
            }
        } else if (state == BatteryState.CHARGING) {
            double powerDemand = this.capacity - this.charge; //TODO: Change assumption that battery can be charged instantaneously

            if (powerDemand != this.powerDemand) {
                this.pushDemand(this.powerSupplyEdge, powerDemand);
            }
        } else if (state == BatteryState.IDLE) {
            // do nothing
        }

        return Long.MAX_VALUE;
    }

    public void updateCounters() {
        updateCounters(clock.millis());
    }

    /**
     * Calculate the energy usage up until <code>now</code>.
     */
    public void updateCounters(long now) {
        long lastUpdate = this.lastUpdate;
        this.lastUpdate = now;

        long duration = now - lastUpdate;
        if (duration > 0) {
            // Compute the energy usage of the psu
            this.totalEnergyUsage += (this.powerSupplied * duration * 0.001);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FlowGraph Related functionality
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Charge Battery
    @Override
    public void pushSupply(FlowEdge consumerEdge, double newSupply) {
        this.powerSupplied = newSupply;
        muxEdge.pushSupply(newSupply);
    }

    @Override
    public void handleDemand(FlowEdge consumerEdge, double newPowerDemand) {

        updateCounters();
        this.powerDemand = newPowerDemand;

        pushDemand(this.powerSupplyEdge, newPowerDemand);
    }

    // Discharge Battery
    @Override
    public void handleSupply(FlowEdge supplierEdge, double newPowerSupply) {

        updateCounters();
        this.powerSupplied = newPowerSupply;

        pushSupply(this.muxEdge, newPowerSupply);
    }

    @Override
    public void pushDemand(FlowEdge supplierEdge, double newDemand) {
        this.powerDemand = newDemand;
        powerSupplyEdge.pushDemand(newDemand);
    }

    @Override
    public void addConsumerEdge(FlowEdge consumerEdge) {
        this.muxEdge = consumerEdge;
    }

    @Override
    public void addSupplierEdge(FlowEdge supplierEdge) {
        this.powerSupplyEdge = supplierEdge;
    }

    @Override
    public void removeConsumerEdge(FlowEdge consumerEdge) {
        this.muxEdge = null;
    }

    @Override
    public void removeSupplierEdge(FlowEdge supplierEdge) {
        this.powerSupplyEdge = null;
    }
}
