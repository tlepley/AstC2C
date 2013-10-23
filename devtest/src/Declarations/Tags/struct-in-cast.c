//##################################################################
// Structure declared in a cast
//##################################################################

int a,b;

void f(void *p) {
  // Named struct
  a=((struct S {int i; int toto;} *)p)->i;
  // Unnamed struct
  b=((struct {int i; int toto;} *)p)->toto;

  if (1) {
    struct S s;  // Correct reference
  }
}
