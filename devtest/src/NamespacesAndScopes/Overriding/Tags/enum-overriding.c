//##################################################################
// Enum overriding
//##################################################################


//----------------------
// Complete declarations
//----------------------

enum S1 {A, B};

// Complete declaration overloading
void f_complete() {
  enum S1 {A=3, C};          // Overides
  if (1) {
    enum S1 {A=10, B, C}; // Overides
  }
}

//------------------------
// Incomplete declarations
//------------------------

// Incomplete declaration overloading
void f_incomplete() {
  enum S5;
  enum S5 {X, Y} ;  // Completes declaration
  if (1) {
    enum S5;	        // Overrides
    enum S5 {X=4, Y, Z}; // Completes
    if (1) {
      enum S5;       // Overrides
    }
  }
}
