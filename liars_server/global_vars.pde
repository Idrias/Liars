import processing.net.*;






Server server;
Player lastplayer;

PImage pi_freddy;
PImage pi_freddym;
int STATE = 0;
boolean gameStarted = false;
boolean newPlayerFlag = false;
boolean lied = false;
boolean freddy = false;


String lastsubt = "";
String whoseturn = "-1";
String playingas = "none";
String winner = "nowinner";
String appendix = "";
int gamestarttime = 0;
int continueschedule = 0;


void reset_vars() {
  for (Player player : players) {
    player.cards = new ArrayList<Server_Card>(); 
    player.theirturn=false;
  }
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

  pi_freddy = loadImage("/assets/background/freddy.jpg");
  pi_freddy.resize(width-530, 0);
  pi_freddym = loadImage("/assets/background/freddym.jpg");
  pi_freddym.resize(width-530, 0);

  players = new ArrayList<Player>();
  sta_game = new ArrayList<Server_Card>();
  lastcards = new ArrayList<Server_Card>();
  storedmsgs = new ArrayList<String>();
  toDisconnect = new ArrayList<String>();
  giveturn = new Button(width-240, height-75, 100, 25, "Give turn", false);
  kick = new Button(width-240, height-40, 100, 25, "Kick player", false);
  reset = new Button(width-120, height-75, 100, 25, "Reset", true);
  start = new Button(width-120, height-40, 100, 25, "Start", false);
}