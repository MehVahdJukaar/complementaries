package net.mehvahdjukaar.complementaries.common.worldgen;

public interface BeardifierWithSaltProcessor {

    void addSaltPostProcessor(SaltPostProcessor saltBeardifier);

    SaltPostProcessor getSaltPostProcessor();

}
