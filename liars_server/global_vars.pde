import processing.net.*;

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


void reset_vars() {
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

void setup_vars() {
  setup_cards();
 
  players = new ArrayList<Player>();
  sta_game = new ArrayList<Server_Card>();
  lastcards = new ArrayList<Server_Card>();
  storedmsgs = new ArrayList<String>();
  toDisconnect = new ArrayList<String>();
  giveturn = new Button(width-240, height-75, 100, 25, "Give turn", false);
  kick = new Button(width-240, height-40, 100, 25, "Kick player", false);
}

 
void setup_cards() {
   all_cards = new ArrayList<Server_Card>();

     // karo
  all_cards.add(new Server_Card("karo", "7", false));
  all_cards.add(new Server_Card("karo", "8", false));
  all_cards.add(new Server_Card("karo", "9", false));
  all_cards.add(new Server_Card("karo", "10", false));
  all_cards.add(new Server_Card("karo", "bube", false));
  all_cards.add(new Server_Card("karo", "dame", false));
  all_cards.add(new Server_Card("karo", "könig", false));
  all_cards.add(new Server_Card("karo", "ass", false));
  
  // herz
  all_cards.add(new Server_Card("herz", "7", false));
  all_cards.add(new Server_Card("herz", "8", false));
  all_cards.add(new Server_Card("herz", "9", false));
  all_cards.add(new Server_Card("herz", "10", false));
  all_cards.add(new Server_Card("herz", "bube", false));
  all_cards.add(new Server_Card("herz", "dame", false));
  all_cards.add(new Server_Card("herz", "könig", false));
  all_cards.add(new Server_Card("herz", "ass", false));
  
  // pik
  all_cards.add(new Server_Card("pik", "7", false));
  all_cards.add(new Server_Card("pik", "8", false));
  all_cards.add(new Server_Card("pik", "9", false));
  all_cards.add(new Server_Card("pik", "10", false));
  all_cards.add(new Server_Card("pik", "bube", false));
  all_cards.add(new Server_Card("pik", "dame", false));
  all_cards.add(new Server_Card("pik", "könig", false));
  all_cards.add(new Server_Card("pik", "ass", false));
  
  // kreuz
  all_cards.add(new Server_Card("kreuz", "7", false));
  all_cards.add(new Server_Card("kreuz", "8", false));
  all_cards.add(new Server_Card("kreuz", "9", false));
  all_cards.add(new Server_Card("kreuz", "10", false));
  all_cards.add(new Server_Card("kreuz", "bube", false));
  all_cards.add(new Server_Card("kreuz", "dame", false));
  all_cards.add(new Server_Card("kreuz", "könig", false));
  all_cards.add(new Server_Card("kreuz", "ass", false));
 }