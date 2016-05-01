import processing.net.*;

Network network;
Game game;
Client client;
Prescreen prescreen;
PlayAsRequest par;
String lastip = "";
String lastname = "";
ArrayList<ReferencedImage> images;

void setup_vars() {
  setup_referencedImages();
  network = new Network();
  game = new Game(); 
  game.stage = -1;
  prescreen = new Prescreen();
  par = new PlayAsRequest();

}


void setup_referencedImages() {  
    images = new ArrayList<ReferencedImage>();
    
    // Herz
    images.add(new ReferencedImage("/assets/cards/herz/7.jpg", "herz7"));
    images.add(new ReferencedImage("/assets/cards/herz/8.jpg", "herz8"));
    images.add(new ReferencedImage("/assets/cards/herz/9.jpg", "herz9"));
    images.add(new ReferencedImage("/assets/cards/herz/10.jpg", "herz10"));
    images.add(new ReferencedImage("/assets/cards/herz/bube.jpg", "herzbube"));
    images.add(new ReferencedImage("/assets/cards/herz/dame.jpg", "herzdame"));
    images.add(new ReferencedImage("/assets/cards/herz/könig.jpg", "herzkönig"));
    images.add(new ReferencedImage("/assets/cards/herz/ass.jpg", "herzass"));
    
    // Karo
    images.add(new ReferencedImage("/assets/cards/karo/7.jpg", "karo7"));
    images.add(new ReferencedImage("/assets/cards/karo/8.jpg", "karo8"));
    images.add(new ReferencedImage("/assets/cards/karo/9.jpg", "karo9"));
    images.add(new ReferencedImage("/assets/cards/karo/10.jpg", "karo10"));
    images.add(new ReferencedImage("/assets/cards/karo/bube.jpg", "karobube"));
    images.add(new ReferencedImage("/assets/cards/karo/dame.jpg", "karodame"));
    images.add(new ReferencedImage("/assets/cards/karo/könig.jpg", "karokönig"));
    images.add(new ReferencedImage("/assets/cards/karo/ass.jpg", "karoass"));
    
    // Kreuz
    images.add(new ReferencedImage("/assets/cards/kreuz/7.jpg", "kreuz7"));
    images.add(new ReferencedImage("/assets/cards/kreuz/8.jpg", "kreuz8"));
    images.add(new ReferencedImage("/assets/cards/kreuz/9.jpg", "kreuz9"));
    images.add(new ReferencedImage("/assets/cards/kreuz/10.jpg", "kreuz10"));
    images.add(new ReferencedImage("/assets/cards/kreuz/bube.jpg", "kreuzbube"));
    images.add(new ReferencedImage("/assets/cards/kreuz/dame.jpg", "kreuzdame"));
    images.add(new ReferencedImage("/assets/cards/kreuz/könig.jpg", "kreuzkönig"));
    images.add(new ReferencedImage("/assets/cards/kreuz/ass.jpg", "kreuzass"));
    
     // Pik
    images.add(new ReferencedImage("/assets/cards/pik/7.jpg", "pik7"));
    images.add(new ReferencedImage("/assets/cards/pik/8.jpg", "pik8"));
    images.add(new ReferencedImage("/assets/cards/pik/9.jpg", "pik9"));
    images.add(new ReferencedImage("/assets/cards/pik/10.jpg", "pik10"));
    images.add(new ReferencedImage("/assets/cards/pik/bube.jpg", "pikbube"));
    images.add(new ReferencedImage("/assets/cards/pik/dame.jpg", "pikdame"));
    images.add(new ReferencedImage("/assets/cards/pik/könig.jpg", "pikkönig"));
    images.add(new ReferencedImage("/assets/cards/pik/ass.jpg", "pikass")); 
    
    // Mutated
    images.add(new ReferencedImage("/assets/cards/mutated/7.jpg", "mutated7"));
    images.add(new ReferencedImage("/assets/cards/mutated/8.jpg", "mutated8"));
    images.add(new ReferencedImage("/assets/cards/mutated/9.jpg", "mutated9"));
    images.add(new ReferencedImage("/assets/cards/mutated/10.jpg", "mutated10"));
    images.add(new ReferencedImage("/assets/cards/mutated/bube.jpg", "mutatedbube"));
    images.add(new ReferencedImage("/assets/cards/mutated/dame.jpg", "mutateddame"));
    images.add(new ReferencedImage("/assets/cards/mutated/könig.jpg", "mutatedkönig"));
    images.add(new ReferencedImage("/assets/cards/mutated/ass.jpg", "mutatedass")); 
      
     // Back
    images.add(new ReferencedImage("/assets/cards/back/black.jpg", "backblack"));
  }