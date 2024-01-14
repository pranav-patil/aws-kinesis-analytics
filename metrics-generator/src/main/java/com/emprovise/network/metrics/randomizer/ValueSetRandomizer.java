package com.emprovise.network.metrics.randomizer;

import org.jeasy.random.randomizers.AbstractRandomizer;

public class ValueSetRandomizer<T> extends AbstractRandomizer<T> {

    private T[] values;

    public ValueSetRandomizer(T... values) {
        this.values = values;
    }

    @Override
    public T getRandomValue() {
        return values[random.nextInt(values.length)];
    }
}