//
// Test 'this' management with prototypes
//

// Stadard prototype
int g(int i);

// Prototype with void as unique parameter
int h(void);

// Prototypes compatibles with f
int f();
int f(int);

int f(int i) {
  return(i);
}


