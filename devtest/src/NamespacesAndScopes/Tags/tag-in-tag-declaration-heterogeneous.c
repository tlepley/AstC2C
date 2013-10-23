//##################################################################
// Multiple embedded type tag declaration (heterogeneous)
//##################################################################

// Mixed declaration 1

struct S1 {	      // Global scope
  union U1 {	      // Global scope
    int i;
    char c;
  } i;
  struct S2 {	      // Global scope
    union U2 {	      // Global scope
      int i;
      char c;
    } i;
    enum E0 {	      // Global scope
      A,
      B=10,
      C
    } j;
  } j; int k;
} s1, *ps1;


// Reference to defined symbols
struct S2 s2;
union  U1 u1;
union  U2 u2;
enum   E0 e = B;


// Mixed declaration 2 (typedef)
typedef union U {struct S{char c;} i; enum E {enum_A, enum_B=0, enum_C} j; char * k;} z, x;
z t1;
x t2;
