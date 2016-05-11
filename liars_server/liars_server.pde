static String SERVER_VERSION = "1.0.0";


void setup() {
  server = new Server(this, 6878);
  size(800, 400);
  setup_vars();
}


void draw() {
  environment.update();
}