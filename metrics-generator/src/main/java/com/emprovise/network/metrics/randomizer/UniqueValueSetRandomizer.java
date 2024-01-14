package com.emprovise.network.metrics.randomizer;

import org.jeasy.random.randomizers.AbstractRandomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class UniqueValueSetRandomizer<T> extends AbstractRandomizer<T> {

    private List<T> values;
    private List<T> iterValues;

    public UniqueValueSetRandomizer(Set<T> values) {
        if(values != null) {
            List<T> list = new ArrayList<>(values);
            this.values = Collections.unmodifiableList(list);
            this.iterValues = list;
        }
    }
    public UniqueValueSetRandomizer(T... values) {
        if(values != null) {
            List<T> list = Arrays.asList(values);
            this.values = Collections.unmodifiableList(list);
            this.iterValues = new ArrayList<>(list);
        }
    }

    @Override
    public T getRandomValue() {

        if(iterValues.isEmpty()) {
            iterValues = new ArrayList<>(values);
        }

        int index = random.nextInt(iterValues.size());
        T value = iterValues.get(index);
        iterValues.remove(index);
        return value;
    }
}