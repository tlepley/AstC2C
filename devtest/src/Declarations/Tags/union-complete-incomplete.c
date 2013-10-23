//##################################################################
// Union uncomplete declaration and completion
//##################################################################

union U1 *u1;  // Uncomplete

union U2;      // Uncomplete
union U2 *u2;  // Reference to uncomplete
union U2 {int i; char c;};  // Completes the declaration
union U2 u2c;  // Reference to complete


// Auto-reference
struct U3 { // incomplete here
  int i;
  struct U3 *p; // reference to incomplete
}; // complete here
