import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Counter {
    //singleton
    private static Counter instance;
    private static long SECOND_TO_MILLISECOND = 1000L;
    private static long BASE = 60L;
    private static long MINUTE_TO_MILLISECOND = SECOND_TO_MILLISECOND * BASE;
    private static long HOUR_TO_MILLISECOND = MINUTE_TO_MILLISECOND * BASE;
    private static long DAY_TO_MILLISECOND = MINUTE_TO_MILLISECOND * BASE * 24;
    private List<Long> evalCalls;
    private List<Long> timeCalls;
    private List<String> evalExpressions;

    private Counter() {
        this.evalCalls = new ArrayList<>();
        this.timeCalls = new ArrayList<>();
        this.evalExpressions = new ArrayList<>();
    }

    public static Counter createCounter(){
        if (instance == null) {
            instance = new Counter();
        }
        return instance;
    }

    public List<Long> getEvalCalls() {
        return evalCalls;
    }

    public void addEvalCalls(long timestamp) {
        this.evalCalls.add(timestamp);
    }

    public List<Long> getTimeCalls() {
        return timeCalls;
    }

    public void addTimeCalls(long timestamp) {
        this.timeCalls.add(timestamp);
    }

    public List<String> getEvalExpressions() {
        return evalExpressions;
    }

    public void addEvalExpressions(String newExpression) {
        this.evalExpressions.add(newExpression);
    }

    public List<String> getLastTenExpressions(){
        int n = this.evalExpressions.size();
        return n > 10 ? this.evalExpressions.subList(n-10, n) : this.evalExpressions;
    }

    public int getCallsCountLastMinute(List<Long> calls, long currentTime){
        long lastMinute = currentTime - MINUTE_TO_MILLISECOND;
        return binarySearchLastOccurrence(calls, currentTime) - binarySearchFirstOccurrence(calls, lastMinute);
    }

    public int getCallsCountLastHour(List<Long> calls, long currentTime){
        long lastHour = currentTime - HOUR_TO_MILLISECOND;
        return binarySearchLastOccurrence(calls, currentTime) - binarySearchFirstOccurrence(calls, lastHour);
    }

    public int getCallsCountLastDay(List<Long> calls, long currentTime){
        long lastDay = currentTime - DAY_TO_MILLISECOND;
        return binarySearchLastOccurrence(calls, currentTime) - binarySearchFirstOccurrence(calls, lastDay);
    }

    public int getCallsCountTotal(List<Long> calls){
        return calls.size();
    }

    private int binarySearchFirstOccurrence(List<Long> calls, long key){
        int l = 0, r = calls.size();
        while(l < r){
            int m = l + (r - l) / 2;
            long midVal = calls.get(m);
            if (midVal < key){
                l = m+1;
            } else {
                r = m;
            }
        }
        return l;
    }

    private int binarySearchLastOccurrence(List<Long> calls, long key){
        int l = 0, r = calls.size();
        while(l < r){
            int m = l + (r - l) / 2;
            long midVal = calls.get(m);
            if (midVal <= key){
                l = m+1;
            } else {
                r = m;
            }
        }
        return l;
    }

}
