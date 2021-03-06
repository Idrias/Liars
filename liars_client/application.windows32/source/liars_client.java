import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 
import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class liars_client extends PApplet {

//
static String CLIENT_VERSION = "1.0.0";
//

public void setup() {
  
  rectMode(RADIUS);
  textAlign(CENTER, CENTER);
  minim = new Minim(this);
  audio = new AudioManager();
  setup_vars();
}


public void draw() {
  audio.mixAmbient();
  
  switch(game.stage) {
    case -1:     pregame(); break;
    case  5:     credits(); break;
    
    default:     network.pull();
                 game.stateCheck();
                 game.checkclick();
                 game.board.draw();
                 break;
  } 
}


/*
  STAGES:
-1 - menu
 0 - end of round reset (default)
 1 - trying to connect
 2 - connected, server is in waiting phase
 3 - playing
 4 - results (todo)
 5- credits
 */

// todo: wenn client leavt w\u00e4hrend er dran ist, spiel soll weitergehen

class Game {
  Board board;

  int stage = 0;
  int dctime = 0;
  String playerid = "999";
  boolean myTurn = false;
  boolean firstTurn = false;
  boolean connectionStatus = true;
  boolean got_clicked = false;
  boolean hassentname = false;
  boolean hadConnection = false;
  boolean lied = false;
  String liar = "";


  Game() {
    board = new Board();
  }


  public void checkclick() {
    if (mousePressed) {
      if (!got_clicked) {
        got_clicked = true;

        if (board.button_sort.checkclick()) board.sort();

        if (board.four_available && !board.four_available_kind.equals("dame") && board.button_remove_four.checkclick()) {
          network.push("-dl4", board.four_available_kind, "");
          for (int i=0; i<board.sta_player.size(); i++) {
            if (board.sta_player.get(i).id.equals(board.four_available_kind)) {
              board.sta_player.remove(board.sta_player.get(i));
              i = -1;
            }
          }
        }

        if (myTurn) {  
          if (firstTurn) par.checkclick();

          for (int i=board.sta_player.size()-1; i>=0; i--) {
            Card card = board.sta_player.get(i);
            if (card.checkclick()) {
              card.highlighted = !card.highlighted; 
              break;
            };
          }

          if (board.button_lie.checkclick()) {
            network.push("+lam", playerid, "");
            game.myTurn = false;
          }

          if (board.button_accept.checkclick()) {
            if (game.myTurn) {
              if ((game.firstTurn && !par.playas.equals("none")) || !game.firstTurn) {
                for (int i = 0; i< board.sta_player.size(); i++) {
                  Card subject = board.sta_player.get(i);
                  if (subject.highlighted) {
                    String farbe = subject.farbe;
                    String id = subject.id;

                    if (game.firstTurn) {
                      network.push("+paa", par.playas, ""); 
                      game.firstTurn=false;
                    }
                    network.push("+gst", farbe, id);
                    board.sta_player.remove(board.sta_player.get(i));
                    i = -1;
                  }
                }
                network.push("+eot", playerid, "");
                game.myTurn = false;
                game.firstTurn = false;
              }
            }
          }
        }
      }
    } else got_clicked = false;
  }


  public void stateCheck() {
    board.hasfour();

    if (connectionStatus==false) {
      if (dctime==0) dctime = millis();
      if (millis()-dctime > 2000) setup_vars();
    }

    if (!hassentname && !playerid.equals("999"))
    {
      network.push("+npn", playerid, prescreen.playername.content);
      hassentname = true;
    }

    boolean gotOne = false;
    if (myTurn) {


      for (Card card : board.sta_player) {
        if (card.highlighted && (!par.playas.equals("none") || !board.playingas.equals("none"))) {
          board.button_accept.state = true;
          gotOne = true;
        }
      }

      if (!firstTurn) board.button_lie.state = true;
    } else {
      board.button_accept.state = false;
      board.button_lie.state = false;
    }

    if (!gotOne) board.button_accept.state = false;
    if (board.four_available) board.button_remove_four.state = true; 
    else board.button_remove_four.state = false;
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Board {
  Button button_accept;
  Button button_lie;
  Button button_remove_four;
  Button button_sort;

  ArrayList<Card> sta_player;
  ArrayList<Card> sta_game;

  ArrayList<Bomber> bombers;
  ArrayList<String> msgs;
  String playingas = "none";
  String subt = "";
  PImage pi_board;
  boolean four_available = false;
  String four_available_kind = "";


  Board() {
    button_accept = new Button(8.25f*width/10, 5.32f*height/6, width/30, height/36, "Legen", true);
    button_lie = new Button(9.25f*width/10, 5.32f*height/6, width/30, height/36, "L\u00fcge", false);

    button_sort = new Button(8.25f*width/10, 5.73f*height/6, width/30, height/36, "Sortieren", true);
    button_remove_four = new Button(9.25f*width/10, 5.73f*height/6, width/30, height/36, "4 Ablegen", false);

    sta_player = new ArrayList<Card>();
    sta_game = new ArrayList<Card>();
    msgs = new ArrayList<String>();
    bombers = new ArrayList<Bomber>();

    load_background("board.jpg");
  }


  public void load_background(String which) {
    pi_board = loadImage("/assets/board/"+which);
    pi_board.resize(width, height);
  }


  public void draw() { 
    // Draw wooden background and lines
    image(pi_board, 0, 0);
    strokeWeight(5);
    line(0, 2*height/3, 3*width/4, 2*height/3);  // a
    line(3*width/4, 0, 3*width/4, height);  // b
    line(3*width/4, 2.5f*height/3, width, 2.5f*height/3);  // c
    strokeWeight(1);

    // Draw buttons
    button_accept.draw();
    button_lie.draw();
    button_sort.draw();
    button_remove_four.draw();
    //if (four_available && !four_available_kind.equals("dame")) {remove_four.text = "Vier der gleichen Sorte ablegen: " + four_available_kind.substring(0, 1).toUpperCase()+four_available_kind.substring(1, four_available_kind.length()); remove_four.draw();}

    // Draw cards
    int numberOfCards;
    float spacing;
    float xpos;
    float ypos;

    //-> sta_player
    numberOfCards = sta_player.size();
    spacing = (0.72f*width-width/10) / numberOfCards;
    if (spacing>120) spacing = 120;
    xpos = 20;
    ypos = 0.72f * height;

    for (Card card : sta_player) {
      card.draw(xpos, ypos);
      xpos += spacing;
    }

    // -> sta_game
    numberOfCards = sta_game.size();
    spacing = (0.72f*width-width/10) / numberOfCards;
    if (spacing>120) spacing = 120;
    xpos = 20;
    ypos = height / 10;

    for (Card card : sta_game) {
      card.draw(xpos, ypos);
      xpos += spacing;
    }

    // -> par
    if (game.firstTurn && game.myTurn) par.draw();

    // Print msgs
    fill(0);
    ypos = height / 30;
    xpos = 0.765f * width;
    spacing = 14;
    textAlign(LEFT, CENTER);
    while (msgs.size()>30) msgs.remove(0); 

    for (String text : msgs) {
      text(text, xpos, ypos);
      ypos += spacing;
    }

    // Print GameInfo
    fill(0, 0, 255);
    textSize(16);
    if (!playingas.equals("null") && !playingas.equals("none")) text("Es liegt: " + playingas.substring(0, 1).toUpperCase()+playingas.substring(1, playingas.length()), 5*width/1000, height*360/600);
    textSize(11);
    text("GameInfo: Du bist Spieler " + game.playerid, 5*width/1000, height*390/600);

    if (game.myTurn) {
      textSize(15);
      fill(255, 0, 255);
      text("It's your turn!", 828*width/1000, 475*height/600);
      textSize(11);
    }

    if (!game.connectionStatus) {
      fill(255, 0, 0);
      textSize(20);
      text("RETURNING TO MENU IN " + (2000-(millis()-game.dctime)) + "ms!", 1*width/4, 2*height/3);
      textSize(11);
    }

    if (game.stage==2) {
      fill(0);
      textSize(20);
      text("Connected. Waiting for cards from server!", 1*width/4, 2.2f*height/3);
      textSize(11);
    }

    textAlign(CENTER, CENTER);
    fill(0);
    text(subt, 375*width/1000, height*390/600);

    for (Bomber bomber : bombers) bomber.act();
  }

  public PImage find_referencedImage(String reference) {
    for (ReferencedImage refim : images) {
      if (refim.reference.equals(reference)) return refim.image;
    }
    return null;
  }

  public void hasfour() {
    int count7 = 0;
    int count8 = 0;
    int count9 = 0;
    int count10 = 0;
    int countbube = 0;
    int countdame = 0;
    int countkoenig = 0;
    int countass = 0;

    for (Card card : sta_player) {
      if (card.id.equals("7")) count7++;
      else if (card.id.equals("8")) count8++;
      else if (card.id.equals("9")) count9++;
      else if (card.id.equals("10")) count10++;
      else if (card.id.equals("bube")) countbube++;
      else if (card.id.equals("dame")) countdame++;
      else if (card.id.equals("k\u00f6nig")) countkoenig++;
      else if (card.id.equals("ass")) countass++;
    }

    if (count7==4) {
      four_available=true; 
      four_available_kind="7";
    } else if (count8==4) {
      four_available=true; 
      four_available_kind="8";
    } else if (count9==4) {
      four_available=true; 
      four_available_kind="9";
    } else if (count10==4) {
      four_available=true; 
      four_available_kind="10";
    } else if (countbube==4) {
      four_available=true; 
      four_available_kind="bube";
    } else if (countkoenig==4) {
      four_available=true; 
      four_available_kind="k\u00f6nig";
    } else if (countass==4) {
      four_available=true; 
      four_available_kind="ass";
    } else {
      four_available = false; 
      four_available_kind = "none";
    }
  }

  public void sort() {
    ArrayList<Card> sta_sort = new ArrayList<Card>();
    sta_sort  = sta_player;
    sta_player = new ArrayList<Card>();

    for (float value = 7; value < 15; value+=0.1f) {
      for (int i=0; i<sta_sort.size(); i++) {
        Card card = sta_sort.get(i);
        if (card.value <= value) {
          sta_player.add(card);
          sta_sort.remove(card);
          i=0;
        }
      }
    } 
    sta_sort = new ArrayList<Card>();
    for (Card card : sta_player) {
      sta_sort.add(card);
    }
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Card {
  String farbe;
  String id;
  PImage kartenbild;
  PImage kartenbildB;
  int selectedcol;
  int freshcol;
  float lastx, lasty;
  float value;
  boolean highlighted = false;
  boolean hidden;
  int birthtime;


  Card(String p_farbe, String p_id, boolean p_hidden) {
    farbe = p_farbe;
    id = p_id;
    hidden = p_hidden;
    value = determineValue();
    kartenbild = getImage();
    selectedcol = color(0xff14FF00);
    freshcol = color(0xffFAD412);
    birthtime = millis();
  }

  public float determineValue() {
    float value = 0;

    if (farbe.equals("herz")) value+=0.1f;
    else if (farbe.equals("karo")) value+=0.2f;
    else if (farbe.equals("kreuz")) value+=0.3f;
    else if (farbe.equals("pik")) value+=0.4f;

    if (id.equals("7")) value+=7;
    else if (id.equals("8")) value+=8;
    else if (id.equals("9")) value+=9;
    else if (id.equals("10")) value+=10;
    else if (id.equals("bube")) value+=11;
    else if (id.equals("dame")) value+=12;
    else if (id.equals("k\u00f6nig")) value+=13;
    else if (id.equals("ass")) value+=14;

    return value;
  }

  public PImage getImage() {
    String reference = farbe+id;
    PImage image = new PImage();
    image = game.board.find_referencedImage(reference).copy();
    kartenbildB = game.board.find_referencedImage("backblack");

    if (image!=null) {
      image.resize(width/10, 0);
      kartenbildB.resize(width/10, 0);
      return image;
    } else {
      println("nop");
      String path = "/assets/cards/" + farbe + "/" + id + ".jpg";
      image = loadImage(path);
      kartenbildB = loadImage("/assets/cards/back/black.jpg");

      image.resize(width/10, 0);
      kartenbildB.resize(width/10, 0);
      return image;
    }
  }


  public void draw(float xpos, float ypos) {
    if (!hidden) image(kartenbild, xpos, ypos);
    else image(kartenbildB, xpos, ypos);

    // So that checkclick() can actually know where this card is placed
    lastx = xpos;
    lasty = ypos;

    if (highlighted || (millis()-birthtime < 1500 && !farbe.equals("mutated") && !hidden)) {
      if (highlighted) stroke(selectedcol);
      else stroke(freshcol);
      strokeWeight(3);
      noFill();
      rectMode(CORNERS);
      rect(xpos, ypos, xpos+kartenbild.width, ypos+kartenbild.height);
      rectMode(RADIUS);
      stroke(0);
      strokeWeight(1);
    }
  }


  public boolean checkclick() {
    if (mouseX > lastx && mouseX < lastx + kartenbild.width) {
      if (mouseY > lasty && mouseY < lasty + kartenbild.height) {
        return true;
      }
    }
    return false;
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Button {
  float xpos, ypos, radx, rady;
  int col;
  String text;
  boolean state;
  int disabledcol;
  int activecol;


  Button(float p_xpos, float p_ypos, float p_radx, float p_rady, String p_text, boolean p_state) {
    xpos = p_xpos;
    ypos = p_ypos;
    radx = p_radx;
    rady = p_rady;
    text = p_text;
    state = p_state;

    disabledcol = color(0xff808393);
    activecol = color(0xff3CD63D);
    checkstate();
  }


  public void checkstate() {
    if (state) col = activecol;
    else col = disabledcol;
  }


  public boolean checkclick() {
    if (mouseX > xpos-radx && mouseX < xpos+radx) {
      if (mouseY > ypos - rady && mouseY < ypos+rady) {
        if (state) return true;
      }
    }
    return false;
  }


  public void draw() {
    checkstate();
    fill(col);
    rect(xpos, ypos, radx, rady);
    fill(0);
    text(text, xpos, ypos);
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Textbox {
  String content = "";
  float xpos, ypos, radx, rady;
  boolean active = false;
  int maxchars = 20;
  String displaytext = "";

  Textbox(float p_xpos, float p_ypos, float p_radx, float p_rady) {
    xpos = p_xpos;
    ypos = p_ypos;
    radx = p_radx;
    rady = p_rady;
  }

  public void draw() {
    if (active) fill(255);
    else fill(160);

    rect(xpos, ypos, radx, rady);

    fill(0);
    displaytext = content;
    if (content.length()>25) displaytext = content.substring(0, 25);
    text(displaytext, xpos, ypos);
  }

  public boolean checkclick() {
    if (mouseX > xpos-radx && mouseX < xpos+radx) {
      if (mouseY > ypos - rady && mouseY < ypos+rady) {
        return true;
      }
    }
    return false;
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class PlayAsRequest {
  ArrayList<Card> cards;
  int x0, y0;
  String playas = "none";

  PlayAsRequest() {
    cards = new ArrayList<Card>();
    cards.add(new Card("mutated", "7", false));
    cards.add(new Card("mutated", "8", false));
    cards.add(new Card("mutated", "9", false));
    cards.add(new Card("mutated", "10", false));
    cards.add(new Card("mutated", "bube", false));
    cards.add(new Card("mutated", "k\u00f6nig", false));
    cards.add(new Card("mutated", "ass", false));

    for (Card card : cards) card.kartenbild.resize(card.kartenbild.width/2, 0);
  }


  public void draw() {
    fill(0, 255, 255);
    textSize(20);
    text("Ausspielen als:", 120*width/1000, 341*height/600);
    textSize(11);

    x0 = 380*width/1000-307*height/1200;
    y0 = 341*height/600-34*height/600;

    int xpos = x0;
    int ypos = y0;

    for (Card card : cards) {

      card.draw(xpos, ypos);
      xpos += 70*width/1000;
    }
  }

  public void checkclick() {

    for (Card card : cards) {
      if (card.checkclick()) {
        for (Card card2 : cards) {
          card2.highlighted = false;
        }
        card.highlighted = true;
        playas = card.id;
        println("Wir spielen als", playas);
      }
    }
  }
}


////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class ReferencedImage {
  PImage image;
  String reference;

  ReferencedImage(String p_path, String p_reference) {
    image = loadImage(p_path);
    reference = p_reference;
  }
}


////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Bomber {
  float xpos, ypos;
  PVector v;
  boolean active;
  int stepslived = 0;
  PImage pi_bomber;

  Bomber() {
    pi_bomber = loadImage("/assets/etc/bomber.png");
    pi_bomber.resize(150, 0);
    v = new PVector();

    v.x = random(2, 8);
    v.y = random(-0.5f, 0.5f);

    xpos = 0;
    ypos = height/2-150;
  }

  public void act() {
    if (xpos>width+200) return;

    xpos += v.x;
    ypos += v.y;

    draw();
    stepslived++;
  }

  public void draw() {
    image(pi_bomber, xpos, ypos);
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class AudioManager {
  boolean mutedAmbient = false;
  boolean mutedSound = false;
  ArrayList<ReferencedPlayer> players;
  ArrayList<AudioPlayer> playingAmbient;
  ArrayList<AudioPlayer> playingSound;
  String ambient = "";

  AudioManager() {
    players = new ArrayList<ReferencedPlayer>();
    playingSound = new ArrayList<AudioPlayer>();
    playingAmbient = new ArrayList<AudioPlayer>();
    setup_players();
  }

  public void muteAmbient() {
    mutedAmbient = !mutedAmbient;
    if (mutedAmbient) stopAll("ambient");
    else ambient = "";
  }

  public void muteSound() {
    mutedSound = !mutedSound;
    if (mutedSound) {
      stopAll("sound");
    }
  }

  public void mixAmbient() {
    if (game.stage == -1 && !ambient.equals("menu")) {
      ambient = "menu";
      stopAll("ambient");
      play("ambient", "piano", true, true);
    } else if (game.stage == 2 && !ambient.equals("waiting")) {
      ambient = "waiting";
      stopAll("ambient");
      play("ambient", "saloon", true, true);
    } else if (game.stage == 3 && !ambient.equals("playing")) {
      ambient = "playing";
      stopAll("ambient");
      play("sound", "start", true, false);
    } else if (game.stage == 5 && !ambient.equals("credits")) {
      ambient = "credits";
      stopAll("ambient");
      play("ambient", "credits", true, true);
    }
  }

  public void setup_players() {
    players.add( new ReferencedPlayer("/ambient/piano.mp3", "ambient_piano") );
    players.add( new ReferencedPlayer("/ambient/saloon.mp3", "ambient_saloon") );
    players.add( new ReferencedPlayer("/ambient/credits.mp3", "ambient_credits") );
    players.add( new ReferencedPlayer("/fx/cena.mp3", "sound_cena") );
    players.add( new ReferencedPlayer("/fx/reset.mp3", "sound_reset"));
    players.add( new ReferencedPlayer("/fx/start.mp3", "sound_start"));
    players.add( new ReferencedPlayer("/fx/set.mp3", "sound_set"));
    players.add( new ReferencedPlayer("/fx/turn.mp3", "sound_turn"));
  }

  public void play(String sort, String what, boolean rewind, boolean loop) {
    if ((mutedAmbient && sort.equals("ambient")) || (mutedSound && sort.equals("sound"))) return;
    what = sort + "_" + what;

    AudioPlayer toPlay = getByReference(what);
    if (toPlay == null) {
      println("Was not able to find sound: " + what); 
      return;
    }
    if (rewind) toPlay.rewind();
    if (loop) toPlay.loop();
    else toPlay.play();

    if (sort.equals("ambient")) playingAmbient.add(toPlay);
    else playingSound.add(toPlay);
  }

  public void stop(String sort, String what) {
    what = sort + "_" + what;
    AudioPlayer toStop = getByReference(what);
    if (toStop == null) {
      println("Was not able to find sound: " + what); 
      return;
    }
    toStop.pause();
    toStop.rewind();
    if (sort.equals("ambient")) playingAmbient.remove(toStop);
    else playingSound.remove(toStop);
  }

  public void pause(String sort, String what) {
    what = sort + "_" + what;
    AudioPlayer toStop = getByReference(what);
    if (toStop == null) {
      println("Was not able to find sound: " + what); 
      return;
    }
    toStop.pause();
    if (sort.equals("ambient")) playingAmbient.remove(toStop);
    else playingSound.remove(toStop);
  }

  public void stopAll(String which) {
    if (which.equals("ambient") || which.equals("all")) {
      for (AudioPlayer player : playingAmbient) {
        player.pause();
        player.rewind();
      }
      playingAmbient = new ArrayList<AudioPlayer>();
    }

    if (which.equals("sound") || which.equals("all")) {
      for (AudioPlayer player : playingSound) {
        player.pause();
        player.rewind();
      }
      playingSound = new ArrayList<AudioPlayer>();
    }
  }

  public AudioPlayer getByReference(String reference) {
    for (ReferencedPlayer rs : players) {
      if (rs.reference.equals(reference))
        return rs.player;
    }
    return null;
  }
} 



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class ReferencedPlayer {
  AudioPlayer player;
  String reference;

  ReferencedPlayer(String p_path, String p_reference) {
    player = minim.loadFile("/assets/audio"+p_path);
    reference = p_reference;
  }
}
ArrayList<String> creditroll;
int starttime = 0;
float yroll = 0;

public void credits() {
  if(starttime == 0) starttime = millis();
  if(creditroll==null) setup_creditroll();
  
  background(0);
  textSize(20);
  fill(255);
  
  int ypos = height;
  
  for(String cS : creditroll) {
    if(cS.length()>80) textSize(15);
    text(cS, width/2, ypos-yroll);
    textSize(20);
    ypos+=25;
  }
  
   yroll+=0.3f;
  if(millis()-starttime > 400 && (mousePressed || keyPressed)) {game.stage = -1; starttime=0; yroll = 0;}
}

public void setup_creditroll() {
  creditroll = new ArrayList<String>();
  creditroll.add("---------|||---------");
  creditroll.add("Credits");
  creditroll.add("       ");
  creditroll.add("Programming");
  creditroll.add("Game Logic - Rouven Grenz");
  creditroll.add("Networking - Rouven Grenz");
  creditroll.add("Graphical Interface - Rouven Grenz");
  creditroll.add("Sound System - Rouven Grenz");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("Testing");
  creditroll.add("Test coordination - Rouven Grenz");
  creditroll.add("Alpha Tester - Lukas Marquetant");
  creditroll.add("Alpha Tester - Oliver Arndt");
  creditroll.add("Tester - Fabian Brendli");
  creditroll.add("Tester - Dustin Evers");
  creditroll.add("Tester - Jan Sch\u00f6ppe");
  creditroll.add("Tester - Tim Seifert");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("Art");
  creditroll.add("Saloon background");
  creditroll.add("http://cdn.wccftech.com/wp-content/uploads/2015/04/Unreal-Engine-4-Saloon-1.png");
  creditroll.add("       ");
  creditroll.add("Wooden background");
  creditroll.add("http://www.holz-gmeiner.com/images/stories/Produkte/Eiche_Natur.jpg");
  creditroll.add("       ");
  creditroll.add("Disconnected background");
  creditroll.add("http://7-themes.com/data_images/out/74/7025025-disconnected.jpg");
  creditroll.add("       ");
  creditroll.add("Freddy Easter Egg");
  creditroll.add("https://i.ytimg.com/vi/Wy_GUZs0Jzc/maxresdefault.jpg");
  creditroll.add("       ");
  creditroll.add("Card assets");
  creditroll.add("https://images.gutefrage.net/media/fragen-antworten/bilder/5007241/0_original.jpg?v=1245315359000");
  creditroll.add("       ");
  creditroll.add("Card back");
  creditroll.add("http://siriusbow.com/resources/400_F_54264304_KnKhcQpyBWGjo9hMGcHYtyDakzXKaNVk.jpg");
  creditroll.add("       ");
  creditroll.add("Paste Icon");
  creditroll.add("https://www.iconfinder.com/icons/27862/download/png/256");
  creditroll.add("       ");
  creditroll.add("Server Freddy normal");
  creditroll.add("https://lh4.googleusercontent.com/-1DwXNc9iZUk/AAAAAAAAAAI/AAAAAAAAABE/aIFcYE8bl-M/photo.jpg");
  creditroll.add("       ");
  creditroll.add("Server Freddy spooky");
  creditroll.add("http://vignette4.wikia.nocookie.net/freddy-fazbears-pizza/images/6/61/FNaF4Mobile.jpg/revision/latest?cb=20150808001857");
  creditroll.add("       ");
  creditroll.add("Bomber easter egg");
  creditroll.add("https://s-media-cache-ak0.pinimg.com/736x/4b/76/b9/4b76b946f938093762311572f794e0c3.jpg");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("Music & Sounds");
  creditroll.add("Menu piano");
  creditroll.add("https://www.jamendo.com/album/116640/cinematic-western-wild-west-cowboy-soundtrack-instrumental-album");
  creditroll.add("       ");
  creditroll.add("Waiting state song");
  creditroll.add("http://freemusicarchive.org/music/Smurd/rORRET_tENALP/03_Saloon");
  creditroll.add("       ");
  creditroll.add("Credits song");
  creditroll.add("https://www.freesound.org/people/joshuaempyre/sounds/250856/");
  creditroll.add("       ");
  creditroll.add("Round startup sound");
  creditroll.add("https://www.freesound.org/people/plasterbrain/sounds/243020/");
  creditroll.add("       ");
  creditroll.add("Round reset sound");
  creditroll.add("https://www.freesound.org/people/Corsica_S/sounds/107546/");
  creditroll.add("       ");
  creditroll.add("Set card sound");
  creditroll.add("https://www.freesound.org/people/fins/sounds/171521/");
  creditroll.add("       ");
  creditroll.add("Your turn sound");
  creditroll.add("https://www.freesound.org/people/satrebor/sounds/113218/");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("You can also view the credits");
  creditroll.add("in the readme file.");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("--");
  creditroll.add("Istud, quod tu summum putas, gradus est.");
  creditroll.add("--");
  creditroll.add("       ");
  creditroll.add("MMXVI");
  creditroll.add("---------|||---------");
}




Network network;
Game game;
Client client;
Prescreen prescreen;
PlayAsRequest par;
AudioManager audio;
Minim minim;
String lastip = "";
String lastname = "";
ArrayList<ReferencedImage> images;




public void setup_vars() {
  setup_referencedImages();
  network = new Network();
  game = new Game(); 
  game.stage = -1;
  prescreen = new Prescreen();
  par = new PlayAsRequest();
}


public void setup_referencedImages() {  
    images = new ArrayList<ReferencedImage>();
    
    // Herz
    images.add(new ReferencedImage("/assets/cards/herz/7.jpg", "herz7"));
    images.add(new ReferencedImage("/assets/cards/herz/8.jpg", "herz8"));
    images.add(new ReferencedImage("/assets/cards/herz/9.jpg", "herz9"));
    images.add(new ReferencedImage("/assets/cards/herz/10.jpg", "herz10"));
    images.add(new ReferencedImage("/assets/cards/herz/bube.jpg", "herzbube"));
    images.add(new ReferencedImage("/assets/cards/herz/dame.jpg", "herzdame"));
    images.add(new ReferencedImage("/assets/cards/herz/k\u00f6nig.jpg", "herzk\u00f6nig"));
    images.add(new ReferencedImage("/assets/cards/herz/ass.jpg", "herzass"));
    
    // Karo
    images.add(new ReferencedImage("/assets/cards/karo/7.jpg", "karo7"));
    images.add(new ReferencedImage("/assets/cards/karo/8.jpg", "karo8"));
    images.add(new ReferencedImage("/assets/cards/karo/9.jpg", "karo9"));
    images.add(new ReferencedImage("/assets/cards/karo/10.jpg", "karo10"));
    images.add(new ReferencedImage("/assets/cards/karo/bube.jpg", "karobube"));
    images.add(new ReferencedImage("/assets/cards/karo/dame.jpg", "karodame"));
    images.add(new ReferencedImage("/assets/cards/karo/k\u00f6nig.jpg", "karok\u00f6nig"));
    images.add(new ReferencedImage("/assets/cards/karo/ass.jpg", "karoass"));
    
    // Kreuz
    images.add(new ReferencedImage("/assets/cards/kreuz/7.jpg", "kreuz7"));
    images.add(new ReferencedImage("/assets/cards/kreuz/8.jpg", "kreuz8"));
    images.add(new ReferencedImage("/assets/cards/kreuz/9.jpg", "kreuz9"));
    images.add(new ReferencedImage("/assets/cards/kreuz/10.jpg", "kreuz10"));
    images.add(new ReferencedImage("/assets/cards/kreuz/bube.jpg", "kreuzbube"));
    images.add(new ReferencedImage("/assets/cards/kreuz/dame.jpg", "kreuzdame"));
    images.add(new ReferencedImage("/assets/cards/kreuz/k\u00f6nig.jpg", "kreuzk\u00f6nig"));
    images.add(new ReferencedImage("/assets/cards/kreuz/ass.jpg", "kreuzass"));
    
     // Pik
    images.add(new ReferencedImage("/assets/cards/pik/7.jpg", "pik7"));
    images.add(new ReferencedImage("/assets/cards/pik/8.jpg", "pik8"));
    images.add(new ReferencedImage("/assets/cards/pik/9.jpg", "pik9"));
    images.add(new ReferencedImage("/assets/cards/pik/10.jpg", "pik10"));
    images.add(new ReferencedImage("/assets/cards/pik/bube.jpg", "pikbube"));
    images.add(new ReferencedImage("/assets/cards/pik/dame.jpg", "pikdame"));
    images.add(new ReferencedImage("/assets/cards/pik/k\u00f6nig.jpg", "pikk\u00f6nig"));
    images.add(new ReferencedImage("/assets/cards/pik/ass.jpg", "pikass")); 
    
    // Mutated
    images.add(new ReferencedImage("/assets/cards/mutated/7.jpg", "mutated7"));
    images.add(new ReferencedImage("/assets/cards/mutated/8.jpg", "mutated8"));
    images.add(new ReferencedImage("/assets/cards/mutated/9.jpg", "mutated9"));
    images.add(new ReferencedImage("/assets/cards/mutated/10.jpg", "mutated10"));
    images.add(new ReferencedImage("/assets/cards/mutated/bube.jpg", "mutatedbube"));
    images.add(new ReferencedImage("/assets/cards/mutated/dame.jpg", "mutateddame"));
    images.add(new ReferencedImage("/assets/cards/mutated/k\u00f6nig.jpg", "mutatedk\u00f6nig"));
    images.add(new ReferencedImage("/assets/cards/mutated/ass.jpg", "mutatedass")); 
      
     // Back
    images.add(new ReferencedImage("/assets/cards/back/black.jpg", "backblack"));
  }
class Network {
  String appendix = "";

  Network() {
  }


  public void push (String command, String tag1, String tag2) {
    client.write(command+"<"+tag1+"><"+tag2+">;");
  }


  public void pull() {

    if (!client.active() && game.connectionStatus && game.hadConnection) {
      game.connectionStatus = false; 
      game.board.load_background("disconnected.jpg");
    }
    
    else if(!client.active() && game.connectionStatus && !game.hadConnection) {
      game.connectionStatus = false; 
      game.board.load_background("disconnected.jpg"); //!!
      //game.dctime -= 5000; ?????????
    }


    if (client.available()!=0) {
      String incoming = appendix + client.readString();
      println("APPENDIX:", appendix);
      println("INCOMING:", incoming);
      appendix = "";
      ArrayList<String> msgs = new ArrayList<String>();
    
      String happen = "";

      for (int i=0; i < incoming.length(); i++) {
        if (incoming.charAt(i) == ';') {
          msgs.add(happen); 
          happen="";
        } else happen += incoming.charAt(i);
      }
      if(happen.length() > 0) {appendix += happen; println("adding", happen, "to", appendix);}
      decode(msgs);
    }
  }


  public void decode(ArrayList<String> msgs) {
    for (String s : msgs) {
      if(s.length()>4) get_tags(s, s.substring(0, 4));
    }
  }


  public void get_tags(String msg, String command) {
    String tag1 = "";
    String tag2 = "";
    int open1 = 0;
    int close1 = 0;

    for (int i=0; i<msg.length(); i++) {
      if (msg.charAt(i) == '<') open1 = i;
      else if (msg.charAt(i) == '>') close1 = i;

      if (open1 != 0 && close1 != 0) {
        tag1 = msg.substring(open1+1, close1);
        tag2 = msg.substring(close1+2, msg.length()-1);
        break;
      }
    }
    execute(command, tag1, tag2);
  }


  public void execute(String command, String tag1, String tag2) {
    if      (command.equals("+p"+game.playerid))                                  {game.board.sta_player.add(new Card(tag1, tag2, false)); /*println("Now we have", game.board.sta_player.size(), "cards");*/}
    else if (command.equals("+gst"))                                              {game.board.sta_game.add(new Card(tag1, tag2, true)); audio.play("sound", "set", true, false);}
    else if (command.equals("+msg"))                                              game.board.msgs.add(tag1+tag2);
    else if (command.equals("+npl") && game.playerid.equals("999"))               game.playerid = tag1; 
    else if (command.equals("-pst") && tag1.equals("all") && tag2.equals("all"))  game.board.sta_player = new ArrayList<Card>();
    else if (command.equals("-gst") && tag1.equals("all") && tag2.equals("all"))  game.board.sta_game = new ArrayList<Card>();
    else if (command.equals("+nsa"))                                              game.stage = PApplet.parseInt(tag2);
    else if (command.equals("+dsp") && tag1.equals(game.playerid))                {game.myTurn = true; game.firstTurn = true; par = new PlayAsRequest(); audio.play("sound", "turn", true, false);}
    else if (command.equals("+dsp") && !tag1.equals(game.playerid))               game.board.playingas = "null";
    else if (command.equals("+npt") && tag1.equals(game.playerid))                {game.myTurn = true; game.firstTurn = false; audio.play("sound", "turn", true, false);} 
    else if (command.equals("+paa"))                                              game.board.playingas = tag1;
    else if (command.equals("+tsc"))                                              {Card card = new Card(tag1, tag2, true); for(int i=0; i<game.board.sta_game.size(); i++) {if(game.board.sta_game.get(i).farbe.equals(tag1) && game.board.sta_game.get(i).id.equals(tag2)) {game.board.sta_game.remove(i); i=-1;}} card.hidden=false; game.board.sta_game.add(card);}
    else if (command.equals("+con"))                                              {game.board.playingas="null";}  
    else if (command.equals("+res") && tag1.equals("ALL") && tag2.equals("ALL"))  {println("here"); String id = game.playerid; game = new Game(); game.playerid = id; audio.play("sound", "reset", true, false);}
    else if (command.equals("+fre") && tag1.equals("+"))                          {game.board.load_background("fnaf.jpg");}
    else if (command.equals("+fre") && tag1.equals("-"))                          {game.board.load_background("board.jpg");}
    else if (command.equals("+npi") && tag1.equals(game.playerid))                {game.playerid = tag2;}
    else if (command.equals("+eot"))                                              {game.myTurn = false; game.firstTurn=false;}
    else if (command.equals("+sub"))                                              {game.board.subt = tag1;}
    else if (command.equals("+bom"))                                              {game.board.bombers.add(new Bomber());}
    else if (command.equals("+cen"))                                              {audio.play("sound", "cena", true, false);}
  }
}
class Prescreen {
  PImage pi_saloon;
  PImage pi_dame;
  PImage pi_paste;
  boolean mouseProcessed;
  Textbox serverip;
  Textbox playername;
  Button start;

  Prescreen() {
    pi_saloon = loadImage("/assets/etc/saloon.png");
    pi_dame = loadImage("/assets/cards/karo/dame.jpg");
    pi_paste = loadImage("/assets/etc/paste.png");

    pi_saloon.resize(width, height);
    pi_dame.resize(width/15, 0);
    pi_paste.resize(24, 0);
  
    serverip = new Textbox(209*width/1000, 568*height/600, width/10, height/40);
    playername = new Textbox(552*width/1000, 568*height/600, width/10, height/40);
    
    serverip.content = lastip;
    playername.content = lastname;
    mouseProcessed = false;
    
    start = new Button(801*width/1000, 569*height/600, 58*width/1000, 20*height/600, "Connect", false);
  }

  public void draw() {
    image(pi_saloon, 0, 0);
    image(pi_dame, width-pi_dame.width, height-pi_dame.height);
    image(pi_paste, width*311/1000, height*558/600);
    
    serverip.draw();
    playername.draw();
    start.draw();

    fill(0, 255, 0);
    textSize(15);
    text("Server-IP:", 68*width/1000, 566*height/600);
    text("Your Name:", 405*width/1000, 566*height/600);
    text("Credits", 967*width/1000, 493*height/600);
    textSize(11);
    
    text("v."+CLIENT_VERSION, 20, 5);
  }

  public void checkclick() {
    if (mousePressed && !mouseProcessed) {
      mouseProcessed = true;
      if(mouseX>(width*311/1000) && mouseX<(width*311/1000)+24 && mouseY>(height*558/600) && mouseY < (height*558/600)+24) {
       serverip.content += GClip.paste();
      } else if (mouseX > (width-prescreen.pi_dame.width) && mouseX < width && mouseY > height- prescreen.pi_dame.height && mouseY < height) {
        game.stage = 5;
      } else if (serverip.checkclick()) {
        serverip.active=true; 
        playername.active=false;
      } else if (playername.checkclick()) {
        playername.active=true; 
        serverip.active=false;
      }
    }
    else if(!mousePressed) mouseProcessed = false;
  }
}

public void pregame() {
  prescreen.checkclick();
  prescreen.draw();
  
  if(prescreen.playername.content.length()>0 && prescreen.serverip.content.length()>0) prescreen.start.state = true;
  else prescreen.start.state = false;
  
  if(mousePressed && prescreen.start.checkclick()) {
    connect();
  }
}

public void connect() {
     if(prescreen.serverip.content.equals("x")) {prescreen.serverip.content="127.0.0.1";}
     if(prescreen.playername.content.equals("x")) {prescreen.playername.content="Player"+hour()+minute()+second();}
     client = new Client(this, prescreen.serverip.content, 6878);
     game.stage++;
     println(prescreen.playername.content);
     lastname = prescreen.playername.content;
     lastip = prescreen.serverip.content;
}

public void keyPressed() {
  if(key=='+') audio.muteAmbient();
  else if(key=='#') audio.muteSound();
  else if(key=='-') audio.play("sound", "cena", true, false);
  
  if (game.stage==-1) {;
    
    if(key==TAB) {
      if(prescreen.playername.active) {prescreen.playername.active = false; prescreen.serverip.active=true;}
      else if(prescreen.serverip.active) {prescreen.serverip.active = false; prescreen.playername.active=true;}
      else prescreen.serverip.active = true;
    }
    
    if(key==RETURN || key==ENTER) {
      if(prescreen.start.state) connect();
    }
    
    if(key==TAB || key==ENTER || key==RETURN || key==ESC || key==DELETE || key==SHIFT || key==ALT || key==CODED) return;
    
    Textbox target;
    
    if (prescreen.serverip.active) {
      target = prescreen.serverip;
    } else if (prescreen.playername.active) {
      target = prescreen.playername;
    } else return;

    if (key==BACKSPACE && target != null) {
      if (target.content.length() > 0)
        target.content = target.content.substring(0, target.content.length()-1);
    } else if (target!=null) target.content += key;
  }
}
  public void settings() {  size(1000, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "liars_client" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
