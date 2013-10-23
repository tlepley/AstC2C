//##################################################################
// Structure uncomplete declaration and completion
//##################################################################

struct S1 *s1;  // Uncomplete

struct S2;      // Uncomplete
struct S2 *s2;  // Reference to uncomplete
struct S2 {int i; char c;};  // Completes the declaration
struct S2 s2c;  // Reference to complete


// Auto-reference
struct S3 { // incomplete here
  int i;
  struct S3 *next; // reference to incomplete
}; // complete here
