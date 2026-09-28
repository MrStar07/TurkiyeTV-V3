package com.mrstar.turkiyetv;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    static class Channel {
        int no; String name, group, shortName; int accent; String[] sources;
        Channel(int no,String name,String group,String shortName,int accent,String...sources){
            this.no=no;this.name=name;this.group=group;this.shortName=shortName;this.accent=accent;this.sources=sources;
        }
    }

    final Handler handler=new Handler(Looper.getMainLooper());
    final List<Channel> channels=new ArrayList<>();
    final String[] categories={"Tümü","Ulusal","Haber","Spor","Çocuk","TRT Tematik","Belgesel / Yaşam","Müzik","Eğitim","Diğer"};

    FrameLayout root,guide,hud,statusBox;
    PlayerView playerView;
    ExoPlayer player;
    TextView hudNo,hudLogo,hudName,hudGroup,statusText,categoryText,guideTitle;
    LinearLayout rows;
    ScrollView scroll;
    ProgressBar loading;

    int current=0,source=0,guideIndex=0,playToken=0;
    String category="Tümü",digits="";
    boolean guideOpen=false;
    Runnable digitCommit,hideHud;

    final String userAgent="Mozilla/5.0 (Linux; Android 11; Android TV) AppleWebKit/537.36 Chrome/126.0 Safari/537.36";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        immersive(); buildChannels(); restoreLast(); buildUi(); buildPlayer(); play(true);
    }

    void immersive(){getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
    int c(String hex){return Color.parseColor(hex);} int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    void add(int no,String name,String group,String shortName,String accent,String...sources){channels.add(new Channel(no,name,group,shortName,c(accent),sources));}

    void buildChannels(){
        add(1,"TRT 1","Ulusal","TRT 1","#E31E24","https://tv-trt1.medya.trt.com.tr/master.m3u8");
        add(2,"ATV","Ulusal","atv","#FF7A00","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_1080p.m3u8","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv.m3u8");
        add(3,"Kanal D","Ulusal","KANAL D","#2F7DE1","https://demiroren.daioncdn.net/kanald/kanald.m3u8?app=kanald_web&ce=3","https://demiroren-live.daioncdn.net/kanald/kanald.m3u8");
        add(4,"Show TV","Ulusal","SHOW","#FF7A00","https://ciner-live.daioncdn.net/showtv/showtv.m3u8","https://ciner.daioncdn.net/showtv/showtv.m3u8?app=showtv_web");
        add(5,"Star TV","Ulusal","STAR","#E12738","https://dogus.daioncdn.net/startv/startv_720p.m3u8?&sid=8l4w3lst4co5&app=a20ac41e-bdc3-4aa1-934d-26b484480ac9&ce=3","https://dogus-live.daioncdn.net/startv/playlist.m3u8");
        add(6,"NOW","Ulusal","NOW","#F04B23","https://uycyyuuzyh.turknet.ercdn.net/nphindgytw/nowtv/nowtv.m3u8");
        add(7,"TV8","Ulusal","TV8","#6D43C1","https://tv8.daioncdn.net/tv8/tv8.m3u8?app=7ddc255a-ef47-4e81-ab14-c0e5f2949788&ce=3");
        add(8,"Kanal 7","Ulusal","KANAL 7","#E12A34","https://kanal7-live.daioncdn.net/kanal7/kanal7.m3u8");
        add(9,"a2","Ulusal","a2","#E1262F","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/a2tv/a2tv.m3u8");
        add(10,"360 TV","Ulusal","360","#2777C8","https://turkmedya-live.ercdn.net/tv360/tv360.m3u8","https://turkmedya-live.ercdn.net/tv360/tv360_720p.m3u8");
        add(11,"Beyaz TV","Ulusal","BEYAZ","#D21F2B","https://beyaztv.daioncdn.net/beyaztv/beyaztv.m3u8?app=fcd5c66b-da9d-44ba-a410-4f34805c397d&ce=3","https://beyaztv-live.daioncdn.net/beyaztv/beyaztv.m3u8");
        add(12,"teve2","Ulusal","teve2","#E62A84","https://demiroren-live.daioncdn.net/teve2/teve2.m3u8");
        add(13,"TV4","Ulusal","TV4","#DD2534","https://turkmedya-live.ercdn.net/tv4/tv4.m3u8");
        add(14,"CNBC-e","Ulusal","CNBC-e","#6645B8","https://hnpsechtsc.turknet.ercdn.net/xpnvudnlsv/cnbc-e/cnbc-e.m3u8");

        add(15,"TRT 2","TRT Tematik","TRT 2","#C81F32","https://tv-trt2.medya.trt.com.tr/master.m3u8");
        add(16,"TRT Belgesel","Belgesel / Yaşam","TRT BELGESEL","#18A36C","https://tv-trtbelgesel.medya.trt.com.tr/master.m3u8");
        add(17,"TRT Müzik","Müzik","TRT MÜZİK","#B92868","https://tv-trtmuzik.medya.trt.com.tr/master.m3u8");
        add(18,"TRT Çocuk","Çocuk","TRT ÇOCUK","#F0A523","https://tv-trtcocuk.medya.trt.com.tr/master.m3u8");
        add(19,"TRT Diyanet Çocuk","Çocuk","TRT DİYANET","#5FA649","https://tv-trtdiyanetcocuk.medya.trt.com.tr/master.m3u8","https://tv-trt-diyanet-cocuk.medya.trt.com.tr/master_720.m3u8");
        add(20,"TRT Türk","TRT Tematik","TRT TÜRK","#D32A37","https://tv-trtturk.medya.trt.com.tr/master.m3u8");
        add(21,"TRT Avaz","TRT Tematik","TRT AVAZ","#E17125","https://tv-trtavaz.medya.trt.com.tr/master.m3u8");
        add(22,"TRT Kurdî","TRT Tematik","TRT KURDÎ","#7A4397","https://tv-trtkurdi.medya.trt.com.tr/master.m3u8");
        add(23,"TRT World","TRT Tematik","TRT WORLD","#1E97C8","https://tv-trtworld.medya.trt.com.tr/master.m3u8");

        add(24,"Minika Çocuk","Çocuk","minika ÇOCUK","#F48B27","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk.m3u8");
        add(25,"Minika GO","Çocuk","minika GO","#EC385B","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago/minikago.m3u8");

        add(26,"TRT Haber","Haber","TRT HABER","#CF1E2B","https://tv-trthaber.medya.trt.com.tr/master.m3u8");
        add(27,"NTV","Haber","NTV","#1678BD","https://dogus-live.daioncdn.net/ntv/ntv.m3u8","https://dogus.daioncdn.net/ntv/ntv.m3u8?app=ntv_web");
        add(28,"CNN Türk","Haber","CNN TÜRK","#C21D2A","https://live.duhnet.tv/S2/HLS_LIVE/cnnturknp/playlist.m3u8");
        add(29,"Habertürk","Haber","HABERTÜRK","#DA2433","https://ciner-live.daioncdn.net/haberturktv/haberturktv.m3u8");
        add(30,"Bloomberg HT","Haber","BLOOMBERG HT","#ED6B25","https://ciner.daioncdn.net/bloomberght/bloomberght.m3u8","https://ciner-live.daioncdn.net/bloomberght/bloomberght.m3u8");
        add(31,"24 TV","Haber","24","#E53231","https://mn-nl.mncdn.com/kanal24/smil:kanal24.smil/playlist.m3u8","https://turkmedya-live.ercdn.net/tv24/tv24_720p.m3u8");
        add(32,"TGRT Haber","Haber","TGRT HABER","#D22431","https://canli.tgrthaber.com/tgrt.m3u8");
        add(33,"A Haber","Haber","A HABER","#D71920","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/ahaber/ahaber.m3u8");
        add(34,"Akit TV","Haber","AKİT TV","#C51B25","https://akittv-live.ercdn.net/akittv/akittv.m3u8");
        add(35,"Halk TV","Haber","HALK TV","#D31E31","https://halktv-live.daioncdn.net/halktv/halktv.m3u8");
        add(36,"Tele1","Haber","TELE1","#D11E2E","https://tele1-live.ercdn.net/tele1/tele1.m3u8");
        add(37,"TV100","Haber","TV100","#E31A2E","https://tv100-live.daioncdn.net/tv100/tv100.m3u8");
        add(38,"TVNET","Haber","TVNET","#D31F35","https://tvnet-live.lg.mncdn.com/tvnet/tvnet/playlist.m3u8");
        add(39,"TBMM TV","Haber","TBMM TV","#B81C2C","https://meclistv-live.ercdn.net/meclistv/meclistv.m3u8");

        add(40,"TRT Spor","Spor","TRT SPOR","#18A05E","https://tv-trtspor1.medya.trt.com.tr/master.m3u8","https://tv-trt-spor.medya.trt.com.tr/master_720.m3u8");
        add(41,"TRT Spor Yıldız","Spor","TRT SPOR ★","#159066","https://tv-trtspor2.medya.trt.com.tr/master.m3u8","https://tv-trt-spor-yildiz.medya.trt.com.tr/master_720.m3u8");
        add(42,"A Spor","Spor","A SPOR","#E5232D","https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8");
        add(43,"HT Spor","Spor","HT SPOR","#D31F30","https://ciner.daioncdn.net/ht-spor/ht-spor.m3u8?app=web","https://ciner-live.ercdn.net/htspor/htspor.m3u8");

        add(44,"Kral Pop TV","Müzik","KRAL POP","#E82A6A","https://dogus-live.daioncdn.net/kralpoptv/playlist.m3u8");
        add(45,"PowerTürk TV","Müzik","POWER TÜRK","#E7222C","https://livetv.powerapp.com.tr/powerturkTV/powerturkhd.smil/playlist.m3u8");
        add(46,"Number 1 TV","Müzik","NUMBER 1","#E2273B","https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8");
        add(47,"Number 1 Türk","Müzik","NR1 TÜRK","#D82939","https://mn-nl.mncdn.com/blutv_nr1turk2/live.m3u8");

        add(48,"TRT EBA İlkokul","Eğitim","EBA İLK","#2E8B57","https://tv-e-okul00.medya.trt.com.tr/master.m3u8");
        add(49,"TRT EBA Ortaokul","Eğitim","EBA ORTA","#287A52","https://tv-e-okul01.medya.trt.com.tr/master.m3u8");
        add(50,"TRT EBA Lise","Eğitim","EBA LİSE","#226D4C","https://tv-e-okul02.medya.trt.com.tr/master.m3u8");

        add(51,"GZT","Diğer","GZT","#1AA06D","https://gzttv-live.lg.mncdn.com/gzttv/gzttv/playlist.m3u8");
        add(52,"Flash TV","Diğer","FLASH TV","#D82030","https://mn-nl.mncdn.com/blutv_flashtv/live.m3u8");
        add(53,"Bizimev TV","Diğer","BİZİMEV","#8D6E63","https://mn-nl.mncdn.com/blutv_bizimev/bizimev_sd.smil/playlist.m3u8");
        add(54,"CGTN Documentary","Belgesel / Yaşam","CGTN DOC","#B88A2B","https://mn-nl.mncdn.com/dogusdyg_drone/cgtn/playlist.m3u8");
    }

    void restoreLast(){int last=getPreferences(MODE_PRIVATE).getInt("last",1);for(int i=0;i<channels.size();i++)if(channels.get(i).no==last){current=i;break;}}

    void buildPlayer(){
        DefaultHttpDataSource.Factory http=new DefaultHttpDataSource.Factory().setUserAgent(userAgent).setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(12000).setReadTimeoutMs(18000);
        player=new ExoPlayer.Builder(this).setMediaSourceFactory(new DefaultMediaSourceFactory(http)).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener(){
            @Override public void onPlaybackStateChanged(int state){if(state==Player.STATE_BUFFERING)setStatus("Bağlanıyor…",true,0);else if(state==Player.STATE_READY)setStatus("CANLI",false,1800);}
            @Override public void onPlayerError(PlaybackException error){tryNext();}
        });
    }

    void buildUi(){
        root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);
        playerView=new PlayerView(this);playerView.setUseController(false);playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);playerView.setBackgroundColor(Color.BLACK);
        root.addView(playerView,new FrameLayout.LayoutParams(-1,-1));
        addVignette();buildHud();buildStatus();buildGuide();setContentView(root);
        hideHud=()->hud.setVisibility(View.GONE);
    }

    void addVignette(){View v=new View(this);GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x88000000,0x00000000,0x00000000,0x99000000});v.setBackground(g);root.addView(v,new FrameLayout.LayoutParams(-1,-1));}

    GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    GradientDrawable outline(int fill,int stroke,int width,int radius){GradientDrawable d=round(fill,radius);d.setStroke(dp(width),stroke);return d;}

    TextView tv(String text,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}

    void buildHud(){
        hud=new FrameLayout(this);hud.setPadding(dp(14),dp(12),dp(18),dp(12));hud.setBackground(outline(0xD91A1D25,0x55FFFFFF,1,22));
        LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);line.setGravity(Gravity.CENTER_VERTICAL);hud.addView(line,new FrameLayout.LayoutParams(-2,-2));
        hudNo=tv("01",27,Color.WHITE,true);hudNo.setGravity(Gravity.CENTER);hudNo.setBackground(round(0xFF222832,15));line.addView(hudNo,new LinearLayout.LayoutParams(dp(62),dp(58)));
        hudLogo=tv("TRT 1",15,Color.WHITE,true);hudLogo.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lpl=new LinearLayout.LayoutParams(dp(90),dp(58));lpl.setMargins(dp(10),0,dp(14),0);line.addView(hudLogo,lpl);
        LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.setGravity(Gravity.CENTER_VERTICAL);line.addView(texts,new LinearLayout.LayoutParams(dp(250),dp(58)));
        hudName=tv("TRT 1",23,Color.WHITE,true);hudGroup=tv("Ulusal • CANLI",13,0xFFBFC7D4,false);texts.addView(hudName);texts.addView(hudGroup);
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-2,-2);hp.gravity=Gravity.TOP|Gravity.LEFT;hp.setMargins(dp(28),dp(26),0,0);root.addView(hud,hp);
    }

    void buildStatus(){
        statusBox=new FrameLayout(this);statusBox.setPadding(dp(12),dp(7),dp(14),dp(7));statusBox.setBackground(outline(0xD9191D25,0x44FFFFFF,1,99));
        LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);line.setGravity(Gravity.CENTER_VERTICAL);statusBox.addView(line,new FrameLayout.LayoutParams(-2,-2));
        loading=new ProgressBar(this,null,android.R.attr.progressBarStyleSmall);line.addView(loading,new LinearLayout.LayoutParams(dp(22),dp(22)));
        statusText=tv("Bağlanıyor…",13,Color.WHITE,true);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.setMargins(dp(8),0,0,0);line.addView(statusText,sp);
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(-2,-2);fp.gravity=Gravity.TOP|Gravity.RIGHT;fp.setMargins(0,dp(28),dp(28),0);root.addView(statusBox,fp);
    }

    void buildGuide(){
        guide=new FrameLayout(this);guide.setBackgroundColor(0xE60A0D12);guide.setVisibility(View.GONE);root.addView(guide,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.HORIZONTAL);shell.setPadding(dp(28),dp(28),dp(28),dp(24));guide.addView(shell,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout rail=new LinearLayout(this);rail.setOrientation(LinearLayout.VERTICAL);rail.setPadding(dp(18),dp(18),dp(18),dp(18));rail.setBackground(outline(0xE91B202A,0x33FFFFFF,1,24));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(280),-1);rp.setMargins(0,0,dp(18),0);shell.addView(rail,rp);
        TextView brand=tv("TÜRKİYE TV",27,Color.WHITE,true);rail.addView(brand);TextView ver=tv("V3.1 • Android TV",13,0xFF9AA6B6,false);ver.setPadding(0,dp(2),0,dp(18));rail.addView(ver);
        categoryText=tv("",17,0xFFE6EAF0,true);categoryText.setLineSpacing(dp(8),1f);rail.addView(categoryText);
        LinearLayout main=new LinearLayout(this);main.setOrientation(LinearLayout.VERTICAL);main.setPadding(dp(18),dp(16),dp(18),dp(14));main.setBackground(outline(0xE91B202A,0x33FFFFFF,1,24));shell.addView(main,new LinearLayout.LayoutParams(0,-1,1f));
        guideTitle=tv("Tümü",25,Color.WHITE,true);guideTitle.setPadding(dp(6),dp(4),dp(6),dp(12));main.addView(guideTitle);
        scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);main.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);scroll.addView(rows,new ScrollView.LayoutParams(-1,-2));
        TextView help=tv("↑ ↓ Kanal   •   ← → Kategori   •   OK Seç   •   0–9 Numara   •   Geri Kapat",13,0xFF9FAABB,false);help.setPadding(dp(6),dp(12),0,0);main.addView(help);
    }

    void play(boolean reset){
        if(reset)source=0;Channel ch=channels.get(current);getPreferences(MODE_PRIVATE).edit().putInt("last",ch.no).apply();showHud();setStatus("Bağlanıyor…",true,0);playToken++;
        player.stop();player.clearMediaItems();player.setMediaItem(MediaItem.fromUri(ch.sources[source]));player.prepare();player.setPlayWhenReady(true);
    }

    void tryNext(){Channel ch=channels.get(current);if(source+1<ch.sources.length){source++;setStatus("Yedek akış deneniyor…",true,1200);play(false);}else setStatus("Yayın şu anda açılamadı",false,0);}
    void switchChannel(int d){current=(current+d+channels.size())%channels.size();source=0;play(true);} void jump(int no){for(int i=0;i<channels.size();i++)if(channels.get(i).no==no){current=i;source=0;play(true);return;}setStatus("Kanal bulunamadı",false,1300);}

    void showHud(){
        Channel ch=channels.get(current);hudNo.setText(String.format(Locale.getDefault(),"%02d",ch.no));hudLogo.setText(ch.shortName);hudLogo.setBackground(round(ch.accent,15));hudName.setText(ch.name);hudGroup.setText(ch.group+"  •  CANLI");hud.setVisibility(View.VISIBLE);handler.removeCallbacks(hideHud);handler.postDelayed(hideHud,3300);
    }

    void setStatus(String text,boolean busy,long hideAfter){statusText.setText(text);loading.setVisibility(busy?View.VISIBLE:View.GONE);statusBox.setVisibility(View.VISIBLE);if(hideAfter>0)handler.postDelayed(()->statusBox.setVisibility(View.GONE),hideAfter);}

    List<Channel> filtered(){if(category.equals("Tümü"))return new ArrayList<>(channels);List<Channel> out=new ArrayList<>();for(Channel ch:channels)if(ch.group.equals(category))out.add(ch);return out;}
    void openGuide(){guideOpen=true;guide.setVisibility(View.VISIBLE);guideIndex=0;List<Channel>a=filtered();for(int i=0;i<a.size();i++)if(a.get(i).no==channels.get(current).no)guideIndex=i;renderGuide();}
    void closeGuide(){guideOpen=false;guide.setVisibility(View.GONE);showHud();}

    void renderGuide(){
        StringBuilder sb=new StringBuilder();for(String s:categories)sb.append(s.equals(category)?"●  ":"   ").append(s).append("\n\n");categoryText.setText(sb.toString());
        List<Channel>a=filtered();if(a.isEmpty())return;guideIndex=Math.max(0,Math.min(guideIndex,a.size()-1));guideTitle.setText(category+"    "+a.size()+" kanal");rows.removeAllViews();
        TextView selected=null;
        for(int i=0;i<a.size();i++){
            Channel ch=a.get(i);TextView r=tv(String.format(Locale.getDefault(),"%02d    %-18s    %s",ch.no,ch.name,ch.group),18,0xFFE7EAF0,true);r.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(18),dp(15),dp(18),dp(15));
            if(i==guideIndex){r.setTextColor(Color.WHITE);r.setTextSize(20);r.setBackground(outline(0xFF242B37,ch.accent,3,16));r.setScaleX(1.015f);r.setScaleY(1.015f);selected=r;}
            else if(ch.no==channels.get(current).no)r.setBackground(outline(0xFF1A2733,ch.accent,1,14));
            else r.setBackground(round(0x66212730,14));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(3),dp(4),dp(3),dp(4));rows.addView(r,p);
        }
        final TextView sel=selected;if(sel!=null)scroll.post(()->scroll.smoothScrollTo(0,Math.max(0,sel.getTop()-dp(150))));
    }

    void moveGuide(int d){List<Channel>a=filtered();if(a.isEmpty())return;guideIndex=(guideIndex+d+a.size())%a.size();renderGuide();}
    void cycleCategory(int d){int i=Arrays.asList(categories).indexOf(category);category=categories[(i+d+categories.length)%categories.length];guideIndex=0;renderGuide();}
    void chooseGuide(){List<Channel>a=filtered();if(a.isEmpty())return;int no=a.get(guideIndex).no;closeGuide();jump(no);}

    void number(int d){digits+=d;if(digits.length()>2)digits=digits.substring(digits.length()-2);setStatus("Kanal "+digits,false,1200);if(digitCommit!=null)handler.removeCallbacks(digitCommit);digitCommit=()->{try{int n=Integer.parseInt(digits);digits="";jump(n);}catch(Exception e){digits="";}};handler.postDelayed(digitCommit,900);}

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(e.getAction()!=KeyEvent.ACTION_DOWN)return super.dispatchKeyEvent(e);int k=e.getKeyCode();
        if(guideOpen){if(k==KeyEvent.KEYCODE_DPAD_UP){moveGuide(-1);return true;}if(k==KeyEvent.KEYCODE_DPAD_DOWN){moveGuide(1);return true;}if(k==KeyEvent.KEYCODE_DPAD_LEFT){cycleCategory(-1);return true;}if(k==KeyEvent.KEYCODE_DPAD_RIGHT){cycleCategory(1);return true;}if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER){chooseGuide();return true;}if(k==KeyEvent.KEYCODE_BACK){closeGuide();return true;}return true;}
        if(k==KeyEvent.KEYCODE_DPAD_UP){switchChannel(-1);return true;}if(k==KeyEvent.KEYCODE_DPAD_DOWN){switchChannel(1);return true;}if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_MENU){openGuide();return true;}if(k>=KeyEvent.KEYCODE_0&&k<=KeyEvent.KEYCODE_9){number(k-KeyEvent.KEYCODE_0);return true;}return super.dispatchKeyEvent(e);
    }

    @Override public void onBackPressed(){if(guideOpen)closeGuide();else super.onBackPressed();}
    @Override protected void onResume(){super.onResume();immersive();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(player!=null)player.release();super.onDestroy();}
}
