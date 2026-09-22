package com.mrstar.turkiyetv;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    static class Channel {
        int no; String name, group; String[] sources;
        Channel(int no, String name, String group, String... sources) {
            this.no=no; this.name=name; this.group=group; this.sources=sources;
        }
    }

    final Handler handler = new Handler(Looper.getMainLooper());
    final List<Channel> channels = new ArrayList<>();
    final String[] categories = {"Tümü","Ulusal","Haber","Spor","Çocuk","TRT Tematik","Belgesel / Yaşam","Müzik"};

    FrameLayout root, guide;
    VideoView video;
    TextView hud, status, categoryText, guideTitle;
    LinearLayout rows;
    ScrollView scroll;

    int current=0, source=0, guideIndex=0;
    String category="Tümü", digits="";
    boolean guideOpen=false;
    Runnable digitCommit, hideHud;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        immersive();
        buildChannels();
        int last=getPreferences(MODE_PRIVATE).getInt("last",1);
        for(int i=0;i<channels.size();i++) if(channels.get(i).no==last) current=i;
        buildUi();
        play(true);
    }

    void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    void buildChannels() {
        channels.add(new Channel(1,"TRT 1","Ulusal","https://tv-trt1.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(2,"ATV","Ulusal","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv.m3u8"));
        channels.add(new Channel(3,"Kanal D","Ulusal","https://demiroren-live.daioncdn.net/kanald/kanald.m3u8"));
        channels.add(new Channel(4,"Show TV","Ulusal","https://ciner-live.daioncdn.net/showtv/showtv.m3u8"));
        channels.add(new Channel(5,"Star TV","Ulusal","https://dogus-live.daioncdn.net/startv/playlist.m3u8"));
        channels.add(new Channel(6,"NOW","Ulusal","https://uycyyuuzyh.turknet.ercdn.net/nphindgytw/nowtv/nowtv.m3u8"));
        channels.add(new Channel(7,"TV8","Ulusal","https://tv8.daioncdn.net/tv8/tv8.m3u8?app=7ddc255a-ef47-4e81-ab14-c0e5f2949788&ce=3"));
        channels.add(new Channel(8,"Kanal 7","Ulusal","https://kanal7-live.daioncdn.net/kanal7/kanal7.m3u8"));
        channels.add(new Channel(9,"a2","Ulusal","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/a2tv/a2tv.m3u8"));
        channels.add(new Channel(10,"360","Ulusal","https://turkmedya-live.ercdn.net/tv360/tv360_720p.m3u8"));
        channels.add(new Channel(11,"Beyaz TV","Ulusal","https://beyaztv-live.daioncdn.net/beyaztv/beyaztv.m3u8"));

        channels.add(new Channel(12,"TRT 2","TRT Tematik","https://tv-trt2.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(13,"TRT Belgesel","Belgesel / Yaşam","https://tv-trtbelgesel.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(14,"TRT Müzik","Müzik","https://tv-trtmuzik.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(15,"TRT Çocuk","Çocuk","https://tv-trtcocuk.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(16,"TRT Diyanet Çocuk","Çocuk","https://tv-trt-diyanet-cocuk.medya.trt.com.tr/master_720.m3u8"));
        channels.add(new Channel(17,"TRT Türk","TRT Tematik","https://tv-trtturk.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(18,"TRT Avaz","TRT Tematik","https://tv-trtavaz.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(19,"TRT Kurdî","TRT Tematik","https://tv-trtkurdi.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(20,"TRT World","TRT Tematik","https://tv-trtworld.medya.trt.com.tr/master.m3u8"));

        channels.add(new Channel(21,"Minika Çocuk","Çocuk","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk.m3u8"));
        channels.add(new Channel(22,"Minika GO","Çocuk","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago/minikago.m3u8"));

        channels.add(new Channel(23,"TRT Haber","Haber","https://tv-trthaber.medya.trt.com.tr/master.m3u8"));
        channels.add(new Channel(24,"NTV","Haber","https://dogus.daioncdn.net/ntv/ntv.m3u8?app=ntv_web"));
        channels.add(new Channel(25,"CNN Türk","Haber","https://live.duhnet.tv/S2/HLS_LIVE/cnnturknp/playlist.m3u8"));
        channels.add(new Channel(26,"Habertürk","Haber","https://ciner-live.daioncdn.net/haberturktv/haberturktv.m3u8"));
        channels.add(new Channel(27,"Bloomberg HT","Haber","https://ciner-live.daioncdn.net/bloomberght/bloomberght.m3u8"));
        channels.add(new Channel(28,"24 TV","Haber","https://turkmedya-live.ercdn.net/tv24/tv24_720p.m3u8"));
        channels.add(new Channel(29,"TGRT Haber","Haber","https://canli.tgrthaber.com/tgrt.m3u8"));

        channels.add(new Channel(30,"TRT Spor","Spor","https://tv-trt-spor.medya.trt.com.tr/master_720.m3u8"));
        channels.add(new Channel(31,"TRT Spor Yıldız","Spor","https://tv-trt-spor-yildiz.medya.trt.com.tr/master_720.m3u8"));
        channels.add(new Channel(32,"A Spor","Spor","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8"));
        channels.add(new Channel(33,"HT Spor","Spor","https://ciner-live.ercdn.net/htspor/htspor.m3u8"));

        channels.add(new Channel(34,"PowerTürk TV","Müzik","https://livetv.powerapp.com.tr/powerturkTV/powerturkhd.smil/playlist.m3u8"));
        channels.add(new Channel(35,"Number 1 TV","Müzik","https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8"));
    }

    void buildUi() {
        root=new FrameLayout(this); root.setBackgroundColor(Color.BLACK);
        video=new VideoView(this);
        root.addView(video,new FrameLayout.LayoutParams(-1,-1));

        hud=boxText(21,0xDD111722); hud.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(dp(460),-2);
        hp.gravity=Gravity.TOP|Gravity.LEFT; hp.setMargins(dp(28),dp(24),0,0); root.addView(hud,hp);

        status=boxText(14,0xDD111722);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-2,-2);
        sp.gravity=Gravity.TOP|Gravity.RIGHT; sp.setMargins(0,dp(26),dp(28),0); root.addView(status,sp);

        guide=new FrameLayout(this); guide.setBackgroundColor(0xEE05070B); guide.setVisibility(View.GONE);
        root.addView(guide,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout shell=new LinearLayout(this); shell.setOrientation(LinearLayout.HORIZONTAL); shell.setPadding(dp(28),dp(28),dp(28),dp(28));
        guide.addView(shell,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout left=new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL); left.setPadding(dp(16),dp(16),dp(16),dp(16)); left.setBackground(bg(0xEE171D27,18));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(270),-1); lp.setMargins(0,0,dp(18),0); shell.addView(left,lp);

        TextView title=new TextView(this); title.setText("Türkiye TV\nV3 Android TV"); title.setTextColor(Color.WHITE); title.setTextSize(24); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); title.setPadding(dp(8),dp(6),dp(8),dp(18)); left.addView(title);
        categoryText=new TextView(this); categoryText.setTextColor(0xFFE0E5ED); categoryText.setTextSize(18); left.addView(categoryText);

        LinearLayout right=new LinearLayout(this); right.setOrientation(LinearLayout.VERTICAL); right.setPadding(dp(14),dp(14),dp(14),dp(14)); right.setBackground(bg(0xEE171D27,18)); shell.addView(right,new LinearLayout.LayoutParams(0,-1,1f));
        guideTitle=new TextView(this); guideTitle.setTextColor(Color.WHITE); guideTitle.setTextSize(22); guideTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD); guideTitle.setPadding(dp(8),dp(4),dp(8),dp(10)); right.addView(guideTitle);

        scroll=new ScrollView(this); scroll.setVerticalScrollBarEnabled(false); right.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        rows=new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL); scroll.addView(rows,new ScrollView.LayoutParams(-1,-2));

        TextView help=new TextView(this); help.setText("↑ ↓ Kanal   •   ← → Kategori   •   OK Seç   •   Geri Kapat"); help.setTextColor(0xFFB9C1CE); help.setTextSize(14); help.setPadding(dp(8),dp(10),dp(8),dp(4)); right.addView(help);

        setContentView(root);

        video.setOnPreparedListener(mp->{ video.start(); setStatus("CANLI",1800); });
        video.setOnErrorListener((mp,what,extra)->{ tryNext(); return true; });

        hideHud=()->hud.setVisibility(View.GONE);
    }

    TextView boxText(int size,int color) {
        TextView t=new TextView(this); t.setTextColor(Color.WHITE); t.setTextSize(size); t.setPadding(dp(16),dp(10),dp(16),dp(10)); t.setBackground(bg(color,16)); return t;
    }

    GradientDrawable bg(int color,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }

    int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }

    void play(boolean reset) {
        if(reset) source=0;
        Channel c=channels.get(current);
        getPreferences(MODE_PRIVATE).edit().putInt("last",c.no).apply();
        showHud(); setStatus("Bağlanıyor…",0);
        try { video.stopPlayback(); video.setVideoURI(Uri.parse(c.sources[source])); video.start(); }
        catch(Exception e){ tryNext(); }
    }

    void tryNext() {
        Channel c=channels.get(current);
        if(source+1<c.sources.length){ source++; play(false); }
        else setStatus("Yayın şu anda açılamadı",0);
    }

    void switchChannel(int d){ current=(current+d+channels.size())%channels.size(); source=0; play(true); }

    void jump(int no){
        for(int i=0;i<channels.size();i++) if(channels.get(i).no==no){ current=i; source=0; play(true); return; }
        setStatus("Kanal bulunamadı",1500);
    }

    void showHud(){
        Channel c=channels.get(current);
        hud.setText(String.format(Locale.getDefault(),"%02d   %s\n%s   •   CANLI",c.no,c.name,c.group));
        hud.setVisibility(View.VISIBLE); handler.removeCallbacks(hideHud); handler.postDelayed(hideHud,3200);
    }

    void setStatus(String s,long ms){
        status.setText(s); status.setVisibility(View.VISIBLE);
        if(ms>0) handler.postDelayed(()->status.setVisibility(View.GONE),ms);
    }

    List<Channel> filtered(){
        if(category.equals("Tümü")) return new ArrayList<>(channels);
        List<Channel> out=new ArrayList<>(); for(Channel c:channels) if(c.group.equals(category)) out.add(c); return out;
    }

    void openGuide(){ guideOpen=true; guide.setVisibility(View.VISIBLE); guideIndex=0; List<Channel> a=filtered(); for(int i=0;i<a.size();i++) if(a.get(i).no==channels.get(current).no) guideIndex=i; renderGuide(); }
    void closeGuide(){ guideOpen=false; guide.setVisibility(View.GONE); showHud(); }

    void renderGuide(){
        StringBuilder sb=new StringBuilder(); for(String c:categories) sb.append(c.equals(category)?"▶  ":"    ").append(c).append("\n\n"); categoryText.setText(sb);
        List<Channel> a=filtered(); if(a.isEmpty()) return; guideIndex=Math.max(0,Math.min(guideIndex,a.size()-1));
        guideTitle.setText(category+"   •   "+a.size()+" kanal"); rows.removeAllViews();
        for(int i=0;i<a.size();i++){
            Channel c=a.get(i); TextView r=new TextView(this);
            r.setText(String.format(Locale.getDefault(),"%02d     %-20s     %s",c.no,c.name,c.group)); r.setTextSize(18); r.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); r.setPadding(dp(16),dp(13),dp(16),dp(13));
            if(i==guideIndex){ r.setTextColor(Color.BLACK); r.setBackground(bg(Color.WHITE,12)); }
            else if(c.no==channels.get(current).no){ r.setTextColor(Color.WHITE); r.setBackground(bg(0xFF173354,12)); }
            else r.setTextColor(0xFFE3E7EF);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(3),0,dp(3)); rows.addView(r,p);
        }
    }

    void moveGuide(int d){ List<Channel>a=filtered(); if(a.isEmpty())return; guideIndex=(guideIndex+d+a.size())%a.size(); renderGuide(); }
    void cycleCategory(int d){ int i=Arrays.asList(categories).indexOf(category); category=categories[(i+d+categories.length)%categories.length]; guideIndex=0; renderGuide(); }
    void chooseGuide(){ List<Channel>a=filtered(); if(a.isEmpty())return; int no=a.get(guideIndex).no; closeGuide(); jump(no); }

    void number(int d){
        digits=(digits+d); if(digits.length()>2) digits=digits.substring(digits.length()-2);
        setStatus("Kanal "+digits,1200);
        if(digitCommit!=null) handler.removeCallbacks(digitCommit);
        digitCommit=()->{ try{ int n=Integer.parseInt(digits); digits=""; jump(n);}catch(Exception e){digits="";} };
        handler.postDelayed(digitCommit,900);
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(e.getAction()!=KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(e);
        int k=e.getKeyCode();
        if(guideOpen){
            if(k==KeyEvent.KEYCODE_DPAD_UP){moveGuide(-1);return true;}
            if(k==KeyEvent.KEYCODE_DPAD_DOWN){moveGuide(1);return true;}
            if(k==KeyEvent.KEYCODE_DPAD_LEFT){cycleCategory(-1);return true;}
            if(k==KeyEvent.KEYCODE_DPAD_RIGHT){cycleCategory(1);return true;}
            if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER){chooseGuide();return true;}
            if(k==KeyEvent.KEYCODE_BACK){closeGuide();return true;}
            return true;
        }
        if(k==KeyEvent.KEYCODE_DPAD_UP){switchChannel(-1);return true;}
        if(k==KeyEvent.KEYCODE_DPAD_DOWN){switchChannel(1);return true;}
        if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_MENU){openGuide();return true;}
        if(k>=KeyEvent.KEYCODE_0&&k<=KeyEvent.KEYCODE_9){number(k-KeyEvent.KEYCODE_0);return true;}
        return super.dispatchKeyEvent(e);
    }

    @Override public void onBackPressed(){ if(guideOpen) closeGuide(); else super.onBackPressed(); }
    @Override public void onResume(){ super.onResume(); immersive(); }
    @Override public void onDestroy(){ handler.removeCallbacksAndMessages(null); try{video.stopPlayback();}catch(Exception ignored){} super.onDestroy(); }
}
