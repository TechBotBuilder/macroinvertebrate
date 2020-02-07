package com.techbotbuilder.streamteamohio.forms;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

class FormData {

    private Map<String, String> data;
    private Map<String, CalculationDefinition> calculations;
    private final static String CALCULATEDFIELD;

    static {
        CALCULATEDFIELD = "CALCULATED_FIELD";
    }

    FormData() {
        data = null;
        calculations = null;
    }

    public String get(String name) {
        return _get(name, 0);
    }

    /*
     * Check if the data item under /name/ is a calculated field by comparing String reference.
     * If so, look up the calculation and <get> required data items.
     * Note the recurrence call. It is not this class's responsibility
     * to handle cyclic dependencies causing infinite loops.
     * To avoid hard-crashing, an early exception will be thrown.
     * Check your form-defining XML !calculated definitions carefully.
     */
    private String _get(String name, int depth) {

        String value = data.get(name);
        if(value == null) return "";
        if (value == CALCULATEDFIELD) {

            if (depth > InfiniteRecursionException.maxRecursionDepth) throw new InfiniteRecursionException();

            CalculationDefinition calculationDefinition = calculations.get(name);
            List<String> summands = calculationDefinition.variables;
            Calculation calculation = calculationDefinition.calculation;
            if (summands == null) return "0";

            //@TODO Improve determination of which/how many variables to average over
            float result = 0;
            int nonzero = 0;
            for (String summandName : summands) {
                float summandValue = Float.parseFloat(_get(summandName, depth + 1));
                nonzero += (summandValue==0) ? 0 : 1;
                result += summandValue;
            }
            if(Calculation.AVERAGE == calculation && nonzero!=0) result /= nonzero;
            value = Float.toString(result);
        }
        return value;
    }

    boolean set(@NonNull String name, String value) {
        boolean hadThisKey = data.containsKey(name);
        data.put(name, value);
        return hadThisKey;
    }

    public void set(String name, int value) {
        set(name, Integer.toString(value));
    }

    public void set(String name, float value) {
        set(name, Float.toString(value));
    }
    public void setCalculation(String name, String calculation, List<String> data){
        calculations.put(name, new CalculationDefinition(Calculation.valueOf(calculation), data));
    }

    public void setOn(String name){

    }
    public void setOff(String name){

    }


    private static class InfiniteRecursionException extends RuntimeException{
        static int maxRecursionDepth = 100;
        InfiniteRecursionException(){
            super();
        }
    }
    private enum Calculation{
        SUM,
        AVERAGE
    }
    private static class CalculationDefinition {
        Calculation calculation;
        List<String> variables;
        CalculationDefinition(Calculation c, List<String> s){
            this.calculation = c;
            this.variables = s;
        }
    }

    //@TODO Need this?
    private static class Data {
        private String data;
        private boolean on = false;
        Data(Object value){
            data = value.toString();
        }
        private float getFloat(){
            return (on && data != null) ? Float.parseFloat(data) : 0;
        }
        private String getString(){
            return (on && data != null) ? data : "";
        }
    }

}
