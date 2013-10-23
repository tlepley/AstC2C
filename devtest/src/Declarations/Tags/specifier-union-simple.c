//##################################################################
// Union specifier
//
// Test simple union tag declarations
//##################################################################

// Named complete union
union S {int i; int j;};            // alone
union S1 {int i; int j; int k;} s1; // variable
typedef union S2 {int i;} s2;      // typedef

// Unnamed complete union
union {int i; int j;};           // alone
union {int i; int j; int k;} u1; // variable
typedef union {int i;} u2;      // typedef

// Uncomplete union
union U *c;

// Reference to a union
union S r1;
union S2 r2;
