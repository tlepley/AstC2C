//##################################################################
// Enum declared in a cast
//##################################################################

int a,b;

void f(void *p) {
  // Named struct
  a=(enum E {A,B,C})b;
  // Unnamed struct
  a=(enum {A1,B1,C1})b;

  if (1) {
    enum E e=A+B+C;  // Correct references
    int i=A1+B1+C1;  // Correct references
  }
}
