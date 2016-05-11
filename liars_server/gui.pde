class Server_GUI {
  Button giveturn;
  Button kick;
  Button reset;
  Button start;
  void draw() {
    // Basics
    background(0);
    stroke(255);
    line(545, 300, width, 300);
    line(545, 0, 545, height);

    // Freddy
    if (!freddy) image(pi_freddy, 555, 20);
    else image(pi_freddym, 546, 0);

    // Buttons
    textAlign(CENTER, CENTER);
    giveturn.draw();
    kick.draw();
    reset.draw();
    start.draw();
    textAlign(BASELINE, BASELINE);

    // Table
    fill(255);
    text("IP", 10, 20);
    text("ID", 100, 20);
    text("Alias", 190, 20);
    text("No. of cards", 280, 20);
    text("Done", 370, 20);
    text("Has turn", 450, 20);

    int yoffset = 24;
    if (yoffset*(players.size()+4)> 3*height/4) 
      yoffset = height / (players.size()+4);

    for (Player player : players) {
      player.entryY = 20+yoffset;
      text(player.client.ip(), 10, 20+yoffset);
      text(player.id, 100, 20+yoffset);
      text(player.alias, 190, 20+yoffset);
      text(player.cards.size(), 280, 20+yoffset);
      text(player.done, 370, 20+yoffset);
      text(int(player.theirturn), 450, 20+yoffset);

      if (player.entryselected) {
        noFill();
        stroke(#3CD63D);
        rect(5, player.entryY-10, 535, 10);
      }
      yoffset+=14;
    }

    // Subtitle
    if (STATE==0) text("Waiting", 10, height-10);
    else if (STATE==1) text("Playing", 10, height-10);
    else text("Done", 10, height-10);
    text(hour()+":"+minute()+":"+second()+"  -  "+Server.ip(), 100, height-10);
  }


  void checksubt() {
    String subt = "| ";
    for (Player player : players) {
      subt += player.alias + " ("+player.id+")" + " | ";
    }

    if (!subt.equals(lastsubt)) {
      server.write("+sub<"+subt+"><>;"); 
      println("Sending subt");
    }
    lastsubt = subt;
  }
}