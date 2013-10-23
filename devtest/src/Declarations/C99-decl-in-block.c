// In C99, declarations can be done at the middle of the block#

int i;
int f() {
  i++;  // expression
  typedef struct S {int i;} T; // declaration
  T a; // declaration
  a.i=1; // expression
  int b=a.i; // declaration
}
