//
static String CLIENT_VERSION = "1.0.0";
//

void setup() {
  size(1000, 600);
  rectMode(RADIUS);
  textAlign(CENTER, CENTER);
  minim = new Minim(this);
  audio = new AudioManager();
  setup_vars();
}


void draw() {
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

// todo: wenn client leavt während er dran ist, spiel soll weitergehen