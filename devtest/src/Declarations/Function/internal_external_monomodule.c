//
// Test distinction between internal and external functions
// (this should only be put to internal functins of the component)
//

// internal
int f(int);

// external
extern int g(int, int);


int main() {
  printf("Hello\n");
  return(f(1)+g(2,4));
}

int f(int i) {
  return(1);
}
