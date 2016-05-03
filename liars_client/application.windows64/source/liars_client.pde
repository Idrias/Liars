//
static String CLIENT_VERSION = "0.9.4";
//

void setup() {
  size(1000, 600);
  //fullScreen();
  rectMode(RADIUS);
  textAlign(CENTER, CENTER);
  setup_vars();
}


void draw() {
  switch(game.stage) {
    case -1:     pregame(); break;
    case  5:     credits(); break;
    
    default:     network.pull();
                 game.stateCheck();
                 game.checkclick();
                 game.board.draw();
                 break;
  } 
  
  text(mouseX + " " + mouseY, mouseX+50, mouseY+10);
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