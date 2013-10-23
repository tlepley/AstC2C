//##################################################################
// Union declared in a cast
//##################################################################

int a;
char b;

void f(void *p) {
  // Named struct
  a=((union U {int i; char toto;} *)p)->i;
  // Unnamed struct
  b=((struct {int i; char toto;} *)p)->toto;

  if (1) {
    union U u;  // Correct reference
  }
}
