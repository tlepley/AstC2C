// Tags (struct, union and enum) are in a specific name space, so that no collision
// occurs with other type
//

// Define some symbols in the global namespace
//-------------------------------------------

// Variable
int A;

// typedef
typedef long B;

// Function prototype
void C();

// Function definition
int D(int i) {
  return(i);
}

// Enum field
enum {E, F};


// Define tags with same name
//---------------------------

// Type tags
struct A {int i;};
enum B {E1, E2, E3};
union C{int i;char c;};
struct D {int i;};
enum E {F1, F2};
union F {int i;char c;};

