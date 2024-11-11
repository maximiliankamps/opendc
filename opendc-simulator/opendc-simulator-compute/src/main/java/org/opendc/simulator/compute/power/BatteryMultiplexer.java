package org.opendc.simulator.compute.power;

import org.opendc.simulator.engine.FlowConsumer;
import org.opendc.simulator.engine.FlowEdge;
import org.opendc.simulator.engine.FlowGraph;

//TODO: Multiplex between Battery and PowerSource based on CarbonPolicy
public class BatteryMultiplexer {
    FlowGraph graph;
    private SimPowerSource simPowerSource;
    private SimBattery simBattery;
    CarbonPolicy carbonPolicy = new SimpleCarbonPolicy();

    public BatteryMultiplexer(SimPowerSource simPowerSource) {
        this.simPowerSource = simPowerSource;
        graph = simPowerSource.getGraph();
        //simBattery = new SimBattery(graph, Long.MAX_VALUE); This makes the program freeze for some reason
    }

    public long onUpdate(long now) {
        double powerDemand = simPowerSource.getPowerDemand();
        double carbonIntensity = simPowerSource.getCarbonIntensity();

        if (carbonPolicy.greenEnergyAvailable(carbonIntensity, now)) {
            simPowerSource.supplyPower(powerDemand);
            //charge battery
        } else {
            //discharge battery
        }

        return Long.MAX_VALUE;
    }
}
