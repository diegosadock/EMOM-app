import br.com.emomtimer.TimerEngine;
public class TimerEngineTest {
  static void check(boolean ok){if(!ok)throw new AssertionError();}
  public static void main(String[] args){
    TimerEngine t=new TimerEngine(20,60,1);t.start(1000);
    check(t.remaining(1000)==60000 && t.round(1000)==0);
    check(!t.half(30999) && t.half(31000));
    check(t.round(61000)==1 && t.rest(61000) && t.remaining(61000)==60000);
    check(t.round(121000)==2 && !t.rest(121000));
    t.pause(122000);check(t.elapsed(999000)==121000);t.start(999000);check(t.elapsed(1000000)==122000);
    t.update(999000+1200000);check(t.state()==TimerEngine.State.DONE && t.remaining(9999999)==0);
    TimerEngine x=new TimerEngine(1,40,0);x.start(0);check(x.count()==2 && x.remaining(40000)==20000);x.update(60000);check(x.remaining(60000)==0);
    check(TimerEngine.format(59999).equals("01:00"));
    System.out.println("TimerEngine: all assertions passed");
  }
}
