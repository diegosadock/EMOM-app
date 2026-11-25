package br.com.emomtimer;

/** Monotonic-clock timer, independent of Android UI. */
public final class TimerEngine {
    public enum State { READY, RUNNING, PAUSED, DONE }
    public final long totalMs, roundMs;
    public final int restEvery;
    private long accumulated, anchor;
    private State state = State.READY;
    public TimerEngine(int minutes, int seconds, int restEvery) {
        if(minutes<1 || minutes>180 || seconds<10 || seconds>300 || restEvery<0 || restEvery>5) throw new IllegalArgumentException();
        this.totalMs=minutes*60_000L; this.roundMs=seconds*1000L; this.restEvery=restEvery;
    }
    public State state(){return state;}
    public long elapsed(long now){return Math.min(totalMs, accumulated+(state==State.RUNNING?Math.max(0,now-anchor):0));}
    public void start(long now){if(state==State.READY || state==State.PAUSED){anchor=now;state=State.RUNNING;}}
    public void pause(long now){if(state==State.RUNNING){accumulated=elapsed(now);state=accumulated>=totalMs?State.DONE:State.PAUSED;}}
    public void update(long now){if(state==State.RUNNING && elapsed(now)>=totalMs){accumulated=totalMs;state=State.DONE;}}
    public int count(){return (int)((totalMs+roundMs-1)/roundMs);}
    public int round(long now){return Math.min(count()-1,(int)(elapsed(now)/roundMs));}
    public long position(long now){return elapsed(now)-round(now)*roundMs;}
    public long remaining(long now){return Math.min(roundMs,totalMs-round(now)*roundMs)-position(now);}
    public boolean rest(long now){return restEvery>0 && round(now)%(restEvery+1)==restEvery;}
    public boolean half(long now){return position(now)>=roundMs/2;}
    public int event(long now){return round(now)*2+(half(now)?1:0);}
    public static String format(long ms){long s=(Math.max(0,ms)+999)/1000;return String.format(java.util.Locale.ROOT,"%02d:%02d",s/60,s%60);}
}
