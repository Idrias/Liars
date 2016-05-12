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
    if(cS.length()>80) textSize(15);
    text(cS, width/2, ypos-yroll);
    textSize(20);
    ypos+=25;
  }
  
   yroll+=0.3;
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
  creditroll.add("Sound System - Rouven Grenz");
  creditroll.add("       ");
  creditroll.add("       ");
  creditroll.add("Testing");
  creditroll.add("Test coordination - Rouven Grenz");
  creditroll.add("Alpha Tester - Lukas Marquetant");
  creditroll.add("Alpha Tester - Oliver Arndt");
  creditroll.add("Tester - Fabian Brendli");
  creditroll.add("Tester - Dustin Evers");
  creditroll.add("Tester - Jan Schöppe");
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