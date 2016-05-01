ArrayList<String> creditroll;
int starttime = 0;
float yroll = 0;

void credits() {
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
  
  if(yroll+75<height) yroll+=0.5;
  if(millis()-starttime > 400 && (mousePressed || keyPressed)) {game.stage = -1; starttime=0; yroll = 0;}
}

void setup_creditroll() {
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