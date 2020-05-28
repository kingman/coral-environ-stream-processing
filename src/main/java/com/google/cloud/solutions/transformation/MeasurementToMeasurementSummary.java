package com.google.cloud.solutions.transformation;

import com.google.cloud.solutions.common.Measurement;
import com.google.cloud.solutions.common.MeasurementSummary;
import com.google.cloud.solutions.common.MeasurementSummaryAccumulator;

import org.apache.beam.sdk.transforms.Combine.CombineFn;

public class MeasurementToMeasurementSummary
        extends CombineFn<Measurement, MeasurementSummaryAccumulator, MeasurementSummary> {

    private static final long serialVersionUID = -6143657788075488407L;

    @Override
    public MeasurementSummaryAccumulator createAccumulator() {
        return new MeasurementSummaryAccumulator();
    }

    @Override
    public MeasurementSummaryAccumulator addInput(MeasurementSummaryAccumulator accum, Measurement input) {
        if (accum.getMin() > input.getValue()) {
            accum.setMin(input.getValue());
        }
        if (accum.getMax() < input.getValue()) {
            accum.setMax(input.getValue());
        }
        if (accum.getStart().isAfter(input.getTimestamp().getMillis())) {
            accum.setStart(input.getTimestamp());
            accum.setStartStr(input.getTimestampStr());
        }
        if (accum.getEnd().isBefore(input.getTimestamp().getMillis())) {
            accum.setEnd(input.getTimestamp());
            accum.setEndStr(input.getTimestampStr());
        }
        accum.setTotal(accum.getTotal() + input.getValue());
        accum.setCount(accum.getCount() + 1);
        return accum;
    }

    @Override
    public MeasurementSummaryAccumulator mergeAccumulators(Iterable<MeasurementSummaryAccumulator> accums) {
        MeasurementSummaryAccumulator merged = createAccumulator();
        for (MeasurementSummaryAccumulator accum : accums) {
            if (merged.getMin() > accum.getMin()) {
                merged.setMin(accum.getMin());
            }
            if (merged.getMax() < accum.getMax()) {
                merged.setMax(accum.getMax());
            }
            if (merged.getStart().isAfter(accum.getStart().getMillis())) {
                merged.setStart(accum.getStart());
                merged.setStartStr(accum.getStartStr());
            }
            if (merged.getEnd().isBefore(accum.getEnd().getMillis())) {
                merged.setEnd(accum.getEnd());
                merged.setEndStr(accum.getEndStr());
            }
            merged.setTotal(merged.getTotal() + accum.getTotal());
            merged.setCount(merged.getCount() + accum.getCount());
        }
        return merged;
    }

    @Override
    public MeasurementSummary extractOutput(MeasurementSummaryAccumulator accum) {
        MeasurementSummary summary = new MeasurementSummary();
        summary.setMin(accum.getMin());
        summary.setMax(accum.getMax());
        summary.setStart(accum.getStartStr());
        summary.setEnd(accum.getEndStr());
        summary.setAverage(accum.getTotal() / accum.getCount());
        return summary;
    }

}