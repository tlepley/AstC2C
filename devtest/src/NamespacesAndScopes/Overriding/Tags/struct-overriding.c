//##################################################################
// Structure overriding
//##################################################################


//----------------------
// Complete declarations
//----------------------

struct S1 {int i;};

// Complete declaration overloading
void f_complete() {
  struct S1 {char j;};          // Overides
  if (1) {
    struct S1 {char i;char j;}; // Overides
  }
}

//------------------------
// Incomplete declarations
//------------------------

// Incomplete declaration overloading
void f_incomplete() {
  struct S5;
  struct S5 {char j;};  // Completes declaration
  if (1) {
    struct S5;	        // Overrides
    struct S5 {char i;char j;}; // Completes
    if (1) {
      struct S5;       // Overrides
    }
  }
}

