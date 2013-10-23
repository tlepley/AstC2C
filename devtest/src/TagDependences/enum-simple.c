//##################################################################
// Extraction of enumerate variables
//##################################################################


// Global extraction
//------------------

// separated
enum E1 {A=1, B, C};
enum E1 e1;

// all in one
enum E2 {D=1, E, F} e2;




// Local extraction
//------------------

int f1() {
  static enum E1 e; // reference to global definition
}

int f3() {
  enum E3 {D=10, E, F};
  static enum E3 e;
  enum E3 e_which_stays;
}

int f2() {
  // all in one
 static enum E3 {D=10, E, F} e;
 enum E3 e_which_stays;
}

