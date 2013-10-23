//##################################################################
// Union overriding
//##################################################################


//----------------------
// Complete declarations
//----------------------

union U1 {int i;};

// Complete declaration overloading
void f_complete() {
  union U1 {char j;};         // Overides
  if (1) {
    union U1 {char i;int j;}; // Overides
  }
}

//------------------------
// Incomplete declarations
//------------------------

// Incomplete declaration overloading
void f_incomplete() {
  union U5;
  union U5 {char j;};  // Completes declaration
  if (1) {
    union U5;	        // Overrides
    union U5 {char i;char j;}; // Completes
    if (1) {
      union U5;       // Overrides
    }
  }
}

