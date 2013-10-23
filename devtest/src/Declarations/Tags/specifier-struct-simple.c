//##################################################################
// Struct specifier
//
// Test simple struct tag declarations
//##################################################################

// Named complete stucture
struct S {int i; int j;};            // alone
struct S1 {int i; int j; long k;} s1; // variable
typedef struct S2 {char i;} s2;      // typedef
struct S3 {};		    	       // empty

// Unnamed complete stucture
struct {int i; int j;};           // useless
struct {int i; int j; int k;} u1; // variable
typedef struct {int i;} u2;      // typedef

// Uncomplete stucture
struct U *c;

// Reference to a stucture
struct S r1;
struct S2 r2;
