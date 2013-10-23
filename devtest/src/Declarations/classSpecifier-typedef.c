//##################################################################
// typedef declaration
//##################################################################

// Simple scalar type
typedef int t1;

// Tag type
struct S {int i;};
typedef struct S ts;
union U {int i;};
typedef union U tu;
enum E {A, B, C, D};
typedef enum E te;

// with type qualifier
typedef const int tq1;
typedef volatile int tq2;
// No restrict allowed !?


// With more complex declarator
typedef unsigned short * t2;
typedef double * t4[10];
typedef void (*tpf1)(int);





