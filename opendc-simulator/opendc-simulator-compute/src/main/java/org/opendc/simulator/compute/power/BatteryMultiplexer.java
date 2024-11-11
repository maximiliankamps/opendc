package org.opendc.simulator.compute.power;

import org.opendc.simulator.engine.FlowEdge;

//TODO: Implement switching of the cpuEdge between Battery/PowerSource based on carbon emission
public class BatteryMultiplexer {
    SimBattery simBattery;
    SimPowerSource simPowerSource;

    private FlowEdge cpuEdge;
}
