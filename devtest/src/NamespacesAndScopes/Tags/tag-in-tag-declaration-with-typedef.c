//##################################################################
// Multiple embedded type tag declaration (heterogeneous) with a
// typedef 
//##################################################################


typedef 
union U {
  struct S{char c;} i;
  enum E {A, B} j;
  char * k;
} T;


// Reference to defined symbols
T t1;
union  U u;
struct S s;
enum   E e = B;
