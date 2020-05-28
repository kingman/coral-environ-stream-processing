package com.google.cloud.solutions.transformation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.Measurement;

import org.apache.beam.sdk.transforms.Combine.CombineFn;

public class MeasurementToDeviceInfoMap
        extends CombineFn<Measurement, MeasurementToDeviceInfoMap.Accum, Map<String, DeviceInfo>> {
    private static final long serialVersionUID = -5881427161558611369L;

    static class Accum implements Serializable {
        private static final long serialVersionUID = 8699910053875323132L;
        Map<String, DeviceInfo> map;

        Accum() {
            this.map = new HashMap<>();
        }

    }

    @Override
    public Accum createAccumulator() {
        return new Accum();
    }

    @Override
    public Accum addInput(Accum accum, Measurement input) {
        if (!accum.map.containsKey(input.getDeviceInfo().getDeviceNumId())) {
            accum.map.put(input.getDeviceInfo().getDeviceNumId(), input.getDeviceInfo());
        }
        return accum;
    }

    @Override
    public Accum mergeAccumulators(Iterable<Accum> accums) {
        Accum merged = createAccumulator();
        for (Accum accum : accums) {
            for (String deviceNumId : accum.map.keySet()) {
                if (!merged.map.containsKey(deviceNumId)) {
                    merged.map.put(deviceNumId, accum.map.get(deviceNumId));
                }
            }
        }
        return merged;
    }

    @Override
    public Map<String, DeviceInfo> extractOutput(Accum accum) {
        return accum.map;
    }
}