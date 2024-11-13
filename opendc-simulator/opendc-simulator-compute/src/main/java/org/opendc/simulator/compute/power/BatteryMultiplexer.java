package org.opendc.simulator.compute.power;

import org.opendc.simulator.Multiplexer;
import org.opendc.simulator.engine.FlowConsumer;
import org.opendc.simulator.engine.FlowEdge;
import org.opendc.simulator.engine.FlowGraph;
import org.opendc.simulator.engine.FlowNode;
import org.opendc.simulator.engine.FlowSupplier;

import java.util.HashMap;
import java.util.LinkedHashMap;

//TODO: Multiplex between Battery and PowerSource based on CarbonPolicy
public class BatteryMultiplexer extends FlowNode implements FlowSupplier, FlowConsumer {
    private long lastUpdate;

    private FlowEdge simPowerSupplierEdge;
    private FlowEdge simBatterySupplierEdge;
    private FlowEdge multiplexerConsumerEdge;
    private FlowEdge simBatteryConsumerEdge;

    private HashMap<FlowEdge, Double> demands = new LinkedHashMap<>();
    private HashMap<FlowEdge, Double> supplies = new LinkedHashMap<>();


    private PowerAdapter powerAdapter;
    private SimBattery simBattery;

    private double powerDemand = 0.0f;
    private double powerSupplied = 0.0f;
    private double totalEnergyUsage = 0.0f;


    CarbonPolicy carbonPolicy = new SimpleCarbonPolicy();

    public BatteryMultiplexer(FlowGraph graph, PowerAdapter powerAdapter, SimBattery simBattery) {
        super(graph);
        this.powerAdapter = powerAdapter;
        this.simBattery = simBattery;

        lastUpdate = this.clock.millis();
    }

    /*
    public long onUpdate(long now) {
        double powerDemand = powerAdapter.getPowerDemand();
        double carbonIntensity = powerAdapter.getCarbonIntensity();

        if (carbonPolicy.greenEnergyAvailable(carbonIntensity, now)) {
            //charge battery
        } else {
            //TODO: Connect the battery to the same multiplexer as PowerAdapter

            //Note, the edge is not added to PowerAdapter on initialization

           //simBattery.supplyPower(powerDemand);
        }

        return Long.MAX_VALUE;
    }
    */



    public long onUpdate(long now) {
        updateCounters();
        double powerSupply = this.powerDemand;

        if (powerSupply != this.powerSupplied) {
            this.pushSupply(this.multiplexerConsumerEdge, powerSupply);
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
            double energyUsage = (this.powerSupplied * duration * 0.001);

            // Compute the energy usage of the machine
            this.totalEnergyUsage += energyUsage;
        }
    }


    @Override
    public void pushDemand(FlowEdge supplierEdge, double newDemand) {
        supplierEdge.pushDemand(newDemand);
    }

    @Override
    public void pushSupply(FlowEdge consumerEdge, double newSupply) {
        this.powerSupplied = newSupply;
        consumerEdge.pushSupply(newSupply);
    }

    //handleDemand of Multiplexer (+ Battery down the line)
    @Override
    public void handleDemand(FlowEdge consumerEdge, double newDemand) {
        System.out.println(consumerEdge.getConsumer().getClass() + " " + newDemand);
        this.powerDemand = newDemand;

        this.invalidate();
    }

    @Override
    public void handleSupply(FlowEdge supplierEdge, double newSupply) {

    }

    @Override
    public void addConsumerEdge(FlowEdge consumerEdge) {
        FlowConsumer consumer = consumerEdge.getConsumer();
        if (consumer.getClass().equals(Multiplexer.class)) {
            this.multiplexerConsumerEdge = consumerEdge;
        }
        if (consumer.getClass().equals(SimBattery.class)) {
            this.simBatteryConsumerEdge = consumerEdge;
        }
        this.demands.put(consumerEdge, 0.0);
        this.invalidate();
    }

    /**
     * Additionally sets powerAdapter and simBattery
     * @param supplierEdge
     */
    @Override
    public void addSupplierEdge(FlowEdge supplierEdge) {
        FlowSupplier supplier = supplierEdge.getSupplier();
        if (supplier.getClass().equals(PowerAdapter.class)) {
            this.simPowerSupplierEdge = supplierEdge;
        }
        if (supplier.getClass().equals(SimBattery.class)) {
            this.simBatterySupplierEdge = supplierEdge;
        }
        this.supplies.put(supplierEdge, 0.0);
        this.invalidate();
    }

    @Override
    public void removeSupplierEdge(FlowEdge supplierEdge) {
        //
    }

    @Override
    public void removeConsumerEdge(FlowEdge consumerEdge) {
        //
    }

    @Override
    public double getCapacity() {
        return 0;
    }
}
