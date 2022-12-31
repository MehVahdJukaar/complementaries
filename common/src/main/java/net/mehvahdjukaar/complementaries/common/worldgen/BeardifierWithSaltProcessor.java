package net.mehvahdjukaar.complementaries.common.worldgen;

public interface BeardifierWithSaltProcessor {

    void addSaltPostProcessor(SaltBeardifier saltBeardifier);

    SaltBeardifier getSaltPostProcessor();
}
