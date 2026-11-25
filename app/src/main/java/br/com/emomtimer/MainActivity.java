package br.com.emomtimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.text.InputType;
import android.content.SharedPreferences;

public class MainActivity extends Activity {
    private static final int DARK=0xff101515, GREEN=0xff254c39, RED=0xffb72d39, LIME=0xffc6f65e, WHITE=Color.WHITE;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private TimerEngine engine;
    private ToneGenerator tone;
    private LinearLayout root;
    private TextView clock, label, rounds, total, pause;
    private ProgressBar progress;
    private boolean sound=true, vibration=true, halfway=true, inWorkout=false;
    private int lastEvent=-1;
    private final Runnable tick=new Runnable(){public void run(){draw(true);if(engine!=null && engine.state()==TimerEngine.State.RUNNING && inWorkout)handler.postDelayed(this,50);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("settings",MODE_PRIVATE);setVolumeControlStream(AudioManager.STREAM_MUSIC);try{tone=new ToneGenerator(AudioManager.STREAM_MUSIC,90);}catch(RuntimeException ignored){}showSettings();}
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void add(LinearLayout l,View v,int height){l.addView(v,new LinearLayout.LayoutParams(-1,height<0?height:dp(height)));}
    private void space(LinearLayout l,int h){add(l,new View(this),h);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView button(String title,int color,int ink,Runnable action){TextView v=text(title,16,ink,true);v.setBackground(bg(color,14));v.setContentDescription(title);v.setClickable(true);v.setFocusable(true);v.setOnClickListener(w->action.run());return v;}
    private void immersive(boolean on){getWindow().getDecorView().setSystemUiVisibility(on?View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE:0);}
    private EditText field(LinearLayout l,String title,int value){TextView caption=text(title,14,0xffb8c4bd,false);caption.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);add(l,caption,28);EditText e=new EditText(this);e.setText(String.valueOf(value));e.setTextColor(WHITE);e.setTextSize(21);e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER);e.setPadding(dp(14),0,dp(14),0);e.setBackground(bg(0xff26302d,12));add(l,e,52);space(l,10);return e;}
    private int number(EditText e,int min,int max){try{int n=Integer.parseInt(e.getText().toString());if(n>=min&&n<=max)return n;}catch(Exception ignored){}e.setError("Use um valor entre "+min+" e "+max);return -1;}
    private Switch toggle(LinearLayout l,String s,boolean on){Switch v=new Switch(this);v.setText(s);v.setTextColor(WHITE);v.setTextSize(15);v.setChecked(on);add(l,v,48);return v;}
    private void showSettings(){handler.removeCallbacks(tick);inWorkout=false;getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);immersive(false);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(DARK);LinearLayout l=column();l.setPadding(dp(24),dp(24),dp(24),dp(24));scroll.addView(l);setContentView(scroll);l.setFocusableInTouchMode(true);l.requestFocus();TextView heading=text("EMOM TIMER",28,LIME,true);heading.setGravity(Gravity.START);add(l,heading,52);TextView desc=text("Configure. Inicie. Treine.",15,0xffb8c4bd,false);desc.setGravity(Gravity.START);add(l,desc,30);space(l,18);
        EditText minutes=field(l,"Duração total (minutos)",prefs.getInt("minutes",20));EditText seconds=field(l,"Duração do round (segundos)",prefs.getInt("seconds",60));TextView restLabel=text("Descanso extra",14,0xffb8c4bd,false);restLabel.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);add(l,restLabel,28);Spinner rests=new Spinner(this);String[] opts={"Sem descanso extra","1 treino + 1 descanso","2 treinos + 1 descanso","3 treinos + 1 descanso","4 treinos + 1 descanso","5 treinos + 1 descanso"};ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,opts);rests.setAdapter(adapter);rests.setSelection(prefs.getInt("rest",0));add(l,rests,50);Switch snd=toggle(l,"Alertas sonoros",prefs.getBoolean("sound",true));Switch vib=toggle(l,"Vibração",prefs.getBoolean("vibration",true));Switch half=toggle(l,"Aviso na metade do round",prefs.getBoolean("halfway",true));space(l,16);
        add(l,button("INICIAR TREINO",LIME,DARK,()->{int m=number(minutes,1,180),s=number(seconds,10,300);if(m<0||s<0)return;int r=rests.getSelectedItemPosition();sound=snd.isChecked();vibration=vib.isChecked();halfway=half.isChecked();prefs.edit().putInt("minutes",m).putInt("seconds",s).putInt("rest",r).putBoolean("sound",sound).putBoolean("vibration",vibration).putBoolean("halfway",halfway).apply();engine=new TimerEngine(m,s,r);lastEvent=-1;showWorkout();engine.start(SystemClock.elapsedRealtime());handler.post(tick);}),58);space(l,12);add(l,button("Testar alerta",0xff26302d,WHITE,()->{sound=snd.isChecked();vibration=vib.isChecked();signal(1);}),48);space(l,15);TextView hint=text("A metade é um aviso, não um descanso obrigatório. Pare se sentir mal-estar.",13,0xffb8c4bd,false);add(l,hint,-2);
    }
    private void showWorkout(){inWorkout=true;root=column();root.setPadding(dp(20),dp(38),dp(20),dp(32));root.setGravity(Gravity.CENTER_HORIZONTAL);root.setBackgroundColor(GREEN);setContentView(root);immersive(true);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        rounds=text("ROUND 01 / 20",19,WHITE,true);add(root,rounds,48);total=text("20:00 restantes",15,WHITE,false);add(root,total,32);LinearLayout center=column();center.setGravity(Gravity.CENTER);root.addView(center,new LinearLayout.LayoutParams(-1,0,1));clock=text("60",100,WHITE,true);clock.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));clock.setSingleLine(true);if(Build.VERSION.SDK_INT>=26)clock.setAutoSizeTextTypeUniformWithConfiguration(40,160,1,android.util.TypedValue.COMPLEX_UNIT_SP);add(center,clock,180);label=text("TREINO",24,WHITE,true);add(center,label,54);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgressTintList(android.content.res.ColorStateList.valueOf(WHITE));add(root,progress,8);space(root,20);pause=button("PAUSAR",WHITE,DARK,this::togglePause);add(root,pause,58);space(root,12);add(root,button("ENCERRAR",0x33222222,WHITE,this::confirmStop),52);
    }
    private void draw(boolean alerts){if(engine==null||!inWorkout)return;long now=SystemClock.elapsedRealtime();TimerEngine.State before=engine.state();engine.update(now);TimerEngine.State state=engine.state();boolean done=state==TimerEngine.State.DONE, paused=state==TimerEngine.State.PAUSED;boolean rest=engine.rest(now),half=engine.half(now);int event=engine.event(now);if(alerts&&done&&before!=TimerEngine.State.DONE)signal(3);else if(alerts&&state==TimerEngine.State.RUNNING&&event!=lastEvent){lastEvent=event;if(!half)signal(rest?2:0);else if(halfway)signal(1);}
        int color=done||paused?DARK:rest||half&&halfway?RED:GREEN;root.setBackgroundColor(color);clock.setText(String.valueOf((engine.remaining(now)+999)/1000));label.setText(done?"CONCLUÍDO":paused?"PAUSADO":rest?"DESCANSO":half&&halfway?"SEGUNDA METADE":"TREINO");rounds.setText(String.format(java.util.Locale.ROOT,"ROUND %02d / %02d",engine.round(now)+1,engine.count()));total.setText(TimerEngine.format(engine.totalMs-engine.elapsed(now))+" restantes");progress.setProgress((int)(1000*engine.elapsed(now)/engine.totalMs));pause.setText(done?"VOLTAR":paused?"CONTINUAR":"PAUSAR");if(done||paused)getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void signal(int type){if(sound&&tone!=null){int t=type==1?ToneGenerator.TONE_PROP_BEEP:type==2?ToneGenerator.TONE_PROP_NACK:type==3?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_BEEP2;tone.startTone(t,type==1?180:300);}if(vibration){try{Vibrator v=(Vibrator)getSystemService(VIBRATOR_SERVICE);if(v!=null&&v.hasVibrator()){long ms=type==1?150:280;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));else v.vibrate(ms);}}catch(Exception ignored){}}}
    private void togglePause(){if(engine.state()==TimerEngine.State.DONE){showSettings();return;}if(engine.state()==TimerEngine.State.RUNNING){pauseWorkout();}else if(engine.state()==TimerEngine.State.PAUSED){engine.start(SystemClock.elapsedRealtime());getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);handler.removeCallbacks(tick);handler.post(tick);}}
    private void pauseWorkout(){if(engine!=null&&engine.state()==TimerEngine.State.RUNNING){engine.pause(SystemClock.elapsedRealtime());handler.removeCallbacks(tick);draw(false);}}
    private void confirmStop(){if(engine.state()==TimerEngine.State.DONE){showSettings();return;}pauseWorkout();new AlertDialog.Builder(this).setTitle("Encerrar treino?").setMessage("O cronômetro está pausado. Você pode continuar ou voltar às configurações.").setNegativeButton("Cancelar",null).setPositiveButton("Encerrar",(d,w)->showSettings()).show();}
    @Override public void onBackPressed(){if(inWorkout)confirmStop();else super.onBackPressed();}
    @Override protected void onPause(){pauseWorkout();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus&&inWorkout)immersive(true);}
    @Override protected void onDestroy(){handler.removeCallbacks(tick);if(tone!=null)tone.release();super.onDestroy();}
}
