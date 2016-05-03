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

public class liars_server extends PApplet {

static String SERVER_VERSION = "0.9.4";

// TODO IMPLEMENT "DONE" as player var!
// Server crasht wenn nachfolgende ID nicht mehr da ist |FIXED!
// First player skip: nachfolgender kann nicht als auslegen |Workaround
// recommendation: sort cards  

public void setup() {
  server = new Server(this, 6878);
  
  setup_vars();
}


public void draw() {
  for(String name : toDisconnect) {
    for(int i=0; i<players.size(); i++) {
      if(name.equals(players.get(i).id)) {server.disconnect(players.get(i).client); toDisconnect=new ArrayList<String>();}
    }
  }
  
  checksubt();
  checkMouse();
  drawinfo();
  if        (STATE == 0)   waitstate();
  else if   (STATE == 1)   play();
}


public void checkMouse() {
  if(mousePressed && !gotMousePress) {
    gotMousePress = true;
    
    
     boolean kickflag = false;
     boolean maketurnflag = false;
     
     if(kick.checkclick()) {kickflag=true;}
     if(giveturn.checkclick()) {maketurnflag=true;}
     
     for (Player player : players) {
       println("checking", player.alias);
       if(maketurnflag && player.entryselected) {
         player.theirturn=true;
         server.write("+eot<><>;");
         server.write("+msg<"+player.alias+" ist jetzt am Zug!><>;");
         server.write("+dsp<"+player.id+"><>;");
         println("ACHTUNG SERVER ADMIN HAT SPIEL MANUPULIERT");
       }
       else if(maketurnflag && !player.entryselected) {
         player.theirturn = false;
       }
       
       else if(kickflag && player.entryselected) {
         println("Disconnecting", player.alias);
         toDisconnect.add(player.id);
       }
     } 
     
     
    for(Player player : players) {        
      if(mouseX<540 && mouseY > player.entryY-10 && mouseY < player.entryY) {
        println(player.alias, "got clicked");
        player.entryselected = !player.entryselected;
        for(Player playeralt : players) {
          if(!playeralt.id.equals(player.id))
            playeralt.entryselected = false;
        }
      }
    }
    
     
    for(Player testplayer : players) {
      if(testplayer.entryselected) {
        kick.state = true;
        giveturn.state = true;
        return;
      }
      kick.state = false;
      giveturn.state = false;
    }
  }
  
  else if(!mousePressed) gotMousePress = false;
}


public void keyReleased() {
  if(key=='f') {
    freddy = false;
    server.write("+fre<-><>;");
  }
}

public void keyPressed() {
  if (key=='p') {
    STATE=1;
  }
  
  else if(key=='r') {
    STATE = 0;
    reset_vars();
  }
  
  else if(key=='f' && !freddy) {
    freddy = true;
    server.write("+fre<+><>;");
  }
}

public void checksubt() {
  String subt = "| ";
  for(Player player : players) {
    subt += player.alias + " ("+player.id+")" + " | "; 
  }
  
  if(!subt.equals(lastsubt)) {server.write("+sub<"+subt+"><>;"); println("Sending subt");}
  lastsubt = subt;
}

public void drawinfo() {
  background(0);
  stroke(255);
  line(545, 300, width, 300);
  line(545, 0, 545, height);
  
  giveturn.draw();
  kick.draw();
  fill(255);
  text("IP", 10, 20);
  text("ID", 100, 20);
  text("Alias", 190, 20);
  text("No. of cards", 280, 20);
  text("Done", 370, 20);
  text("Has turn", 450, 20);
  int yoffset = 24;
  if(yoffset*(players.size()+4)> 3*height/4) yoffset = height / (players.size()+4);
  
  for(Player player : players) {
    player.entryY = 20+yoffset;
    text(player.client.ip(), 10, 20+yoffset);
    text(player.id, 100, 20+yoffset);
    text(player.alias, 190, 20+yoffset);
    text(player.cards.size(), 280, 20+yoffset);
    text(player.done, 370, 20+yoffset);
    text(PApplet.parseInt(player.theirturn), 450, 20+yoffset);
    
    if(player.entryselected) {
      noFill();
      stroke(0xff3CD63D);
      rect(5, player.entryY-10, 535, 10);
    }
    
    yoffset+=14;
  }
  
   if(STATE==0) text("Waiting", 10, height-10);
   else if(STATE==1) text("Playing", 10, height-10);
   else text("Done", 10, height-10);
}

public void getplayerturn() {
  for(Player player : players) {
    if(player.theirturn) println("PLAYER", player.id, "HAS HIS TURN");
  }
}
class Player {
  Client client;
  String id;
  int done;
  int entryY = 0;
  String alias = "none";  
  ArrayList<Server_Card> cards;
  boolean theirturn = false;
  boolean entryselected = false;
  
  Player(Client p_client, String p_id) {
    client = p_client;
    id = p_id;
    done = 0;
    cards = new ArrayList<Server_Card>();
  }
}


class Server_Card {
  String farbe;
  String id;
  boolean hidden;

  Server_Card(String p_farbe, String p_id, boolean p_hidden) {
    farbe = p_farbe;
    id = p_id;
    hidden = p_hidden;
  }
}




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
    text(text, xpos+25, ypos+rady/2+5);
  }
}
public void serverEvent(Server theServer, Client theClient) {
  if(STATE==0) {
    lastsubt = "";
    int playerid = 1;
  
    for (int i=0; i<players.size(); i++) {
      if ((playerid<10 && players.get(i).id.equals("0" + playerid)) || (playerid>=10 && players.get(i).id.equals(""+playerid))) {
        playerid++; 
        i=-1; // CHANGE!
      }
    }
    
    String sid = "";
    if(playerid>=10) sid = ""+playerid;
    else sid = "0"+playerid;
    
    players.add(new Player(theClient, sid));
    println("WE HAVE PLAYER " + sid);
    server.write("+npl<"+sid+"><>;");
  }
  
  else server.disconnect(theClient);
}


public void disconnectEvent(Client theClient) {
  lastsubt = "";
   for(int i = 0; i<players.size(); i++) {
     Player player = players.get(i);
     if(player.client == theClient) {
       players.remove(player);
       storedmsgs.add("+msg<"+players.get(i).alias+">< left the server!>;");
     
       for(Server_Card card : players.get(i).cards) {
         sta_game.add(card);
         storedmsgs.add("+gst<"+(card.farbe)+"><"+(card.id)+">;");
       } 
       
       
        int awaitednum = 1;
        for(Player player2 : players) {
          if(PApplet.parseInt(player2.id) != awaitednum) {
        String newid = ""+awaitednum;
        if(awaitednum<10) newid = "0"+newid;
        server.write("+npi<"+player2.id+"><"+newid+">;");
        player2.id = newid;
      }
      awaitednum++;
    }
     }
   }
}
public void waitstate() {
  gameStarted = false;
  if (frameCount % 30 == 0) server.write("+nsa<stage><2>;");
  recv();
  

}

public void schedule() {
  
  if (continueschedule!=0 && millis() > continueschedule) {
    continueschedule=0;
    getplayerturn();
    server.write("+con<><>;");
    server.write("-gst<all><all>;");
    println("GAFHAF"); //<>//
    for (Player player : players) {
      println(player.id, "check");
      if (player.theirturn) { //!!!!//
        if (lied) {
          println("THEY LIED");
          server.write("+msg<"+lastplayer.alias+" hat gelogen!><>;");
          server.write("+dsp<"+player.id+"><>;"); println("NEXT TURN: " + player.id); server.write("+msg< >< >;"); server.write("+msg<"+player.alias+">< ist jetzt am Zug!>;");
          for(Server_Card card : sta_game) {lastplayer.cards.add(card); server.write("+p"+lastplayer.id+"<"+card.farbe+"><"+card.id+">;");}
        }
        
        else {
          println("THEY DID NOT LIE");
          server.write("+msg<"+lastplayer.alias+" hat die Wahrheit gesagt!><>;");
          for(Server_Card card : sta_game) {player.cards.add(card); server.write("+p"+player.id+"<"+card.farbe+"><"+card.id+">;");}
          player.theirturn=false;
          String  newPlayerS = "";
          int lastPlayer = PApplet.parseInt(player.id);
          int donecount = 0;
          do {
            int newPlayer = lastPlayer+1;
            if (newPlayer > players.size()) {
              newPlayer = 1;
            } 
            
              newPlayerS = ""+newPlayer;
              if (newPlayer<10) newPlayerS = "0" + newPlayerS;
            
            lastPlayer = newPlayer;
            donecount++;
            if(donecount>players.size()) {
              server.write("+msg<Alle Spieler sind fertig!><>;");
              break;
            }
            println("HALLOOOOO:", newPlayerS);
          } while(getById(newPlayerS).cards.size() == 0);
          
          getById(newPlayerS).theirturn = true;
          server.write("+msg< >< >;");
          server.write("+msg<"+getById(newPlayerS).alias+" ist jetzt am Zug!><>;");
          server.write("+dsp<"+newPlayerS+"><>;");
          // THEIR TURN!!!!! MAKE IIT
          println("NEXT TURN: " + newPlayerS);
          //server.write("+msg<"+null+"><>");
          //server.write("+msg<"+null+"><>");
        }
        break; ///!
      } else println("not their turn:", player.id);

      }
        
        
        println("resetting");
        lied = false;
        sta_game = new ArrayList<Server_Card>();
        lastplayer = null;
        lastcards = new ArrayList<Server_Card>();
    }
  }


public void play() {
  schedule();
  for (String msg : storedmsgs) 
    server.write(msg);
  storedmsgs = new ArrayList<String>();
  recv();
  
  if(frameCount%240 == 0 && players.size() > 1) {
    for(Player player : players) {
      int damencount = 0;
      for(Server_Card card : player.cards) {
        if(card.id.equals("dame")) damencount++;
      }
      if(damencount==4 && players.size()>0) {
        STATE = 0;
        reset_vars();
        server.write("+msg<Das Spiel wurde automatisch beendet:><>;");
        server.write("+msg<"+player.alias+">< hatte vier Damen!>;");
        server.write("+msg< ><>;");
        return;
      }
    }
  }

  if (!gameStarted) {
    gameStarted = true;
    server.write("+nsa<stage><3>;");
    give_cards();

    Player startplayer = getById(findCardOwner("herz", "7"));
    if (startplayer==null) startplayer = getById(findCardOwner("karo", "7"));
    if (startplayer==null) startplayer = getById(findCardOwner("kreuz", "7"));
    if (startplayer==null) startplayer = getById(findCardOwner("pik", "7"));
    if (startplayer==null) startplayer = players.get(PApplet.parseInt(random(players.size())));
    println(startplayer.id, "ist Startspieler");
    startplayer.theirturn=true;
    server.write("+dsp<"+startplayer.id+"><>;");
    server.write("+msg< ><>;");   
    server.write("+msg<---|||---><>;");
    server.write("+msg<Spielstart><>;");
    server.write("+msg<"+startplayer.alias+">< beginnt!>;");
    server.write("+msg< ><>;");
  }


}


ArrayList<Server_Card> all_cards;
ArrayList<Player> players;
ArrayList<Server_Card> sta_game;
ArrayList<Server_Card> lastcards;
ArrayList<String> storedmsgs; 
ArrayList<String> toDisconnect;

Button giveturn;
Button kick;
Server server;
Player lastplayer;

int STATE = 0;
boolean gameStarted = false;
boolean newPlayerFlag = false;
boolean lied = false;
boolean freddy = false;
boolean gotMousePress = false;

String lastsubt = "";
String whoseturn = "-1";
String playingas = "none";
String winner = "nowinner";
String appendix = "";
int gamestarttime = 0;
int continueschedule = 0;


public void reset_vars() {
 for(Player player : players) {player.cards = new ArrayList<Server_Card>(); player.theirturn=false;}
 sta_game = new ArrayList<Server_Card>();
 lastcards = new ArrayList<Server_Card>();
 storedmsgs = new ArrayList<String>();
 toDisconnect = new ArrayList<String>();
 lastplayer = null;
 gameStarted = false;
 newPlayerFlag = false;
 lied = false;
 whoseturn = "-1";
 playingas = "none";
 winner = "nowinner";
 appendix = "";
 gamestarttime = 0;
 continueschedule = 0;
 server.write("+res<ALL><ALL>;");
}

public void setup_vars() {
  setup_cards();
 
  players = new ArrayList<Player>();
  sta_game = new ArrayList<Server_Card>();
  lastcards = new ArrayList<Server_Card>();
  storedmsgs = new ArrayList<String>();
  toDisconnect = new ArrayList<String>();
  giveturn = new Button(width-240, height-75, 100, 25, "Give turn", false);
  kick = new Button(width-240, height-40, 100, 25, "Kick player", false);
}

 
public void setup_cards() {
   all_cards = new ArrayList<Server_Card>();

     // karo
  all_cards.add(new Server_Card("karo", "7", false));
  all_cards.add(new Server_Card("karo", "8", false));
  all_cards.add(new Server_Card("karo", "9", false));
  all_cards.add(new Server_Card("karo", "10", false));
  all_cards.add(new Server_Card("karo", "bube", false));
  all_cards.add(new Server_Card("karo", "dame", false));
  all_cards.add(new Server_Card("karo", "k\u00f6nig", false));
  all_cards.add(new Server_Card("karo", "ass", false));
  
  // herz
  all_cards.add(new Server_Card("herz", "7", false));
  all_cards.add(new Server_Card("herz", "8", false));
  all_cards.add(new Server_Card("herz", "9", false));
  all_cards.add(new Server_Card("herz", "10", false));
  all_cards.add(new Server_Card("herz", "bube", false));
  all_cards.add(new Server_Card("herz", "dame", false));
  all_cards.add(new Server_Card("herz", "k\u00f6nig", false));
  all_cards.add(new Server_Card("herz", "ass", false));
  
  // pik
  all_cards.add(new Server_Card("pik", "7", false));
  all_cards.add(new Server_Card("pik", "8", false));
  all_cards.add(new Server_Card("pik", "9", false));
  all_cards.add(new Server_Card("pik", "10", false));
  all_cards.add(new Server_Card("pik", "bube", false));
  all_cards.add(new Server_Card("pik", "dame", false));
  all_cards.add(new Server_Card("pik", "k\u00f6nig", false));
  all_cards.add(new Server_Card("pik", "ass", false));
  
  // kreuz
  all_cards.add(new Server_Card("kreuz", "7", false));
  all_cards.add(new Server_Card("kreuz", "8", false));
  all_cards.add(new Server_Card("kreuz", "9", false));
  all_cards.add(new Server_Card("kreuz", "10", false));
  all_cards.add(new Server_Card("kreuz", "bube", false));
  all_cards.add(new Server_Card("kreuz", "dame", false));
  all_cards.add(new Server_Card("kreuz", "k\u00f6nig", false));
  all_cards.add(new Server_Card("kreuz", "ass", false));
 }
public void give_cards() {
  ArrayList<Server_Card> cards = new ArrayList<Server_Card>();
  for (Server_Card card : all_cards) cards.add(card); // Gleichsetzen nicht m\u00f6glich da sonst reference!

  while (cards.size() / players.size() >= 1) {
    for (Player player : players) {
      int givenCard = PApplet.parseInt(random(cards.size()));
      println(givenCard);
      println(cards.size());
      player.cards.add(cards.get(givenCard));
      server.write("+p"+player.id+"<"+cards.get(givenCard).farbe+"><"+cards.get(givenCard).id+">;");
      cards.remove(givenCard);
    }
  }

  for (Server_Card card : cards) {
    sta_game.add(card);
    println(card.farbe, card.id);
    server.write("+gst<"+card.farbe+"><"+card.id+">;");
  }
}


public String findCardOwner(String farbe, String id) {
  for (Player player : players) {
    for (Server_Card card : player.cards) {
      if (card.farbe == farbe && card.id== id)
        return player.id;
    }
  }

  for (Server_Card card : sta_game) {
    if (card.farbe == farbe && card.id== id)
      return "GAMESTACK";
  }

  return "ERROR: NOT FOUND";
}



public Player getById(String id) {
  for (Player player : players) {
    if (player.id.equals(id)) return player;
  }

  Player Gustaf = null;
  return Gustaf;
}

public void recv() {
  Client client;

  do {
    client = server.available();
    
    
    if (client != null) {
      String incoming = client.readString();
      ArrayList<String> msgs = new ArrayList<String>();
      
      String happen = "";

      for (int i=0; i < incoming.length(); i++) {
        if (incoming.charAt(i) == ';') {
          msgs.add(happen); 
          println(happen);
          happen="";
        } else happen += incoming.charAt(i);
      }
      if(happen.length()>0) {
        // appendix!! n
      }
      
      
      decode(msgs);
    }
  } while (client != null);
}



public void decode(ArrayList<String> msgs) {
  for (String s : msgs) {
    println(s);
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

    if (open1 > 0 && close1 > 0) { //?!
      tag1 = msg.substring(open1+1, close1);
      tag2 = msg.substring(close1+2, msg.length()-1);
      break;
    }
  }
  execute(command, tag1, tag2);
}


public void execute(String command, String tag1, String tag2) {
  if (command.equals("+gst")) {
    sta_game.add(new Server_Card(tag1, tag2, true));
    server.write(command+"<"+tag1+"><"+tag2+">;");
 
    for(Player player : players) {
      for(Server_Card card : player.cards) {
        if(card.farbe.equals(tag1) && card.id.equals(tag2)) {
          if(lastplayer == player) lastcards.add(card);
          else {lastplayer=player; lastcards=new ArrayList<Server_Card>(); lastcards.add(card);}
        }
      }
    }
    for(int i=0; i<lastplayer.cards.size(); i++) {
      if(lastplayer.cards.get(i).farbe.equals(tag1) && lastplayer.cards.get(i).id.equals(tag2)) {
        lastplayer.cards.remove(i);
        i=-1;
      } 
    }
  }
  
  else if(command.equals("+npn")) {
        Player player = getById(tag1);
        
        if(player!=null) {
          player.alias=tag2;
          if(player.alias.length()>10) player.alias = player.alias.substring(0,10);
          println(tag1, "is now", tag2);
          server.write("+msg<"+tag2+">< joined the server!>;");
        }
  }
  
  
  else if(command.equals("+eot")) {
    server.write("+msg<"+getById(tag1).alias+" hat "+lastcards.size()+">< Karten gelegt.;");
    server.write("+msg<"+getById(tag1).alias+" hat noch "+getById(tag1).cards.size()+" Karten!><>;");
    getById(tag1).theirturn = false;
    int lastPlayer = PApplet.parseInt(tag1);
    int newPlayer = lastPlayer+1;
    if(newPlayer > players.size()) {newPlayer = 1;}
    
    String newPlayerS = ""+newPlayer;
    if(newPlayer<10) newPlayerS = "0" + newPlayerS;
    getById(newPlayerS).theirturn = true;
    
    int numfinished = 0;
    while(getById(newPlayerS).cards.size()==0) {
      getById(newPlayerS).theirturn=false;
        server.write("+msg<><"+getById(newPlayerS).alias+", ist schon fertig!>;");
        if(winner.equals("nowinner")) {winner = "a winner"; server.write("+msg<><"+getById(newPlayerS).alias+", HAT GEWONNEN!>;");}
        lastPlayer = PApplet.parseInt(newPlayer);
        newPlayer = lastPlayer+1;
        if(newPlayer > players.size()) {newPlayer = 1;}
        newPlayerS = ""+newPlayer;
        if(newPlayer<10) newPlayerS = "0" + newPlayerS;
        getById(newPlayerS).theirturn = true;
      
      numfinished++;
      if(numfinished>=players.size()) {
        server.write("+msg<Alle sind schon fertig!><>;");
        STATE++;
        return;
      }
      
    }
    server.write("+msg<"+getById(newPlayerS).alias+" ist jetzt am Zug!><>;");
    server.write("+npt<"+newPlayerS+"><>;");
    getById(newPlayerS).theirturn = true;
    println("NEXT TURN: " + newPlayerS);
    

    server.write("+msg< >< >;");
  }
  
  else if(command.equals("+paa")) {playingas = tag1; server.write("+paa<"+tag1+"><>;");}
  
  else if(command.equals("+lam")) {
    for (Player player : players) {if(player.theirturn) server.write("+msg<"+player.alias+">< glaubt: L\u00fcge!>;");}
    for(Server_Card card : lastcards) {
      server.write("+tsc<"+card.farbe+"><"+card.id+">;");
      if(!card.id.equals(playingas)) {lied = true; server.write("+pll<"+tag1+"><>;");}
      
      continueschedule = millis()+2500;
  }
    
  }
  
  else if (command.equals("-dl4")) {
    String msgstring = "";
    for(Player player : players) {
      for(int i = 0; i<player.cards.size(); i++) { // !!!!!
        if(player.cards.get(i).id.equals(tag1)) {
          player.cards.remove(player.cards.get(i));
          println("removed a " + tag1);
          msgstring = player.alias;
          i=-1;
        }
      }
    }
    println("+msg<"+msgstring+" hat alle ><"+tag1.toUpperCase()+" vom Spiel entfernt.>;");
    server.write("+msg<"+msgstring+" hat alle ><"+tag1.toUpperCase()+" vom Spiel entfernt.>;");
  }
}
  public void settings() {  size(800, 400); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "liars_server" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
