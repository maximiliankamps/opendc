package org.opendc.simulator.compute.power;

public interface CarbonPolicy {
    public boolean greenEnergyAvailable(double carbonIntensity, long now);
}
