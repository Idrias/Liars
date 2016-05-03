
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


  void checkclick() {
    if (mousePressed) {
      if (!got_clicked) {
        got_clicked = true;
        
        if(board.button_sort.checkclick()) board.sort();
        
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

                    if (game.firstTurn) {network.push("+paa", par.playas, ""); game.firstTurn=false;}
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


  void stateCheck() {
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
    if(board.four_available) board.button_remove_four.state = true; else board.button_remove_four.state = false;
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

  ArrayList<String> msgs;
  String playingas = "none";
  PImage pi_board;
  boolean four_available = false;
  String four_available_kind = "";


  Board() {
    button_accept = new Button(8.25*width/10, 5.32*height/6, width/30, height/36, "Legen", true);
    button_lie = new Button(9.25*width/10, 5.32*height/6, width/30, height/36, "Lüge", false);
    
    button_sort = new Button(8.25*width/10, 5.73*height/6, width/30, height/36, "Sortieren", true);
    button_remove_four = new Button(9.25*width/10, 5.73*height/6, width/30, height/36, "4 Ablegen", false);

    sta_player = new ArrayList<Card>();
    sta_game = new ArrayList<Card>();
    msgs = new ArrayList<String>();

    load_background("board.jpg");
  }


  void load_background(String which) {
    pi_board = loadImage("/assets/board/"+which);
    pi_board.resize(width, height);
  }


  void draw() { 
    // Draw wooden background and lines
    image(pi_board, 0, 0);
    strokeWeight(5);
    line(0, 2*height/3, 3*width/4, 2*height/3);  // a
    line(3*width/4, 0, 3*width/4, height);  // b
    line(3*width/4, 2.5*height/3, width, 2.5*height/3);  // c
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
    spacing = (0.72*width-width/10) / numberOfCards;
    if (spacing>120) spacing = 120;
    xpos = 20;
    ypos = 0.72 * height;

    for (Card card : sta_player) {
      card.draw(xpos, ypos);
      xpos += spacing;
    }

    // -> sta_game
    numberOfCards = sta_game.size();
    spacing = (0.72*width-width/10) / numberOfCards;
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
    xpos = 0.765 * width;
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
      text("It's your turn!", 828*1000/width, 475*600/height);
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
      text("Connected. Waiting for cards from server!", 1*width/4, 2.2*height/3);
      textSize(11);
    }

    textAlign(CENTER, CENTER);
  }

  PImage find_referencedImage(String reference) {
    for(ReferencedImage refim : images) {
      if(refim.reference.equals(reference)) return refim.image;
    }
    return null;
  }
  
  void hasfour() {
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
      else if (card.id.equals("könig")) countkoenig++;
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
      four_available_kind="könig";
    } else if (countass==4) {
      four_available=true; 
      four_available_kind="ass";
    } else {
      four_available = false; 
      four_available_kind = "none";
    }
  }
  
  void sort() {
    ArrayList<Card> sta_sort = new ArrayList<Card>();
    sta_sort  = sta_player;
    sta_player = new ArrayList<Card>();

    for(float value = 7; value < 15; value+=0.1) {
      for(int i=0; i<sta_sort.size(); i++) {
        Card card = sta_sort.get(i);
        if(card.value <= value) {
          sta_player.add(card);
          sta_sort.remove(card);
          i=0;
        }
      }
    } 
    sta_sort = new ArrayList<Card>();
    for(Card card : sta_player) {sta_sort.add(card);}
  }
}



////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
class Card {
  String farbe;
  String id;
  PImage kartenbild;
  PImage kartenbildB;
  color selectedcol;
  color freshcol;
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
    selectedcol = color(#14FF00);
    freshcol = color(#FAD412);
    birthtime = millis();
  }

  float determineValue() {
    float value = 0;
    
    if(farbe.equals("herz")) value+=0.1;
    else if(farbe.equals("karo")) value+=0.2;
    else if(farbe.equals("kreuz")) value+=0.3;
    else if(farbe.equals("pik")) value+=0.4;
    
    if(id.equals("7")) value+=7;
    else if(id.equals("8")) value+=8;
    else if(id.equals("9")) value+=9;
    else if(id.equals("10")) value+=10;
    else if(id.equals("bube")) value+=11;
    else if(id.equals("dame")) value+=12;
    else if(id.equals("könig")) value+=13;
    else if(id.equals("ass")) value+=14;
    
    return value;
  }
  
  PImage getImage() {
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


  void draw(float xpos, float ypos) {
    if (!hidden) image(kartenbild, xpos, ypos);
    else image(kartenbildB, xpos, ypos);

    // So that checkclick() can actually know where this card is placed
    lastx = xpos;
    lasty = ypos;

    if (highlighted || (millis()-birthtime < 1500 && !farbe.equals("mutated") && !hidden)) {
      if(highlighted) stroke(selectedcol);
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


  boolean checkclick() {
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
  color col;
  String text;
  boolean state;
  color disabledcol;
  color activecol;


  Button(float p_xpos, float p_ypos, float p_radx, float p_rady, String p_text, boolean p_state) {
    xpos = p_xpos;
    ypos = p_ypos;
    radx = p_radx;
    rady = p_rady;
    text = p_text;
    state = p_state;

    disabledcol = color(#808393);
    activecol = color(#3CD63D);
    checkstate();
  }


  void checkstate() {
    if (state) col = activecol;
    else col = disabledcol;
  }


  boolean checkclick() {
    if (mouseX > xpos-radx && mouseX < xpos+radx) {
      if (mouseY > ypos - rady && mouseY < ypos+rady) {
        if (state) return true;
      }
    }
    return false;
  }


  void draw() {
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

  void draw() {
    if (active) fill(255);
    else fill(160);

    rect(xpos, ypos, radx, rady);

    fill(0);
    displaytext = content;
    if (content.length()>25) displaytext = content.substring(0, 25);
    text(displaytext, xpos, ypos);
  }

  boolean checkclick() {
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
    cards.add(new Card("mutated", "könig", false));
    cards.add(new Card("mutated", "ass", false));
    
    for(Card card : cards) card.kartenbild.resize(card.kartenbild.width/2, 0);
  }


  void draw() {
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

  void checkclick() {

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