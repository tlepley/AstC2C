//##################################################################
// Enum uncomplete declaration and completion
//##################################################################

enum E1 *s1;  // Uncomplete

enum E2;      // Uncomplete
enum E2 *s2;  // Reference to uncomplete
enum E2 {A2, B2, B3=7}; // Completes the declaration
enum E2 s2c;  // Reference to complete
