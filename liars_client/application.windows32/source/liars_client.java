import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 

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
static String CLIENT_VERSION = "0.9.3";
//

public void setup() {
  
  //fullScreen();
  rectMode(RADIUS);
  textAlign(CENTER, CENTER);
  setup_vars();
}


public void draw() {
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

        if (board.four_available && !board.four_available_kind.equals("dame") && board.remove_four.checkclick()) {
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

                    if (game.firstTurn) network.push("+paa", par.playas, "");
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
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Board {
  Button button_accept;
  Button button_lie;
  Button remove_four;
  ArrayList<Card> sta_player;
  ArrayList<Card> sta_game;

  ArrayList<String> msgs;
  String playingas = "none";
  PImage pi_board;
  boolean four_available = false;
  String four_available_kind = "";


  Board() {
    button_accept = new Button(8.25f*width/10, 5.55f*height/6, width/25, height/30, "Legen", true);
    button_lie = new Button(9.25f*width/10, 5.55f*height/6, width/25, height/30, "L\u00fcge", false);
    remove_four = new Button(550*width/1000-307*height/1200, 320*height/600-34*height/600, width/5, height/30, "Vier der gleichen Sorte ablegen", true);

    sta_player = new ArrayList<Card>();
    sta_game = new ArrayList<Card>();
    msgs = new ArrayList<String>();

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
    if (four_available && !four_available_kind.equals("dame")) {remove_four.text = "Vier der gleichen Sorte ablegen: " + four_available_kind.substring(0, 1).toUpperCase()+four_available_kind.substring(1, four_available_kind.length()); remove_four.draw();}

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
    while(msgs.size()>30) msgs.remove(0); 
    
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
      text("It's your turn!", 828, 517);
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
  }

  public PImage find_referencedImage(String reference) {
    for(ReferencedImage refim : images) {
      if(refim.reference.equals(reference)) return refim.image;
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
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Card {
  String farbe;
  String id;
  PImage kartenbild;
  PImage kartenbildB;
  int selectedcol;
  float lastx, lasty;
  boolean highlighted = false;
  boolean hidden;


  Card(String p_farbe, String p_id, boolean p_hidden) {
    farbe = p_farbe;
    id = p_id;
    hidden = p_hidden;
    kartenbild = getImage();
    selectedcol = color(0xff14FF00);
  }


  public PImage getImage() {
    String reference = farbe+id;
    PImage image = new PImage();
    image = game.board.find_referencedImage(reference).copy();
    kartenbildB = game.board.find_referencedImage("backblack");
    
    if(image!=null) {
      image.resize(width/10, 0);
      kartenbildB.resize(width/10, 0);
      return image;
    }
    
    else {
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

    if (highlighted) {
      stroke(selectedcol);
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
    
    for(Card card : cards) card.kartenbild.resize(card.kartenbild.width/2, 0);
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
    text(cS, width/2, ypos-yroll);
    ypos+=25;
  }
  
  if(yroll+75<height) yroll+=0.5f;
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
  creditroll.add("       ");
  creditroll.add("Testing");
  creditroll.add("Test coordination - Rouven Grenz");
  creditroll.add("Tester - Lukas Marquetant");
  creditroll.add("Tester - Oliver Arndt");
  creditroll.add("       ");
  creditroll.add("Additional");
  creditroll.add("For additional credit");
  creditroll.add("view the readme doc");
  creditroll.add("       ");
  creditroll.add("MMXVI");
  creditroll.add("---------|||---------");
}


Network network;
Game game;
Client client;
Prescreen prescreen;
PlayAsRequest par;
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
    else if (command.equals("+gst"))                                              game.board.sta_game.add(new Card(tag1, tag2, true));
    else if (command.equals("+msg"))                                              game.board.msgs.add(tag1+tag2);
    else if (command.equals("+npl") && game.playerid.equals("999"))               game.playerid = tag1; 
    else if (command.equals("-pst") && tag1.equals("all") && tag2.equals("all"))  game.board.sta_player = new ArrayList<Card>();
    else if (command.equals("-gst") && tag1.equals("all") && tag2.equals("all"))  game.board.sta_game = new ArrayList<Card>();
    else if (command.equals("+nsa")) game.stage = PApplet.parseInt(tag2);
    else if (command.equals("+dsp") && tag1.equals(game.playerid))                {game.myTurn = true; game.firstTurn = true; par = new PlayAsRequest();}
    else if (command.equals("+dsp") && !tag1.equals(game.playerid))               game.board.playingas = "null";
    else if (command.equals("+npt") && tag1.equals(game.playerid))                {game.myTurn = true; game.firstTurn = false;} 
    else if (command.equals("+paa"))                                              game.board.playingas = tag1;
    else if (command.equals("+tsc"))                                              {Card card = new Card(tag1, tag2, true); for(int i=0; i<game.board.sta_game.size(); i++) {if(game.board.sta_game.get(i).farbe.equals(tag1) && game.board.sta_game.get(i).id.equals(tag2)) {game.board.sta_game.remove(i); i=-1;}} card.hidden=false; game.board.sta_game.add(card);}
    else if (command.equals("+con"))                                              {game.board.playingas="null";}  
    else if (command.equals("+res") && tag1.equals("ALL") && tag2.equals("ALL"))  {String id = game.playerid; game = new Game(); game.playerid = id;}
    else if (command.equals("+fre") && tag1.equals("+"))                          {game.board.load_background("fnaf.jpg");}
    else if (command.equals("+fre") && tag1.equals("-"))                          {game.board.load_background("board.jpg");}
    else if (command.equals("+npi") && tag1.equals(game.playerid))                {game.playerid = tag2;}
    else if (command.equals("+eot"))                                              {game.myTurn = false; game.firstTurn=false;}
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
     client = new Client(this, prescreen.serverip.content, 6878);
     game.stage++;
     println(prescreen.playername.content);
     lastname = prescreen.playername.content;
     lastip = prescreen.serverip.content;
}

public void keyPressed() {
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
